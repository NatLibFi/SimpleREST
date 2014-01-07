/**
 * A RESTful web service on top of DSpace.
 * Copyright (C) 2010-2014 National Library of Finland
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package fi.helsinki.lib.simplerest;

import com.google.gson.Gson;
import fi.helsinki.lib.simplerest.options.GetOptions;
import fi.helsinki.lib.simplerest.stubs.StubItem;
import java.sql.SQLException;
import java.util.ArrayList;
import org.apache.log4j.Logger;
import org.apache.log4j.Priority;
import org.dspace.content.Collection;
import org.dspace.content.Community;
import org.dspace.content.Item;
import org.dspace.content.ItemIterator;
import org.dspace.core.Context;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.Options;

/**
 *
 * @author moubarik
 */
public class AllItemsResource extends BaseResource{
    
    private static Logger log = Logger.getLogger(AllItemsResource.class);
    
    private Community[] allCommunities;
    private Collection[] allCollections;
    private Item[] allItems;
    private Context context;
    
    public AllItemsResource(Community[] communities, Collection[] collections, Item[] items){
        this.allCommunities = communities;
        this.allCollections = collections;
        this.allItems = items;
    }
    
    public AllItemsResource(){
        this.allCollections = null;
        this.allCommunities = null;
        this.allItems = null;
        try{
            this.context = new Context();
        }catch(SQLException e){
            log.log(Priority.INFO, e);
        }
        try{
            this.allCollections = Collection.findAll(context);
        }catch(Exception e){
            log.log(Priority.INFO, e);
        }
    }
    
    static public String relativeUrl(int dummy){
        return "items";
    }
    
    @Options
    public void doOptions(Representation entity){
        GetOptions.allowAccess(getResponse());
    }
    
    @Get("json")
    public String toJson() throws SQLException{
        GetOptions.allowAccess(getResponse());
        Gson gson = new Gson();
        ArrayList<StubItem> toJsonItems = new ArrayList<StubItem>(100);
        for(Collection c : allCollections){
            ItemIterator i = c.getAllItems();
            while(i.hasNext()){
                toJsonItems.add(new StubItem(i.next()));
            }
        }
        try{
            this.context.abort();
        }catch(NullPointerException e){
            log.log(Priority.FATAL, e);
        }
        return gson.toJson(toJsonItems);
    }
    
}
