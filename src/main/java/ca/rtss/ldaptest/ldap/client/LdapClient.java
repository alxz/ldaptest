package ca.rtss.ldaptest.ldap.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.ldap.NamingException;
import org.springframework.ldap.core.*;
import org.springframework.ldap.support.LdapNameBuilder;

import com.sun.tools.javac.code.Attribute.Array;

import javax.naming.Name;
import javax.swing.event.ListSelectionEvent;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.Attributes;

public class LdapClient {

    @Autowired
    private Environment env;

    @Autowired
    private ContextSource contextSource;

    @Autowired
    private LdapTemplate ldapTemplate;

    public void authenticate(final String username, final String password) {
    	System.out.println("\n authenticate by: " + "name=" + username + " \n ");
//    			+ env.getRequiredProperty("ldap.url") + " / " 
//    			+ env.getRequiredProperty("ldap.principal") + " / " 
//    			+ env.getRequiredProperty("ldap.partitionSuffix"));
    	try {
    		contextSource.getContext("cn=" + username + ",ou=people," + env.getRequiredProperty("ldap.partitionSuffix"), password);
    		System.out.println("\n ======== SUCCESS ========== \n");
    	} catch (Exception ex) {
    		System.out.println("\n ======== AUTH FAILED! ========== \n");
    		System.out.println("Error: " + ex.toString());
    	}        
        
//        System.out.println("\nShow contextSource: " + contextSource.toString());
        // contextSource.getContext("cn=" + username + ",ou=users," + env.getRequiredProperty("ldap.partitionSuffix"), password);
    }
    
    
    public void authenticateUID(final String uid, final String password) {
    	System.out.println("\n authenticate by: " + "useId=" + uid + " \n "
    			+ env.getRequiredProperty("ldap.url") + " / " 
    			+ env.getRequiredProperty("ldap.principal") + " / " 
    			+ env.getRequiredProperty("ldap.partitionSuffix"));
        contextSource.getContext("uid=" + uid + ",ou=people," + env.getRequiredProperty("ldap.partitionSuffix"), password);  
        System.out.println("\n ======== SUCCESS ========== \n");
    }
    

    public List<String> search(final String username) {
    	System.out.println("Search for name: " + username);
    	
    	List<String> foundObj;
    	foundObj = ldapTemplate.search(
    	          "ou=people", 
    	          "cn=" + username,
    	          (AttributesMapper<String>) attrs 
    	          -> (String) attrs.get("cn").get() 
    	          + (String) " "
    	          + (String) attrs.get("mail").get()
    	          + (String) " "
    	          + (String) attrs.get("description").get()
    	          );    	
    	
        return foundObj;
    }

    public List<Map<String,String>> searchPerson(final String username) {
    	List<Map<String,String>> foundObj;
    	foundObj = ldapTemplate.search(
    	          "ou=people", 
    	          "cn=" + username,
    	          (AttributesMapper<Map<String,String>>) attrs 
    	          -> 
	    	          {
	    	        	  Map<String,String> ss = new HashMap<>(); 
	    	        	  attrs.getAll().asIterator().forEachRemaining( atr -> {
							try {
								String skipAttrName = "USERPASSWORD"; //"userPassword";
								String tmpAttrName = atr.getID().toUpperCase();
								if (skipAttrName.equals(tmpAttrName)) {
									// skip the attribute we do not want to save here
								} else {
									ss.put(atr.getID(), atr.get().toString());
								}
								
							} catch (javax.naming.NamingException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}); 
	    	        	  return ss; 
	    	          }
    	          );   
       
        return foundObj;    	
    	
//    	System.out.println("Search for name: " + username);
//    	
//    	List<String> foundObj;
//    	foundObj = ldapTemplate.search(
//    	          "ou=people", 
//    	          "cn=" + username,
//    	          (AttributesMapper<String>) attrs 
//    	          -> (String) attrs.get("cn").get() 
//    	          + (String) " "
//    	          + (String) attrs.get("mail").get()
//    	          + (String) " "
//    	          + (String) attrs.get("description").get()
//    	          );    	
//    	
//        return foundObj;
    }    
    
    public void create(final String username, final String password, final String uid) {
        Name dn = LdapNameBuilder
          .newInstance()
          .add("ou", "people") //.add("ou", "users")          
          .add("username", username)
          .build();
        DirContextAdapter context = new DirContextAdapter(dn);

        context.setAttributeValues("objectclass", new String[] { "top", "person", "organizationalPerson", "inetOrgPerson" });        
        context.setAttributeValue("username", username);
        context.setAttributeValue("sn", username);
        context.setAttributeValue("uid", uid);
        context.setAttributeValue("userPassword", digestSHA(password));

        ldapTemplate.bind(context);
    }

    public void modify(final String username, final String password) {
        Name dn = LdapNameBuilder
          .newInstance()
          .add("ou", "people") //.add("ou", "users")
          .add("username", username)
          .build();
        DirContextOperations context = ldapTemplate.lookupContext(dn);

        context.setAttributeValues("objectclass", new String[] { "top", "person", "organizationalPerson", "inetOrgPerson" });
        context.setAttributeValue("username", username);
        context.setAttributeValue("sn", username);
        context.setAttributeValue("userPassword", digestSHA(password));

        ldapTemplate.modifyAttributes(context);
    }

    private String digestSHA(final String password) {
        String base64;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA");
            digest.update(password.getBytes());
            base64 = Base64
              .getEncoder()
              .encodeToString(digest.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        return "{SHA}" + base64;
    }


	public Object searchAll() {
		// TODO Auto-generated method stub
		return null;
	}
}



/*
 *     public List<String> search(final String username) {
    	System.out.println("Search for name: " + username);
    	
    	
        return ldapTemplate.search(
          "ou=people", //"ou=users",
          "cn=" + username,
          (AttributesMapper<String>) attrs -> (String) attrs
          .get("cn")
          .get() 
          + (String) " "
          + (String) attrs.get("mail").get() 
          + (String) attrs.get("description")
          .get());
    }
 */

