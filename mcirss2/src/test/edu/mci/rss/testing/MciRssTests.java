package edu.mci.rss.testing;


import edu.mci.rss.controllers.MciRssController;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sakaiproject.component.cover.ComponentManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

//
// junit        4.13.2
// hamcrest     1.3
// spring       5.3.39


@Slf4j
@RunWith(SpringRunner.class)
@WebAppConfiguration // weil --> AnnotationConfigWebApplicationContext
@ContextConfiguration(classes = {TestConfiguration.class})
public class MciRssTests {


    @Autowired
    private WebApplicationContext wac;
    @Autowired
    private MciRssController mciRssController;

    private MockMvc mockMvc;


    static {
        try {
            Class<?> clazz = ComponentManager.class;
            Field testingModeField = clazz.getDeclaredField("testingMode");
            testingModeField.setAccessible(true);
            testingModeField.set(null, true);
            ComponentManager.getInstance();
        } catch (NoSuchFieldException | IllegalAccessException e) {
            log.error("ComponentManager could not set to testing mode for Junit Run", e);
        }
    }


    @Before
    public void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }

    @Test
    public void testWebApplicationContext() {
        assertNotNull(wac);
    }


    /*
Set-Cookie: SAKAI2SESSIONID=bc9bff56-7cac-4abb-9d9b-32c04394f55e.cddd874a9c5b; Path=/; HttpOnly
X-Frame-Options: SAMEORIGIN
Content-Type: application/atom+xml;charset=UTF-8
Content-Length: 1331
Date: Wed, 02 Jul 2025 08:34:20 GMT


     */

    @Test
    public void testDispatcherServletRootMapping() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isNotFound()); // Assuming no handler is mapped to "/"
    }

    @Test
    public void testStringHttpMessageConverterBean() {
        // Retrieve the bean from the application context
        StringHttpMessageConverter stringHttpMessageConverter =
                wac.getBean(StringHttpMessageConverter.class);
        assertNotNull("StringHttpMessageConverter bean should not be null", stringHttpMessageConverter);
        // Verify the charset is set to UTF-8
        assertEquals("Charset should be UTF-8", StandardCharsets.UTF_8, stringHttpMessageConverter.getDefaultCharset());
        assertTrue("Supported media type should be application/atom+xml", stringHttpMessageConverter.getSupportedMediaTypes().contains(MediaType.APPLICATION_ATOM_XML));
    }





}







