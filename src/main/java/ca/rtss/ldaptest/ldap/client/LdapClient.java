package ca.rtss.ldaptest.ldap.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.ldap.NamingException;
import org.springframework.ldap.core.*;
import org.springframework.ldap.support.LdapNameBuilder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
//import com.sun.tools.javac.code.Attribute.Array;

import ca.rtss.ldaptest.ldap.data.repository.User;

import javax.naming.Name;
import javax.naming.NamingEnumeration;
import javax.naming.directory.Attribute;
import javax.swing.event.ListSelectionEvent;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.Enumeration;
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
    	try {
    		contextSource.getContext("cn=" + username + ",ou=" + env.getRequiredProperty("ldap.usersOU") + "," + env.getRequiredProperty("ldap.partitionSuffix"), password);
//    		contextSource.getContext("cn=" + username + ",ou=people," + env.getRequiredProperty("ldap.partitionSuffix"), password);
    		System.out.println("\n ======== SUCCESS ========== \n");
    	} catch (Exception ex) {
    		System.out.println("\n ======== AUTH FAILED! ========== \n");
    		System.out.println("Error: " + ex.toString());
    	}        

    }
    
    
    public void authenticateUID(final String uid, final String password) {
    	System.out.println("\n authenticate by: " + "useId=" + uid + " \n "
    			+ env.getRequiredProperty("ldap.url") + " / " 
    			+ env.getRequiredProperty("ldap.principal") + " / " 
    			+ env.getRequiredProperty("ldap.partitionSuffix"));
    	
        contextSource.getContext("uid=" + uid + ",ou=" + env.getRequiredProperty("ldap.usersOU") + "," + env.getRequiredProperty("ldap.partitionSuffix"), password);
//        contextSource.getContext("uid=" + uid + ",ou=people," + env.getRequiredProperty("ldap.partitionSuffix"), password);
        System.out.println("\n ======== SUCCESS ========== \n");
    }
    

    public List<String> search(final String username) {
    	System.out.println("Search for name: " + username);
    	String ouPeople = env.getRequiredProperty("ldap.usersOU"); // read: ldap.usersOU= Users,o=Local and replace for "ou=people"
    	List<String> foundObj;
    	foundObj = ldapTemplate.search(
    			  "ou=" + ouPeople, 
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
    
    public List<String> searchUIDOnly(final String uid) {    	
    	List<String> foundObj;
    	String ouPeople = env.getRequiredProperty("ldap.usersOU"); // read: ldap.usersOU= Users,o=Local and replace for "ou=people"
    	foundObj = ldapTemplate.search(
    			  "ou=" + ouPeople, 
    	          "uid=" + uid,
    	          (AttributesMapper<String>) attrs 
    	          -> (String) attrs.get("cn").get() 
    	          ); 
    	
    	System.out.print("\nHere is what object we found: " + foundObj.toString());
    	return foundObj;
    }

    public List<Map<String,String>> searchPerson(final String username) {
    	List<Map<String,String>> foundObj;
    	String ouPeople = env.getRequiredProperty("ldap.usersOU"); // read: ldap.usersOU= Users,o=Local and replace for "ou=people"
    	foundObj = ldapTemplate.search(
    			  "ou=" + ouPeople, 
    	          "cn=" + username,
    	          (AttributesMapper<Map<String,String>>) attrs 
    	          -> 
	    	          {
	    	        	  Map<String,String> ss = new HashMap<>(); 
	    	        	  for(NamingEnumeration<? extends Attribute> all = attrs.getAll(); all.hasMoreElements(); ) {
								try {
									Attribute atr = all.nextElement();
										String skipAttrName = "USERPASSWORD"; //"userPassword";
										String tmpAttrName = atr.getID().toUpperCase();
										if (skipAttrName.equals(tmpAttrName)) {
											// skip the attribute we do not want to save here
										} else {
											ss.put(atr.getID(), atr.get().toString());
										}								
									} catch (javax.naming.NamingException e) {
										e.printStackTrace();
									}
	    	        	  }	    	        	  
	    	        	  return ss; 
	    	          }
    	          );   
       
        return foundObj;    	
    	
    }    
    
    public List<Map<String,String>> searchUid(final String uid) {
    	List<Map<String,String>> foundObj;
    	String ouPeople = env.getRequiredProperty("ldap.usersOU"); // read: ldap.usersOU= Users,o=Local and replace for "ou=people"
    	foundObj = ldapTemplate.search(
    	          "ou=" + ouPeople, 
    	          "uid=" + uid,
    	          (AttributesMapper<Map<String,String>>) attrs 
    	          -> {
    	        	   Map<String,String> ss = new HashMap<>();   
	    	        	  for(NamingEnumeration<? extends Attribute> all = attrs.getAll(); all.hasMoreElements(); ) {
								try {
									Attribute atr = all.nextElement();
										String skipAttrName = "USERPASSWORD"; //"userPassword";
										String tmpAttrName = atr.getID().toUpperCase();
										if (skipAttrName.equals(tmpAttrName)) {
											// skip the attribute we do not want to save here
										} else {
											ss.put(atr.getID(), atr.get().toString());
										}								
									} catch (javax.naming.NamingException e) {
										e.printStackTrace();
									}
	    	        	  }			
	    	        	  return ss; 
	    	          }
    	          );          
        return foundObj;    
    }        
    
    public void create(final String givenName,final String sn,
    		final String password,final String uid,final String mail, 
    		final String businessCategory, final String employeeType, 
    		final String employeeNumber, final String departmentNumber) throws Exception {
    	//final String username, final String givenName,final String sn,final String password,final String uid,final String mail
    	String username, cn = null, ouPeople;    	
//    	try {
        	username = givenName + ' ' + sn;
        	cn = readObjectAttribute(uid, "cn");
        	ouPeople = env.getRequiredProperty("ldap.usersOU"); // read: ldap.usersOU= Users,o=Local and replace for "ou=people"
        	if ( cn == null ) {        		
        		Name dn = LdapNameBuilder
        	              .newInstance()
        	              .add("ou", ouPeople) //.add("ou", "users")          
        	              .add("cn", username)
        	              .build();
        		DirContextAdapter context = new DirContextAdapter(dn);        
        	            context.setAttributeValues("objectclass", new String[] { "top", "person", "organizationalPerson", "inetOrgPerson" });        
        	            context.setAttributeValue("cn", username);
        	            context.setAttributeValue("givenName", givenName);
        	            context.setAttributeValue("sn", sn);
        	            context.setAttributeValue("mail", mail);
        	            context.setAttributeValue("description", codeB64(username)); 
        	            context.setAttributeValue("uid", uid);
        	            
        	            context.setAttributeValue("businessCategory", businessCategory);
        	            context.setAttributeValue("employeeType", employeeType); 
        	            context.setAttributeValue("employeeNumber", employeeNumber);
        	            context.setAttributeValue("departmentNumber", departmentNumber); 
        	            
        	            context.setAttributeValue("userPassword", digestSHA(password));
        	            
        	            System.out.println("Creating user account dn: " + dn.toString());
        	            System.out.println("current context is: " + context.toString());
//        	            System.out.println("=============== end =============== \n");
        	            
        	            ldapTemplate.bind(context);
        	} else {
        		throw new Exception("Exception: account creation failed! Account already exists?");
        	}       	

    }
    
    public void createUser(final String username,final String passwordn) {
    	String ouPeople = env.getRequiredProperty("ldap.usersOU"); // read: ldap.usersOU= Users,o=Local and replace for "ou=people"
        Name dn = LdapNameBuilder
          .newInstance()
          .add("ou", ouPeople)         
          .add("cn", username)
          .build();
        DirContextAdapter context = new DirContextAdapter(dn);
        context.setAttributeValues("objectclass", new String[] { "top", "person", "organizationalPerson", "inetOrgPerson" });        
        context.setAttributeValue("cn", username);
        context.setAttributeValue("sn", username);        
        
//        System.out.println("Creating user account dn: " + dn.toString());
        System.out.println("current context is: " + context.toString());
//        System.out.println("=============== end =============== \n");
        
        ldapTemplate.bind(context);
    }    

    public void modify (final String givenName,final String sn,
    		final String password,final String uid,final String mail, 
    		final String businessCategory, final String employeeType, 
    		final String employeeNumber, final String departmentNumber)  {
    	String username = givenName + ' ' + sn;
    	String ouPeople = env.getRequiredProperty("ldap.usersOU"); // read: ldap.usersOU= Users,o=Local and replace for "ou=people"
        Name dn = LdapNameBuilder
          .newInstance()
          .add("ou", ouPeople) //.add("ou", "users")
          .add("cn", username)
          .build();
        DirContextOperations context = ldapTemplate.lookupContext(dn);      
        context.setAttributeValues("objectclass", new String[] { "top", "person", "organizationalPerson", "inetOrgPerson" });        
        context.setAttributeValue("cn", username);
        context.setAttributeValue("givenName", givenName);
        context.setAttributeValue("sn", sn);
        context.setAttributeValue("mail", mail);
        context.setAttributeValue("description", codeB64(username)); 
        context.setAttributeValue("uid", uid);
        
        context.setAttributeValue("businessCategory", businessCategory);
        context.setAttributeValue("employeeType", employeeType); 
        context.setAttributeValue("employeeNumber", employeeNumber);
        context.setAttributeValue("departmentNumber", departmentNumber); 
        
        context.setAttributeValue("userPassword", digestSHA(password));
        
//        System.out.println("To modify a user account: where dn= " + dn.toString());
        System.out.println("And where current context is: " + context.toString());
//        System.out.println("=============== end =============== \n");
        
        ldapTemplate.modifyAttributes(context);
    }
    
    
    public void modifyUser (
    		// UID must remain the same as it was before modification - this is the way we bind to a user:
    		final String givenName,final String sn,
    		final String password,final String uid,final String mail, 
    		final String businessCategory, final String employeeType, 
    		final String employeeNumber, final String departmentNumber) throws Exception{    	

    	ObjectMapper objectMapper = new ObjectMapper();		
//		String json = null;
    	String ouPeople = env.getRequiredProperty("ldap.usersOU"); // read: ldap.usersOU= Users,o=Local and replace for "ou=people"
		String cn = readObjectAttribute(uid, "cn");  	
    	String username = givenName + ' ' + sn;
    	
        Name oldDn = LdapNameBuilder
          .newInstance()
          .add("ou", ouPeople)
          .add("cn", cn)
          .build();
        
        Name newDn = LdapNameBuilder
                .newInstance()
                .add("ou", ouPeople) 
                .add("cn", username)
                .build();
        
        if (!oldDn.equals(newDn)) {
        	try {
            	ldapTemplate.rename(oldDn, newDn); //rename the object using its DN	
            	cn = readObjectAttribute(uid, "cn");
//    			System.out.println("After update ==> We found a user account cn: " + cn.toString());    			
            } catch (Exception e) {
            	System.out.println(" === LDAP Account rename failed  === ");
            	e.printStackTrace();
            	throw new Exception("Exception: account creation failed!");
            }
        }        
                
        DirContextOperations context = ldapTemplate.lookupContext(newDn);      
        context.setAttributeValues("objectclass", new String[] { "top", "person", "organizationalPerson", "inetOrgPerson" });        
        context.setAttributeValue("cn", username);
        context.setAttributeValue("givenName", givenName);
        context.setAttributeValue("sn", sn);
        context.setAttributeValue("mail", mail);
        context.setAttributeValue("description", codeB64(username)); 
       
        context.setAttributeValue("businessCategory", businessCategory);
        context.setAttributeValue("employeeType", employeeType); 
        context.setAttributeValue("employeeNumber", employeeNumber);
        context.setAttributeValue("departmentNumber", departmentNumber); 
        
        context.setAttributeValue("userPassword", digestSHA(password));
        
//        System.out.println("To modify a user account: where dn= " + oldDn.toString());
        System.out.println("And where current context is: " + context.toString());
//        System.out.println("=============== end =============== \n");
        
//        ldapTemplate.modifyAttributes(context);

        ldapTemplate.modifyAttributes(context);
    }    
    
    private String readObjectAttribute (String uid, String attributeName) {
//    	ObjectMapper objectMapper = new ObjectMapper();	    	
		String cn = null;
		List<String> listOfCnS = null;
		if (attributeName == "cn") {			
//			try {
//				jsonStr = new ObjectMapper().writeValueAsString(searchUIDOnly(uid));
//				System.out.println("\nWe found a user account: " + jsonStr.toString());
//			} catch (JsonProcessingException e1) {
//				System.out.println(" === LDAP Account not found!  === ");
//				e1.printStackTrace();
//			}			
			try {
				listOfCnS = searchUIDOnly(uid);
				cn = (!listOfCnS.isEmpty() && listOfCnS != null) ? listOfCnS.get(0) : null;
//				if (!listOfCnS.isEmpty()) {
//					cn = (listOfCnS.get(0));
//				} else {
//					cn = null;
//				}
			} catch (Exception  e) {
				cn = null;
				e.printStackTrace();				
			} 			
		}
		return cn;
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


	public List<Map<String,String>> searchAll() {

    	List<Map<String,String>> foundObj;
    	foundObj = ldapTemplate.findAll(null); 
       
        return foundObj;   
	}
	
	public String codeB64(String originalInput) {
		String encodedString;
		try {
			encodedString = Base64.getEncoder().encodeToString(originalInput.getBytes());
		} catch (Exception e) {
			encodedString = null;
		}
		return encodedString;
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

/*

	protected void mapToContect (User usr, DirContextOperations context) {
        context.setAttributeValue("cn", usr.username);
        context.setAttributeValue("givenName", givenName);
        context.setAttributeValue("sn", sn);
        context.setAttributeValue("mail", mail);
        context.setAttributeValue("description", codeB64(username)); 
        context.setAttributeValue("uid", uid);
        
        context.setAttributeValue("businessCategory", businessCategory);
        context.setAttributeValue("employeeType", employeeType); 
        context.setAttributeValue("employeeNumber", employeeNumber);
        context.setAttributeValue("departmentNumber", departmentNumber); 
	}

*/

/* 
* Old modify user way:
*
    public void modifyUser(final String username, final String password) {
        Name dn = LdapNameBuilder
          .newInstance()
          .add("ou", "people") //.add("ou", "users")
          .add("cn", username)
          .build();
        DirContextOperations context = ldapTemplate.lookupContext(dn);

        context.setAttributeValues("objectclass", new String[] { "top", "person", "organizationalPerson", "inetOrgPerson" });
        context.setAttributeValue("cn", username);
        context.setAttributeValue("sn", username);
        context.setAttributeValue("userPassword", digestSHA(password));

        ldapTemplate.modifyAttributes(context);
    } 
*/

/*

    private String readObjectAttribute (String uid, String attributeName) {
    	ObjectMapper objectMapper = new ObjectMapper();	
    	String jsonStr = null;
		String cn = null;
		String attributeValue = null;
		if (attributeName == "cn") {			
			try {
				jsonStr = new ObjectMapper().writeValueAsString(searchUid(uid));
				System.out.println("We found a user account: " + jsonStr.toString());
			} catch (JsonProcessingException e1) {
				System.out.println(" === LDAP Account not found!  === ");
				e1.printStackTrace();
			}
			
			try {			
				User[] user = objectMapper.readValue(jsonStr, User[].class);
				System.out.println("Modify by cn = " + user[0].getCn());
				cn = user[0].getCn();
			} catch (IOException  e) {
				cn = null;
				e.printStackTrace();
			} 
			attributeValue = cn;
		}		
		return attributeValue;
    }

*/