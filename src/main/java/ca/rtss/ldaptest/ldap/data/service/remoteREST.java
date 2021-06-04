package ca.rtss.ldaptest.ldap.data.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import ca.rtss.ldaptest.ldap.client.LdapClient;
public class remoteREST {
	
	@Autowired
	private static Environment env;
	private static final Logger LOG = LoggerFactory.getLogger(LdapClient.class);
	
	
	
	public static String getInstitutionHash(String id)
	{
		String remoteURI = env.getRequiredProperty("remote.RESTURI");
	    String result = null;
		try {
			final String uri = remoteURI + "/getInstitutionHash/" + id;
 
			RestTemplate restTemplate = new RestTemplate();
 
			result = restTemplate.getForObject(uri, String.class);
			LOG.info("==> Remote REST reply: " + result.toString());
			//System.out.println(result);
		} catch (Exception e) {
			LOG.error("Error getting Institution hash by id...  " + e.getMessage());			
		}			
	    
	    return result;
	}

}
