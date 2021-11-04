package ca.rtss.ldaptest.ldap.client;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
//By Alexey Zapromyotov --- 2021
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ldap.NamingException;
import org.springframework.ldap.core.*;
import org.springframework.ldap.filter.AndFilter;
import org.springframework.ldap.filter.EqualsFilter;
import org.springframework.ldap.query.LdapQueryBuilder;
import org.springframework.ldap.query.SearchScope;
import org.springframework.ldap.support.LdapNameBuilder;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
//import com.sun.tools.javac.code.Attribute.Array;

import ca.rtss.ldaptest.userController;
import ca.rtss.ldaptest.ldap.data.repository.User;
//import jdk.internal.org.jline.utils.Log;

import javax.naming.Name;
import javax.naming.NamingEnumeration;
import javax.naming.directory.Attribute;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.swing.event.ListSelectionEvent;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
	public String status;
	public String messageString;
//	public Map<String,String> messageList = new HashMap<>();

	public MessageCont(String name, String status, String messageString)
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

	public String isStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getMessageString() {
		return messageString;
	}

	public void setMessageString(String messageString) {
		this.messageString = messageString;
	}


	@Override
	public String toString() {
		return "{ MessageCont  : [name=" + name + ", status=" + status + ", messageString=" + messageString + "]";
	}
	
}

//POGO for grouping multiple fields for the Status Container
final class StatusCont
{
	public int status;
	public String statusString;
	public List<MessageCont> messageCont;
	public Map<String,List<String>> messageList;

	public StatusCont(int status, String statusString, List<MessageCont> messageCont)
	{
		this.status = status;
		this.statusString = statusString;
		this.messageCont = messageCont;
	}
	public StatusCont() {}
	
