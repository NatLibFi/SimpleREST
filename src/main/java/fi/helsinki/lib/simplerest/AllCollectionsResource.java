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
import java.util.ArrayList;
import org.apache.log4j.Logger;
import org.apache.log4j.Priority;
import org.dspace.content.Collection;
import org.dspace.core.Context;
import org.restlet.engine.header.Header;
import org.restlet.engine.header.HeaderConstants;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.Options;
import org.restlet.util.Series;


/**
 *
 * @author moubarik
 */
public class AllCollectionsResource extends BaseResource{
    
    private static Logger log = Logger.getLogger(AllCollectionsResource.class);
    
    private Collection[] allCollections;
    private Context context;
    
    @Options
    public void doOptions(Representation entity) {
        Series<Header> responseHeaders = (Series<Header>) getResponse().getAttributes().get(HeaderConstants.ATTRIBUTE_HEADERS);
        if (responseHeaders == null) {
            responseHeaders = new Series(Header.class);
            getResponse().getAttributes().put(HeaderConstants.ATTRIBUTE_HEADERS,
                    responseHeaders);
        }
        responseHeaders.add(new Header("Access-Control-Allow-Origin", "*"));
    }

    public AllCollectionsResource(Collection[] collections){
        this.allCollections = collections;
    }
    
    public AllCollectionsResource(){
        this.allCollections = null;
        try{
            this.context = new Context();
            
        }catch(SQLException e){
            log.log(Priority.INFO, e);
        }
        try{
            this.allCollections = Collection.findAll(context);
        }catch(Exception e){            
            if(context != null){
                context.abort();
            }
            
            log.log(Priority.INFO, e);
        }finally{
            if(context != null){
                try{
                    context.complete();
                }catch(SQLException e){
                    log.log(Priority.ERROR, e);
                }
            }
        }
    }
    
    static public String relativeUrl(int dummy){
        return "collections";
    }
    
    @Get("json")
    public String toJson() throws SQLException{
        Gson gson = new Gson();
        GetOptions.allowAccess(getResponse());
        ArrayList<StubCollection> toJsonCollections = new ArrayList<StubCollection>(25);
        for(Collection c : allCollections){
            toJsonCollections.add(new StubCollection(c));
        }
        
        try{
            context.complete();
        }catch(NullPointerException e){
            log.log(Priority.INFO, e);
        }catch(SQLException e){
            log.log(Priority.ERROR, e);
        }
        
        return gson.toJson(toJsonCollections);
    }
}
