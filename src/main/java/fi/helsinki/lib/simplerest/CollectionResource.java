/**
 * A RESTful web service on top of DSpace.
 * Copyright (C) 2010-2014 National Library of Finland
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.

 */

package fi.helsinki.lib.simplerest;

import com.google.gson.Gson;
import fi.helsinki.lib.simplerest.options.GetOptions;
import fi.helsinki.lib.simplerest.stubs.StubCollection;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.HashSet;

import org.dspace.core.Context;
import org.dspace.content.Community;
import org.dspace.content.Collection;
import org.dspace.content.Bitstream;

import org.restlet.ext.xml.DomRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.InputRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.Put; 
import org.restlet.resource.Post;
import org.restlet.resource.Delete;
import org.restlet.resource.ResourceException;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.data.Method;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.apache.log4j.Logger;
import org.dspace.authorize.AuthorizeException;

public class CollectionResource extends BaseResource {

    private static Logger log = Logger.getLogger(CollectionResource.class);
    
    private int collectionId;

    static public String relativeUrl(int collectionId) {
        return "collection/" + collectionId;
    }
    
    @Override
    protected void doInit() throws ResourceException {
        try {
            String id =
                (String)getRequest().getAttributes().get("collectionId");
            this.collectionId = Integer.parseInt(id);
        }
        catch (NumberFormatException e) {
            ResourceException resourceException =
                new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST,
                                      "Collection ID must be a number.");
            throw resourceException;
        }
    }

    // TODO: parent?
    @Get("xml")
    public Representation toXml() {
        Context c = null;
        Collection collection = null;
        DomRepresentation representation = null;
        Document d = null;
        try {
            c = new Context();
            collection = Collection.find(c, this.collectionId);
            if (collection == null) {
                return errorNotFound(c, "Could not find the collection.");
            }

            representation = new DomRepresentation(MediaType.TEXT_HTML);  
            d = representation.getDocument();  
        }
        catch (Exception e) {
            return errorInternal(c, e.toString());
        }
        
        Element html = d.createElement("html");  
        d.appendChild(html);

        Element head = d.createElement("head");
        html.appendChild(head);

        Element title = d.createElement("title");
        head.appendChild(title);
        title.appendChild(d.createTextNode("Collection " +
                                           collection.getName()));

        Element body = d.createElement("body");
        html.appendChild(body);
	
        Element dl = d.createElement("dl");
        setId(dl, "attributes");
        body.appendChild(dl);

        Element dtName = d.createElement("dt");
        dtName.appendChild(d.createTextNode("name"));
        dl.appendChild(dtName);
        Element ddName = d.createElement("dd");
        ddName.appendChild(d.createTextNode(collection.getName()));
        dl.appendChild(ddName);

        String[] attributes = { "short_description", "introductory_text",
                                "provenance_description", "license",
                                "copyright_text", "side_bar_text" };
        for (String attribute : attributes) {
            Element dt = d.createElement("dt");
            dt.appendChild(d.createTextNode(attribute));
            dl.appendChild(dt);

            Element dd = d.createElement("dd");
            dd.appendChild(d.createTextNode(collection.getMetadata(attribute)));
            dl.appendChild(dd);
        }

        Bitstream logo = collection.getLogo();
        if (logo != null) {
            Element aLogo = d.createElement("a");
            // FIX!
            String url =
                getRequest().getResourceRef().getIdentifier() + "/logo";
            setAttribute(aLogo, "href", url);
            setId(aLogo, "logo");
            aLogo.appendChild(d.createTextNode("Collection logo"));
            body.appendChild(aLogo);
        }

	// A link to items
        String url = getRequest().getResourceRef().getIdentifier();
        Element pItems = d.createElement("p");
        Element aItems = d.createElement("a");
	setAttribute(aItems, "href", url + "/items");
        setId(aItems, "items");
	aItems.appendChild(d.createTextNode("items"));
        pItems.appendChild(aItems);
        body.appendChild(pItems);

        c.abort(); /* We did not make any changes to the database, so we could
                      call c.complete() instead (only it can potentially raise
                      SQLexception). */

        return representation;
    }
    
    @Get("json")
    public String toJson(){
        GetOptions.allowAccess(getResponse());
        Collection co = null;
        Context c = null;
        try{
            c = new Context();
            co = Collection.find(c, collectionId);
        }catch(Exception e){
            if(c != null)
                c.abort();
            return errorInternal(c, e.toString()).getText();
        }finally{
            if(c != null)
                c.abort();
        }
        
        Gson gson = new Gson();
        StubCollection s = new StubCollection(co.getID(), co.getName(), co.getMetadata("short_description"),
                co.getMetadata("introductory_text"), co.getMetadata("provenance_description"),
                co.getMetadata("license"), co.getMetadata("copyright_text"), co.getMetadata("side_bar_text"), co.getLogo());
        return gson.toJson(s);
    }

    @Put
    public Representation edit(InputRepresentation rep) {
        Context c = null;
        Collection collection;
        try {
            c = getAuthenticatedContext();
            collection = Collection.find(c, this.collectionId);
            if (collection == null) {
                return errorNotFound(c, "Could not find the collection.");
            }
        }
        catch (SQLException e) {
            return errorInternal(c, "SQLException");
        }

        DomRepresentation dom = new DomRepresentation(rep);

        Node attributesNode = dom.getNode("//dl[@id='attributes']");
        if (attributesNode == null) {
            return error(c, "Did not find dl tag with an id 'attributes'.",
                         Status.CLIENT_ERROR_BAD_REQUEST);
        }
	
        collection.setMetadata("name", null);
        collection.setMetadata("short_description", null);
        collection.setMetadata("introductory_text", null);
        collection.setMetadata("provenance_description", null);
        collection.setMetadata("license", null);
        collection.setMetadata("copyright_text", null);
        collection.setMetadata("side_bar_text", null);

        NodeList nodes = attributesNode.getChildNodes();
        LinkedList<String> dtList = new LinkedList();
        LinkedList<String> ddList = new LinkedList();
        int nNodes = nodes.getLength();
        for (int i=0; i < nNodes; i++) {
            Node node = nodes.item(i);
            String nodeName = node.getNodeName();
            if (nodeName.equals("dt")) {
                dtList.add(node.getTextContent());
            }
            else if (nodeName.equals("dd")) {
                ddList.add(node.getTextContent());
            }
        }
        if (dtList.size() != ddList.size()) {
            return error(c, "The number of <dt> and <dd> elements do not match.",
                         Status.CLIENT_ERROR_BAD_REQUEST);
        }
        int size = dtList.size();
        for (int i=0; i < size; i++) {
            String dt = dtList.get(i);
            String dd = ddList.get(i);
            if (dt.equals("name") ||
                dt.equals("short_description") ||
                dt.equals("introductory_text") ||
                dt.equals("provenance_description") ||
                dt.equals("license") ||
                dt.equals("copyright_text") ||
                dt.equals("side_bar_text")) {
                collection.setMetadata(dt, dd);
            }
            else {
                return error(c, "Unexpected data in attributes: " + dt,
                             Status.CLIENT_ERROR_BAD_REQUEST);
            }
        }

        try {
            collection.update();
            c.complete();
        } catch (AuthorizeException ae) {
          return error(c, "Unauthorized", Status.CLIENT_ERROR_UNAUTHORIZED);
        }
        catch (Exception e) {
            return errorInternal(c, e.toString());
        }

        return successOk("Collection updated.");
    }

    @Post
    public Representation delete(Representation dummy) {
        HashSet<Method> allowed = new HashSet();
        allowed.add(Method.GET);
        allowed.add(Method.PUT);
        allowed.add(Method.DELETE);
        setAllowedMethods(allowed);
        return error(null,
                     "Collection resource does not allow POST method.",
                     Status.CLIENT_ERROR_METHOD_NOT_ALLOWED);
    }

    @Delete
    public Representation delete() {
        Context c = null;
        Collection collection;
        try {
            c = getAuthenticatedContext();
            collection = Collection.find(c, this.collectionId);
            if (collection == null) {
                return errorNotFound(c, "Could not find the collection.");
            }
        }
        catch (SQLException e) {
            return errorInternal(c, "SQLException");
        }

        try {
            Community[] parentCommunities = collection.getCommunities();
            for (Community community : parentCommunities) {
                community.removeCollection(collection);
            }
            c.complete();
        } catch (AuthorizeException ae) {
          return error(c, "Unauthorized", Status.CLIENT_ERROR_UNAUTHORIZED);
        }
        catch (Exception e) {
            return errorInternal(c, e.toString());
        }
        
        return successOk("Collection deleted.");
    }
}