	public int isStatus() {
		return status;
	}
	public void setStatus(int status) {
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
	
	public String getStatusString() {
		return statusString;
	}
	public void setStatusString(String statusString) {
		this.statusString = statusString;
	}
	@Override
	public String toString() {
		return "StatusCont [status=" + status + ", statusString=" + statusString + ", messageCont=" + messageCont
				+ ", messageList=" + messageList + "]";
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
    
    private long globalCount;
    
    public class UserResponse {
    	//This is service-like class to support messages between controller and ldapClient:
    	public String uid;
    	public String status;
    	public String statusString;
    	public List<MessageCont> messages;
    	
    	public UserResponse(String uid, String status, String statusString, List<MessageCont> messages) {
    		this.uid = uid;
    		this.status = status;
    		this.statusString = statusString;
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
    	public List<GroupMessageCont> member;
    	
    	public SearchResponse(String uid, Map<String,String> properties, 
    							List<GroupMessageCont> memberOf) {
    		this.uid = uid;
    		this.properties = properties;
    		if ( memberOf == null) {
    			this.memberOf = Arrays.asList() ; //List.of();
    		} else {
    			this.memberOf = memberOf;
    		}
    	}
    	
    	public SearchResponse(String uid, Map<String,String> properties, 
    			List<GroupMessageCont> memberOf, List<GroupMessageCont> member) {
    		this.uid = uid;
    		this.properties = properties;
    		if ( memberOf == null) {
    			this.memberOf = Arrays.asList() ; //List.of();
    		} else {
    			this.memberOf = memberOf;
    		}
    		if ( member == null) {
    			this.member = Arrays.asList() ;
    		} else {
    			this.member = member;
    		}
    	}

		public Map<String, String> getProperties() {
			return properties;
		}

		public void setProperties(Map<String, String> properties) {
			this.properties = properties;
		}

		public List<GroupMessageCont> getMemberOf() {
			return memberOf;
		}

		public void setMemberOf(List<GroupMessageCont> memberOf) {
			this.memberOf = memberOf;
		}

		public List<GroupMessageCont> getMember() {
			return member;
		}

		public void setMember(List<GroupMessageCont> member) {
			this.member = member;
		}
    } 
    
    
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
	
	public Map<String,String> getAppProps() {
		Map<String,String> response = new HashMap<String, String>();		
    	try {
			response.put("version",  env.getRequiredProperty("info.build.version"));
			String partSuffix = env.getRequiredProperty("ldap.partitionSuffix");
			response.put("partSuffix",  partSuffix);
			String ouPeople = env.getRequiredProperty("ldap.usersFullpath"); // read: ldap.usersOU= Users,o=Local and replace for "ou=people"    	
			response.put("ouPeople",  ouPeople);
			String groupsOU = env.getRequiredProperty("ldap.groupsOU");
			response.put("groupsOU",  groupsOU);
			String ouMUHCAD = env.getRequiredProperty("ldap.muhcproxy"); 
			response.put("ouMUHCAD",  ouMUHCAD);			
			// String testParam = env.getRequiredProperty("ldap.testParam"); 
			// response.put("testParam",  testParam);
			// String testParam2 = env.getRequiredProperty("ldap.testParam2"); 
			// response.put("testParam2",  testParam2);
			String groupsENAOu = env.getRequiredProperty("ldap.groupsENAOu"); 
			response.put("groupsENAOu",  groupsENAOu);
		} catch (Exception e) {
			LOG.error(" --==# Error getting app parameters!!! #==--  " + e.getLocalizedMessage());	
			
		}
		return response;
	}	
	
	public String getInstitutionHash(String id) throws Exception	{
		String remoteURI = env.getRequiredProperty("remote.RESTURI");
	    String result = null;
		try {
			final String uri = remoteURI + "/getInstitutionHash/" + id;
 
			RestTemplate restTemplate = new RestTemplate();
 
//			result = restTemplate.getForObject(uri, String.class);
//			LOG.info("==> Remote REST reply: " + result.toString());			
			ResponseEntity<String> response
								= restTemplate.getForEntity(uri, String.class);
			
			ObjectMapper mapper = new ObjectMapper();
			JsonNode root = mapper.readTree(response.getBody());
			JsonNode hashString = root.path("institution_hash");
			JsonNode is_expired = root.path("is_expired");
			
			if (response.getStatusCodeValue() != 200) {
				LOG.error("Error getting Institution hash by id...  " + response.getStatusCode().toString());	
				throw new Exception("Error getting Institution hash by id...");
			}
			// check if is_expired
			if ( is_expired.toString().equals("1")) {
				LOG.error("Institution Key value is_expired = .  " + is_expired.toString());	
				throw new Exception("Error getting Institution hash by id...");
			}
			LOG.info("==> Remote REST reply: " + hashString.toString());	
			result = hashString.toString();
			
			//System.out.println(result);
		} catch (Exception e) {
			LOG.error("Error getting Institution hash by id...  " + e.getMessage());	
//			throw new Exception("Error getting Institution hash by id...");
		}			
	    
	    return result;
	}	
	
	public boolean isInstitutionHashValid(String hashKey) throws Exception	{
		String remoteURI = env.getRequiredProperty("remote.RESTURI");
		String hashKeyStr = removeDoubleQuotes(hashKey.trim());
		boolean responseOk = false; 
		try {
			String uri = remoteURI + "/validateInstitutionHash/" + hashKeyStr; 
			RestTemplate restTemplate = new RestTemplate();
			ResponseEntity<String> response
								= restTemplate.getForEntity(uri, String.class);
			
			ObjectMapper mapper = new ObjectMapper();
			JsonNode root = mapper.readTree(response.getBody());
			JsonNode message = root.path("message");					
//			LOG.info("response.getStatusCode()= " + response.getStatusCode().toString() );
//			LOG.info("response.message = " + message );
//			LOG.info("response.getStatusCodeValue()= " + response.getStatusCodeValue() );
			
			if (response.getStatusCodeValue() == 200) {
				responseOk = true;
			}
		} catch (Exception e) {
			//LOG.error("Error validating Institution hash...  " + e.getMessage());	
			responseOk = false;
			// throw new Exception("Error validating Institution hash...");
		}			    
	    return responseOk;
	}


	 public boolean headerKeysVlidation (@RequestHeader Map<String, String> headers) throws Exception {
		 String institutionid = null;
		 String institutionhash = null;
		 boolean validationResult = false;
		 for (Map.Entry<String,String> entry : headers.entrySet()) {			

				if (	entry.getKey().toLowerCase().equals("institution_hash") ||
						entry.getKey().toLowerCase().equals("authorization") ||
						entry.getKey().toLowerCase().equals("authentication") ) 
				{
					 //LOG.info(String.format("Header '%s' = %s", entry.getKey(), entry.getValue()));
					 institutionhash = entry.getValue();
				}
		}	
			
			try {
				if (institutionhash == null) {
					LOG.warn("No Authentication Token (Institution HashKey) provided!");
					return false;				
				}
				validationResult = isInstitutionHashValid(institutionhash.toString());
					
				if (!validationResult) {
					return  false;	
				}					
			} catch (JsonProcessingException e) {			
				LOG.error(e.toString());
				return  false;
			}
			return validationResult;
	 }
	
	
	public static String removeDoubleQuotes(String input){

		StringBuilder sb = new StringBuilder();
		
		char[] tab = input.toCharArray();
		for( char current : tab ){
	    	if( current != '"' )
	    		sb.append( current );	
		}
		
		return sb.toString();
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
    
	public Map<String,String> lockUser(User user) throws Exception {
		final String ATTRIBUTE_LOCKOUT_VALUE_PERMANENTLY_LOCKED = "000001010000Z";
		Boolean isModified = false;
		String cn = null;
    	String username = null;
    	String ouPeople = env.getRequiredProperty("ldap.usersOU"); //
    	String orgLocal = env.getRequiredProperty("ldap.orgLocal");
		Map<String,String> response = new HashMap<String, String>();
		List<Map<String,String>> userAttribList; // Here were place a user old attributes values
		try {
    		cn = readObjectAttribute(user.getUid(), "cn");   
    		if (cn == null) {
    			LOG.error("Failed to alter ldap account: cannot resolve uid");
    			response.put("FAIL",  "Failed to alter ldap account: cannot resolve uid");    			
    			throw new Exception("Cannot find uid!");
    		}        	
    		userAttribList = searchUid (user.getUid());
    	} catch (Exception excep) {
    		isModified = false;
    		LOG.error("Filed to alter ldap account: " + excep.getMessage());
    		//throw new Exception("LDAP account lock modificaiotn failed!");
    		if (response.size() == 0) {
    			response.put("FAIL",  "LDAP account lock modificaiotn failed");
    		}  
    		//throw new Exception();
			return null;
    	}
		
    	try {   		    		
    		String oldGivenName = userAttribList.get(0).get("givenName") != null ? userAttribList.get(0).get("givenName") : "" ;
			String oldSN = userAttribList.get(0).get("sn") != null ? userAttribList.get(0).get("sn") : "";
    		Name ldapAccountDN = null;			
			if (orgLocal != null && orgLocal != "") {
				// there is an Org-unit (o=local) presented in the ldap configuration
				ldapAccountDN = LdapNameBuilder
						.newInstance()
						.add("o", orgLocal)
						.add("ou", ouPeople)
						.add("cn", cn)
						.build();
			} else {
				// there is only one OU=People in the LDAP path for a user OU
				ldapAccountDN = LdapNameBuilder
						.newInstance()
						.add("ou", ouPeople)
						.add("cn", cn)
						.build();
			}
			
			String desciptionString = codeB64(readObjectAttribute(user.getUid(), "description"));	
			DirContextOperations context = ldapTemplate.lookupContext(ldapAccountDN);
			ModificationItem[] modificationItems;
			modificationItems = new ModificationItem[2];
			modificationItems[0] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE,
					 	            new BasicAttribute("userPassword", crypt_SSHA_512( generateRandomPassword(16), "$6$%s")));
			modificationItems[1] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE,
	 	            new BasicAttribute("description", "[admin-locked] " + desciptionString ));	 	    
			ldapTemplate.modifyAttributes(ldapAccountDN, modificationItems);
			//LOG.info("Modify attributes: " + modificationItems[0].toString());			
			//ldapTemplate.bind(context);
			
			 /*
			 * 	//			DirContextOperations context = ldapTemplate.lookupContext(ldapAccountDN);
			 *	//			context.setAttributeValue("pwdAccountLockedTime", "000001010000Z");
			 *	//			ldapTemplate.modifyAttributes(context);
			 *				//context.attr
			 * 				//ModificationItem item = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, new BasicAttribute("lockoutTime", 0));
			 *				ModificationItem[] modificationItems;
			 *	    		modificationItems = new ModificationItem[1];
			 *	//		    modificationItems[0] = new ModificationItem(DirContext.REMOVE_ATTRIBUTE,
			 *	//          new BasicAttribute("pwdAccountLockedTime"));
			 *	    		modificationItems[0] = new ModificationItem(DirContext.ADD_ATTRIBUTE,
			 *	            new BasicAttribute("pwdAccountLockedTime", ATTRIBUTE_LOCKOUT_VALUE_PERMANENTLY_LOCKED));
			 * 				
			 * 				ldapTemplate.modifyAttributes(ldapAccountDN, modificationItems);
			 * 				LOG.info("Modified account with: dn= " + ldapAccountDN.toString());
			 */
			
			response.put("account: " + cn,  "locked!");
		} catch (Exception e) {
			LOG.error("Error: " + e.getMessage());
			response.put("Error with account: " + cn,  "Message: " + e.getMessage());
			throw new Exception("Error - can not proceed with account lock process!");
		}
		return response;		
		/*
		 * to lock the account, you can set "pwdAccountLockedTime: 000001010000Z" 
		 * (see man slapo-ppolicy)
		 * 
		 */
	}    
    
    
	/*
	 * public List<String> searchUserGetattributes(String username) { return
	 * ldapTemplate .search( "ou=users", "cn=" + username,
	 * (AttributesMapper<String>) attrs -> (String) attrs.get("cn").get()); }
	 */

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

    // ================================================================================
    // ===============  Searching an ldap object by CN  ===============================  ||
    // ===========================  searchPerson    ===================================  \/
    
    public List<Map<String,String>> searchPerson(final String username) {
    	List<Map<String,String>> foundObj;
    	String ouPeople = env.getRequiredProperty("ldap.usersFullpath"); 
    	// read: ldap.usersOU= Users,o=Local and replace for "ou=people"
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
    					.or("uid").like(queryStr)
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
    					.or("uid").like(queryStr)
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

    public List<SearchResponse> searchUserV5 (final String queryStr, String attribPattern) {
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
    					.or("uid").like(queryStr)
    					.or("givenName").like(queryStr)
    					.or("sn").like(queryStr)
    					.or("mail").like(queryStr)
    				),    			
    	          (AttributesMapper<Map<String,String>>) attrs 
    	          -> {
    	        	   Map<String,String> ss = new HashMap<>();
    	        	   String uid = attrs.get("uid").get().toString();
    	        	   List<GroupMessageCont> messageContList = new ArrayList<>();
    	        	   List<GroupMessageCont> messageContListMember = new ArrayList<>();
	    	        	  for(NamingEnumeration<? extends Attribute> all = attrs.getAll(); all.hasMoreElements(); ) {
								try {
									Attribute atr = all.nextElement();
										String skipAttrName = "USERPASSWORD"; //"userPassword";
										String tmpAttrName = atr.getID().toUpperCase();
										String attrName = "MEMBEROF";
										if (skipAttrName.equals(tmpAttrName)) {
											// skip the attribute we do not want to save here
										} 
											
										else if (attrName.equals(tmpAttrName)) {
											// LOG.info("User: id= " + atr.getID() + "; atrStr= " + atr.get().toString());											
											ArrayList<?> membersOf = Collections.list(attrs.get("memberOf").getAll());											
											for (Object member : membersOf) {												
												List<String> grpNameList = Arrays.asList((member.toString()).split(","));
												messageContList.add(new GroupMessageCont(grpNameList.get(0),member.toString()));
											}
						    	           	LOG.info("=> membersOfArray (in <searchUserV5>)= " + membersOf.toString());
										} 
										else {
											ss.put(atr.getID(), atr.get().toString());
										}
										
									} catch (javax.naming.NamingException e) {
										LOG.error(e.getMessage());
									}
	    	        	  }
	    	        	  
	    	        	  // uid
	    	        	  String cn = attrs.get("cn").get().toString();
	    	        	  LOG.info("=> cn (in <searchUserV5>): " + cn.toString());
	    	        	  
	    	        	  List<String> tmpObjMap = null;
	    	        	  try {
	    	        		  tmpObjMap = getMembersOf(cn);
		    	        	  if (tmpObjMap != null) {
		    	        		  for (String member : tmpObjMap) {
		    	        			  String grpDN = buildGroupDn(member).toString();
		    	        			  messageContListMember.add(new GroupMessageCont(member,grpDN));
		    	        		  }
		    	        	  } 
		    	        	  //LOG.info("=> tmpObjMap (in <searchUserV5>): \n" + tmpObjMap);
	    	        	  } catch (Exception ex) {
	    	        		  LOG.error("=> Error getting tmpObjMap (in <searchUserV5>)\n" + ex.getMessage());
	    	        	  }	    	        	  
	    	        	  
	    	        	  finalList.add(new SearchResponse(uid, ss, messageContList, messageContListMember ));
	    	        	  return ss; 
	    	          }
    	          );
        return finalList; 
        
        /** Snippet:
        Verify authentication of user in specific group:

	        FilterBasedLdapUserSearch search = new FilterBasedLdapUserSearch(
	         "OU=users,DC=mycompany,DC=com",
	         "(&(objectCategory=user)(objectClass=person)(sAMAccountName={0})" +
	         "(memberof:=CN=entergroup,OU=Users,DC=mycompany,DC=com)" +")", contextsource );
	     =================================================================================
			An LDAP search filters on attribute values, so your search needs to match on the member attribute:
			
			ldapTemplate.search(
			    query().
			        where("objectclass").is("groupOfNames").
			        and("member").is("cn=key2,ou=keys"), PERSON_CONTEXT_MAPPER);
			        
			Note that in the case above you need to supply the full DN of the user you're looking for. 
			The filter will match all groupOfName entries where the specified DN is present as a member.
			
			Also, please note that you should never build distinguished name strings manually, 
			since escaping rules etc are tricky. For building the user DN to be included in 
			the member attribute match, have a look at LdapNameBuilder.
        */
    }    
    
    public List<SearchResponse> searchUserV6 (final String queryStr, String attribPattern) {
    	// search uses objectclass = 'like *'
    	// in order to catch all objects even if they have attribute 'authorizablePerson (structural)'
    	if (attribPattern == null) {
    		attribPattern = "+"; //else: "memberOf"
    	}
    	List<SearchResponse> finalList = new ArrayList<>() ;    	
    	try {
    		String partSuffix = env.getRequiredProperty("ldap.partitionSuffix");
    		String ouPeople = env.getRequiredProperty("ldap.usersFullpath"); // read: ldap.usersOU= Users,o=Local and replace for "ou=people"    	
        	LOG.info("\n=> (<searchUserV6>)=> Running Search within OU=  " + ouPeople.toString());

        	String baseDN = buildBaseDN("").toString();
        	// (baseDN == null) ? "" : baseDN
        	List<Map<String,String>> foundObj = ldapTemplate.search(
        			LdapQueryBuilder.query().base((baseDN == null) ? "" : baseDN).attributes("*",attribPattern.toString()).where("objectclass").like("*")
        			.and(LdapQueryBuilder.query().where("cn").like(queryStr)
        					.or("uid").like(queryStr)
        					.or("givenName").like(queryStr)
        					.or("sn").like(queryStr)
        					.or("mail").like(queryStr)
        				),    			
        	          (AttributesMapper<Map<String,String>>) attrs 
        	          -> {
        	        	   SearchResponse searchResponse = getObjectAttrs(attrs);        	        	   
        	        	   Map<String,String> ss = new HashMap<>();
        	        	   String uid = searchResponse.getProperties().get("uid");
        	        	   if (uid != null && uid != "") {
        	        		   finalList.add(searchResponse);  
        	        	   }        	        	    
        	        	   ss = searchResponse.getProperties();
        	        	   return ss; 
    	    	          }
        	          );
    	} catch (Exception allExc) {
    		LOG.error("=> (Error <searchUserV6>)" + allExc.getMessage());
    		throw new RuntimeException(allExc);
    	}    	
        return finalList; 
    }      
    
    public List<SearchResponse> searchUserV7 (final String queryStr) {
    	// search uses objectclass = 'like *'
    	// in order to catch all objects even if they have attribute 'authorizablePerson (structural)'
    	
    	List<SearchResponse> finalList = new ArrayList<>() ;    	
    	try {
    		String partSuffix = env.getRequiredProperty("ldap.partitionSuffix");
    		String ouPeople = env.getRequiredProperty("ldap.usersFullpath"); // read: ldap.usersOU= Users,o=Local and replace for "ou=people"    	
        	LOG.info("\n=> (<searchUserV7>)=> Running Search within OU=  " + ouPeople.toString());
        	String baseDN = buildBaseDN("").toString();
        	List<Map<String,String>> foundObj;
        	BasicAttributes userResult = searchUser( queryStr );
        	//LOG.info("=> (<searchUserV7>) Attributes found \n " + userResult.toString());
        	finalList.add(new SearchResponse(null, null, null ));        	
        	
    	} catch (Exception allExc) {
    		LOG.error("=> (Error <searchUserV7>)" + allExc.getLocalizedMessage());
    		//throw new RuntimeException(allExc);
    	}
    	
        return finalList; 
    }          

    public List<SearchResponse> searchUserV8 (final String queryStr, String attribPattern) {
    	/* search uses objectclass = 'like *'
    	/  in order to catch all objects even if they have attribute 'authorizablePerson (structural)'
    	/  also searching in another OUs
    	*/   	 
    	String ouPeople = null, partSuffix = null, ouMUHCAD = null, groupsENAOu = null;
    	
    	Map<String,String> appPropsMap = new HashMap<String,String>();
    	// String ouPeople = env.getRequiredProperty("ldap.usersFullpath"); // read: ldap.usersOU= Users,o=Local and replace for "ou=people"    	
		// String ouPeople = env.getRequiredProperty("ldap.muhcproxy"); 
    	// String baseDN = buildBaseDN("").toString();
    	// String baseDN = null;
    	try {    		
    		appPropsMap = getAppProps();
    		// LOG.info("\n=> appPropsMap: " + appPropsMap.toString());    		  
    		partSuffix = (appPropsMap.get("partSuffix") != null) ? appPropsMap.get("partSuffix") : "" ;
    		ouPeople = (appPropsMap.get("ouPeople") != null) ? appPropsMap.get("ouPeople") : "" ;    		
    		ouMUHCAD = appPropsMap.get("ouMUHCAD");    		
    		groupsENAOu = (appPropsMap.get("groupsENAOu") != null) ? appPropsMap.get("groupsENAOu") : "" ;
    	} catch (Exception exParams) {
    		//IllegalStateException?
    		LOG.error(" --==# Error reading app params from the search-V8 procedure #==--  " 
    				+ exParams.getLocalizedMessage());
    	}
    	if (attribPattern == null) {
    		attribPattern = "+"; //else: "memberOf"
    	}
    	List<SearchResponse> finalList = new ArrayList<>() ;
    	
    	try { 		

    		String baseDN = buildBaseDN("").toString(); 
        	LOG.info("\n=> (<searchUserV8-AD>)=> Running -= (1st) =- Search with actual baseDN: " + baseDN);
        	
        	List<Map<String,String>> foundObj = ldapTemplate.search(
        			LdapQueryBuilder.query().base((baseDN == null) ? "" : baseDN).attributes("*",attribPattern.toString()).where("objectclass").like("*")
        			.and(LdapQueryBuilder.query().where("cn").like(queryStr)
        					.or("uid").like(queryStr)
        					.or("givenName").like(queryStr)
        					.or("sn").like(queryStr)
        					.or("mail").like(queryStr)
        				),    			
        	          (AttributesMapper<Map<String,String>>) attrs 
        	          -> {
        	        	   SearchResponse searchResponse = getObjectAttrs(attrs);        	        	   
        	        	   Map<String,String> ss = new HashMap<>();
        	        	   String uid = searchResponse.getProperties().get("uid");
        	        	   if (uid != null && uid != "") {
        	        		   finalList.add(searchResponse);  
        	        	   }        	        	    
        	        	   ss = searchResponse.getProperties();
        	        	   return ss; 
    	    	          }
        	          );    		
    		
        	baseDN = (ouMUHCAD != null) ? ouMUHCAD : "" ; //"ou=muhcad,o=proxy";         	
        	LOG.info("\n=> (<searchUserV8-AD>)=> Running -= (2nd) =- Search with actual baseDN: " + baseDN);
        	
        	foundObj = ldapTemplate.search(
        			LdapQueryBuilder.query().base((baseDN == null) ? "" : baseDN).attributes("*",attribPattern.toString()).where("objectclass").like("*")
        			.and(LdapQueryBuilder.query().where("cn").like(queryStr)
        					.or("uid").like(queryStr)
        					.or("givenName").like(queryStr)
        					.or("sn").like(queryStr)
        					.or("mail").like(queryStr)
        				),    			
        	          (AttributesMapper<Map<String,String>>) attrs 
        	          -> {
        	        	   SearchResponse searchResponse = getObjectAttrs(attrs);        	        	   
        	        	   Map<String,String> ss = new HashMap<>();
        	        	   String uid = searchResponse.getProperties().get("uid");
        	        	   if (uid != null && uid != "") {
        	        		   finalList.add(searchResponse);  
        	        	   }        	        	    
        	        	   ss = searchResponse.getProperties();
        	        	   return ss; 
    	    	          }
        	          );
        	
    	} catch (Exception allExc) {
    		LOG.error("=> !(Error <searchUserV8>)!: " + allExc.getMessage());
    		//throw new RuntimeException(allExc);
    	}
    	
        return finalList; 
    }    
    
    public List<SearchResponse> listOUV1 (final String queryStr) {
    	// list OU contents
    	List<SearchResponse> finalList = new ArrayList<>() ;
    	List<GroupMessageCont> messageContList = new ArrayList<>();
    	try {
    		String partSuffix = env.getRequiredProperty("ldap.partitionSuffix");
    		String ouPeople = env.getRequiredProperty("ldap.usersFullpath");    		
        	LOG.info("\n=> (<listOUV1>)=> Running list process within OU=  " + queryStr.toString());        	
        	List<String> objectsNamesInOu = new ArrayList<>();
        	objectsNamesInOu = ldapTemplate.list(buildBaseDN(queryStr));
        	// LOG.info("\n=> (<listOUV1>)=> ldapTemplate=  " + objectsNamesInOu);
        	String ouPath = buildBaseDN(queryStr).toString() + ","+ partSuffix;
        	for (Object member : objectsNamesInOu) {												
				List<String> grpNameList = Arrays.asList((member.toString()).split(","));
				String fullPath = grpNameList.get(0) + "," + buildBaseDN(queryStr).toString() + ","+ partSuffix;
				messageContList.add(new GroupMessageCont(grpNameList.get(0), fullPath));
			}
        	finalList.add(new SearchResponse( ouPath , null, messageContList ));
    	} catch (Exception allExc) {
    		LOG.error("=> (Error <listOUV1> getting the list of OUs)" + allExc.getMessage());
    		throw new RuntimeException(allExc);
    	}
    	
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

   
    
//    public List<Map<String,String>> createUsersGetStatus( User[] users) throws Exception {    	
  public List<UserResponse> createUsersGetStatus( User[] users) throws Exception {    	
    	
    	List<UserResponse> finalList = new ArrayList<>() ;
    	Map<String,String> usersList = new HashMap<>();
    	try {
    		for(User user : users){
    			int operationStatus; // statuses: 0 - fail, 1 - success, 2 - warning...
    			StatusCont operationResultSet;
    			usersList = new HashMap<>();
    			try {
    				// LOG.info("UserID: " + user.getUid());				
    				operationResultSet = createLdapUserObjectAndGetStatus(user);
    				operationStatus = operationResultSet.isStatus();
    				List<MessageCont> groupStatusList = operationResultSet.getMessageCont();
    				String groupMessages = "";
    				for (MessageCont groupStatus : groupStatusList ) {
    					if (groupStatus.status.toUpperCase().equals("FAIL")) {
    						groupMessages += " Failed: " + groupStatus.name + "! ";
    						//LOG.info("==> Group Operation Is ok: " + groupStatus.name);  
    						// We disable changing status:
    						operationStatus = 2; //Warning!
    					} 
    				}   				
    				
    				if (operationStatus == 1) {
    					finalList.add(new UserResponse(user.getUid(), "OK", operationResultSet.statusString, operationResultSet.messageCont ));
    					usersList.put("uid",user.getUid());
    					usersList.put("status","OK");
    					usersList.put("groups", operationResultSet.messageCont.toString() );
    				} else if (operationStatus == 2) {
						/*
						 * finalList.add(new UserResponse(user.getUid(), "WARN: " +
						 * groupMessages.toString(), operationResultSet.messageCont ));
						 */
    					finalList.add(new UserResponse(user.getUid(),"WARN", operationResultSet.statusString, operationResultSet.messageCont ));
    					usersList.put("uid", user.getUid());
    					usersList.put("status","WARN");
    					usersList.put("groups",operationResultSet.messageCont.toString());
    				}  else if (operationStatus == 3) {						
    					finalList.add(new UserResponse(user.getUid(),"EXISTS", operationResultSet.statusString, operationResultSet.messageCont ));
    					usersList.put("uid", user.getUid());
    					usersList.put("status","EXISTS");
    					usersList.put("groups",operationResultSet.messageCont.toString());
    				} else  {
    					finalList.add(new UserResponse(user.getUid(), "FAIL",  operationResultSet.statusString, operationResultSet.messageCont ));
    					usersList.put("uid", user.getUid());
    					usersList.put("status","FAIL");
    					usersList.put("groups",operationResultSet.messageCont.toString());
    				}

    			} catch (Exception intException) {
    				
    				finalList.add(new UserResponse(user.getUid(), "FAIL", "FAIL",Arrays.asList(new MessageCont(null, "FAIL", intException.getMessage()))));
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
					"FAIL", "FAIL", Arrays.asList(new MessageCont(null, "FAIL", e.getMessage()))));
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
    				if (addMemberToGroup(user.getGroupMember(), user.getUid()) == 1) {
    					LOG.info("Successfully added to the group(s)");
    				} else if (addMemberToGroup(user.getGroupMember(), user.getUid()) == 2) {
    					LOG.warn("The were problems adding to the group(s)");
    				} else {
    					LOG.error("Failure adding to the group(s)");
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
    
// ========================================================================================
// ================    createLdapUserObjectAndGetStatus  (User)   =========================  ||
// ========================================================================================  \/
    
    public StatusCont createLdapUserObjectAndGetStatus (User user) throws Exception {
//    	Map<String,List<MessageCont>> usersList = new HashMap<>();
    	StatusCont usersList;
    	String statusString = null, username = null, newUserName = null;
    	List<MessageCont> messageContList = new ArrayList<>();
		int isCreated = 0;
		boolean isDuplicateCNs = false, isFullDuplicates = false;
    	try {    		
    		username = user.getGivenName().trim() + ' ' + user.getSn().trim();
    		String cn = readObjectAttribute(user.getUid(), "cn");
    		
    		// Search for user CN: 	// searchPerson (cn)
    		
    		String userGivenName = user.getGivenName().trim(), userSN = user.getSn().trim(), userUID = user.getUid();
    		// LOG.info("\n--> Creating account: uid=" + user.getUid() + " proposed CN=" + username + " -------<");
    		LOG.info("--> Creating user: \n" + user.toString() + " -------<");
    		List<Map<String,String>> foundByCNList = searchPerson(username);
    		
    		if (foundByCNList.size() > 0) {
    			isDuplicateCNs = true;    			    			
    			// foundByCNList.forEach((foundCN) -> LOG.info(foundCN.toString()));   			
    			
    			for (Map<String, String> objFoundByCN : foundByCNList)	{    				
    				if (objFoundByCN.get("uid").equals(userUID)) {
    					// same UID same CN - duplicates!!!
    					LOG.warn(">>(create ldap)>> Found with the same CN and UID as suggested: uid= " 
    							+ objFoundByCN.get("uid") 
    							+ " cn= " + objFoundByCN.get("cn") );
    		    		isFullDuplicates = true;
    				}    				
    			}
    			
    			if (isFullDuplicates == false) {
    				user.setCn(user.getGivenName().trim() + ' ' + user.getSn().trim());
    				globalCount = 0; //reset globalCount
    				Map<String, String> buildObjectCNMap = buildObjectCNString(user);
    				
    				if (buildObjectCNMap == null) {
    					isCreated = 0;
    	    			statusString = "Failed to create account - CN calculation error";
    	    			LOG.error("Failed to create account - CN callculation error");
    	    			MessageCont messageCont = new MessageCont(null, "FAIL" ,"CN callculation error");
    					messageContList.add(messageCont);
    					usersList = new StatusCont(isCreated, statusString, messageContList);
    			    	return usersList;
    				}
    				LOG.info("***> newCN: " + buildObjectCNMap.get("newCN"));
    				LOG.info("***> message: " + buildObjectCNMap.get("message"));
    				
    				newUserName = user.getCn();
    				/*
    				//userSN = userSN + " (" + user.getUid() + ")";
    				newUserName = user.getGivenName() + " " + user.getSn() + " (" + user.getUid() + ")";
    				foundByCNList = searchPerson(newUserName);
    				if (foundByCNList == null || foundByCNList.size() == 0) {
    					//user.setCn(username);
    					isFullDuplicates = false;
    					//isDuplicateCNs = false;    					
    					LOG.info(">>(create ldap)>> Finally we resolve full duplicate, CN changed to: " + newUserName);    	
    				} else {
    					// there still a duplicated name!
    					isFullDuplicates = true;
    					//isDuplicateCNs = true;
    					statusString = "Account creation failed: full duplicate - same CN and UID";
    					LOG.warn(">>(create ldap)>> Found again full duplicate with the same CN and UID: " + foundByCNList.toString());
    				}
    				*/
    			}				
    			
    		} else {
    			LOG.info(">>(create ldap)>> No accounts found by CN: ");
    		}
    		
    		
    		String ouPeople = env.getRequiredProperty("ldap.usersOU"); // read: ldap.usersOU= Users,o=Local and replace for "ou=people"
    		String orgLocal = env.getRequiredProperty("ldap.orgLocal");
    		if ( isFullDuplicates == false && cn == null &&  user.getUid() != null ) {
    			if (newUserName != null) {
    				username = newUserName;
    			}
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
    			context.setAttributeValue("cn", username.trim());
    			
    			if (user.getGivenName() != null && user.getGivenName() != "") {
    				context.setAttributeValue("givenName", user.getGivenName().trim());
    			}
    			if (user.getSn() != null && user.getSn() != "") {
    				context.setAttributeValue("sn", user.getSn());
    			}
    			String mail = user.getMail();
    			if ( mail != null && mail != "") {
    				context.setAttributeValue("mail", mail.trim());
    			}
    			if (user.getTitle() != null && user.getTitle() != "") {
    				context.setAttributeValue("title", user.getTitle());
    			}    			
    			
    			context.setAttributeValue("description", codeB64(username)); 
    			context.setAttributeValue("uid", user.getUid());
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
    			// Lets try different methods:
    			// like: crypt_SSHA_512
    			context.setAttributeValue("userPassword", crypt_SSHA_512(user.getPassword(), "$6$%s"));
    			
    			ldapTemplate.bind(context);
    			isCreated = 1;
    			statusString = "New Account: " + username ;
    			LOG.info("Created user account dn: " + dn.toString());	
    			if (user.getGroupMember() != null && user.getGroupMember().size() != 0) {
    				messageContList = addMemberToGroupAndGetStatus(user.getGroupMember(), user.getUid());    			
					
    			}
    		} else if (cn != null && cn != "" && user.getUid() != null) {
    			isCreated = 3; // account exists status = 3
    			statusString = "Account exists: " + cn;
    			LOG.warn("Account exists: " + user.getUid()
    						+ " found cn= " + cn.toString());
    			if (user.getGroupMember() != null && user.getGroupMember().size() != 0) {
    				messageContList = addMemberToGroupAndGetStatus(user.getGroupMember(), user.getUid());    	
    			} else {
    				//messageContList = addMemberToGroupAndGetStatus(user.getGroupMember(), user.getUid());
    				MessageCont messageCont = new MessageCont(null, "WARN" ,"No groups modification");
    				messageContList.add(messageCont);
    			}
    		} else if (user.getUid() == null ) {
    			isCreated = 0;
    			statusString = "Failed to create account - empty UID";
    			LOG.error("Failed to create account - empty UID! ");
    			MessageCont messageCont = new MessageCont(null, "FAIL" ,"No user-ID");
				messageContList.add(messageCont);
    		} else {
    			isCreated = 0;
    			statusString = "Failed to create account";
    			LOG.error("Failed to create account!");    			
    			// throw new Exception(" failed: Account already exists?");    			
    			
    		}	
    	} catch (Exception intException) {
    		isCreated = 0;
    		statusString = "Failed to create account";
    		LOG.error("Exception: account creation failed! " + intException.toString());
    		throw new Exception(" Error: account creation failed! " + intException.getCause().getLocalizedMessage()); //intException.toString())			
    	}
    	LOG.info("statusString= " + statusString.toString());
    	usersList = new StatusCont(isCreated, statusString, messageContList);
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

    		/*if (groupMemberList != null && groupMemberList.size() != 0) {
//    			List<String> groupsList = new ArrayList<>();
//    			groupsList.add(groupMember);
    			if (addMemberToGroup(groupMemberList, uid) == 1) {
    				//LOG.info("Successfully added to the group");
    			} else if (addMemberToGroup(groupMemberList, uid) == 2) {
    				LOG.warn("There were some issues adding to the group");
    			}    			
    			else {
    				LOG.warn("Failure adding to the group");
    			}   		        		
    		} 
    		*/   		

    	} else {
    		LOG.info("Failed to create account with: " + uid.toString());
    		throw new Exception("Exception: account creation failed! Account already exists?");
    	} 
    }
    
    
// =============================================================================    
// =====================  createUserGetStatus (User)      ======================  ||
// =============================================================================  \/
    public List<UserResponse> createUserGetStatus( User user) throws Exception {   	
    	List<UserResponse> finalList = new ArrayList<>() ;
    	try {
    			int operationStatus;
    			StatusCont operationResultSet;
    			
    			try {
    				// LOG.info("UserID: " + user.getUid());				
    				operationResultSet = createLdapUserObjectAndGetStatus(user);
    				operationStatus = operationResultSet.isStatus();
    				List<MessageCont> groupStatusList = operationResultSet.getMessageCont();
    				String groupMessages = "";
    				for (MessageCont groupStatus : groupStatusList ) {
    					if (groupStatus.status.toUpperCase().equals("FAIL")) {
    						groupMessages += " Failed: " + groupStatus.name + "! ";
    						//LOG.info("==> Group Operation Is ok: " + groupStatus.name);  
    						operationStatus = 2;
    					} else if (groupStatus.status.toUpperCase().equals("WARN")) {
    						operationStatus = 2;
    					}
    				}
    				if (operationStatus == 1) {
    					finalList.add(new UserResponse(user.getUid(), "OK", operationResultSet.statusString, operationResultSet.messageCont ));    					
    				} else if (operationStatus == 2) {
    					finalList.add(new UserResponse(user.getUid(), "WARN", operationResultSet.statusString, operationResultSet.messageCont ));
    				} else if (operationStatus == 3) {
    					finalList.add(new UserResponse(user.getUid(), "EXISTS", operationResultSet.statusString, operationResultSet.messageCont ));
    				}
    				else {
						/*
						 * finalList.add(new UserResponse(user.getUid(), "WARN: " +
						 * groupMessages.toString(), operationResultSet.messageCont ));
						 */
    					finalList.add(new UserResponse(user.getUid(), 
								"FAIL", "FAIL",
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
					"FAIL", "FAIL", 
					Arrays.asList(new MessageCont(null, "FAIL", e.getMessage()))));
    		
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

		/*
		 * if (!oldDn.equals(newDn)) { try { ldapTemplate.rename(oldDn, newDn); //rename
		 * the object using its DN cn = readObjectAttribute(uid, "cn"); } catch
		 * (Exception e) { LOG.error("Filed to modify an account with: oldDn= " +
		 * oldDn.toString() + " newDn= " + newDn.toString()); //e.printStackTrace();
		 * throw new Exception("Exception: account modification failed!"); } }
		 */        

    	DirContextOperations context = ldapTemplate.lookupContext(oldDn);      
    	context.setAttributeValues("objectclass", new String[] { "top", "person", "organizationalPerson", "inetOrgPerson" });        
    	// context.setAttributeValue("cn", username);
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
    	// context.setAttributeValue("userPassword", crypt_SSHA_512( password, uid));
    	if (password != null && password.trim() != "" ) {
			context.setAttributeValue("userPassword", crypt_SSHA_512( password, uid));
		}

    	ldapTemplate.modifyAttributes(context);
    	//LOG.info("Modified account with: Dn= " + oldDn.toString());

    	if (groupMemberList != null && groupMemberList.size() != 0) {
    		try {
    			if (addMemberToGroup(groupMemberList, uid) == 1) {
    				//LOG.info("Successfully added to the group(s)");
    			} else if (addMemberToGroup(groupMemberList, uid) == 2) {
    				LOG.warn("There were some issues with membership!");
    			}
    			else {
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
    	String cn = null, statusString = null;
    	int operationStatus;
		StatusCont operationResultSet;		
		List<MessageCont> messageContList = new ArrayList<>();    	
		List<Map<String,String>> userAttribList; // Here were place a user old attributes values
    	Name oldDn = null;
    	Name newDn = null;
    	int isModified = 0;
    	try {
    		try {
        		cn = readObjectAttribute(user.getUid(), "cn");   
        		if (cn == null) {
        			LOG.error("Failed to modify group membership: cannot find uid");
        			throw new Exception("Cannot find uid!");
        		}        		
        	} catch (Exception excep) {
        		isModified = 0;
        		LOG.error("Filed to modify group membership: " + excep.getMessage());
        		throw new Exception("Exception: account modification failed!" );			
        	}  	        
    		
    		userAttribList = searchUid (user.getUid());
			if (userAttribList == null || userAttribList.size() == 0) {
				LOG.error("Failed to modify an account - cant find ldap account!");
				throw new Exception("Exception: Cant get ldap account proerties - account modification failed!");
			}

        	try {	    			
				// if (oldGivenName.equals(user.getGivenName()) && oldSN.equals(user.getSn())) 
				{
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
				} 				
				
			} catch (Exception e) {
				isModified = 0;
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
				String updatedObjCN = readObjectAttribute(user.getUid(), "cn");
				statusString = updatedObjCN;
				isModified = 1;
			} catch (Exception e) {
				LOG.error("Error when modifying ldap atttribute: " + e.getMessage());
				isModified = 0;
			}		
			
			if (user.getGroupMember() != null && user.getGroupMember().size() != 0) {
				messageContList = addMemberToGroupAndGetStatus(user.getGroupMember(), user.getUid());    			
				        		
			} else {
				LOG.info("No group modification required!");
			}
				
    		operationResultSet = new StatusCont(isModified, statusString, messageContList);		
    		
			operationStatus = operationResultSet.isStatus();
			List<MessageCont> groupStatusList = operationResultSet.getMessageCont();
			// String groupMessages = "";
			if (groupStatusList != null && groupStatusList.size() != 0) {
				for (MessageCont groupStatus : groupStatusList ) {
					if (groupStatus.status.toUpperCase().equals("FAIL")) {
						// groupMessages += " Groups failed: " + groupStatus.name + "; ";
						operationStatus = 0;
					} else if (groupStatus.status.toUpperCase().equals("SUCCESS")) {
						// groupMessages += " Groups failed: " + groupStatus.name + "; ";
						operationStatus = 1;
					} else if (groupStatus.status.toUpperCase().equals("WARN")) {
						// groupMessages += " Groups failed: " + groupStatus.name + "; ";
						operationStatus = 2;
					}
				}
			}			
			
			if (operationStatus == 1) {
				finalList.add(new UserResponse(user.getUid(), 
						"OK", operationResultSet.statusString, operationResultSet.messageCont ));
			} else if (operationStatus == 2) {
				finalList.add(new UserResponse(user.getUid(), 
						"WARN", operationResultSet.statusString, operationResultSet.messageCont ));
			} else if (operationStatus == 3) {
				finalList.add(new UserResponse(user.getUid(), 
						"WARN", operationResultSet.statusString, operationResultSet.messageCont ));
			}
			else {
				finalList.add(new UserResponse(user.getUid(), 
						"WARN", operationResultSet.statusString, operationResultSet.messageCont ));
				// finalList.add(new UserResponse(user.getUid(),"WARN: " + groupMessages.toString(), operationResultSet.messageCont ));
			}

		} catch (Exception intException) {
			isModified = 0;	
			finalList.add(new UserResponse(user.getUid(), 
					"FAIL", "FAIL", 
					Arrays.asList(new MessageCont(null, "FAIL", intException.getMessage()))));
			
		}    	    	
    	return finalList;
    } 
    
    

    private String readObjectAttribute (String uid, String attributeName) {
    	//    	ObjectMapper objectMapper = new ObjectMapper();	    	
    	String cn = null, description = null;
    	String attributeReturns = null;
    	List<String> listOfCnS = null;
    	if (attributeName == "cn") {				
    		try {
    			listOfCnS = searchUIDOnly(uid);
    			cn = (!listOfCnS.isEmpty() && listOfCnS != null) ? listOfCnS.get(0) : null;
    		} catch (Exception  e) {
    			// cn not found - there is no user with such CN in ldap!    			
    			cn = null;
    			//e.printStackTrace();				
    		}
    		attributeReturns = cn;
    	} else if (attributeName == "description") {				
    		try {
    			listOfCnS = searchUIDOnly(uid);
    			description = (!listOfCnS.isEmpty() && listOfCnS != null) ? listOfCnS.get(0) : null;
    		} catch (Exception  e) {
    			description = null;
    			e.printStackTrace();				
    		}
    		attributeReturns = description;
    	}
    	return attributeReturns;
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
    	String username = null, statusString = null;
    	int operationStatus;
		StatusCont operationResultSet;		
		List<MessageCont> messageContList = new ArrayList<>();   
    	int isModified = 0;
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
			String updatedObjCN = readObjectAttribute(user.getUid(), "cn");
			statusString = "CN= " + updatedObjCN;
			
			LOG.info("Modified account password! DN= " + userDn.toString());
			isModified = 1;			
    		operationResultSet = new StatusCont(isModified, statusString, messageContList);		
    		finalList.add(
    						new UserResponse(user.getUid(), 
    											"OK", "OK", Arrays.asList(new MessageCont(
    															"password update", "SUCCESS", null))
    						)
    					);
		} catch (Exception intException) {
			isModified = 0;	
			finalList.add(new UserResponse(user.getUid(), 
					"FAIL", "FAIL", 
					Arrays.asList(new MessageCont(null, "FAIL", intException.getMessage()))));	
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
	
	
	public int addMemberToGroup(List<String> groupList, String uid) {
		int isAddedSuccessfully=1;
		
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
					isAddedSuccessfully=0;
					LOG.error("Failure adding " + uid + " to the group: " + groupList.get(i).toString());
				}
			}
			
			// LOG.info("Account uid= " + uid + " added to the group(s): " + groupList.toString());
		}
		catch(Exception e){
			isAddedSuccessfully=0;
			LOG.error(e.getMessage());
		}
		return isAddedSuccessfully;
	}
	
	public List<MessageCont> addMemberToGroupAndGetStatus(List<String> groupList, String uid) throws Exception {
		// int isAddedSuccessfully=0;
		List<MessageCont> messageContList = new ArrayList<>();		
		List<Map<String,String>> userList = searchUid(uid);
		String givenName = userList.get(0).get("givenName").trim();
		String sn = userList.get(0).get("sn").trim() ;
		try{
			for(int i=0;i<groupList.size();i++){
				MessageCont messageCont;
				try {
					Name groupDn = buildGroupDn(groupList.get(i));
					DirContextOperations ctx =   ldapTemplate.lookupContext(groupDn);
					ctx.addAttributeValue("member",buildPersonDn(uid,givenName,sn).toString() 
											+ ","+ env.getRequiredProperty("ldap.partitionSuffix"));
					ldapTemplate.modifyAttributes(ctx);
					
					messageCont = new MessageCont(groupList.get(i), "SUCCESS" ,"Success");
				} catch (Exception e) {
//					LOG.error("Failure adding " + uid + " to the group: " 
//								+ groupList.get(i).toString() + " " + e.getMessage());
					LOG.error("Failure adding " + uid + " to the group: " 
							+ groupList.get(i).toString() + " Cause: " + e.getCause().getLocalizedMessage());					
					if (e.getCause().getMessage()
							.lastIndexOf("No Such Object", e.getCause().getMessage().length()) > 0) {
						String msgString = "Failure Adding a Member to a Group";
						try {
							msgString = e.getCause().getMessage().replaceAll("\\[", "").replaceAll("\\]","");
						} catch (Exception intExc) {
							LOG.error("Failure adding ldap account to the group: " + intExc.getMessage());
							msgString = "Failure Adding a Member to a Group";
						}						
						messageCont = new MessageCont(groupList.get(i), "FAIL", msgString);
					} else {
						messageCont = new MessageCont(groupList.get(i), "FAIL", "Failure Adding a Member to a Group");
					}
				}
				messageContList.add(messageCont);
			}
						
			//LOG.info("Account uid= " + uid + " added to the group(s): " + groupList.toString());
		}
		catch(Exception e){
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
		String salt, hash;
		try {
			random.nextBytes(saltBytes);
			String s = Base64.getEncoder().encodeToString(saltBytes); //new String(saltBytes, StandardCharsets.UTF_8);
			salt = "$6$" + s;
			hash = Crypt.crypt(passwordToHash.getBytes(),salt);
		} catch (Exception e) {
			LOG.error("Salt generation error! " + e.getMessage());
			salt = "$6$" + "salt=" + saltStr.toString();
			hash = Crypt.crypt(passwordToHash.getBytes(),salt);
		}
		
		if (hash == null || hash == "") {
			hash = digestSHA(passwordToHash);
			LOG.warn("Password generation failed for a user account! automatically changed to {SHA-512}" );
			return hash;
		}
		
		//String hash = Crypt.crypt(passwordToHash.getBytes(),salt);

//		LOG.info("Password generated based on: passwordString=" + passwordToHash.toString() + " salt=" +salt.toString());
//		LOG.info("Password result hash= " +  "{SSHA-512}" + hash);
		return "{CRYPT}" + hash;
	}
	
	
	// Method to generate a random alphanumeric password of a specific length
    public static String generateRandomPassword(int len)    {
        // ASCII range – alphanumeric (0-9, a-z, A-Z)
        final String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
 
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder();
 
        // each iteration of the loop randomly chooses a character from the given
        // ASCII range and appends it to the `StringBuilder` instance
 
        for (int i = 0; i < len; i++)
        {
            int randomIndex = random.nextInt(chars.length());
            sb.append(chars.charAt(randomIndex));
        }
 
        return sb.toString();
    }
    
    public Map<String, String> buildObjectCNString (User user) {
    	// dedicated procedure to create a new CN for an object
    	int lastNumberInt;
    	if (globalCount < 100) {
    		globalCount +=1; //incrementing global count
    		/*
    		LOG.info("===>>> New Iteration for: uid= [" 
    				+ user.getUid() + "] with globalCount = " 
    				+ globalCount + " <<<=====" );
    		*/
    	} else {
    		return null;
    	}
    	boolean isFullDuplicates = false;
    	String newUserName = null, lastNumber = null;
    	List<String> digitsFound = new ArrayList<>();
    	Map<String, String> resultMap = new HashMap<>();        	
    	
    	String regex = "\\d+";        
        Pattern pattern = Pattern.compile(regex); //Creating a pattern object        
        Matcher matcher = pattern.matcher(user.getCn()); //Creating a Matcher object       
        
        while(matcher.find()) {
           digitsFound.add(matcher.group());
        } 
    	
        if (digitsFound.size() > 0) {
        	//LOG.info("Digits in the given string (" + user.getCn() + ") are: " + digitsFound.toString());
        	
        	lastNumber = digitsFound.get(digitsFound.size() -1);
        	//LOG.info("lastNumber: " + lastNumber.toString());
        	final Pattern lastIntPattern = Pattern.compile("[^0-9]+([0-9]+)$");
        	String input = user.getCn();
        	matcher = lastIntPattern.matcher(input);
        	if (matcher.find()) {
        	    String someNumberStr = matcher.group(1);
        	    lastNumberInt = Integer.parseInt(someNumberStr);
        	    //LOG.info("--> lastNumberInt= " + lastNumberInt);
        	}
        	
        	int i = input.length();
        	if (Character.isDigit(input.charAt(i - 1))) {
        		// if the last character is a digit?
        		while (i > 0 && Character.isDigit(input.charAt(i - 1))) {
                    i--;
                }
        		input = input.substring(0, i);
                //LOG.info("After a WHILE here is input= " + input);
                
                long lastNumberAsNumber = Long.parseLong(lastNumber);
                //LOG.info("Extracted lastNumberAsNumber: " + lastNumberAsNumber);
                        
                // int posOfNumber = user.getCn().lastIndexOf(lastNumber);
                // LOG.info("posOfNumber [lastIndexOf(lastNumber)]: " + posOfNumber);
                // (s.replace(s.substring(s.lastIndexOf(".1"), s.length()), "_1"))
                // String s = user.getCn();
                newUserName = input  + (lastNumberAsNumber + 1);
                //newUserName = s.replace(s.substring(posOfNumber, s.length()), "")  + (lastNumberAsNumber + 1);
                //newUserName = s.replaceLast(lastNumber.toString(),"") + (lastNumberAsNumber + 1);
        	}            
            
        } else {
        	//LOG.info("No numbers found in CN: " + user.getCn() + " - so we use first number: " );
        	lastNumber = "0";        	     	
        	newUserName = user.getCn() + " " + (1);
        }        
        
        LOG.info("!!! Suggested userCN(newUserName): " + newUserName);
        user.setCn(newUserName);
        
        List<Map<String,String>> foundByCNList = searchPerson(newUserName);   
    	
		if (foundByCNList == null || foundByCNList.size() == 0) {
			//It seems there are no more duplicates, so we use suggested CN			
			isFullDuplicates = false;
			//isDuplicateCNs = false;    					
			LOG.info(">>(create ldap)>> Finally we resolve full duplicate, CN changed to: " + newUserName);    	
		} else {
			// there still a duplicated name!
			isFullDuplicates = true;
			//isDuplicateCNs = true;
			//statusString = "Account creation failed: full duplicate - same CN and UID";
			LOG.warn(">>(create ldap)>> Found again full duplicate with the same CN and UID: \n foundByCNList: " + foundByCNList.toString());
			buildObjectCNString(user); //call it again -recursion
		}    	
    	resultMap.put("newCN", user.getCn());
    	resultMap.put("message", "OK");    
    	return resultMap;
    }
    
    public String removeLast(String s, int n) {
        if (null != s && !s.isEmpty()) {
            s = s.substring(0, s.length()-n);
        }
        return s;
    }
    
    public List<String> getMembersOf(String userCN) {
    	String ouPeople = env.getRequiredProperty("ldap.usersOU"); 
    	String orgLocal = env.getRequiredProperty("ldap.orgLocal");
    	String partitionSuffix =  env.getRequiredProperty("ldap.partitionSuffix");
    	String ouGroups = env.getRequiredProperty("ldap.groupsOU"); 
    	String groupsENA = env.getRequiredProperty("ldap.groupsENAOu");
    	String groupsENAOuFullPath =  env.getRequiredProperty("ldap.groupsENAOuFullPath");
 
    	/*
    	 * #ldap.groupsOU=Groups
			#ldap.groupsENAOuFullPath=ou=ENA,ou=Groups,o=Local
			#ldap.groupsENAOu=ENA
    	*/
    	List<String> allGroups = null ;
    	try {
        	Name ldapAccountDN = null, baseSearchDN = null;			
			if (orgLocal != null && orgLocal != "") {
				// there is an Org-unit (o=local) presented in the ldap configuration
				ldapAccountDN = LdapNameBuilder
						.newInstance()
						.add("o", orgLocal)
						.add("ou", ouPeople)
						.add("cn", userCN)
						.build();
				baseSearchDN = (groupsENA !=null || groupsENA != "") ? LdapNameBuilder
						.newInstance()
						.add("o", orgLocal)
						.add("ou", ouGroups)
						.add("ou", groupsENA)
						.build() :
						 LdapNameBuilder
							.newInstance()
							.add("o", orgLocal)
							.add("ou", ouGroups)
							.build() ;
			} else {
				// there is only one OU=People in the LDAP path for a user OU
				ldapAccountDN = LdapNameBuilder
						.newInstance()
						.add("ou", ouPeople)
						.add("cn", userCN)
						.build();
				baseSearchDN = (groupsENA !=null && groupsENA != "") ? ( LdapNameBuilder
						.newInstance()						
						.add("ou", groupsENA)
						.add("ou", ouGroups)
						.build() ) :
						( LdapNameBuilder
							.newInstance()
							.add("ou", ouGroups)							
							.build() );
			}
			
			String distinguishedName = ldapAccountDN.toString() + "," +  partitionSuffix;
			String baseSearchDNStr = baseSearchDN.toString();
			LOG.info("==> value of distinguishedName (in <getMembersOf> )= " + distinguishedName);
			LOG.info("==> value of baseSearchDN (in <getMembersOf> )= " + baseSearchDNStr);
        	/*
        	 * This one recursively search for all (nested) group that this user belongs to
        	 * "member:1.2.840.113556.1.4.1941:" is a magic attribute, Reference: 
        	 * https://msdn.microsoft.com/en-us/library/aa746475(v=vs.85).aspx
        	 * However, this filter is usually slow in case your ad directory is large.
        	 */    		
   		
    		if (allGroups == null) {
//    			allGroups = ldapTemplate.search( 
//        				LdapQueryBuilder.query()
//        				.base(baseSearchDN.toString())
//        				.countLimit(1000)
//        				.searchScope(SearchScope.SUBTREE)
//        				.where("objectclass").is("group")
//        				.and("member").is(distinguishedName),
//        			    (AttributesMapper<String>) attributes -> attributes.get("cn").get().toString()
//        			);
    			
    	    	AndFilter groupFilter = new AndFilter();
    	    	//groupFilter.and(new EqualsFilter("objectclass","group"));
    	    	groupFilter.and(new EqualsFilter("member",distinguishedName));
    	    	
    	    	allGroups = ldapTemplate.search(LdapQueryBuilder.query()
    	    			.base(baseSearchDN)
    	    			.searchScope(SearchScope.SUBTREE)
    	    			.filter(groupFilter), 
    	    			(AttributesMapper<String>) attributes -> attributes.get("cn").get().toString());
    	    	
    	    	LOG.info("==> groupFilter  (in <getMembersOf> ): " + groupFilter.toString());
    		}
    		LOG.info("==> allGroups  (in <getMembersOf> ): " + allGroups);

    	} catch (Exception ex) {
    		LOG.error("ERROR getting user group membership list in <getMembersOf()>\n " + ex.getLocalizedMessage());
    	}
    	
    	
    	return allGroups;
    }
    
    public Map<String,String> getLdapBackup( ) {
		//	(String entryDN, LdapConnection connection) throws CursorException, IOException, LdapException
		/**
		 * Get child list
		 *
		 * @param entryDN The distinguished name of an Entry in the LDAP
		 * @param connection An initialized LDAP-Context
		 * @return All child's of an Entry
		 * @throws IOException
		 * @throws CursorException
		 * @throws LdapException
		 */
		/*
		EntryCursor cursor = connection.search( "ou=users, dc=example, dc=com", "(objectclass=*)", SearchScope.ONELEVEL, "*" );
		 while ( cursor.next() )
		 {
		   Entry entry = cursor.get();
		     //play with the entry
		 }
		 */
    	String ouPeople = env.getRequiredProperty("ldap.usersOU"); 
    	String orgLocal = env.getRequiredProperty("ldap.orgLocal");
    	String partSuffix = env.getRequiredProperty("ldap.partitionSuffix");
		Map<String,String> response = new HashMap<String, String>();

		try {

			LOG.info("### Running LDAP backup... App version: " + env.getRequiredProperty("info.build.version"));
			//response.put("version",  env.getRequiredProperty("info.build.version"));
			
			//EntryCursor cursor = connection.search( "ou=users, dc=example, dc=com", "(objectclass=*)", SearchScope.ONELEVEL, "*" );
//			 while ( cursor.next() )
//			 {
//			   Entry entry = cursor.get();
//			     //play with the entry
//			 }

			// EntryCursor cursor = connection.search(new Dn("o=partition"), "(ObjectClass=*)", SearchScope.SUBTREE, "*", "+");
			List<Attributes> cursor = ldapTemplate.search (
					LdapQueryBuilder.query().base("ou=" + ouPeople).attributes("*","+").where("objectclass").is("person"), 
					(AttributesMapper<Attributes>) attrs 
	    	          -> {
	    	        	   Attributes ss = new Attributes() ;   
		    	        	  for(NamingEnumeration<? extends Attribute> all = attrs.getAll(); all.hasMoreElements(); ) {
									try {
										Attribute atr = all.nextElement();																	
										ss.put(atr, all) ;
										LOG.info(atr.toString());
										} catch (Exception e) {
											LOG.error(e.getMessage());
										}
		    	        	  }			
		    	        	  return ss; 
		    	          }); 
			Charset charset = Charset.forName("UTF-8");
			Path filePath = Paths.get("src/main/resources", "backup.ldif");
			BufferedWriter writer = Files.newBufferedWriter(filePath, charset);
			String st = ""; 
			for (Attributes cursorItemMap : cursor ) {
				//String ss = LdapUtils.convertToLdif(cursorItemMap.toString());
				//String ss = LdifUtils.convertToLdif(cursorItemMap);
				LOG.info(cursorItemMap.toString());
			}
//			while (cursor.next()) { 
//				Entry entry = cursor.get();
//				String ss = LdapUtils.convertToLdif(entry);
//				st += ss + "\n";
//			}
			writer.write(st);
			writer.close();
		} catch (IOException e) {
	      LOG.error("With IOException when closing EntryCursor. " + e);
	    }
		return response;
	}
    
    public Name buildBaseDN(String ouName) {    	
    	String ouPeople = env.getRequiredProperty("ldap.usersOU"); 
    	String orgLocal = env.getRequiredProperty("ldap.orgLocal");
    	String partitionSuffix =  env.getRequiredProperty("ldap.partitionSuffix");
    	String ouGroups = env.getRequiredProperty("ldap.groupsOU"); 
    	String groupsENA = env.getRequiredProperty("ldap.groupsENAOu");
    	String groupsENAOuFullPath =  env.getRequiredProperty("ldap.groupsENAOuFullPath");
    	Name ldapAccountDN = null;	
    	Boolean isParams = false;
    	if (ouName != null && ouName != "") {
    		isParams = true;
    	}
    	try {
        	if (isParams) {
        		if (orgLocal != null && orgLocal != "") {
    				// there is an Org-unit (o=local) presented in the ldap configuration
    				ldapAccountDN = LdapNameBuilder
    						.newInstance()
    						.add("o", orgLocal)
    						.add("ou", ouPeople)
    						.add("ou", ouName.trim())
    						.build();				
    			} else {
    				// there is only one OU=People in the LDAP path for a user OU
    				ldapAccountDN = LdapNameBuilder
    						.newInstance()
    						.add("ou", ouPeople)
    						.add("ou", ouName.trim())
    						.build();
    			}
        	} else {
        		if (orgLocal != null && orgLocal != "") {
    				// there is an Org-unit (o=local) presented in the ldap configuration
    				ldapAccountDN = LdapNameBuilder
    						.newInstance()
    						.add("o", orgLocal)
    						.add("ou", ouPeople)
    						.build();				
    			} else {
    				// there is only one OU=People in the LDAP path for a user OU
    				ldapAccountDN = LdapNameBuilder
    						.newInstance()
    						.add("ou", ouPeople)
    						.build();
    			}	
        	}
					
			String distinguishedName = ldapAccountDN.toString() + "," +  partitionSuffix;
			//LOG.info("==> value of distinguishedName (in <getMembersOf> )= " + distinguishedName);
    		
    	} catch (Exception allExc) {
    		LOG.error("=> Error while getting baseDN! " + allExc.getLocalizedMessage() );
    	}
		return ldapAccountDN;
    	
    }
    
    
    
    public SearchResponse getObjectAttrs(javax.naming.directory.Attributes  attrs) 	 {
    Map<String,String> ss = new HashMap<>(); 	
 	List<GroupMessageCont> messageContList = new ArrayList<>();
 	List<GroupMessageCont> messageContListMember = new ArrayList<>();
 	SearchResponse searchResponse = null;
 	String cn = null, uid = null;
 	Boolean isPerson = false;
 	try {
 		cn = attrs.get("cn").get().toString();
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
 				} else {
 					ss.put(atr.getID(), atr.get().toString());
 				}
 				String ojectTypeAttr = "OBJECTCLASS";
 				if (tmpAttrName.equals(ojectTypeAttr)) {
 					ArrayList<?> objectClass = Collections.list(attrs.get("objectClass").getAll());											
 					for (Object objectItem : objectClass) {												
 						List<String> objectItemList = Arrays.asList((objectItem.toString()).split(","));
 						LOG.info("Object type = " + objectItemList);
 						objectItem = objectItem.toString().toUpperCase();
 						if (objectItem.equals("ORGANIZATIONALPERSON") 
 	 							|| objectItem.equals("AUTHORIZABLEPERSON") 
 	 							|| objectItem.equals("INETORGPERSON")
 	 							|| objectItem.equals("PERSON")){
 	 						uid = attrs.get("uid").get().toString();
 	 						isPerson = true;
 	 					} else if (objectItem.equals("GROUP")) {
 	 						uid = null; //"!GROUP!";
 	 					}
 					}
 				}
 				 				
 			} catch (Exception ex) {
 				LOG.error("Error at <getObjectAttrs>: " + ex.getLocalizedMessage());
 			}
 		}
 		
 		LOG.info("=> (<getObjectAttrs>) Attributes found for cn= " + cn.toString() + ":: (ss): \n" + ss.toString());	    	        	  
 		List<String> tmpObjMap = null;
 		tmpObjMap = getMembersOf(cn);
 		if (tmpObjMap != null && tmpObjMap.size() != 0) {
 			for (String member : tmpObjMap) {
 				String grpDN = buildGroupDn(member).toString();
 				messageContListMember.add(new GroupMessageCont(member,grpDN));
 			}
 		} 
 		//uid = attrs.get("uid").get().toString();
 		searchResponse = new SearchResponse(uid, ss, messageContList, messageContListMember ); 		
	  } catch (Exception ex) {
		  LOG.error("=> (<getObjectAttrs>) Error getting Attributes details \n" + ex.getMessage());
		  searchResponse = new SearchResponse(null, ss, messageContList, messageContListMember );
	  }   	        	
	  return searchResponse; 
    }
    
    public BasicAttributes searchUser(String userName) throws javax.naming.NamingException {
    	String partSuffix = env.getRequiredProperty("ldap.partitionSuffix");
    	
        NamingEnumeration results = null;
        SearchResult searchResult;
        BasicAttributes attributes = null;
        SearchControls controls = new SearchControls();
        controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        AndFilter filter = new AndFilter();
        filter.and(new EqualsFilter("objectclass", "person"));
        Pattern patternProxy = Pattern.compile("cn=(.*?),(ou=.*)");
        Matcher matcher = patternProxy.matcher(userName);
        String baseDn = partSuffix;
        String userBaseDn = "cn=" + userName + "," + buildBaseDN("").toString() + ","+ partSuffix; // baseDn;
        String dn  = buildBaseDN("").toString() + ","+ partSuffix;
        DirContextAdapter context = new DirContextAdapter(dn);
//        LOG.info("userBaseDn DN string = " + userBaseDn);
//        LOG.info("context string = " + context.toString());
//        LOG.info("patternProxy string = " + patternProxy.toString());
//        LOG.info("matcher string = " + matcher.toString());       
        if (matcher.find()) {
            LOG.info(" -- Match 1 : " + matcher.group(1) + " ---- ");
            LOG.info(" -- Match 2 : " + matcher.group(2) + " ---- ");
            filter.and(new EqualsFilter("cn", matcher.group(1)));
            userBaseDn = matcher.group(2);
        }
        LOG.info(" -- Searching for: " + filter.encode() + " ---- ");
        //String searchFilter = "(dn=" + userName + ")";
        
        try {
            results = context.search(userBaseDn, filter.encode(), controls);
            if (results.hasMore()) {
            	LOG.info(" -- Found user :" + userName + " ---- ");
                searchResult = (SearchResult) results.nextElement();
                attributes = (BasicAttributes) searchResult.getAttributes();
                LOG.info(" ---> attributes:" + attributes.toString());
            } else {
            	LOG.info(" ---- Object: " + userName + " not found in target Ldap.");
            }
        } catch (NamingException ne) {
        	LOG.error(" Error has occured when searching the LDAP. " + ne.getMessage());
        } finally {
            if (results != null) {
                try {
                    results.close();
                } catch (NamingException e) {
                	LOG.info(" Error has occured when closing search results. " + e.getMessage());
                }
            }
        }
        return attributes;
    }
    

}


// =================================  END of CLASS: LdapClient ==============================================
