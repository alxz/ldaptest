package ca.rtss.ldaptest.ldap.javaconfig;

import ca.rtss.ldaptest.ldap.client.LdapClient;
import ca.rtss.ldaptest.ldap.data.service.remoteREST;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.data.ldap.repository.config.EnableLdapRepositories;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;

@Configuration
@PropertySource("classpath:application.properties")
@ComponentScan(basePackages = {"ca.rtss.ldaptest.ldap.*"})  //com.baeldung.ldap
@Profile("default")
@EnableLdapRepositories(basePackages = "ca.rtss.ldaptest.ldap.**")
public class AppConfig {

    @Autowired
    private Environment env;

    @Bean
    public LdapContextSource contextSource() {
        LdapContextSource contextSource = new LdapContextSource();
        contextSource.setUrl(env.getRequiredProperty("ldap.url"));
        contextSource.setBase(env.getRequiredProperty("ldap.partitionSuffix"));
        contextSource.setUserDn(env.getRequiredProperty("ldap.principal"));
        contextSource.setPassword(env.getRequiredProperty("ldap.password"));
        return contextSource;
    }

    @Bean
    public LdapTemplate ldapTemplate() {
        return new LdapTemplate(contextSource());
    }

    @Bean
    public LdapClient ldapClient() {
        return new LdapClient();
    }

    /*
     * Authentication code:
     */
    
//    @Bean
//    public RequestHeaderAuthenticationFilter requestHeaderAuthenticationFilter() throws Exception {
//        RequestHeaderAuthenticationFilter requestHeaderAuthenticationFilter = new RequestHeaderAuthenticationFilter();
//        requestHeaderAuthenticationFilter.setPrincipalRequestHeader("X-AUTH-TOKEN");
//        requestHeaderAuthenticationFilter.setAuthenticationManager(authenticationManager());
//        requestHeaderAuthenticationFilter.setExceptionIfHeaderMissing(false);
//
//        return requestHeaderAuthenticationFilter;
//    }

}