package edu.mci.rss;


import lombok.extern.slf4j.Slf4j;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.servlet.DispatcherServlet;

import javax.servlet.DispatcherType;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import java.util.EnumSet;
import java.util.Set;


import org.sakaiproject.util.RequestFilter;
import org.sakaiproject.util.SakaiContextLoaderListener;

@Slf4j
public class MciRssWebAppConfiguration implements WebApplicationInitializer {

    @Override
    public void onStartup(ServletContext servletContext) throws ServletException {
        System.out.println("MciRssWebAppConfiguration.onStartup()");
        //TODO develompent try catch: remove
        try {
            AnnotationConfigWebApplicationContext applicationContext = new AnnotationConfigWebApplicationContext();
            applicationContext.setServletContext(servletContext);

            applicationContext.register(MciRssWebMvcConfiguration.class);
            // applicationContext.scan("edu.mci.rss");
           // applicationContext.register(EventHandlerConfiguration.class);


            servletContext.addListener(new SakaiContextLoaderListener(applicationContext));


            servletContext.addFilter("sakai.request", RequestFilter.class)
                    .addMappingForUrlPatterns(
                            EnumSet.of(DispatcherType.REQUEST, DispatcherType.FORWARD, DispatcherType.INCLUDE),
                            true,
                            "/*");

            CharacterEncodingFilter characterEncodingFilter = new CharacterEncodingFilter();
            characterEncodingFilter.setEncoding("UTF-8");
            characterEncodingFilter.setForceEncoding(true);
            servletContext.addFilter("characterEncodingFilter", characterEncodingFilter)
                    .addMappingForUrlPatterns(
                            EnumSet.of(DispatcherType.REQUEST, DispatcherType.FORWARD, DispatcherType.INCLUDE),
                            true,
                            "/*");


            DispatcherServlet dispatcherServlet = new DispatcherServlet(applicationContext);
            dispatcherServlet.setThrowExceptionIfNoHandlerFound(true);
            ServletRegistration.Dynamic servlet = servletContext.addServlet("mcirss2", new DispatcherServlet(applicationContext));
            Set<String> alreadyMappedSet = servlet.addMapping("/");
            servlet.setLoadOnStartup(1);


            //TODO develompent log stuff: remove
            System.out.println("alreadyMappedSet: " + alreadyMappedSet);
            servletContext.getServletRegistrations().forEach((name, reg) -> {
                System.out.println("Servlet Name: " + name);
                System.out.println("Mappings: " + reg.getMappings());
            });
            System.out.println("Context Path: " + servletContext.getContextPath());


        } catch (Exception e2) {
            log.error(e2.getMessage());
            System.out.println("Error: " + e2.getMessage());
            throw e2;
        }

    }


}



