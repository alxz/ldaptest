package ca.rtss.ldaptest;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
//By Alexey Zapromyotov --- 2021
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.web.filter.HiddenHttpMethodFilter;

@SpringBootApplication
public class LdaptestApplication extends SpringBootServletInitializer{

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(LdaptestApplication.class);
	}
	
	public static void main(String[] args) {
		SpringApplication.run(LdaptestApplication.class, args);
	}
	
	/*
	 * @Override public void onStartup(ServletContext aServletContext) throws
	 * ServletException { super.onStartup(aServletContext);
	 * registerHiddenFieldFilter(aServletContext); }
	 * 
	 * public void registerHiddenFieldFilter(ServletContext aContext) {
	 * aContext.addFilter("hiddenHttpMethodFilter", new
	 * HiddenHttpMethodFilter()).addMappingForUrlPatterns(null,true, "/*"); }
	 */
	

}
