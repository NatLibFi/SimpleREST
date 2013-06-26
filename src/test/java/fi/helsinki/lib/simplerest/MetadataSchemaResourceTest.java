/**
 * A RESTful web service on top of DSpace.
 * Copyright (C) 2010-2011 National Library of Finland
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
/*
 * @(#)MetadataSchemaResourceTest.java
 */
package fi.helsinki.lib.simplerest;

import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import org.restlet.data.MediaType;
import org.restlet.representation.StringRepresentation;


/**
 * JUnit test.
 * <ul>
 * <li>http://www.junit.org/</li>
 * <li>http://junit.sourceforge.net/doc/faq/faq.htm</li>
 * </ul>
 * Testing the methods of <code>MetadataSchemaResource</code> class.
 * @author Markos Mevorah
 * @version %I%, %G%
 * @see fi.helsinki.lib.simplerest.MetadataSchemaResource
 */
public class MetadataSchemaResourceTest {

    /**
     * @see fi.helsinki.lib.simplerest.MetadataSchemaResource
     */
    private MetadataSchemaResource metadataSchemaResource;

    public MetadataSchemaResourceTest() {
    }

    /**
     * JUnit method annotated with {@link org.junit.Before}.
     * Initializing the test resources.
     */
    @Before
    public void setUp() {
        this.metadataSchemaResource = new MetadataSchemaResource();
    }

    /**
     * JUnit method annotated with {@link org.junit.After}.
     * Releasing the test resources.
     */
    @After
    public void tearDown() {
        this.metadataSchemaResource = null;
    }

    /**
     * Test of relativeUrl method, of class MetadataSchemaResource.
     */
    @Test
    public void testRelativeUrl() {
        String actualUrl = MetadataSchemaResource.relativeUrl(89);
        assertEquals("metadataschema/89", actualUrl);
    }

    /**
     * Test of doInit method, of class MetadataSchemaResource.
     */
    @Test(expected = NullPointerException.class)
    public void testDoInit() throws Exception {
        this.metadataSchemaResource.doInit();
    }

    /**
     * Test of edit method, of class MetadataSchemaResource.
     */
    @Test(expected = NullPointerException.class)
    public void testEdit() {
        StringRepresentation representation =
                             (StringRepresentation) this.metadataSchemaResource.edit(null);
    }

    /**
     * Test of post method, of class MetadataSchemaResource.
     */
    @Test
    public void testPost() {
        StringRepresentation representation =
                             (StringRepresentation) this.metadataSchemaResource.post(null);
        assertEquals(MediaType.TEXT_PLAIN, representation.getMediaType());
        assertEquals("Metadata schema resource does not allow POST method.",
                     representation.getText());
    }

    /**
     * Test of delete method, of class MetadataSchemaResource.
     */
    @Test
    public void testDelete() {
        StringRepresentation representation =
                             (StringRepresentation) this.metadataSchemaResource.delete();
        assertEquals(MediaType.TEXT_PLAIN, representation.getMediaType());
        assertEquals("java.lang.NullPointerException", representation.getText());
    }
}