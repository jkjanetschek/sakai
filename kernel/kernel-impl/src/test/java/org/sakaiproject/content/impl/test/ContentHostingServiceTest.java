/**
 * Copyright (c) 2003-2016 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.content.impl.test;

import static org.junit.runners.MethodSorters.NAME_ASCENDING;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Iterator;
import java.sql.Connection;
import java.sql.Statement;

import java.security.MessageDigest;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;

import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.content.api.ContentCollection;
import org.sakaiproject.content.api.ContentCollectionEdit;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.content.api.ContentResourceEdit;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.exception.IdInvalidException;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.IdUsedException;
import org.sakaiproject.exception.InconsistentException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.test.SakaiKernelTestBase;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.util.BasicConfigItem;
import org.sakaiproject.db.api.SqlService;
import org.sakaiproject.util.StorageUtils;

@FixMethodOrder(NAME_ASCENDING)
@Slf4j
public class ContentHostingServiceTest extends SakaiKernelTestBase {

	private static final String SIMPLE_FOLDER1 = "/admin/folder1/";
	
	@BeforeClass
	public static void beforeClass() {
		try {
			log.debug("starting oneTimeSetup");
			oneTimeSetup();
			log.debug("finished oneTimeSetup");
		} catch (Exception e) {
			log.warn(e.getMessage(), e);
		}
	}
	
	/**
	 * Checks the resources of zero bytes are handled correctly.
	 */
	@Test
	public void testEmptyResources() throws Exception {
		ContentHostingService ch = getService(ContentHostingService.class);
		SessionManager sm = getService(SessionManager.class);
		Session session = sm.getCurrentSession();
		session.setUserEid("admin");
		session.setUserId("admin");
		ContentResourceEdit cr;
		cr = ch.addResource("/emptyFileStreamed");
		cr.setContent(new ByteArrayInputStream(new byte[0]));
		ch.commitResource(cr);
		
		cr = ch.addResource("/emptyFileArray");
		cr.setContent(new byte[0]);
		ch.commitResource(cr);
		
		ContentResource resource;
		InputStream stream;
		resource = ch.getResource("/emptyFileStreamed");
		stream = resource.streamContent();
		Assert.assertEquals(0, stream.available());
		Assert.assertEquals(0, resource.getContentLength());
		Assert.assertEquals(0, resource.getContent().length);
		
		resource = ch.getResource("/emptyFileArray");
		stream = resource.streamContent();
		Assert.assertEquals(0, stream.available());
		Assert.assertEquals(0, resource.getContentLength());
		Assert.assertEquals(0, resource.getContent().length);
		
		
	}
	
	@Test
	public void testSaveRetriveFolder() {
		ContentHostingService ch = getService(ContentHostingService.class);
		SessionManager sm = getService(SessionManager.class);
		Session session = sm.getCurrentSession();
		session.setUserEid("admin");
		session.setUserId("admin");
		
		try {
			ContentCollectionEdit ce = ch.addCollection(SIMPLE_FOLDER1);
			ch.commitCollection(ce);
			log.info("commited folder:" + ce.getId());
		} catch (IdUsedException e) {
			log.error(e.getMessage(), e);
			Assert.fail("Got an id Used exception!");
		} catch (IdInvalidException e) {
			log.error(e.getMessage(), e);
			Assert.fail("That id is invalid!");
		} catch (PermissionException e) {
			log.error(e.getMessage(), e);
			Assert.fail();
		} catch (InconsistentException e) {
			log.error(e.getMessage(), e);
			Assert.fail();
		}
		
		
		//now try retrieve the folder
		try {
			ContentCollection cc = ch.getCollection(SIMPLE_FOLDER1);
			Assert.assertNotNull(cc);
		} catch (Exception e) {
			Assert.fail(e.getMessage());
		}
		
		//lets test saving a utf8
		String utf8Folder = String.valueOf("\u6c92\u6709\u5df2\u9078\u8981\u522a\u9664\u7684\u9644\u4ef6");
		String utfId = "/admin/" + utf8Folder + "/";
		try {
			ContentCollectionEdit cce = ch.addCollection(utfId);
			ch.commitCollection(cce);
			log.info("commited folder:" + cce.getId());
		} catch (IdUsedException e) {
			log.error(e.getMessage(), e);
		} catch (Exception e) {
			Assert.fail(e.getMessage());
		}
		
		//now try retrieve the folder
		try {
			ContentCollection cc = ch.getCollection(utfId);
			Assert.assertNotNull(cc);
		} catch (Exception e) {
			Assert.fail(e.getMessage());
		}
		
	}
	
	/**
	 * See SAK-17308 test for cases if resources saved in tf8 folders
	 */
	@Test
	public void testUtfFolders() {
		//lets test saving a utf8
		ContentHostingService ch = getService(ContentHostingService.class);
		SessionManager sm = getService(SessionManager.class);
		Session session = sm.getCurrentSession();
		session.setUserEid("admin");
		session.setUserId("admin");

		String utf8Folder = String.valueOf("\u6c92\u6709\u5df2\u9078\u8981\u522a\u9664\u7684\u9644\u4ef6");
		String utfId = "/admin/" + utf8Folder + "/";
		String resId = null;
		String fileName = "someFile";
		String fileExtension = ".txt";
		try {
			ContentResourceEdit cre = ch.addResource(utfId, fileName, fileExtension, 10);
			ch.commitResource(cre);
			resId = cre.getId();
			log.info("saved: " + cre.getId());
			log.info("url is: " + cre.getUrl());
			log.info("relative url:" + cre.getUrl(true));
			String urlDecode = URLDecoder.decode(cre.getUrl(true), "utf8");
			log.info("decoded url: " + urlDecode);
			String url = "/access/content" + utfId + fileName + fileExtension;
			Assert.assertEquals(url, urlDecode);
		} catch (Exception e) {
			Assert.fail(e.getMessage());
		}
		
		//now lets try retrieve it
		try {
			ContentResource res = ch.getResource(resId);
			Assert.assertNotNull(res);
		} catch (Exception e) {
			Assert.fail(e.getMessage());
		}
	}
	
	@Test
	public void testDeleteResource() {
		ContentHostingService ch = getService(ContentHostingService.class);
		SessionManager sm = getService(SessionManager.class);
		Session session = sm.getCurrentSession();
		session.setUserEid("admin");
		session.setUserId("admin");
		try {
			ch.removeResource("noSuchResource");
			Assert.fail();
		} catch (Exception e) {
			Assert.assertTrue(e instanceof IdUnusedException);
		}
	}

	@Test
	public void testSha256Is64Characters() {
		String plainText = "Sakaiger for the win!";
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			md.update(plainText.getBytes());
			String hex = StorageUtils.bytesToHex(md.digest());
			Assert.assertTrue(hex.length() == 64);
			Assert.assertEquals(hex, "5bda9b8aca332e256d310e8f052c088c33c7782e1fe7898f19d05af007026ac0");
		} catch (Exception e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test
	public void testDuplicateUpload() throws Exception {
		ContentHostingService ch = getService(ContentHostingService.class);
		SessionManager sm = getService(SessionManager.class);
		Session session = sm.getCurrentSession();
		session.setUserEid("admin");
		session.setUserId("admin");

		ContentResource cr;
		InputStream stream;
		//Insert the same resource 4 times
		for (int i=0;i<4;i++) {
			String UploadFile = "/testTXT.txt";
			String DisplayName = "/testTXT_"+i+".txt";
			log.debug("Loading up file: {} as {}", UploadFile, DisplayName);
			stream = this.getClass().getResourceAsStream("/test-documents"+UploadFile);
			Assert.assertNotNull(stream);
			ResourcePropertiesEdit props = ch.newResourceProperties();
			props.addProperty (ResourceProperties.PROP_DISPLAY_NAME, DisplayName);
			//Put it on the root of the filesystem
			ch.addResource(DisplayName, "", stream, props ,0);
			//Now get it back and check the mime type
			cr = ch.getResource(DisplayName);
			Assert.assertEquals(cr.getContentLength(), 49);
			Assert.assertNotNull(ch);
			Assert.assertNotNull(stream);
			stream.close();
		}
	}

	//Resources for this from http://svn.apache.org/repos/asf/tika/trunk/tika-parsers/src/test/resources/test-documents/
	//Test mime type detector, might be useful to test it off as well
	@Test
	public void testMimeDetection() throws Exception {
		//Some popular test cases
		//First is really an excel file but incorrect extension
		//Next is a Word doc with correct extension
		//Next is an html doc with correct extension
		//Next is an really and excel file with no extension
		//Last is a html snippet with correct extension

		final Map<String, String> fileNamesToMimes = new HashMap<>() {{
			put("testCSV.csv", "text/csv");
			put("testNotebook.ipynb", "application/x-ipynb+json");
			put("testEXCEL.mp3", "application/vnd.ms-excel");
			put("testWORD.doc", "application/msword");
			put("testHTML.html", "text/html");
			put("testEXCEL", "application/vnd.ms-excel");
			put("LSNBLDR-359-snippet.html", "text/html");
			put("testCSS.css", "text/css");
			put("testHTMLbody.html", "text/html");
			put("jquery-1.6.1.min.js", "application/javascript");
		}};
		//Set the mime magic to be true
		ServerConfigurationService serv = getService(ServerConfigurationService.class);
		serv.registerConfigItem(BasicConfigItem.makeConfigItem("content.useMimeMagic","true",ServerConfigurationService.UNKNOWN));
		//Special test to work around KNL-1306 / TIKA-1141
		serv.registerConfigItem(BasicConfigItem.makeConfigItem("content.mimeMagic.ignorecontent.extensions.count","1",ServerConfigurationService.UNKNOWN));
		serv.registerConfigItem(BasicConfigItem.makeConfigItem("content.mimeMagic.ignorecontent.extensions.1","js",ServerConfigurationService.UNKNOWN));

    	ContentHostingService ch = getService(ContentHostingService.class);
    	SessionManager sm = getService(SessionManager.class);
    	Session session = sm.getCurrentSession();
    	session.setUserEid("admin");
    	session.setUserId("admin");

		ContentResource cr;
    	InputStream stream;
    	//Insert all resources to CHS
		for (Map.Entry<String, String> entry : fileNamesToMimes.entrySet()) {
			String fileName = entry.getKey();
			//Stored in CHS it needs a slash
			String CHSfileName = "/"+fileName;
			log.debug("Loading up file: {}", fileName);
			stream = this.getClass().getResourceAsStream("/test-documents"+CHSfileName);
			Assert.assertNotNull(stream);
			ResourcePropertiesEdit props = ch.newResourceProperties();
			props.addProperty (ResourceProperties.PROP_DISPLAY_NAME, fileName);
			//Put it on the root of the filesystem
			ch.addResource(CHSfileName, "", stream, props ,0);
			//Now get it back and check the mime type
			cr = ch.getResource(CHSfileName);
			log.debug("Expecting mime:{} and got {}", entry.getValue(), cr.getContentType());
			Assert.assertEquals(cr.getContentType(), entry.getValue());
			stream.close();
		}
    }

    @Test
    public void testSqlSanity() {
        ContentHostingService ch = getService(ContentHostingService.class);
        SqlService m_sqlService = getService(SqlService.class);

        // Make sure that the structure of the CONTENT_ tables passes the "modern schema" test
        try {
            Connection connection = m_sqlService.borrowConnection();
            Statement statement = connection.createStatement();

           try {
                statement.execute("select BINARY_ENTITY from CONTENT_COLLECTION where COLLECTION_ID = 'does-not-exist' " );
            } catch ( Exception ex ) {
                Assert.fail();
            }
            try {
                statement.execute("select XML from CONTENT_COLLECTION where COLLECTION_ID = 'does-not-exist' ");
            } catch ( Exception ex ) {
                Assert.fail();
            }

            try {
                statement.execute("select BINARY_ENTITY from CONTENT_RESOURCE where RESOURCE_ID = 'does-not-exist' " );
            } catch ( Exception ex ) {
                Assert.fail();
            }
            try {
                statement.execute("select XML from CONTENT_RESOURCE where RESOURCE_ID = 'does-not-exist' ");
            } catch ( Exception ex ) {
                Assert.fail();
            }
            try {
                statement.execute("select BINARY_ENTITY from CONTENT_RESOURCE_DELETE where RESOURCE_ID = 'does-not-exist' " );
            } catch ( Exception ex ) {
                Assert.fail();
            }
            try {
                statement.execute("select XML from CONTENT_RESOURCE_DELETE where RESOURCE_ID = 'does-not-exist' ");
            } catch ( Exception ex ) {
                Assert.fail();
            }
           try {
                statement.execute("select RESOURCE_SHA256 from CONTENT_RESOURCE where RESOURCE_ID = 'does-not-exist' " );
            } catch ( Exception ex ) {
                Assert.fail();
            }

            // Yes, these are a bit less than exciting, post autoDDL - but what they heck -
            // they should not fail and be correct
            checkCount("select count(*) from CONTENT_COLLECTION where BINARY_ENTITY IS NULL ", 0);
            checkCount("select count(*) from CONTENT_RESOURCE where BINARY_ENTITY IS NULL ", 0);
            checkCount("select count(*) from CONTENT_RESOURCE_DELETE where BINARY_ENTITY IS NULL ", 0);

            checkCount("select count(*) from CONTENT_COLLECTION where XML IS NOT NULL ", 0);

        } catch ( Exception ex ) {
            Assert.fail();
        }
    }

    protected void checkCount(String sql, int expected) throws Exception
    {

        SqlService m_sqlService = getService(SqlService.class);
        List list = m_sqlService.dbRead(sql, null, null);
        if (list == null) Assert.fail("Nothing returned for: "+sql);

        Iterator iter = list.iterator();
        if (iter.hasNext())
        {
            Object val = null;
            try
            {
                val = iter.next();
                Integer found = Integer.parseInt((String) val);
                if ( found == expected ) return;
                Assert.fail("Mismatch expecting: " + expected + " found: " + found + " sql: "+sql);
            }
            catch (Exception ignore)
            {
                Assert.fail("Bad integer: " + val + " from: "+sql);
            }
        }
        Assert.fail("Empty list for: "+sql);
        return;
    }

}
