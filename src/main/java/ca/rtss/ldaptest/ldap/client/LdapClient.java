package ca.rtss.ldaptest.ldap.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.ldap.NamingException;
import org.springframework.ldap.core.*;
import org.springframework.ldap.query.LdapQueryBuilder;
import org.springframework.ldap.support.LdapNameBuilder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
//import com.sun.tools.javac.code.Attribute.Array;

import ca.rtss.ldaptest.userController;
import ca.rtss.ldaptest.ldap.data.repository.User;
//import jdk.internal.org.jline.utils.Log;

import javax.naming.Name;
import javax.naming.NamingEnumeration;
import javax.naming.directory.Attribute;
import javax.swing.event.ListSelectionEvent;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.Attributes;

import org.apache.commons.codec.digest.Crypt;

//POGO for grouping multiple fields for the Message Container
final class MessageCont
{
	public String name;
	public boolean status;
	public String messageString;
//	public Map<String,String> messageList = new HashMap<>();

	public MessageCont(String name, boolean status, String messageString)
	{
		this.name = name;
		this.status = status;
		this.messageString = messageString;
	}

	public MessageCont() {}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isStatus() {
		return status;
	}

	public void setStatus(boolean status) {
		this.status = status;
	}

	public String getMessageString() {
		return messageString;
	}

	public void setMessageString(String messageString) {
		this.messageString = messageString;
	}

//	public Map<String, String> getMessageList() {
//		String statusStr = Boolean.toString(status);
//		messageList.put("name", name);
//		messageList.put("status", statusStr);
//		messageList.put("messageString", messageString);
//		return messageList;
//	}
//
//	public void setMessageList(Map<String, String> messageList) {
//		this.messageList = messageList;
//	}

	@Override
	public String toString() {
		return "{ MessageCont  : [name=" + name + ", status=" + status + ", messageString=" + messageString + "]";
	}
	
}

//POGO for grouping multiple fields for the Status Container
final class StatusCont
{
	public boolean status;
	public List<MessageCont> messageCont;
	public Map<String,List<String>> messageList;

	public StatusCont(boolean status, List<MessageCont> messageCont)
	{
		this.status = status;
		this.messageCont = messageCont;
	}
	public StatusCont() {}
	
	public boolean isStatus() {
		return status;
	}
	public void setStatus(boolean status) {
		this.status = status;
	}
	public List<MessageCont> getMessageCont() {
		return messageCont;
	}
	public void setMessageCont(List<MessageCont> messageCont) {
		this.messageCont = messageCont;
	}
	
	public Map<String,List<String>> getMessageList() {
		List<String> listMsg = new ArrayList<>();
		for (MessageCont message : messageCont) {
			listMsg.add(message.toString());
		}
		messageList.put("messageString", listMsg);
		return messageList;
	}

	public void setMessageList(Map<String,List<String>> messageList) {
		this.messageList = messageList;
	}
	
	@Override
	public String toString() {
		return "StatusCont [\"status\": " + status + ", \"messageCont:\" \"" + messageCont + "\"]";
	}
	
}

final class GroupMessageCont
{
	public String name;
	public String fullPath;

	public GroupMessageCont(String name, String fullPath)
	{
		this.name = name;
		this.fullPath = fullPath;
	}

	public GroupMessageCont() {}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getFullPath() {
		return fullPath;
	}

	public void setFullPath(String fullPath) {
		this.fullPath = fullPath;
	}

	@Override
	public String toString() {
		return "GroupMessageCont [name=" + name + ", fullPath=" + fullPath + "]";
	}

	
}

// =================================  CLASS: LdapClient ==============================================

public class LdapClient {

    @Autowired
    private Environment env;

    @Autowired
    private ContextSource contextSource;

    @Autowired
    private LdapTemplate ldapTemplate;
    
    private static final Logger LOG = LoggerFactory.getLogger(LdapClient.class);
    
    public String greetings(String name)  {
    	String greetString = "";
    	try {
    		if ((name.trim()).toString().toLowerCase().equals("medved")) {
    			greetString = " <= Preved Medved! => ";
    		}
    		if ((name.trim()).toString().toLowerCase().equals("kirk")) {
    			greetString = " <= Captain on the bridge! => ";
    			name = "Captain James Tiberius Kirk";
    		}
			LOG.info("App version: " + env.getRequiredProperty("info.build.version"));
			greetString += "Greetings: Hello, " 
					+ name + " ... App: " + env.getRequiredProperty("info.build.version");
		} catch (Exception e) {
			LOG.error("Error getting app version");
			greetString += "App: version unknown";
		}		
    	
    	return greetString;
    }
    

	public Map<String,String> getStatus() {
		Map<String,String> response = new HashMap<String, String>();
		
    	try {
    		
			LOG.info("App version: " + env.getRequiredProperty("info.build.version"));
			response.put("version",  env.getRequiredProperty("info.build.version"));
		} catch (Exception e) {
			LOG.error("Error getting app version");
			
		}
		return response;
	}	    

    public void authenticate(final String username, final String password) {
    	String ouPeople = env.getRequiredProperty("ldap.usersFullpath"); 
    	LOG.info("\n authenticate by: " + "name=" + username + " \n ");
    	try {
    		contextSource.getContext("cn=" + username + ",ou=" + env.getRequiredProperty("ldap.usersFullpath") + "," + env.getRequiredProperty("ldap.partitionSuffix"), (password));
//    		contextSource.getContext("cn=" + username + ",ou=people," + env.getRequiredProperty("ldap.partitionSuffix"), password);
    		LOG.info("======== AUTH with UserName -> SUCCESS ========== \n");
    	} catch (Exception e) {
    		LOG.error("Error at authentication process: " + e.getMessage());
    	} 
    	LOG.info("username(cn)=" + username + ",ou=" + env.getRequiredProperty("ldap.usersFullpath") + "," + env.getRequiredProperty("ldap.partitionSuffix"));
    	LOG.info("password with SHA-512 is: " + digestSHA(password));
    	LOG.info("Password digest with CRYPT-SHA-512 = " 
    				+ crypt_SSHA_512(password, null));
    }
    
    
    public void authenticateUID(final String uid, final String password) {
    	 
    	LOG.info("authenticate by: " + "useId=" + uid + " \n "
    			+ env.getRequiredProperty("ldap.url") + " / " 
    			+ env.getRequiredProperty("ldap.principal") + " / " 
    			+ env.getRequiredProperty("ldap.partitionSuffix"));
    	String cn;
        try {
        	cn = readObjectAttribute(uid, "cn");
			contextSource.getContext("cn=" + cn + ",ou=" + env.getRequiredProperty("ldap.usersFullpath") + "," + env.getRequiredProperty("ldap.partitionSuffix"), (password));
			LOG.info("cn=" + cn + ",ou=" + env.getRequiredProperty("ldap.usersFullpath") + "," + env.getRequiredProperty("ldap.partitionSuffix"));
		} catch (NamingException e) {
			e.printStackTrace();
			LOG.error("Error at authentication process: " + e.getMessage());
		} catch (IllegalStateException e) {
			e.printStackTrace();
			LOG.error("Error at authentication process: " + e.getMessage());
		}        
        LOG.info("password is: " + digestSHA(password));
        LOG.info("Password digest with CRYPT-SHA-512 = " 
						+ crypt_SSHA_512(password, null));
        LOG.info("\n ======== Auth with UID -> SUCCESS ========== \n");
    }
    
    
    public List<String> searchUserGetattributes(String username) {
        return ldapTemplate
          .search(
            "ou=users", 
            "cn=" + username, 
            (AttributesMapper<String>) attrs -> (String) attrs.get("cn").get());
    }    
    

    public List<String> search(final String username) {
    	System.out.println("Search for name: " + username);
    	String ouPeople = env.getRequiredProperty("ldap.usersFullpath"); // read: ldap.usersOU= Users,o=Local and replace for "ou=people"
    	List<String> foundObj;
    	foundObj = ldapTemplate.search(
    			  "ou=" + ouPeople, 
    	          "cn=" + username,
    	          (AttributesMapper<String>) attrs 
    	          -> (String) attrs.get("cn").get() 
    	          + (String) " "
    	          + (String) attrs.get("mail").get()
    	          + (String) " "
    	          + (String) attrs.get("title").get()
    	          );    
        return foundObj;
    }
    
    public List<String> searchUIDOnly(final String uid) {    	
    	List<String> foundObj;
    	String ouPeople = env.getRequiredProperty("ldap.usersFullpath"); // read: ldap.usersOU= Users,o=Local and replace for "ou=people"
    	foundObj = ldapTemplate.search(
    			  "ou=" + ouPeople, 
    	          "uid=" + uid,
    	          (AttributesMapper<String>) attrs 
    	          -> (String) attrs.get("cn").get() 
    	          );    	
    	return foundObj;
    }

