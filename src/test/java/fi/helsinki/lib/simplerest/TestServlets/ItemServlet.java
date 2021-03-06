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


package fi.helsinki.lib.simplerest.TestServlets;

import fi.helsinki.lib.simplerest.ItemResource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.Date;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;
import org.apache.log4j.Priority;
import org.dspace.content.DCValue;
import org.dspace.content.Item;
import static org.mockito.Mockito.*;
import org.restlet.representation.InputRepresentation;
import org.restlet.representation.Representation;

/**
 *
 * @author moubarik
 */
public class ItemServlet extends HttpServlet{
    
    private Item mockedItem;
    private ItemResource ir;
    private static Logger log = Logger.getLogger(ItemServlet.class);
    
    @Override
    public void init(ServletConfig config) throws ServletException{
        mockedItem = mock(Item.class);
        when(mockedItem.getID()).thenReturn(1);
        when(mockedItem.getName()).thenReturn("testi");
        when(mockedItem.isArchived()).thenReturn(true);
        when(mockedItem.isWithdrawn()).thenReturn(false);
        when(mockedItem.isDiscoverable()).thenReturn(true);
        Date d = new Date();
        when(mockedItem.getLastModified()).thenReturn(d);
        
        DCValue[] value = new DCValue[2];
        value[0] = new DCValue(); value[1] = new DCValue();
        value[0].schema = "dc"; value[0].element = "contributor"; value[0].qualifier = "author";
        value[0].value = "Testi Testaaja";
        value[1].schema = "dc"; value[1].element = "date"; value[1].qualifier = "issued";
        value[1].value = "2013";
        
        when(mockedItem.getMetadata(Item.ANY, Item.ANY, Item.ANY, Item.ANY)).thenReturn(value);
        
        ir = new ItemResource(mockedItem, mockedItem.getID());
    }
    
    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException{
        if(req.getPathInfo().equals("/xml")){
            xmlTest(resp);
        }else if(req.getPathInfo().equals("/json")){
            jsonTest(resp);
        }
    }
    
    public void xmlTest(HttpServletResponse resp) throws IOException{
        PrintWriter out = resp.getWriter();
        out.write(ir.toXml().getText());
    }

    private void jsonTest(HttpServletResponse resp) throws IOException {
        PrintWriter out = resp.getWriter();
        Representation rep = ir.toJson();
        out.write(rep.getText());
    }
    
    @Override
    public void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException{
        Representation rep = ir.toXml();
        
        Item origItem = mock(Item.class);
        when(origItem.getID()).thenReturn(2);
        try {
            when(origItem.getBundles()).thenReturn(null);
            when(origItem.getCollections()).thenReturn(null);
        } catch (SQLException ex) {
            log.log(Priority.INFO, ex);
        }
        DCValue[] value = new DCValue[2];
        value[0] = new DCValue(); value[1] = new DCValue();
        value[0].schema = "dc"; value[0].element = "contributor"; value[0].qualifier = "author";
        value[0].value = "Testi Testaaja2";
        value[1].schema = "dc"; value[1].element = "date"; value[1].qualifier = "issued";
        value[1].value = "2012";
        
        when(origItem.getMetadata(Item.ANY, Item.ANY, Item.ANY, Item.ANY)).thenReturn(value);
        
        ItemResource originalIr = new ItemResource(origItem, 2);
        
        InputRepresentation ir = new InputRepresentation(rep.getStream());
        
        PrintWriter out = resp.getWriter();
        if(req.getPathInfo().equals("/edit")){
            out.write(originalIr.editItem(ir).getText());
            out.write(originalIr.toXml().getText());
        }
        
    }
}