    public List<Map<String,String>> searchPerson(final String username) {
    	List<Map<String,String>> foundObj;
    	String ouPeople = env.getRequiredProperty("ldap.usersFullpath"); // read: ldap.usersOU= Users,o=Local and replace for "ou=people"
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
    	String ouPeople = env.getRequiredProperty("ldap.usersFullpath"); // read: ldap.usersOU= Users,o=Local and replace for "ou=people"
    	foundObj = ldapTemplate.search(
    			LdapQueryBuilder.query().base("ou=" + ouPeople).attributes("*","memberOf").where("objectclass").is("person").and("uid").like(uid),
    	          (AttributesMapper<Map<String,String>>) attrs 
    	          -> {
    	        	   Map<String,String> ss = new HashMap<>();   
	    	        	  for(NamingEnumeration<? extends Attribute> all = attrs.getAll(); all.hasMoreElements(); ) {
								try {
									Attribute atr = all.nextElement();
										String skipAttrName = "USERPASSWORD"; //"userPassword";
										String tmpAttrName = atr.getID().toUpperCase();							
										
										String attrName = "MEMBEROF";
										if (skipAttrName.equals(tmpAttrName)) {
											// skip the attribute we do not want to save here
										} else if (attrName.equals(tmpAttrName)) {
											// LOG.info("User: id= " + atr.getID() + "; atrStr= " + atr.get().toString());											
											ArrayList<?> membersOf = Collections.list(attrs.get("memberOf").getAll());
											ss.put(atr.getID(), membersOf.toString());
						    	           	// LOG.info("=> membersOfArray= " + membersOf.toString());
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
    
    public List<Map<String,String>> searchUid(final String uid, final String searchStr) {

    	// We will search by: UID + either CN, SN, givenName:
    	List<Map<String,String>> foundObjByCN;
    	List<Map<String,String>> foundObjBySN;
    	List<Map<String,String>> foundObjByGivenName;
    	List<Map<String,String>> finalList ;

    	String ouPeople = env.getRequiredProperty("ldap.usersFullpath"); // read: ldap.usersOU= Users,o=Local and replace for "ou=people"
    	// replaced (is) with (like) to be able to capture wildchars in the search string for email:
    	foundObjByCN = ldapTemplate.search(
    			LdapQueryBuilder.query().base("ou=" + ouPeople).attributes("*","memberOf").where("objectclass").is("person").and("cn").like(searchStr).and("uid").like(uid),
    			(AttributesMapper<Map<String,String>>) attrs 
    			-> 
    			{
    				Map<String,String> ss = new HashMap<>(); 
    				for(NamingEnumeration<? extends Attribute> all = attrs.getAll(); all.hasMoreElements(); ) {
    					try {
    						Attribute atr = all.nextElement();
    						String skipAttrName = "USERPASSWORD"; //"userPassword";
    						String tmpAttrName = atr.getID().toUpperCase();								
    						String attrName = "MEMBEROF";
							if (skipAttrName.equals(tmpAttrName)) {
								// skip the attribute we do not want to save here
							} else if (attrName.equals(tmpAttrName)) {
								// LOG.info("User: id= " + atr.getID() + "; atrStr= " + atr.get().toString());											
								ArrayList<?> membersOf = Collections.list(attrs.get("memberOf").getAll());
								ss.put(atr.getID(), membersOf.toString());
			    	           	// LOG.info("=> membersOfArray= " + membersOf.toString());
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

    	foundObjBySN = ldapTemplate.search(
    			LdapQueryBuilder.query().base("ou=" + ouPeople).attributes("*","memberOf").where("objectclass").is("person").and("sn").like(searchStr).and("uid").like(uid),
    			(AttributesMapper<Map<String,String>>) attrs 
    			-> 
    			{
    				Map<String,String> ss = new HashMap<>(); 
    				for(NamingEnumeration<? extends Attribute> all = attrs.getAll(); all.hasMoreElements(); ) {
    					try {
    						Attribute atr = all.nextElement();
    						String skipAttrName = "USERPASSWORD"; //"userPassword";
    						String tmpAttrName = atr.getID().toUpperCase();
							/*
							 * if (skipAttrName.equals(tmpAttrName)) { // skip the attribute we do not want
							 * to save here } else { ss.put(atr.getID(), atr.get().toString()); }
							 */
    						String attrName = "MEMBEROF";
							if (skipAttrName.equals(tmpAttrName)) {
								// skip the attribute we do not want to save here
							} else if (attrName.equals(tmpAttrName)) {
								// LOG.info("User: id= " + atr.getID() + "; atrStr= " + atr.get().toString());											
								ArrayList<?> membersOf = Collections.list(attrs.get("memberOf").getAll());
								ss.put(atr.getID(), membersOf.toString());
			    	           	// LOG.info("=> membersOfArray= " + membersOf.toString());
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

    	foundObjByGivenName = ldapTemplate.search(
    			LdapQueryBuilder.query().base("ou=" + ouPeople).attributes("*","memberOf").where("objectclass").is("person").and("givenName").like(searchStr).and("uid").like(uid),
    			// "givenName=" + searchStr,
    			(AttributesMapper<Map<String,String>>) attrs 
    			-> 
    			{
    				Map<String,String> ss = new HashMap<>(); 
    				for(NamingEnumeration<? extends Attribute> all = attrs.getAll(); all.hasMoreElements(); ) {
    					try {
    						Attribute atr = all.nextElement();
    						String skipAttrName = "USERPASSWORD"; //"userPassword";
    						String tmpAttrName = atr.getID().toUpperCase();
								
    						String attrName = "MEMBEROF";
							if (skipAttrName.equals(tmpAttrName)) {
								// skip the attribute we do not want to save here
							} else if (attrName.equals(tmpAttrName)) {
								// LOG.info("User: id= " + atr.getID() + "; atrStr= " + atr.get().toString());											
								ArrayList<?> membersOf = Collections.list(attrs.get("memberOf").getAll());
								ss.put(atr.getID(), membersOf.toString());
			    	           	// LOG.info("=> membersOfArray= " + membersOf.toString());
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

    	finalList = foundObjByCN;
    	for (Map<String,String> mapItem : foundObjBySN) {
    		finalList.add(mapItem);    		
    	}
    	for (Map<String,String> mapItem : foundObjByGivenName) {
    		finalList.add(mapItem);    		
    	}
    	return finalList;        	
    	
    } 
    
    public List<Map<String,String>> searchUid(final String uid, final String searchStr, final String mail) {
    	// search ldap user by uid and mail:
    	List<Map<String,String>> foundObj;
    	String ouPeople = env.getRequiredProperty("ldap.usersFullpath"); // read: ldap.usersOU= Users,o=Local and replace for "ou=people"
    	foundObj = ldapTemplate.search(
    			LdapQueryBuilder.query().base("ou=" + ouPeople).attributes("*","memberOf").where("objectclass").is("person").and("uid").like(uid).and("mail").like(mail),
    	          (AttributesMapper<Map<String,String>>) attrs 
    	          -> {
    	        	   Map<String,String> ss = new HashMap<>();   
	    	        	  for(NamingEnumeration<? extends Attribute> all = attrs.getAll(); all.hasMoreElements(); ) {
								try {
									Attribute atr = all.nextElement();
										String skipAttrName = "USERPASSWORD"; //"userPassword";
										String tmpAttrName = atr.getID().toUpperCase();
										String attrName = "MEMBEROF";
										if (skipAttrName.equals(tmpAttrName)) {
											// skip the attribute we do not want to save here
										} else if (attrName.equals(tmpAttrName)) {
											// LOG.info("User: id= " + atr.getID() + "; atrStr= " + atr.get().toString());											
											ArrayList<?> membersOf = Collections.list(attrs.get("memberOf").getAll());
											ss.put(atr.getID(), membersOf.toString());
						    	           	// LOG.info("=> membersOfArray= " + membersOf.toString());
										} else {
											ss.put(atr.getID(), atr.get().toString());
										}
														
									} catch (javax.naming.NamingException e) {
										LOG.error(e.getMessage());
									}
	    	        	  }			
	    	        	  return ss; 
	    	          }
    	          );          
        return foundObj;    
    }
    
    public List<Map<String,String>> searchUserWithQuery(final String queryStr, String attribPattern) {
    	// search ldap user by either of parameters:
    	if (attribPattern == null) {
    		attribPattern = "+"; //else: "memberOf"
    	}
    	List<Map<String,String>> foundObj;
    	String ouPeople = env.getRequiredProperty("ldap.usersFullpath"); // read: ldap.usersOU= Users,o=Local and replace for "ou=people"
    	foundObj = ldapTemplate.search(
    			LdapQueryBuilder.query().base("ou=" + ouPeople).attributes("*",attribPattern.toString()).where("objectclass").is("person")
    			.and(LdapQueryBuilder.query().where("cn").like(queryStr)
    					.or("givenName").like(queryStr)
    					.or("sn").like(queryStr)
    					.or("mail").like(queryStr)
    				),    			
    	          (AttributesMapper<Map<String,String>>) attrs 
    	          -> {
    	        	   Map<String,String> ss = new HashMap<>();
    	        	   String myCN= attrs.get("cn").get().toString();
	    	        	  for(NamingEnumeration<? extends Attribute> all = attrs.getAll(); all.hasMoreElements(); ) {
								try {
									Attribute atr = all.nextElement();
										String skipAttrName = "USERPASSWORD"; //"userPassword";
										String tmpAttrName = atr.getID().toUpperCase();
										String attrName = "MEMBEROF";
										if (skipAttrName.equals(tmpAttrName)) {
											// skip the attribute we do not want to save here
										} else if (attrName.equals(tmpAttrName)) {
											// LOG.info("User: id= " + atr.getID() + "; atrStr= " + atr.get().toString());											
											ArrayList<?> membersOf = Collections.list(attrs.get("memberOf").getAll());
											String memberOfStr = "";
											for (Object member : membersOf) {
												memberOfStr += member.toString() + ";"; 
											}
											ss.put(atr.getID(), memberOfStr);
						    	           	// LOG.info("=> membersOfArray= " + membersOf.toString());
										} else {
											ss.put(atr.getID(), atr.get().toString());
										}
										
									} catch (javax.naming.NamingException e) {										
										LOG.error(e.getMessage());
									}
	    	        	  }			
	    	        	  return ss; 
	    	          }
    	          );        
    	
        return foundObj;    
    }    

    public List<SearchResponse> searchUserWithQueryGetObject(final String queryStr, String attribPattern) {
    	// search ldap user by either of parameters:
    	if (attribPattern == null) {
    		attribPattern = "+"; //else: "memberOf"
    	}
    	List<SearchResponse> finalList = new ArrayList<>() ;    	
    	List<Map<String,String>> foundObj;
    	String ouPeople = env.getRequiredProperty("ldap.usersFullpath"); // read: ldap.usersOU= Users,o=Local and replace for "ou=people"
    	
    	foundObj = ldapTemplate.search(
    			LdapQueryBuilder.query().base("ou=" + ouPeople).attributes("*",attribPattern.toString()).where("objectclass").is("person")
    			.and(LdapQueryBuilder.query().where("cn").like(queryStr)
    					.or("givenName").like(queryStr)
    					.or("sn").like(queryStr)
    					.or("mail").like(queryStr)
    				),    			
    	          (AttributesMapper<Map<String,String>>) attrs 
    	          -> {
    	        	   Map<String,String> ss = new HashMap<>();
    	        	   String uid = attrs.get("uid").get().toString();
    	        	   List<GroupMessageCont> messageContList = new ArrayList<>();
	    	        	  for(NamingEnumeration<? extends Attribute> all = attrs.getAll(); all.hasMoreElements(); ) {
								try {
									Attribute atr = all.nextElement();
										String skipAttrName = "USERPASSWORD"; //"userPassword";
										String tmpAttrName = atr.getID().toUpperCase();
										String attrName = "MEMBEROF";
										if (skipAttrName.equals(tmpAttrName)) {
											// skip the attribute we do not want to save here
										} else if (attrName.equals(tmpAttrName)) {
											// LOG.info("User: id= " + atr.getID() + "; atrStr= " + atr.get().toString());											
											ArrayList<?> membersOf = Collections.list(attrs.get("memberOf").getAll());											
											for (Object member : membersOf) {
												
												List<String> grpNameList = Arrays.asList((member.toString()).split(","));
												messageContList.add(new GroupMessageCont(grpNameList.get(0),member.toString()));
											}
						    	           	// LOG.info("=> membersOfArray= " + membersOf.toString());
										} else {
											ss.put(atr.getID(), atr.get().toString());
										}
										
									} catch (javax.naming.NamingException e) {
										LOG.error(e.getMessage());
									}
	    	        	  }
	    	        	  finalList.add(new SearchResponse(uid, ss, messageContList ));
	    	        	  return ss; 
	    	          }
    	          );
        return finalList;    
    }    
    
    public List<Map<String,String>> searchMail(final String searchStr) {
    	List<Map<String,String>> foundObj;
    	String ouPeople = env.getRequiredProperty("ldap.usersFullpath"); // read: ldap.usersOU= Users,o=Local and replace for "ou=people"
    	foundObj = ldapTemplate.search(
    			LdapQueryBuilder.query().base("ou=" + ouPeople).attributes("*","memberOf").where("objectclass").is("person").and("mail").like(searchStr),
    	          (AttributesMapper<Map<String,String>>) attrs 
    	          -> {
    	        	   Map<String,String> ss = new HashMap<>();   
	    	        	  for(NamingEnumeration<? extends Attribute> all = attrs.getAll(); all.hasMoreElements(); ) {
								try {
									Attribute atr = all.nextElement();
										String skipAttrName = "USERPASSWORD"; //"userPassword";
										String tmpAttrName = atr.getID().toUpperCase();
										String attrName = "MEMBEROF";
										if (skipAttrName.equals(tmpAttrName)) {
										} else if (attrName.equals(tmpAttrName)) {
											ArrayList<?> membersOf = Collections.list(attrs.get("memberOf").getAll());
											ss.put(atr.getID(), membersOf.toString());
										} else {
											ss.put(atr.getID(), atr.get().toString());
										}							
										
									} catch (javax.naming.NamingException e) {
										LOG.error(e.getMessage());
									}
	    	        	  }			
	    	        	  return ss; 
	    	          }
    	          );          
        return foundObj;    
    }        
    

    public List<Map<String,String>> searchPersonMultiParams(final String searchStr) {
    	// We will search by: CN, SN, givenName: only one parameter passed!
    	List<Map<String,String>> foundObjByCN;
    	List<Map<String,String>> foundObjBySN;
    	List<Map<String,String>> foundObjByGivenName;
    	List<Map<String,String>> finalList ;
    	
    	String ouPeople = env.getRequiredProperty("ldap.usersFullpath"); // read: ldap.usersOU= Users,o=Local and replace for "ou=people"
    	foundObjByCN = ldapTemplate.search(
    			LdapQueryBuilder.query().base("ou=" + ouPeople).attributes("*","memberOf").where("objectclass").is("person").and("cn").like(searchStr),
    	          (AttributesMapper<Map<String,String>>) attrs 
    	          -> 
	    	          {
	    	        	  Map<String,String> ss = new HashMap<>(); 
	    	        	  for(NamingEnumeration<? extends Attribute> all = attrs.getAll(); all.hasMoreElements(); ) {
								try {
									Attribute atr = all.nextElement();
										String skipAttrName = "USERPASSWORD"; //"userPassword";
										String tmpAttrName = atr.getID().toUpperCase();
										String attrName = "MEMBEROF";
										if (skipAttrName.equals(tmpAttrName)) {
										} else if (attrName.equals(tmpAttrName)) {										
											ArrayList<?> membersOf = Collections.list(attrs.get("memberOf").getAll());
											ss.put(atr.getID(), membersOf.toString());
										} else {
											ss.put(atr.getID(), atr.get().toString());
										}
														
									} catch (javax.naming.NamingException e) {
										LOG.error(e.getMessage());
									}
	    	        	  }	    	        	  
	    	        	  return ss; 
	    	          }
    	          );   
    	
    	foundObjBySN = ldapTemplate.search(
    		LdapQueryBuilder.query().base("ou=" + ouPeople).attributes("*","memberOf").where("objectclass").is("person").and("sn").like(searchStr),
  	          (AttributesMapper<Map<String,String>>) attrs 
  	          -> 
	    	          {
	    	        	  Map<String,String> ss = new HashMap<>(); 
	    	        	  for(NamingEnumeration<? extends Attribute> all = attrs.getAll(); all.hasMoreElements(); ) {
								try {
									Attribute atr = all.nextElement();
										String skipAttrName = "USERPASSWORD"; //"userPassword";
										String tmpAttrName = atr.getID().toUpperCase();
										String attrName = "MEMBEROF";
										if (skipAttrName.equals(tmpAttrName)) {
											// skip the attribute we do not want to save here
										} else if (attrName.equals(tmpAttrName)) {
											// LOG.info("User: id= " + atr.getID() + "; atrStr= " + atr.get().toString());											
											ArrayList<?> membersOf = Collections.list(attrs.get("memberOf").getAll());
											ss.put(atr.getID(), membersOf.toString());
						    	           	// LOG.info("=> membersOfArray= " + membersOf.toString());
										} else {
											ss.put(atr.getID(), atr.get().toString());
										}														
									} catch (javax.naming.NamingException e) {
										LOG.error(e.getMessage());
									}
	    	        	  }	    	        	  
	    	        	  return ss; 
	    	          }
  	          );     
    	
    	foundObjByGivenName = ldapTemplate.search(
    		LdapQueryBuilder.query().base("ou=" + ouPeople).attributes("*","memberOf").where("objectclass").is("person").and("givenName").like(searchStr),
  	          (AttributesMapper<Map<String,String>>) attrs 
  	          -> 
	    	          {
	    	        	  Map<String,String> ss = new HashMap<>(); 
	    	        	  for(NamingEnumeration<? extends Attribute> all = attrs.getAll(); all.hasMoreElements(); ) {
								try {
									Attribute atr = all.nextElement();
										String skipAttrName = "USERPASSWORD"; //"userPassword";
										String tmpAttrName = atr.getID().toUpperCase();
										String attrName = "MEMBEROF";
										if (skipAttrName.equals(tmpAttrName)) {
											// skip the attribute we do not want to save here
										} else if (attrName.equals(tmpAttrName)) {
											// LOG.info("User: id= " + atr.getID() + "; atrStr= " + atr.get().toString());											
											ArrayList<?> membersOf = Collections.list(attrs.get("memberOf").getAll());
											ss.put(atr.getID(), membersOf.toString());
						    	           	// LOG.info("=> membersOfArray= " + membersOf.toString());
										} else {
											ss.put(atr.getID(), atr.get().toString());
										}
																									
									} catch (javax.naming.NamingException e) {
										LOG.error(e.getMessage());
									}
	    	        	  }	    	        	  
	    	        	  return ss; 
	    	          }
  	          );    
    	
    	finalList = foundObjByCN;
    	for (Map<String,String> mapItem : foundObjBySN) {
    		finalList.add(mapItem);    		
    	}
    	for (Map<String,String> mapItem : foundObjByGivenName) {
    		finalList.add(mapItem);    		
    	}
        return finalList;    	
    	
    }    
    
    // Now we overload the function to check email as well:
    public List<Map<String,String>> searchPersonMultiParams(final String searchStr, final String mail) {
    	// We will search by: CN, SN, givenName:
    	List<Map<String,String>> foundObjByCN;
    	List<Map<String,String>> foundObjBySN;
    	List<Map<String,String>> foundObjByGivenName;
    	List<Map<String,String>> finalList ;
    	
    	String ouPeople = env.getRequiredProperty("ldap.usersFullpath"); // read: ldap.usersOU= Users,o=Local and replace for "ou=people"
    	// replaced (is) with (like) to be able to capture wildchars in the search string for email:
    	foundObjByCN = ldapTemplate.search(
    			LdapQueryBuilder.query().base("ou=" + ouPeople).attributes("*","memberOf").where("objectclass").is("person").and("cn").is(searchStr).and("mail").like(mail),
    	          (AttributesMapper<Map<String,String>>) attrs 
    	          -> 
	    	          {
	    	        	  Map<String,String> ss = new HashMap<>(); 
	    	        	  for(NamingEnumeration<? extends Attribute> all = attrs.getAll(); all.hasMoreElements(); ) {
								try {
									Attribute atr = all.nextElement();
										String skipAttrName = "USERPASSWORD"; //"userPassword";
										String tmpAttrName = atr.getID().toUpperCase();
										
										String attrName = "MEMBEROF";
										if (skipAttrName.equals(tmpAttrName)) {
											// skip the attribute we do not want to save here
										} else if (attrName.equals(tmpAttrName)) {
											// LOG.info("User: id= " + atr.getID() + "; atrStr= " + atr.get().toString());											
											ArrayList<?> membersOf = Collections.list(attrs.get("memberOf").getAll());
											ss.put(atr.getID(), membersOf.toString());
						    	           	// LOG.info("=> membersOfArray= " + membersOf.toString());
										} else {
											ss.put(atr.getID(), atr.get().toString());
										}
														
									} catch (javax.naming.NamingException e) {
										LOG.error(e.getMessage());
									}
	    	        	  }	    	        	  
	    	        	  return ss; 
	    	          }
    	          );   
    	
    	foundObjBySN = ldapTemplate.search(
    		LdapQueryBuilder.query().base("ou=" + ouPeople).attributes("*","memberOf").where("objectclass").is("person").and("sn").is(searchStr).and("mail").like(mail),
  	          (AttributesMapper<Map<String,String>>) attrs 
  	          -> 
	    	          {
	    	        	  Map<String,String> ss = new HashMap<>(); 
	    	        	  for(NamingEnumeration<? extends Attribute> all = attrs.getAll(); all.hasMoreElements(); ) {
								try {
									Attribute atr = all.nextElement();
										String skipAttrName = "USERPASSWORD"; //"userPassword";
										String tmpAttrName = atr.getID().toUpperCase();
										String attrName = "MEMBEROF";
										if (skipAttrName.equals(tmpAttrName)) {
											// skip the attribute we do not want to save here
										} else if (attrName.equals(tmpAttrName)) {
											// LOG.info("User: id= " + atr.getID() + "; atrStr= " + atr.get().toString());											
											ArrayList<?> membersOf = Collections.list(attrs.get("memberOf").getAll());
											ss.put(atr.getID(), membersOf.toString());
						    	           	// LOG.info("=> membersOfArray= " + membersOf.toString());
										} else {
											ss.put(atr.getID(), atr.get().toString());
										}
														
									} catch (javax.naming.NamingException e) {
										LOG.error(e.getMessage());
									}
	    	        	  }	    	        	  
	    	        	  return ss; 
	    	          }
  	          );     
    	
    	foundObjByGivenName = ldapTemplate.search(
    		LdapQueryBuilder.query().base("ou=" + ouPeople).attributes("*","memberOf").where("objectclass").is("person").and("givenName").is(searchStr).and("mail").like(mail),
  	          // "givenName=" + searchStr,
  	          (AttributesMapper<Map<String,String>>) attrs 
  	          -> 
	    	          {
	    	        	  Map<String,String> ss = new HashMap<>(); 
	    	        	  for(NamingEnumeration<? extends Attribute> all = attrs.getAll(); all.hasMoreElements(); ) {
								try {
									Attribute atr = all.nextElement();
										String skipAttrName = "USERPASSWORD"; //"userPassword";
										String tmpAttrName = atr.getID().toUpperCase();
										String attrName = "MEMBEROF";
										if (skipAttrName.equals(tmpAttrName)) {
											// skip the attribute we do not want to save here
										} else if (attrName.equals(tmpAttrName)) {
											// LOG.info("User: id= " + atr.getID() + "; atrStr= " + atr.get().toString());											
											ArrayList<?> membersOf = Collections.list(attrs.get("memberOf").getAll());
											ss.put(atr.getID(), membersOf.toString());
						    	           	// LOG.info("=> membersOfArray= " + membersOf.toString());
										} else {
											ss.put(atr.getID(), atr.get().toString());
										}	
										
									} catch (javax.naming.NamingException e) {
										LOG.error(e.getMessage());
									}
	    	        	  }	    	        	  
	    	        	  return ss; 
	    	          }
  	          );    
    	
    	finalList = foundObjByCN;
    	for (Map<String,String> mapItem : foundObjBySN) {
    		finalList.add(mapItem);    		
    	}
    	for (Map<String,String> mapItem : foundObjByGivenName) {
    		finalList.add(mapItem);    		
    	}
        return finalList;    	
    	
    }      
    
    // Now we overload the function to check email as well:
    public List<Map<String,String>> searchPersonMultiParams(final String uid, final String searchStr, final String mail) {
    	// We will search by: CN, SN, givenName:
    	List<Map<String,String>> foundObjByCN;
    	List<Map<String,String>> foundObjBySN;
    	List<Map<String,String>> foundObjByGivenName;
    	List<Map<String,String>> finalList ;
    	
    	String ouPeople = env.getRequiredProperty("ldap.usersFullpath"); // read: ldap.usersOU= Users,o=Local and replace for "ou=people"
    	// replaced (is) with (like) to be able to capture wildchars in the search string for email:
    	foundObjByCN = ldapTemplate.search(
    			LdapQueryBuilder.query().base("ou=" + ouPeople).attributes("*","memberOf").where("objectclass").is("person").and("uid").is(uid).and("cn").is(searchStr).and("mail").like(mail),
    	          (AttributesMapper<Map<String,String>>) attrs 
    	          -> 
	    	          {
	    	        	  Map<String,String> ss = new HashMap<>(); 
	    	        	  for(NamingEnumeration<? extends Attribute> all = attrs.getAll(); all.hasMoreElements(); ) {
								try {
									Attribute atr = all.nextElement();
										String skipAttrName = "USERPASSWORD"; //"userPassword";
										String tmpAttrName = atr.getID().toUpperCase();
										String attrName = "MEMBEROF";
										if (skipAttrName.equals(tmpAttrName)) {
											// skip the attribute we do not want to save here
										} else if (attrName.equals(tmpAttrName)) {
											// LOG.info("User: id= " + atr.getID() + "; atrStr= " + atr.get().toString());											
											ArrayList<?> membersOf = Collections.list(attrs.get("memberOf").getAll());
											ss.put(atr.getID(), membersOf.toString());
						    	           	// LOG.info("=> membersOfArray= " + membersOf.toString());
										} else {
											ss.put(atr.getID(), atr.get().toString());
										}
								
									} catch (javax.naming.NamingException e) {
										LOG.error(e.getMessage());
									}
	    	        	  }	    	        	  
	    	        	  return ss; 
	    	          }
    	          );   
    	
    	foundObjBySN = ldapTemplate.search(
    		LdapQueryBuilder.query().base("ou=" + ouPeople).attributes("*","memberOf").where("objectclass").is("person").and("sn").is(searchStr).and("mail").like(mail),
  	          (AttributesMapper<Map<String,String>>) attrs 
  	          -> 
	    	          {
	    	        	  Map<String,String> ss = new HashMap<>(); 
	    	        	  for(NamingEnumeration<? extends Attribute> all = attrs.getAll(); all.hasMoreElements(); ) {
								try {
									Attribute atr = all.nextElement();
										String skipAttrName = "USERPASSWORD"; //"userPassword";
										String tmpAttrName = atr.getID().toUpperCase();
										String attrName = "MEMBEROF";
										if (skipAttrName.equals(tmpAttrName)) {
											// skip the attribute we do not want to save here
										} else if (attrName.equals(tmpAttrName)) {
											// LOG.info("User: id= " + atr.getID() + "; atrStr= " + atr.get().toString());											
											ArrayList<?> membersOf = Collections.list(attrs.get("memberOf").getAll());
											ss.put(atr.getID(), membersOf.toString());
						    	           	// LOG.info("=> membersOfArray= " + membersOf.toString());
										} else {
											ss.put(atr.getID(), atr.get().toString());
										}
																				
									} catch (javax.naming.NamingException e) {
										LOG.error(e.getMessage());
									}
	    	        	  }	    	        	  
	    	        	  return ss; 
	    	          }
  	          );     
    	
    	foundObjByGivenName = ldapTemplate.search(
    		LdapQueryBuilder.query().base("ou=" + ouPeople).attributes("*","memberOf").where("objectclass").is("person").and("givenName").is(searchStr).and("mail").like(mail),
  	          // "givenName=" + searchStr,
  	          (AttributesMapper<Map<String,String>>) attrs 
  	          -> 
	    	          {
	    	        	  Map<String,String> ss = new HashMap<>(); 
	    	        	  for(NamingEnumeration<? extends Attribute> all = attrs.getAll(); all.hasMoreElements(); ) {
								try {
									Attribute atr = all.nextElement();
										String skipAttrName = "USERPASSWORD"; //"userPassword";
										String tmpAttrName = atr.getID().toUpperCase();
										String attrName = "MEMBEROF";
										if (skipAttrName.equals(tmpAttrName)) {
											// skip the attribute we do not want to save here
										} else if (attrName.equals(tmpAttrName)) {
											// LOG.info("User: id= " + atr.getID() + "; atrStr= " + atr.get().toString());											
											ArrayList<?> membersOf = Collections.list(attrs.get("memberOf").getAll());
											ss.put(atr.getID(), membersOf.toString());
						    	           	// LOG.info("=> membersOfArray= " + membersOf.toString());
										} else {
											ss.put(atr.getID(), atr.get().toString());
										}
										
									} catch (javax.naming.NamingException e) {
										LOG.error(e.getMessage());
									}
	    	        	  }	    	        	  
	    	        	  return ss; 
	    	          }
  	          );    
    	
    	finalList = foundObjByCN;
    	for (Map<String,String> mapItem : foundObjBySN) {
    		finalList.add(mapItem);    		
    	}
    	for (Map<String,String> mapItem : foundObjByGivenName) {
    		finalList.add(mapItem);    		
    	}
        return finalList;    	
    	
    }     
    
    public void create(
    		String cn, String username,
    		final String givenName,final String sn,
    		final String password,final String uid,final String mail, final String title,
    		final String businessCategory, final String employeeType, 
    		final String employeeNumber, final String departmentNumber,
    		final List<String> groupMember) throws Exception {    	
    	String ouPeople = null, orgLocal = null;    	
    	//    	try {
    	username = givenName + ' ' + sn;
    	cn = readObjectAttribute(uid, "cn");
    	ouPeople = env.getRequiredProperty("ldap.usersOU"); // read: ldap.usersOU= Users,o=Local and replace for "ou=people"
    	orgLocal = env.getRequiredProperty("ldap.orgLocal");
    	if ( cn == null ) {
    		Name dn = null;
    		if (orgLocal != null && orgLocal != "") {
    			// there is an Org-unit (o=local) presented in the ldap configuration
    			dn = LdapNameBuilder
    					.newInstance()
    					.add("o", orgLocal)
    					.add("ou", ouPeople) //.add("ou", "users")          
    					.add("cn", username)
    					.build();
    		} else {
    			// there is only one OU=People in the LDAP path for a user OU
    			dn = LdapNameBuilder
    					.newInstance()
    					.add("ou", ouPeople) //.add("ou", "users")          
    					.add("cn", username)
    					.build();
    		} 
    		DirContextAdapter context = new DirContextAdapter(dn);        
    		context.setAttributeValues("objectclass", new String[] { "top", "person", "organizationalPerson", "inetOrgPerson" });        
    		context.setAttributeValue("cn", username);
    		context.setAttributeValue("givenName", givenName);
    		context.setAttributeValue("sn", sn);
    		context.setAttributeValue("mail", mail);
    		context.setAttributeValue("title", title);
    		context.setAttributeValue("description", codeB64(username)); 
    		context.setAttributeValue("uid", uid);

    		context.setAttributeValue("businessCategory", businessCategory);
    		context.setAttributeValue("employeeType", employeeType); 
    		context.setAttributeValue("employeeNumber", employeeNumber);
    		context.setAttributeValue("departmentNumber", departmentNumber); 

    		//context.setAttributeValue("userPassword", digestSHA(password));
    		context.setAttributeValue("userPassword", crypt_SSHA_512( password , uid ));
    		
    		ldapTemplate.bind(context);
    		LOG.info("Created account with: " + dn.toString());
    	} else {
    		LOG.warn("Failed to create account with: " + uid.toString());
    		throw new Exception("Exception: account creation failed! Account already exists?");
    	}     

    }

    public List<Map<String,String>> createUsers( User[] users) throws Exception {    	
    	List<Map<String,String>> finalList = new ArrayList<>() ;
    	Map<String,String> usersList = new HashMap<>();
    	try {
    		for(User user : users){
    			boolean operationStatus = false;
    			usersList = new HashMap<>();
    			try {    								
    				operationStatus = createLdapUserObject(user);
    				if (operationStatus) {
    					usersList.put("uid",user.getUid());
    					usersList.put("status","OK");
    					usersList.put("message","OK");
    				} else {
    					usersList.put(user.getUid(),"FAIL");
    				}

    			} catch (Exception intException) {
    				usersList.put("uid",user.getUid());
    				usersList.put("status","FAIL");
    				usersList.put("message", intException.getMessage());

    			}
    			finalList.add(usersList);
    		}
    		
    	} catch (Exception e) {
    		LOG.error("Failed account creation! ");			
    	}
    	
    	return finalList;

    }   

    public class UserResponse {
    	//This is service-like class to support messages between controller and ldapClient:
    	public String uid;
    	public String status;
    	public List<MessageCont> messages;
    	
    	public UserResponse(String uid, String status, List<MessageCont> messages) {
    		this.uid = uid;
    		this.status = status;
    		if ( messages == null) {
    			this.messages = Arrays.asList() ; //List.of();
    		} else {
    			this.messages = messages;
    		}
    	}
    }
    
    public class SearchResponse {
    	//This is service-like class to support messages between controller and ldapClient:
    	public String uid;
    	public Map<String,String> properties;
    	public List<GroupMessageCont> memberOf;
    	
    	public SearchResponse(String uid, Map<String,String> properties, List<GroupMessageCont> memberOf) {
    		this.uid = uid;
    		this.properties = properties;
    		if ( memberOf == null) {
    			this.memberOf = Arrays.asList() ; //List.of();
    		} else {
    			this.memberOf = memberOf;
    		}
    	}
    }    
    
//    public List<Map<String,String>> createUsersGetStatus( User[] users) throws Exception {    	
  public List<UserResponse> createUsersGetStatus( User[] users) throws Exception {    	
    	
    	List<UserResponse> finalList = new ArrayList<>() ;
    	Map<String,String> usersList = new HashMap<>();
    	try {
    		for(User user : users){
    			boolean operationStatus;
    			StatusCont operationResultSet;
    			usersList = new HashMap<>();
    			try {
    				// LOG.info("UserID: " + user.getUid());				
    				operationResultSet = createLdapUserObjectAndGetStatus(user);
    				operationStatus = operationResultSet.isStatus();
    				List<MessageCont> groupStatusList = operationResultSet.getMessageCont();
    				String groupMessages = "";
    				for (MessageCont groupStatus : groupStatusList ) {
    					if (groupStatus.status == false) {
    						groupMessages += " Failed: " + groupStatus.name + "! ";
    						//LOG.info("==> Group Operation Is ok: " + groupStatus.name);  
    						operationStatus = false;
    					}
    				}   				
    				
    				if (operationStatus) {
    					finalList.add(new UserResponse(user.getUid(), "OK", operationResultSet.messageCont ));
    					usersList.put("uid",user.getUid());
    					usersList.put("status","OK");
    					usersList.put("groups", operationResultSet.messageCont.toString() );
    				} else {
						/*
						 * finalList.add(new UserResponse(user.getUid(), "WARN: " +
						 * groupMessages.toString(), operationResultSet.messageCont ));
						 */
    					finalList.add(new UserResponse(user.getUid(),"WARN", operationResultSet.messageCont ));
    					usersList.put("uid", user.getUid());
    					usersList.put("status","WARN");
    					usersList.put("groups",operationResultSet.messageCont.toString());
    				}

    			} catch (Exception intException) {
    				
    				finalList.add(new UserResponse(user.getUid(), "FAIL", Arrays.asList(new MessageCont(null, false, intException.getMessage()))));
    				// finalList.add(new UserResponse(user.getUid(), "FAIL", List.of(new MessageCont(null, false, intException.getMessage()))));
    				usersList.put("uid",user.getUid());
    				usersList.put("status","FAIL");
    				usersList.put("groups", intException.getMessage());

    			}
    		}
    	} catch (Exception e) {
    		LOG.error("Failed account creation! " + e.getMessage());	
//			finalList.add(new UserResponse( "User-Object", 
//					"FAIL", List.of(new MessageCont(null, false, e.getMessage()))));
			finalList.add(new UserResponse( "User-Object", 
					"FAIL", Arrays.asList(new MessageCont(null, false, e.getMessage()))));
    	}
    	return finalList;

    }    
    
    public boolean createLdapUserObject (User user) throws Exception {
    	try {
    		// LOG.info("UserID: " + user.getUid());		
    		String username = user.getGivenName() + ' ' + user.getSn();
    		String cn = readObjectAttribute(user.getUid(), "cn");
    		String ouPeople = env.getRequiredProperty("ldap.usersOU"); // read: ldap.usersOU= Users,o=Local and replace for "ou=people"
    		String orgLocal = env.getRequiredProperty("ldap.orgLocal");
    		if ( cn == null ) {
    			Name dn = null;
    			if (orgLocal != null && orgLocal != "") {
    				// there is an Org-unit (o=local) presented in the ldap configuration
    				dn = LdapNameBuilder
    						.newInstance()
    						.add("o", orgLocal)
    						.add("ou", ouPeople) //.add("ou", "users")          
    						.add("cn", username)
    						.build();
    			} else {
    				// there is only one OU=People in the LDAP path for a user OU
    				dn = LdapNameBuilder
    						.newInstance()
    						.add("ou", ouPeople) //.add("ou", "users")          
    						.add("cn", username)
    						.build();
    			}   
    			DirContextAdapter context = new DirContextAdapter(dn);        
    			context.setAttributeValues("objectclass", new String[] { "top", "person", "organizationalPerson", "inetOrgPerson" });        
    			context.setAttributeValue("cn", username);
    			context.setAttributeValue("givenName", user.getGivenName());
    			context.setAttributeValue("sn", user.getSn());
    			context.setAttributeValue("mail", user.getMail());
    			context.setAttributeValue("title", user.getTitle());
    			context.setAttributeValue("description", codeB64(username)); 
    			context.setAttributeValue("uid", user.getUid());
    			context.setAttributeValue("businessCategory", user.getBusinessCategory());
    			context.setAttributeValue("employeeType", user.getEmployeeType()); 
    			context.setAttributeValue("employeeNumber", user.getEmployeeNumber());
    			context.setAttributeValue("departmentNumber", user.getDepartmentNumber()); 

    			// context.setAttributeValue("userPassword", digestSHA(user.getPassword()));
    			context.setAttributeValue("userPassword", crypt_SSHA_512(user.getPassword(), user.getUid()));
    			//				// we replace sha-256 with SSHA512: get_SHA_512_SecurePassword
    			//	            context.setAttributeValue("userPassword", get_SHA_512_SecurePassword(user.getPassword(), codeB64(username)));
    			ldapTemplate.bind(context);
    			LOG.info("Created user account dn: " + dn.toString());	
    			if (user.getGroupMember() != null && user.getGroupMember().size() != 0) {
//    				List<String> groupsList = new ArrayList<>();
//    				groupsList.add(user.getGroupMember());
    				if (addMemberToGroup(user.getGroupMember(), user.getUid())) {
    					LOG.info("Successfully added to the group(s)");
    				} else {
    					LOG.warn("Failure adding to the group(s)");
    				} 
    			}
    		} else {
    			LOG.info("Failed to create account with: " + user.getUid());
    			throw new Exception("Exception: account creation failed! Account already exists?");
    		}	
    	} catch (Exception intException) {
    		LOG.info("Exception in subFunc: account creation failed!" + intException.toString());
    		throw new Exception(intException.toString());			
    	}
    	return true;
    }
    
    public StatusCont createLdapUserObjectAndGetStatus (User user) throws Exception {
//    	Map<String,List<MessageCont>> usersList = new HashMap<>();
    	StatusCont usersList;
    	List<MessageCont> messageContList = new ArrayList<>();
		Boolean isCreated = false;
    	try {    		
    		String username = user.getGivenName() + ' ' + user.getSn();
    		String cn = readObjectAttribute(user.getUid(), "cn");
    		String ouPeople = env.getRequiredProperty("ldap.usersOU"); // read: ldap.usersOU= Users,o=Local and replace for "ou=people"
    		String orgLocal = env.getRequiredProperty("ldap.orgLocal");
    		if ( cn == null ) {
    			Name dn = null;
    			if (orgLocal != null && orgLocal != "") {
    				// there is an Org-unit (o=local) presented in the ldap configuration
    				dn = LdapNameBuilder
    						.newInstance()
    						.add("o", orgLocal)
    						.add("ou", ouPeople) //.add("ou", "users")          
    						.add("cn", username)
    						.build();
    			} else {
    				// there is only one OU=People in the LDAP path for a user OU
    				dn = LdapNameBuilder
    						.newInstance()
    						.add("ou", ouPeople) //.add("ou", "users")          
    						.add("cn", username)
    						.build();
    			}   
    			DirContextAdapter context = new DirContextAdapter(dn);        
    			context.setAttributeValues("objectclass", new String[] { "top", "person", "organizationalPerson", "inetOrgPerson" });        
    			context.setAttributeValue("cn", username);
    			context.setAttributeValue("givenName", user.getGivenName());
    			context.setAttributeValue("sn", user.getSn());
    			context.setAttributeValue("mail", user.getMail());
    			context.setAttributeValue("title", user.getTitle());
    			context.setAttributeValue("description", codeB64(username)); 
    			context.setAttributeValue("uid", user.getUid());
    			context.setAttributeValue("businessCategory", user.getBusinessCategory());
    			context.setAttributeValue("employeeType", user.getEmployeeType()); 
    			context.setAttributeValue("employeeNumber", user.getEmployeeNumber());
    			context.setAttributeValue("departmentNumber", user.getDepartmentNumber()); 

    			// context.setAttributeValue("userPassword", digestSHA(user.getPassword()));
    			// Lets try different methods:
    			// like: crypt_SSHA_512
    			context.setAttributeValue("userPassword", crypt_SSHA_512(user.getPassword(), "$6$%s"));
    			
    			ldapTemplate.bind(context);
    			isCreated = true;
    			LOG.info("Created user account dn: " + dn.toString());	
    			if (user.getGroupMember() != null && user.getGroupMember().size() != 0) {
    				messageContList = addMemberToGroupAndGetStatus(user.getGroupMember(), user.getUid());    			
					
    			}
    		} else {
    			isCreated = false;
    			LOG.info("Failed to create account with: " + user.getUid());
    			throw new Exception(" failed: Account already exists?");
    		}	
    	} catch (Exception intException) {
    		isCreated = false;
    		LOG.info("Exception: account creation failed! " + intException.toString());
    		throw new Exception(intException.toString());			
    	}
    	usersList = new StatusCont(isCreated, messageContList);
    	return usersList;
    }    
    
    public void createUserWithGroupMember(
    		String cn, String username,
    		final String givenName,final String sn,
    		final String password,final String uid,final String mail, final String title,
    		final String businessCategory, final String employeeType, 
    		final String employeeNumber, final String departmentNumber,
    		final List<String> groupMemberList) throws Exception {    	

    	username = givenName + ' ' + sn;
    	cn = readObjectAttribute(uid, "cn");
    	String ouPeople = env.getRequiredProperty("ldap.usersOU"); // read: ldap.usersOU= Users,o=Local and replace for "ou=people"
    	String orgLocal = env.getRequiredProperty("ldap.orgLocal");    	
    	/*
    	 * String ouGroups = env.getRequiredProperty("ldap.groupsOU"); //Groups String
    	 * groupsENAOu = env.getRequiredProperty("ldap.groupsENAOu"); //ENA String
    	 * groupStudentsCn = null; // = env.getRequiredProperty("ldap.groupStudentsCn");
    	 * //GA-ENA-ETUDIANT
    	 */    	
    	if ( cn == null ) {
    		Name dn = null;
    		if (orgLocal != null && orgLocal != "") {
    			// there is an Org-unit (o=local) presented in the ldap configuration
    			dn = LdapNameBuilder
    					.newInstance()
    					.add("o", orgLocal)
    					.add("ou", ouPeople) //.add("ou", "users")          
    					.add("cn", username)
    					.build();
    		} else {
    			// there is only one OU=People in the LDAP path for a user OU
    			dn = LdapNameBuilder
    					.newInstance()
    					.add("ou", ouPeople) //.add("ou", "users")          
    					.add("cn", username)
    					.build();
    		}   
    		DirContextAdapter context = new DirContextAdapter(dn);        
    		context.setAttributeValues("objectclass", new String[] { "top", "person", "organizationalPerson", "inetOrgPerson" });        
    		context.setAttributeValue("cn", username);
    		context.setAttributeValue("givenName", givenName);
    		context.setAttributeValue("sn", sn);
    		context.setAttributeValue("mail", mail);
    		context.setAttributeValue("title", title);
    		context.setAttributeValue("description", codeB64(username)); 
    		context.setAttributeValue("uid", uid);

    		context.setAttributeValue("businessCategory", businessCategory);
    		context.setAttributeValue("employeeType", employeeType); 
    		context.setAttributeValue("employeeNumber", employeeNumber);
    		context.setAttributeValue("departmentNumber", departmentNumber); 

    		// context.setAttributeValue("userPassword", digestSHA(password));
    		context.setAttributeValue("userPassword", crypt_SSHA_512( password,  uid));
    		//    		// we replace sha-256 with SSHA512: get_SHA_512_SecurePassword
    		//            context.setAttributeValue("userPassword", get_SHA_512_SecurePassword(password, codeB64(username)));

//    		LOG.info("Creating user account dn: " + dn.toString());
//    		LOG.info("current context is: " + context.toString());        	            
    		ldapTemplate.bind(context);
    		LOG.info("Created account with DN: " + dn.toString());

    		if (groupMemberList != null && groupMemberList.size() != 0) {
//    			List<String> groupsList = new ArrayList<>();
//    			groupsList.add(groupMember);
    			if (addMemberToGroup(groupMemberList, uid)) {
    				LOG.info("Successfully added to the group");
    			} else {
    				LOG.warn("Failure adding to the group");
    			}   		        		
    		}    		

    	} else {
    		LOG.info("Failed to create account with: " + uid.toString());
    		throw new Exception("Exception: account creation failed! Account already exists?");
    	} 
    }
    
    
    public List<UserResponse> createUserGetStatus( User user) throws Exception {   	
    	List<UserResponse> finalList = new ArrayList<>() ;
    	try {
    			boolean operationStatus;
    			StatusCont operationResultSet;
    			
    			try {
    				// LOG.info("UserID: " + user.getUid());				
    				operationResultSet = createLdapUserObjectAndGetStatus(user);
    				operationStatus = operationResultSet.isStatus();
    				List<MessageCont> groupStatusList = operationResultSet.getMessageCont();
    				String groupMessages = "";
    				for (MessageCont groupStatus : groupStatusList ) {
    					if (groupStatus.status == false) {
    						groupMessages += " Failed: " + groupStatus.name + "! ";
    						//LOG.info("==> Group Operation Is ok: " + groupStatus.name);  
    						operationStatus = false;
    					}
    				}
    				if (operationStatus) {
    					finalList.add(new UserResponse(user.getUid(), "OK", operationResultSet.messageCont ));
    					
    				} else {
						/*
						 * finalList.add(new UserResponse(user.getUid(), "WARN: " +
						 * groupMessages.toString(), operationResultSet.messageCont ));
						 */
    					finalList.add(new UserResponse(user.getUid(), 
								"WARN", 
								operationResultSet.messageCont )); 
    				}

    			} catch (Exception intException) {
    				throw new Exception(intException.getMessage());
    			}
    		
    	} catch (Exception e) {
    		LOG.error("Failed account creation! " + e.getMessage());
//    		finalList.add(new UserResponse(user.getUid(), 
//    						"FAIL", List.of(new MessageCont(null, false, e.getMessage()))));
    		finalList.add(new UserResponse(user.getUid(), 
					"FAIL", Arrays.asList(new MessageCont(null, false, e.getMessage()))));
    		
    		throw new Exception(e.getMessage());
    	}
    	return finalList;
    }     

    public void modify (final String givenName,final String sn,
    		final String password,final String uid,final String mail, final String title,
    		final String businessCategory, final String employeeType, 
    		final String employeeNumber, final String departmentNumber)  throws Exception {
    	String username = givenName + ' ' + sn;
    	String ouPeople = env.getRequiredProperty("ldap.usersOU"); // read: ldap.usersOU= Users,o=Local and replace for "ou=people"
    	String orgLocal = env.getRequiredProperty("ldap.orgLocal");
    	Name dn = null;
    	if (orgLocal != null && orgLocal != "") {
    		// there is an Org-unit (o=local) presented in the ldap configuration
    		dn = LdapNameBuilder
    				.newInstance()
    				.add("o", orgLocal)
    				.add("ou", ouPeople) //.add("ou", "users")          
    				.add("cn", username)
    				.build();
    	} else {
    		// there is only one OU=People in the LDAP path for a user OU
    		dn = LdapNameBuilder
    				.newInstance()
    				.add("ou", ouPeople) //.add("ou", "users")          
    				.add("cn", username)
    				.build();
    	}        		
    	/*
    	 * Name dn = LdapNameBuilder .newInstance() .add("o", orgLocal) .add("ou",
    	 * ouPeople) //.add("ou", "users") .add("cn", username) .build();
    	 */
    	DirContextOperations context = ldapTemplate.lookupContext(dn);      
    	context.setAttributeValues("objectclass", new String[] { "top", "person", "organizationalPerson", "inetOrgPerson" });        
    	context.setAttributeValue("cn", username);
    	context.setAttributeValue("givenName", givenName);
    	context.setAttributeValue("sn", sn);
    	context.setAttributeValue("mail", mail);
    	context.setAttributeValue("title", title);
    	context.setAttributeValue("description", codeB64(username)); 
    	context.setAttributeValue("uid", uid);

    	context.setAttributeValue("businessCategory", businessCategory);
    	context.setAttributeValue("employeeType", employeeType); 
    	context.setAttributeValue("employeeNumber", employeeNumber);
    	context.setAttributeValue("departmentNumber", departmentNumber); 

    	//context.setAttributeValue("userPassword", digestSHA(password));
    	context.setAttributeValue("userPassword", crypt_SSHA_512( password, uid));
    	//     // we replace sha-256 with SSHA512: get_SHA_512_SecurePassword
    	//        context.setAttributeValue("userPassword", get_SHA_512_SecurePassword(password, codeB64(username)));

    	ldapTemplate.modifyAttributes(context);
    	LOG.info("Modified account with: " + dn.toString());
    }


    public void modifyUser (
    		// UID must remain the same as it was before modification - this is the way we bind to a user:  		
    		String cn, String username,
    		final String givenName,final String sn,
    		final String password,final String uid,final String mail, final String title,
    		final String businessCategory, final String employeeType,
    		final String employeeNumber, final String departmentNumber,
    		final List<String> groupMemberList) throws Exception {    	

    	//    	ObjectMapper objectMapper = new ObjectMapper();		
    	String ouPeople = env.getRequiredProperty("ldap.usersOU"); // read: ldap.usersOU= Users,o=Local and replace for "ou=people"
    	String orgLocal = env.getRequiredProperty("ldap.orgLocal");
    	try {
    		cn = readObjectAttribute(uid, "cn");
    		if (cn == null) {
    			LOG.error("Filed to modify group membership: cannot find uid");
    			throw new Exception("Exception: account modification failed! Cannot find uid!");
    		}
    	} catch (Exception excep) {
    		LOG.error("Filed to modify group membership: " + excep.getMessage());
    		throw new Exception("Exception: account modification failed!" + excep.toString());			
    	}  	
    	username = givenName + ' ' + sn;

    	Name oldDn = null;
    	Name newDn = null;
    	if (orgLocal != null && orgLocal != "") {
    		// there is an Org-unit (o=local) presented in the ldap configuration
    		oldDn = LdapNameBuilder
    				.newInstance()
    				.add("o", orgLocal)
    				.add("ou", ouPeople)
    				.add("cn", cn)
    				.build();
    		newDn = LdapNameBuilder
    				.newInstance()
    				.add("o", orgLocal)
    				.add("ou", ouPeople) 
    				.add("cn", username)
    				.build();
    	} else {
    		// there is only one OU=People in the LDAP path for a user OU
    		oldDn = LdapNameBuilder
    				.newInstance()
    				.add("ou", ouPeople)
    				.add("cn", cn)
    				.build();
    		newDn = LdapNameBuilder
    				.newInstance()
    				.add("ou", ouPeople) 
    				.add("cn", username)
    				.build();
    	}      	

    	if (!oldDn.equals(newDn)) {
    		try {
    			ldapTemplate.rename(oldDn, newDn); //rename the object using its DN	
    			cn = readObjectAttribute(uid, "cn");
    			//    			System.out.println("After update ==> We found a user account cn: " + cn.toString());    			
    		} catch (Exception e) {
    			//            	System.out.println(" === LDAP Account rename failed  === ");
    			LOG.error("Filed to modify an account with: oldDn= " + oldDn.toString() + " newDn= " + newDn.toString());
    			e.printStackTrace();
    			throw new Exception("Exception: account modification failed!");
    		}
    	}        

    	DirContextOperations context = ldapTemplate.lookupContext(newDn);      
    	context.setAttributeValues("objectclass", new String[] { "top", "person", "organizationalPerson", "inetOrgPerson" });        
    	context.setAttributeValue("cn", username);
    	context.setAttributeValue("givenName", givenName);
    	context.setAttributeValue("sn", sn);
    	context.setAttributeValue("mail", mail);
    	context.setAttributeValue("title", title);
    	context.setAttributeValue("description", codeB64(username)); 

    	context.setAttributeValue("businessCategory", businessCategory);
    	context.setAttributeValue("employeeType", employeeType); 
    	context.setAttributeValue("employeeNumber", employeeNumber);
    	context.setAttributeValue("departmentNumber", departmentNumber); 

    	// context.setAttributeValue("userPassword", digestSHA(password));
    	context.setAttributeValue("userPassword", crypt_SSHA_512( password, uid));

    	ldapTemplate.modifyAttributes(context);
    	LOG.info("Modified account with: oldDn= " + oldDn.toString() + " newDn= " + newDn.toString());

    	if (groupMemberList != null && groupMemberList.size() != 0) {
    		try {
    			if (addMemberToGroup(groupMemberList, uid)) {
    				LOG.info("Successfully added to the group(s)");
    			} else {
    				LOG.info("Failure adding to the group(s)");
    				throw new Exception("Group membership modification failed!");
    			}
    		} catch (Exception e) {
    			LOG.error("Filed to modify group membership: " + e.getMessage());
    			throw new Exception("Group membership modification failed!");
    		}   		        		
    	} else {
    		LOG.info("No group modification required!");
    	}
    } 
    
    public List<UserResponse> modifyUserGetStatus ( User user ) throws Exception {    	
    	List<UserResponse> finalList = new ArrayList<>() ;    	   	
    	String ouPeople = env.getRequiredProperty("ldap.usersOU"); // read: ldap.usersOU= Users,o=Local and replace for "ou=people"
    	String orgLocal = env.getRequiredProperty("ldap.orgLocal");
    	String cn = null;
    	String username = null;
    	boolean operationStatus;
		StatusCont operationResultSet;		
		List<MessageCont> messageContList = new ArrayList<>();    	
		List<Map<String,String>> userAttribList; // Here were place a user old attributes values
    	Name oldDn = null;
    	Name newDn = null;
    	Boolean isModified = false;
    	try {
    		try {
        		cn = readObjectAttribute(user.getUid(), "cn");   
        		if (cn == null) {
        			LOG.error("Filed to modify group membership: cannot find uid");
        			throw new Exception("Cannot find uid!");
        		}        		
        	} catch (Exception excep) {
        		isModified = false;
        		LOG.error("Filed to modify group membership: " + excep.getMessage());
        		throw new Exception("Exception: account modification failed!" + excep.toString());			
        	}  	        
    		
    		userAttribList = searchUid (user.getUid());
			if (userAttribList == null) {
				LOG.error("Filed to modify an account - cant find ldap account!");
				throw new Exception("Exception: Cant get ldap account proerties - account modification failed!");
			}
//			LOG.info("Old user attributes List: " + userAttribList.toString());
        	try {	
        		String oldGivenName = userAttribList.get(0).get("givenName") != null ? userAttribList.get(0).get("givenName") : "" ;
    			String oldSN = userAttribList.get(0).get("sn") != null ? userAttribList.get(0).get("sn") : "";
    			
				if (oldGivenName.equals(user.getGivenName()) && oldSN.equals(user.getSn())) {
					if (orgLocal != null && orgLocal != "") {
						// there is an Org-unit (o=local) presented in the ldap configuration
						oldDn = LdapNameBuilder
								.newInstance()
								.add("o", orgLocal)
								.add("ou", ouPeople)
								.add("cn", cn)
								.build();
					} else {
						// there is only one OU=People in the LDAP path for a user OU
						oldDn = LdapNameBuilder
								.newInstance()
								.add("ou", ouPeople)
								.add("cn", cn)
								.build();
					}
					newDn = oldDn;
				} else {
					// We assume ther is a name/sn change so CN must be changed:
					username = user.getGivenName() + ' ' + user.getSn();
					if (orgLocal != null && orgLocal != "") {
						// there is an Org-unit (o=local) presented in the ldap configuration
						oldDn = LdapNameBuilder
								.newInstance()
								.add("o", orgLocal)
								.add("ou", ouPeople)
								.add("cn", cn)
								.build();
						newDn = LdapNameBuilder
								.newInstance()
								.add("o", orgLocal)
								.add("ou", ouPeople) 
								.add("cn", username)
								.build();
						LOG.info("Modified account with: oldDn= " + oldDn.toString() + " newDn= " + newDn.toString());
					} else {
						// there is only one OU=People in the LDAP path for a user OU
						oldDn = LdapNameBuilder
								.newInstance()
								.add("ou", ouPeople)
								.add("cn", cn)
								.build();
						newDn = LdapNameBuilder
								.newInstance()
								.add("ou", ouPeople) 
								.add("cn", username)
								.build();
						LOG.info("Modified account with: oldDn= " + oldDn.toString() + " newDn= " + newDn.toString());
					}
					if (!oldDn.equals(newDn)) {					
							ldapTemplate.rename(oldDn, newDn); //rename the object using its DN	
							cn = readObjectAttribute(user.getUid(), "cn");  // checking if all ok
							if (cn == null ) {
								isModified = false;
								LOG.error("Filed to modify an account - cant find ldap account CN!");
								throw new Exception("Exception: Cant get ldap account proerties - account modification failed!");
							}
					} 
				}
				
			} catch (Exception e) {
				isModified = false;
				LOG.error("Filed to modify an account: " + e.getMessage());
				throw new Exception("Exception: Cant get ldap account proerties - account modification failed!");
			}
        	
			DirContextOperations context = ldapTemplate.lookupContext(newDn); 				
			 
			String oldCN = userAttribList.get(0).get("cn");
			String oldGivenName = userAttribList.get(0).get("givenName") != null ? userAttribList.get(0).get("givenName") : "" ;
			String oldSN = userAttribList.get(0).get("sn") != null ? userAttribList.get(0).get("sn") : "";
			
			String oldMail = userAttribList.get(0).get("mail") != null ? userAttribList.get(0).get("mail") : "";
			String oldTitle = userAttribList.get(0).get("title") != null ? userAttribList.get(0).get("title") : "" ;
			String oldDescription = userAttribList.get(0).get("description") != null ? userAttribList.get(0).get("description") : "";
			
			String oldBusinessCategory = userAttribList.get(0).get("businessCategory") != null ? userAttribList.get(0).get("businessCategory") : "" ;
			String oldEmployeeType = userAttribList.get(0).get("employeeType") != null ? userAttribList.get(0).get("employeeType") : "" ;
			String oldEmployeeNumber = userAttribList.get(0).get("employeeNumber") != null ? userAttribList.get(0).get("employeeNumber") : "";
			String oldDepartmentNumber = userAttribList.get(0).get("departmentNumber") != null ? userAttribList.get(0).get("departmentNumber") : "" ;
			
			String oldAttributes = ("Old values of the Attributes: \n" 
										+ " CN= " + oldCN.toString()
										+ " oldGivenName= " + oldGivenName.toString()
										+ " oldSN= " + oldSN.toString()										
										+ " mail=  " + oldMail.toString()
										+ " Title= " + oldTitle.toString()
										+ " Description= " + oldDescription.toString()										
										+ " BusinessCategory= " + oldBusinessCategory.toString()
										+ " EmployeeType= " + oldEmployeeType.toString()
										+ " employeeNumber= " + oldEmployeeNumber.toString()
										+ " departmentNumber= " + oldDepartmentNumber.toString()
					);
			
			context.setAttributeValues("objectclass", new String[] { "top", "person", "organizationalPerson", "inetOrgPerson" });    
			
			if (user.getGivenName() != null && user.getGivenName() != "") {
				context.setAttributeValue("givenName", user.getGivenName());
			}
			
			if (user.getSn() != null && user.getSn() != "") {
				context.setAttributeValue("sn", user.getSn());
			}				
			
			if (user.getMail() != null && user.getMail() != "") {
				context.setAttributeValue("mail", user.getMail());
			}
			
			if (user.getTitle() != null && user.getTitle() != "") {
				context.setAttributeValue("title", user.getTitle());
			}
			
			if (user.getGivenName() != null && user.getSn() != null) {
				context.setAttributeValue("description", codeB64(user.getGivenName() + "," + user.getSn()));
			}
			
			if (user.getBusinessCategory() != null && user.getBusinessCategory() != "") {
				context.setAttributeValue("businessCategory", user.getBusinessCategory());
			}
			
			if (user.getEmployeeType() != null && user.getEmployeeType() != "") {
				context.setAttributeValue("employeeType", user.getEmployeeType()); 
			}
			
			if (user.getEmployeeNumber() != null && user.getEmployeeNumber() != "") {
				context.setAttributeValue("employeeNumber", user.getEmployeeNumber());
			}
			
			if (user.getDepartmentNumber() != null && user.getDepartmentNumber() != "") {
				context.setAttributeValue("departmentNumber", user.getDepartmentNumber()); 
			}			

			// context.setAttributeValue("userPassword", digestSHA(user.getPassword()));
			if (user.getPassword() != null && user.getPassword() != "" ) {
				context.setAttributeValue("userPassword", crypt_SSHA_512(user.getPassword(), user.getUid()));
			}
			
			try {			
				
				ldapTemplate.modifyAttributes(context);
				isModified = true;
			} catch (Exception e) {
				LOG.error("Error when modifying ldap atttribute: " + e.getMessage());
				isModified = false;
			}		
			
			if (user.getGroupMember() != null && user.getGroupMember().size() != 0) {
				messageContList = addMemberToGroupAndGetStatus(user.getGroupMember(), user.getUid());    			
				        		
			} else {
				LOG.info("No group modification required!");
			}
				
    		operationResultSet = new StatusCont(isModified, messageContList);			
			operationStatus = operationResultSet.isStatus();
			List<MessageCont> groupStatusList = operationResultSet.getMessageCont();
			// String groupMessages = "";
			if (groupStatusList != null && groupStatusList.size() != 0) {
				for (MessageCont groupStatus : groupStatusList ) {
					if (groupStatus.status == false) {
						// groupMessages += " Groups failed: " + groupStatus.name + "; ";
						operationStatus = false;
					}
				}
			}			
			
			if (operationStatus) {
				finalList.add(new UserResponse(user.getUid(), "OK", operationResultSet.messageCont ));
			} else {
				finalList.add(new UserResponse(user.getUid(), 
						"WARN", operationResultSet.messageCont ));
				// finalList.add(new UserResponse(user.getUid(),"WARN: " + groupMessages.toString(), operationResultSet.messageCont ));
			}

		} catch (Exception intException) {
			isModified = false;
//			finalList.add(new UserResponse(user.getUid(), 
//							"FAIL", List.of(new MessageCont(null, isModified, intException.getMessage()))));	
			finalList.add(new UserResponse(user.getUid(), 
					"FAIL", Arrays.asList(new MessageCont(null, isModified, intException.getMessage()))));
			
		}    	    	
    	return finalList;
    } 
    
    

    private String readObjectAttribute (String uid, String attributeName) {
    	//    	ObjectMapper objectMapper = new ObjectMapper();	    	
    	String cn = null;
    	List<String> listOfCnS = null;
    	if (attributeName == "cn") {				
    		try {
    			listOfCnS = searchUIDOnly(uid);
    			cn = (!listOfCnS.isEmpty() && listOfCnS != null) ? listOfCnS.get(0) : null;
    		} catch (Exception  e) {
    			cn = null;
    			e.printStackTrace();				
    		} 			
    	}
    	return cn;
    }


    public void delete(String cn, String uid) throws Exception {
    	String ouPeople = env.getRequiredProperty("ldap.usersOU"); 
    	String orgLocal = env.getRequiredProperty("ldap.orgLocal");
    	// cn = readObjectAttribute(uid, "cn"); 
    	try {
    		if (readObjectAttribute(uid, "cn") == null) {
    			LOG.error("Filed to delete ldap account: cannot find uid");
    			throw new Exception("Exception: account deleteion failed! Cannot find uid!");
    		}
    	} catch (Exception excep) {
    		LOG.error("Filed to delete account: " + excep.getMessage());
    		throw new Exception("Exception: account deletion failed!" + excep.toString());			
    	} 

    	if ( cn != null && !cn.isEmpty() ) {
    		Name dn = null;
    		if (orgLocal != null && orgLocal != "") {
    			// there is an Org-unit (o=local) presented in the ldap configuration
    			dn = LdapNameBuilder
    					.newInstance()
    					.add("o", orgLocal)
    					.add("ou", ouPeople) //.add("ou", "users")          
    					.add("cn", cn)
    					.build();
    		} else {
    			// there is only one OU=People in the LDAP path for a user OU
    			dn = LdapNameBuilder
    					.newInstance()
    					.add("ou", ouPeople) //.add("ou", "users")          
    					.add("cn", cn)
    					.build();
    		}  
    		ldapTemplate.unbind(dn);
    		LOG.warn("Removed account with: " + dn.toString() );

    		/*
    		 * Also TODO:
    		 * Remove from the group:
    		 * removeMember(String uid, String groupMember)
    		 */
    	} else {
    		LOG.info("Failed to remove an account with: oldDn= " + uid.toString() );
    		throw new Exception("Exception: ldap account deletion failed! LDAP object not existing?");
    	}
    }


    public void modifyUserName(String givenName, String sn, String uid) throws Exception{    	

    	// ObjectMapper objectMapper = new ObjectMapper();		
    	String ouPeople = env.getRequiredProperty("ldap.usersOU"); // read: ldap.usersOU= Users,o=Local and replace for "ou=people"
    	String orgLocal = env.getRequiredProperty("ldap.orgLocal");
    	String cn;

    	try {
    		cn = readObjectAttribute(uid, "cn");
    		if (cn == null) {
    			LOG.error("Filed to modify user name: cannot find uid");
    			throw new Exception("Exception: account modification failed! Cannot find uid!");
    		}
    	} catch (Exception excep) {
    		LOG.error("Filed to modify user name: " + excep.getMessage());
    		throw new Exception("Exception: account modification failed!" + excep.toString());			
    	} 
    	String username = givenName + ' ' + sn;

    	Name oldDn = null;
    	Name newDn = null;
    	if (orgLocal != null && orgLocal != "") {
    		// there is an Org-unit (o=local) presented in the ldap configuration
    		oldDn = LdapNameBuilder
    				.newInstance()
    				.add("o", orgLocal)
    				.add("ou", ouPeople)
    				.add("cn", cn)
    				.build();
    		newDn = LdapNameBuilder
    				.newInstance()
    				.add("o", orgLocal)
    				.add("ou", ouPeople) 
    				.add("cn", username)
    				.build();
    	} else {
    		// there is only one OU=People in the LDAP path for a user OU
    		oldDn = LdapNameBuilder
    				.newInstance()
    				.add("ou", ouPeople)
    				.add("cn", cn)
    				.build();
    		newDn = LdapNameBuilder
    				.newInstance()
    				.add("ou", ouPeople) 
    				.add("cn", username)
    				.build();
    	}     

    	if (!oldDn.equals(newDn)) {
    		try {
    			ldapTemplate.rename(oldDn, newDn); //rename the object using its DN	
    			cn = readObjectAttribute(uid, "cn");    			
    		} catch (Exception e) {
    			LOG.info("Filed to modify an account name with: oldDn= " + oldDn.toString() + " newDn= " + newDn.toString());
    			e.printStackTrace();
    			throw new Exception("Exception: account modification failed!");
    		}
    	}        

    	DirContextOperations context = ldapTemplate.lookupContext(newDn);      
    	context.setAttributeValues("objectclass", new String[] { "top", "person", "organizationalPerson", "inetOrgPerson" });        
    	context.setAttributeValue("cn", username);
    	context.setAttributeValue("givenName", givenName);
    	context.setAttributeValue("sn", sn);	
    	ldapTemplate.modifyAttributes(context);
    	LOG.info("Modified account Name with: oldDn= " + oldDn.toString() + " newDn= " + newDn.toString());

    }


    public List<UserResponse> modifyUserPasswordV2 ( User user ) throws Exception {    	
    	List<UserResponse> finalList = new ArrayList<>() ;    	   	
    	String ouPeople = env.getRequiredProperty("ldap.usersOU"); // read: ldap.usersOU= Users,o=Local and replace for "ou=people"
    	String orgLocal = env.getRequiredProperty("ldap.orgLocal");
    	String cn = null;
    	String username = null;
    	boolean operationStatus;
		StatusCont operationResultSet;		
		List<MessageCont> messageContList = new ArrayList<>();   
    	Boolean isModified = false;
    	try {
    		 cn = readObjectAttribute(user.getUid(), "cn");   
    		 if (cn == null) {
        			LOG.error("Filed to modify password: cannot find uid");
        			throw new Exception("Cannot find uid!"); 
        	}        	  	
        	username = user.getGivenName() + ' ' + user.getSn();        	
    		Name userDn = null;
    		if (orgLocal != null && orgLocal != "") {
    			// there is an Org-unit (o=local) presented in the ldap configuration
    			userDn = LdapNameBuilder
    					.newInstance()
    					.add("o", orgLocal)
    					.add("ou", ouPeople)
    					.add("cn", cn)
    					.build();
    		} else {
    			// there is only one OU=People in the LDAP path for a user OU
    			userDn = LdapNameBuilder
    					.newInstance()
    					.add("ou", ouPeople)
    					.add("cn", cn)
    					.build();
    		}        

			DirContextOperations context = ldapTemplate.lookupContext(userDn); 
			// context.setAttributeValue("userPassword", digestSHA(user.getPassword()));
			context.setAttributeValue("userPassword", crypt_SSHA_512(user.getPassword(), user.getUid()));

			ldapTemplate.modifyAttributes(context);
			LOG.info("Modified account password! DN= " + userDn.toString());
			isModified = true;			
    		operationResultSet = new StatusCont(isModified, messageContList);		
    		finalList.add(
    						new UserResponse(user.getUid(), 
    											"OK", Arrays.asList(new MessageCont(
    															"password update", isModified, null))
    						)
    					);
		} catch (Exception intException) {
			isModified = false;	
			finalList.add(new UserResponse(user.getUid(), 
					"FAIL", Arrays.asList(new MessageCont(null, isModified, intException.getMessage()))));	
			throw new Exception("Exception: account modification failed!" + intException.toString());
		}    	    	
    	return finalList;
    }     


    public boolean removeMember(String uid, List<String> groupMemberList) throws Exception {

    	boolean isRemovedSuccessfully=false;
    	List<Map<String,String>> userList = searchUid(uid);
    	String givenName = userList.get(0).get("givenName");
    	String sn = userList.get(0).get("sn") ;
    	try {
    		for (String groupMember : groupMemberList) {
				Name groupDn = buildGroupDn(groupMember.toString());
				DirContextOperations ctx = ldapTemplate.lookupContext(groupDn);
				ctx.removeAttributeValue("member", buildPersonDn(uid, givenName, sn).toString() + ","
						+ env.getRequiredProperty("ldap.partitionSuffix"));
				ldapTemplate.modifyAttributes(ctx);
				isRemovedSuccessfully = true;
				LOG.info("Account uid= " + uid + " removed out of the group(s): " + groupMember);
			}
    	} catch (Exception e){
    		isRemovedSuccessfully=false;
    		LOG.error(e.getMessage());
    		throw new Exception("Error: " + e.getMessage());
    	}
    	return isRemovedSuccessfully;
    }	
	
	
	public boolean addMemberToGroup(List<String> groupList, String uid) {
		boolean isAddedSuccessfully=true;
		
		List<Map<String,String>> userList = searchUid(uid);
		String givenName = userList.get(0).get("givenName");
		String sn = userList.get(0).get("sn") ;
		try{
			for(int i=0;i<groupList.size();i++){
				try {
					Name groupDn = buildGroupDn(groupList.get(i));
					DirContextOperations ctx =   ldapTemplate.lookupContext(groupDn);
					ctx.addAttributeValue("member",buildPersonDn(uid,givenName,sn).toString() 
											+ ","+ env.getRequiredProperty("ldap.partitionSuffix"));
					ldapTemplate.modifyAttributes(ctx);
				} catch (Exception e) {
					isAddedSuccessfully=false;
					LOG.error("Failure adding " + uid + " to the group: " + groupList.get(i).toString());
				}
			}
			//isAddedSuccessfully=true;
			LOG.info("Account uid= " + uid + " added to the group(s): " + groupList.toString());
		}
		catch(Exception e){
			isAddedSuccessfully=false;
			LOG.error(e.getMessage());
		}
		return isAddedSuccessfully;
	}
	
	public List<MessageCont> addMemberToGroupAndGetStatus(List<String> groupList, String uid) throws Exception {
		boolean isAddedSuccessfully=false;
		List<MessageCont> messageContList = new ArrayList<>();		
		List<Map<String,String>> userList = searchUid(uid);
		String givenName = userList.get(0).get("givenName");
		String sn = userList.get(0).get("sn") ;
		try{
			for(int i=0;i<groupList.size();i++){
				MessageCont messageCont;
				try {
					Name groupDn = buildGroupDn(groupList.get(i));
					DirContextOperations ctx =   ldapTemplate.lookupContext(groupDn);
					ctx.addAttributeValue("member",buildPersonDn(uid,givenName,sn).toString() 
											+ ","+ env.getRequiredProperty("ldap.partitionSuffix"));
					ldapTemplate.modifyAttributes(ctx);
					isAddedSuccessfully=true;
					messageCont = new MessageCont(groupList.get(i),isAddedSuccessfully,"Success");
				} catch (Exception e) {
					//e.printStackTrace();
					isAddedSuccessfully=false;
					messageCont = new MessageCont(groupList.get(i),isAddedSuccessfully,e.getMessage());
				}
				messageContList.add(messageCont);
			}
			//isAddedSuccessfully=true;			
			LOG.info("Account uid= " + uid + " added to the group(s): " + groupList.toString());
		}
		catch(Exception e){
			isAddedSuccessfully=false;
			LOG.error(e.getMessage());
			throw new Exception("Exception!!! " + e.getMessage());
		}
		return messageContList;
	}	

	private Name buildGroupDn(String groupName) {
		//return LdapNameBuilder.newInstance("cn=groups").add("cn", groupName).build();
		String ouPeople = env.getRequiredProperty("ldap.usersOU"); 
		String orgLocal = env.getRequiredProperty("ldap.orgLocal");    	
		String ouGroups = env.getRequiredProperty("ldap.groupsOU"); //Groups
		String groupsENAOu = env.getRequiredProperty("ldap.groupsENAOu"); //ENA		
		Name groupDN = null;
		groupName = groupName.trim();
		if (orgLocal != null && orgLocal != "" && ouGroups != null && ouGroups !="" && groupsENAOu != null && groupsENAOu !="") {
			// there is an Org-unit (o=local) presented in the ldap configuration
			groupDN = LdapNameBuilder
					.newInstance()
					.add("o", orgLocal)
					.add("ou", ouGroups)
					.add("ou", groupsENAOu) 
					.add("cn", groupName)
					.build();
		} else {
			// there is only one OU=People in the LDAP path for a user OU
			groupDN = LdapNameBuilder
					.newInstance()
					.add("ou", ouPeople) //.add("ou", "users")          
					.add("cn", groupName)
					.build();
		}  
		//LOG.info("==> Function(buildGroupDn) Group DN: " + groupDN.toString());

		return groupDN;
	}

	private Name buildPersonDn(String uid, String givenName, String sn) throws Exception {
		String username = givenName + ' ' + sn;
    	String cn = readObjectAttribute(uid, "cn");
		String ouPeople = env.getRequiredProperty("ldap.usersOU"); 
		String orgLocal = env.getRequiredProperty("ldap.orgLocal");   
		Name dn = null;		
		if ( cn != null ) {
    		if (orgLocal != null && orgLocal != "") {
    			// there is an Org-unit (o=local) presented in the ldap configuration
    			dn = LdapNameBuilder
    					.newInstance()
    					.add("o", orgLocal)
    					.add("ou", ouPeople) //.add("ou", "users")          
    					.add("cn", username)
    					.build();
    		} else {
    			// there is only one OU=People in the LDAP path for a user OU
    			dn = LdapNameBuilder
    					.newInstance()
    					.add("ou", ouPeople) //.add("ou", "users")          
    					.add("cn", username)
    					.build();
    		}
		} else {
			LOG.error("Failed to buildPersonDn for account uid: " + uid.toString());
    		throw new Exception("Exception: Failed to buildPersonDn for account uid! Account not found?");
		}
		return dn;
	}

	private String digestSHA(final String password) {
		String base64;
		try {
			// MessageDigest digest = MessageDigest.getInstance("SHA");
			MessageDigest digest = MessageDigest.getInstance("SHA-512");
			digest.update(password.getBytes());
			base64 = Base64
					.getEncoder()
					.encodeToString(digest.digest());
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
		return "{SHA-512}" + base64;
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
	
	
	public String get_SHA_512_SecurePassword(String passwordToHash, String salt){
		String generatedPassword = null;
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-512");
			md.update(salt.getBytes(StandardCharsets.UTF_8));
			byte[] bytes = md.digest(passwordToHash.getBytes(StandardCharsets.UTF_8));
			StringBuilder sb = new StringBuilder();
			for(int i=0; i< bytes.length ;i++){
				sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
			}
			generatedPassword = sb.toString();
		} 
		catch (NoSuchAlgorithmException e){
			e.printStackTrace();
		}
//		LOG.info("Password generated based on: passwordString=" + passwordToHash.toString() + " salt=" +salt.toString());
//		LOG.info("Password result= " +  "{SSHA-512}" + generatedPassword);
		return "{SSHA-512}" + generatedPassword;
	}

	
	public String crypt_SSHA_512(String passwordToHash, String saltStr){
		
		/*
		 * ===> This is the way how it is done in bash script:
		 * # make password 
		 * pw=$(genpasswd 8) 
		 * pwd=$(slappasswd -h {ssha} -c '$6$%s' -s $pw)
		 * ===> Now lets try our own implementation:
		 */
		// String salt = "$6$" + saltStr.toString();
		SecureRandom random = new SecureRandom();		
		byte[] saltBytes = new byte[16];	
		String salt;
		try {
			random.nextBytes(saltBytes);
			String s = Base64.getEncoder().encodeToString(saltBytes); //new String(saltBytes, StandardCharsets.UTF_8);
			salt = "$6$" + s;
		} catch (Exception e) {
			LOG.error("Salt generation error! " + e.getMessage());
			salt = "$6$" + "salt=" + saltStr.toString();
		}
		
		String hash = Crypt.crypt(passwordToHash.getBytes(),salt);
//		System.out.println("salted hash: "+hash);
//		// salt is $X$some_bytes
//		String saltOfHash = hash.substring(0, hash.indexOf("$", 3));
//		System.out.println("salt: "+ saltOfHash);
//		// validation:
//		String testString = Crypt.crypt(passwordToHash, saltOfHash);
//		if(testString.equals(hash)) {
//		  System.out.println("Password match");
//		} else {
//		  System.out.println("Invalid password");
//		}				

////		LOG.info("Password generated based on: passwordString=" + passwordToHash.toString() + " salt=" +salt.toString());
////		LOG.info("Password result= " +  "{SSHA-512}" + generatedPassword);
		return "{CRYPT}" + hash;
	}	

}


// =================================  END of CLASS: LdapClient ==============================================

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


/*

    public List<Map<String,String>> searchPersonByNamesAndCN(final String searchStr) {
    	// We will search by: CN, SN, givenName:
    	List<Map<String,String>> foundObjByCN;
    	List<Map<String,String>> foundObjBySN;
    	List<Map<String,String>> foundObjByGivenName;
    	List<Map<String,String>> finalList ;
    	
    	String ouPeople = env.getRequiredProperty("ldap.usersFullpath"); // read: ldap.usersOU= Users,o=Local and replace for "ou=people"
    	foundObjByCN = ldapTemplate.search(
    			  "ou=" + ouPeople, 
    	          "cn=" + searchStr,
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
    	
    	foundObjBySN = ldapTemplate.search(
  			  "ou=" + ouPeople, 
  	          "sn=" + searchStr,
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
    	
    	foundObjByGivenName = ldapTemplate.search(
  			  "ou=" + ouPeople, 
  	          "givenName=" + searchStr,
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
    	
    	finalList = foundObjByCN;
    	for (Map<String,String> mapItem : foundObjBySN) {
    		finalList.add(mapItem);    		
    	}
    	for (Map<String,String> mapItem : foundObjByGivenName) {
    		finalList.add(mapItem);    		
    	}
        return finalList;    	
    	
    } 
*/


/*

    public void createUser(final String username,final String passwordn) {
    	String ouPeople = env.getRequiredProperty("ldap.usersOU"); // read: ldap.usersOU= Users,o=Local and replace for "ou=people"
    	String orgLocal = env.getRequiredProperty("ldap.orgLocal");
    	Name dn = null;
		if (orgLocal != null && orgLocal != "") {
			// there is an Org-unit (o=local) presented in the ldap configuration
			dn = LdapNameBuilder
  	              .newInstance()
  	              .add("o", orgLocal)
  	              .add("ou", ouPeople) //.add("ou", "users")          
  	              .add("cn", username)
  	              .build();
		} else {
			// there is only one OU=People in the LDAP path for a user OU
			dn = LdapNameBuilder
    	              .newInstance()
    	              .add("ou", ouPeople) //.add("ou", "users")          
    	              .add("cn", username)
    	              .build();
		}       		
		  	    	
        DirContextAdapter context = new DirContextAdapter(dn);
        context.setAttributeValues("objectclass", new String[] { "top", "person", "organizationalPerson", "inetOrgPerson" });        
        context.setAttributeValue("cn", username);
        context.setAttributeValue("sn", username);        
        
        System.out.println("current context is: " + context.toString());
        
        ldapTemplate.bind(context);
        LOG.info("Created account with: " + dn.toString());
    }  

*/