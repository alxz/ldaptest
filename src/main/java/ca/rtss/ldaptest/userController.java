package ca.rtss.ldaptest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jackson.JsonObjectSerializer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import ca.rtss.ldaptest.ldap.client.LdapClient;
import ca.rtss.ldaptest.ldap.data.repository.User;
import ch.qos.logback.core.net.server.Client;

@CrossOrigin("*")
@RestController
@RequestMapping("/api")
public class userController {
//	-----------------------------------------------------------------------
	@Autowired
	private LdapClient ldapClient;
	private static final Logger LOG = LoggerFactory.getLogger(userController.class);
//	-----------------------------------------------------------------------	

// <<< =================  SEARCH CONTROLLERS START  =================== >>>
	@GetMapping("/v1/search")
	public ResponseEntity<String> userSearch(@RequestParam(value = "name", defaultValue = "Stranger") String name) throws JsonProcessingException {
		String json = new ObjectMapper().writeValueAsString(ldapClient.searchPerson(name));
		if (json.isEmpty()) {				
			return new ResponseEntity<>("{ \"error\": \" Not Found \" }", HttpStatus.NOT_FOUND); 			
		}		
		return new ResponseEntity<>(json, HttpStatus.OK);
	}	


	@GetMapping("/v2/search")
	public ResponseEntity<String> userSearchByCNSNGiven
			(@RequestParam(value = "uid",  required = false) String uid,
					@RequestParam(value = "name",  required = false) String name,
					@RequestParam(value = "mail",  required = false) String mail) 
			throws JsonProcessingException {
		String json = "[]";	
		if ((uid != null && uid.trim().toString().equals("*")) 
				&& (name != null && name.trim().toString().equals("*")) 
				&& (mail != null && mail.trim().toString().equals("*"))) 
		{
			return new ResponseEntity<>( "{ \"error\": {\"message\": \" This kind of wide search is not allowed here! \",\"content\" :"  + json + " }}", HttpStatus.NOT_FOUND);
		} 
		if ( uid != null && name == null && mail == null ) {
			// only uid provided:
			json = new ObjectMapper().writeValueAsString(ldapClient.searchUid(uid));
		} else if ( uid != null && name != null && mail == null ) {
			// only 2 params: uid and name provided:
			json = new ObjectMapper().writeValueAsString(ldapClient.searchUid(uid,name));
		} else if ( uid != null && name == null && mail != null) {
			//all 3 params provided uid with name and email provided:
			json = new ObjectMapper().writeValueAsString(ldapClient.searchUid(uid, name, mail));
		} else if ( uid != null && name == null && mail != null) {
			//all 3 params provided uid with name and email provided:
			json = new ObjectMapper().writeValueAsString(ldapClient.searchPersonMultiParams(uid, null, mail));
		} else if ( name != null && mail != null) {
			// name and email provided but no uid:
			json = new ObjectMapper().writeValueAsString(ldapClient.searchPersonMultiParams(name, mail));
		} else if ( name != null && mail == null) {
			// name only provided but no uid and no email:
			json = new ObjectMapper().writeValueAsString(ldapClient.searchPersonMultiParams(name));
		} else if ( name == null && mail != null) {	
			// mail only provided but no uid or name:
			json = new ObjectMapper().writeValueAsString(ldapClient.searchMail(mail));
		} else {
			json = "[]";
		}
		
		if (json == null || json.isEmpty()) {			
			//return new ResponseEntity<>( json, HttpStatus.NOT_FOUND); 
			return new ResponseEntity<>( "{ \"error\": {\"message\": \" Not Found \",\"content\" :"  + json + " }}", HttpStatus.NOT_FOUND); 
		}		
		return new ResponseEntity<>(json, HttpStatus.OK);
		//return new ResponseEntity<>( "{ \"data\": " + json + " }", HttpStatus.OK);
	}	
	
	@GetMapping("/v3/search")
	// search return the data with group membership?
	public ResponseEntity<String> userSearchV3
			(@RequestParam(value = "searchstring") String query) 
			throws JsonProcessingException {
		String json = null;	
		// we will use: searchUserWithQuery(String)
		if (query.trim().toString().equals("*")) {
			return new ResponseEntity<>( "{ \"error\": {\"message\": \" This kind of wide search is not allowed here! \",\"content\" :"  + json + " }}", HttpStatus.NOT_FOUND);
		}
		json = new ObjectMapper().writeValueAsString(ldapClient.searchUserWithQuery(query.trim().toString(),"memberOf"));
				
		if (json == null || json.isEmpty()) {			
			return new ResponseEntity<>( "{ \"error\": {\"message\": \" Not Found \",\"content\" :"  + json + " }}", HttpStatus.NOT_FOUND); 			
		}		
		return new ResponseEntity<>( "{ \"data\": " + json + " }", HttpStatus.OK);
	}		

	@GetMapping("/v3/searchgetall")
	// search return the data with group membership and all attributes including 'operational attributes'?
	public ResponseEntity<String> userSearchGetAllV3
			(@RequestParam(value = "searchstring") String query) 
			throws JsonProcessingException {
		String json = null;	
		// we will use: searchUserWithQuery(String)
		if (query.trim().toString().equals("*")) {
			return new ResponseEntity<>( "{ \"error\": {\"message\": \" This kind of wide search is not allowed here! \",\"content\" :"  + json + " }}", HttpStatus.NOT_FOUND);
		}
		json = new ObjectMapper().writeValueAsString(ldapClient.searchUserWithQuery(query.trim().toString(), "+"));		
		if (json == null || json.isEmpty()) {			
			return new ResponseEntity<>( "{ \"error\": {\"message\": \" Not Found \",\"content\" :"  + json + " }}", HttpStatus.NOT_FOUND); 			
		}		
		return new ResponseEntity<>( "{ \"data\": " + json + " }", HttpStatus.OK);
	}	
	
	@GetMapping("/v1/searchuid")
	public ResponseEntity<String> searchUid(@RequestParam(value = "uid", defaultValue = "name") String uid) throws JsonProcessingException {
		 String json = new ObjectMapper().writeValueAsString(ldapClient.searchUid(uid));
		if (json.isEmpty()) {
//			System.out.println("\nEmpty result! " + json);
			return new ResponseEntity<>("{ \"message\": \" Not Found \" }", HttpStatus.NOT_FOUND); 			
		}		
		return new ResponseEntity<>(json, HttpStatus.OK); 
	}	
	
	@GetMapping("/v1/searchmail")
	public ResponseEntity<String> searcMail(@RequestParam(value = "mail", defaultValue = "name") String mail) throws JsonProcessingException {
		 String json = new ObjectMapper().writeValueAsString(ldapClient.searchMail(mail));
		if (json.isEmpty()) {
			return new ResponseEntity<>("{ \"message\": \" Not Found \" }", HttpStatus.NOT_FOUND); 			
		}		
		return new ResponseEntity<>(json, HttpStatus.OK); 
	}		

// <<< =================  SEARCH CONTROLLERS END  =================== >>>
//	-----------------------------------------------------------------------	
	
	@GetMapping("/v1/greet")
	public ResponseEntity<String> showGreetings(@RequestParam(value = "name", defaultValue = "Stranger") String name) {
		
		String greets = ldapClient.greetings(name); //"This is the test message: Hello, " + name;
		//return greets;
		return  new ResponseEntity<>("{ \"data\": \"" + greets + "\" }", HttpStatus.OK);
	}
	
//	-----------------------------------------------------------------------
//	<<< =================  LOGIN CONTROLLERS START  =================== >>>
	
	@GetMapping("/v1/login")
	public void login(@RequestParam(value = "name", defaultValue = "ldapadm") String name, @RequestParam(value = "password", defaultValue = "admin") String password) {
		ldapClient.authenticate(name, password);
	}

	@GetMapping("/v1/loginuid")
	public void loginUID(@RequestParam(value = "uid", defaultValue = "ldapadm") String uid, @RequestParam(value = "password", defaultValue = "admin") String password) {
		ldapClient.authenticateUID(uid, password);
	}	
//	<<< =================  LOGIN CONTROLLERS END  =================== >>>	
//	-----------------------------------------------------------------------
//	<<< =================  CREATE CONTROLLERS START  =================== >>>
	
	@PostMapping(value="/v1/create", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE}, 
									 produces = "application/json")
	public ResponseEntity<String> createUser( 
			@RequestBody User user
//								@RequestParam(value = "givenname", defaultValue = "FirstName") String givenName,
//								@RequestParam(value = "sn", defaultValue = "sn") String sn,
//								@RequestParam(value = "password", defaultValue = "admin") String password,
//								@RequestParam(value = "uid", defaultValue = "uid") String uid,
//								@RequestParam(value = "mail", defaultValue = "mail") String mail,
//								@RequestParam(value = "description", defaultValue = "description") String description,
//								@RequestParam(value = "businessCategory", defaultValue = "code") String businessCategory,
//								@RequestParam(value = "employeeType", defaultValue = "0") String employeeType,
//								@RequestParam(value = "employeeNumber", defaultValue = "1") String employeeNumber,
//								@RequestParam(value = "departmentNumber", defaultValue = "1") String departmentNumber
							) {
		try {
			ldapClient.create(user.getCn(), user.getUsername(), user.getGivenName(), user.getSn(), user.getPassword(), user.getUid(), user.getMail(), 
					user.getBusinessCategory(), user.getEmployeeType(), user.getEmployeeNumber(), user.getDepartmentNumber(), user.getGroupMember());

		} catch (Exception e) {
			LOG.info("Failed account creation! ");
			return  new ResponseEntity<>("{ \"message\": \" " + e.getMessage() + " \" }", HttpStatus.BAD_REQUEST);
		}		
		return  new ResponseEntity<>("{ \"message\": \"All OK\" }", HttpStatus.OK);
	}	
	
	
	@PostMapping(value="/v2/create", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE}, 
			produces = "application/json")
	public ResponseEntity<String> createUserV2( @RequestBody User user	) {
		try {
			ldapClient.createUserWithGroupMember(user.getCn(), user.getUsername(), user.getGivenName(), user.getSn(), user.getPassword(), user.getUid(), user.getMail(), 
					user.getBusinessCategory(), user.getEmployeeType(), user.getEmployeeNumber(), user.getDepartmentNumber(), user.getGroupMember());
		} catch (Exception e) {
			LOG.error("Failed account creation! " + e.getMessage());
			return  new ResponseEntity<>( "{ \"error\": "
											+ "{ \"message\": \"error creating account \"," 
											+ " \"content\" : \"" + e.getMessage() 
											+ " \"} }", 
											HttpStatus.BAD_REQUEST);

		}		
		return  new ResponseEntity<>("{ \"data\": " + 
												"{ \"message\": \"successfully created\"," +
													"\"uid\" : \"" + user.getUid() + "\"," +
													"\"account cn\" : \"" + user.getGivenName() + " " + user.getSn() + 
												"\"} }",
									HttpStatus.OK);
	}		

	@PostMapping(value="/v2/createusers", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE}, 
			produces = "application/json")
	public ResponseEntity<String> createUsersV2(@RequestBody User[] users) throws JsonProcessingException{
		Map<String,String> usersList = new HashMap<>();
		try {
			for(User user : users){		
				try {
					System.out.println("UserID: " + user.getUid());	
					ldapClient.createUserWithGroupMember(user.getCn(), user.getUsername(), user.getGivenName(), user.getSn(), user.getPassword(), user.getUid(), user.getMail(), 
						user.getBusinessCategory(), user.getEmployeeType(), user.getEmployeeNumber(), user.getDepartmentNumber(), user.getGroupMember());
					usersList.put(user.getUid(),"OK");
				} catch (Exception intException) {
					// System.out.println("Error: " + intException.getMessage());
					usersList.put(user.getUid(),"FAIL - " + intException.getMessage());
				}				
			}
			//System.out.println("usersList is: " + usersList.toString());	
		} catch (Exception e) {
			LOG.error("Failed account creation! ");
			String json = new ObjectMapper().writeValueAsString(usersList);
			return  new ResponseEntity<>(json, HttpStatus.BAD_REQUEST);
		}
		String json = new ObjectMapper().writeValueAsString(usersList);
		return  new ResponseEntity<>(json, HttpStatus.OK);
	}	

	@PostMapping(value="/v3/createusers", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE}, 
			produces = "application/json")
	public ResponseEntity<String> createUsersV3(@RequestBody User[] users) throws JsonProcessingException{
		//Map<String, String> usersList = null;
		List<Map<String, String>> usersList = null;
		try {
			usersList = ldapClient.createUsers(users);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			String json = new ObjectMapper().writeValueAsString(usersList);
			return  new ResponseEntity<>(json, HttpStatus.BAD_REQUEST);
		}
		String json = new ObjectMapper().writeValueAsString(usersList);
		return new ResponseEntity<>(json, HttpStatus.OK);

	}	
	
//	<<< =================  CREATE CONTROLLERS END  =================== >>>
//	-----------------------------------------------------------------------	
//	<<< =================  MODIFY CONTROLLERS START  =================== >>>	
	

	@PostMapping(value="/v1/modify", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE}, 
									produces = "application/json") //consumes = "application/json"
	public ResponseEntity<String> modifyUser(@RequestBody User user) {
		try {
			ldapClient.modifyUser(user.getCn(), user.getUsername(), user.getGivenName(), user.getSn(), user.getPassword(), user.getUid(), user.getMail(), 
					user.getBusinessCategory(), user.getEmployeeType(), user.getEmployeeNumber(), user.getDepartmentNumber(), user.getGroupMember());
	
		} catch (Exception e) {
			return  new ResponseEntity<>("{ \"message\": \" " + e.getMessage() + " \" }", HttpStatus.BAD_REQUEST);
		}		
		return  new ResponseEntity<>("{ \"message\": \"Post: All OK\" }", HttpStatus.OK);
	}	

	@PutMapping(value = "/v2/modify", consumes = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE,
			MediaType.MULTIPART_FORM_DATA_VALUE }, produces = "application/json") // consumes = "application/json"
	public ResponseEntity<String> modifyUserAll(@RequestBody User user) {
		try {
			ldapClient.modifyUser(user.getCn(), user.getUsername(), user.getGivenName(), user.getSn(),
					user.getPassword(), user.getUid(), user.getMail(), user.getBusinessCategory(),
					user.getEmployeeType(), user.getEmployeeNumber(), user.getDepartmentNumber(),  user.getGroupMember());

		} catch (Exception e) {
			// return new ResponseEntity<>("{ \"message\": \" " + e.getMessage() + " \" }", HttpStatus.BAD_REQUEST);
			return  new ResponseEntity<>( "{ \"error\": "
					+ "{ \"message\": \"error modifying account \"," 
					+ " \"content\" : \"" + e.getMessage() 
					+ " \"} }", 
					HttpStatus.BAD_REQUEST);				
			
		}
		// return new ResponseEntity<>("{ \"message\": \"Put: All OK\" }", HttpStatus.OK);
		return  new ResponseEntity<>("{ \"data\": " + 
										"{ \"message\": \"successfully modified\"," +
											"\"uid\" : \"" + user.getUid() + "\"," +
											"\"account cn\" : \"" + user.getGivenName() + " " + user.getSn() + 
										"\"} }",
										HttpStatus.OK);
		
	}
	
	@PatchMapping(value = "/v2/modifyname", consumes = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE,
			MediaType.MULTIPART_FORM_DATA_VALUE }, produces = "application/json") // consumes = "application/json"
	public ResponseEntity<String> modifyUserPATCHName(@RequestBody User user) {
		try {
			ldapClient.modifyUserName(user.getGivenName(), user.getSn(), user.getUid());

		} catch (Exception e) {
			return new ResponseEntity<>("{ \"message\": \" " + e.getMessage() + " \" }", HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<>("{ \"message\": \"PatchName: All OK\" }", HttpStatus.OK);
	}

	@PatchMapping(value = "/v2/modifyemail", consumes = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE,
			MediaType.MULTIPART_FORM_DATA_VALUE }, produces = "application/json") // consumes = "application/json"
	public ResponseEntity<String> modifyUserPATCHEmail(@RequestBody User user) {
		try {
			ldapClient.modifyUserEmail(user.getUid(), user.getMail());

		} catch (Exception e) {
			return new ResponseEntity<>("{ \"message\": \" " + e.getMessage() + " \" }", HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<>("{ \"message\": \"PatchName: All OK\" }", HttpStatus.OK);
	}
	
	@PatchMapping(value = "/v2/modifypassword", consumes = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE,
			MediaType.MULTIPART_FORM_DATA_VALUE }, produces = "application/json") // consumes = "application/json"
	public ResponseEntity<String> modifyUserPATCHPassword(@RequestBody User user) {
		boolean isPasswordUpdateSuccessfull = false;
		try {
			isPasswordUpdateSuccessfull = ldapClient.modifyUserPassword(user.getPassword(), user.getUid());
		} catch (Exception e) {
			//return new ResponseEntity<>("{ \"message\": \" " + e.getMessage() + " \" }", HttpStatus.BAD_REQUEST);
			isPasswordUpdateSuccessfull = false;
			return  new ResponseEntity<>( "{ \"error\": "
											+ "{ \"message\": \"error modifying a password\"," 
											+ " \"content\" : \"" + e.getMessage() 
											+ " \"} }", 
											HttpStatus.BAD_REQUEST);
		}
		//return new ResponseEntity<>("{ \"message\": \"PatchName: All OK\" }", HttpStatus.OK);
		return  new ResponseEntity<>("{ \"data\": " + 
										"{ \"message\": \"successfully modified\"," 
											+ "\"uid\" : \"" + user.getUid() 
											+ "\"} }",
										HttpStatus.OK);
	}

//	<<< =================  MODIFY CONTROLLERS END  =================== >>>
//	-----------------------------------------------------------------------	
//	<<< =================  DELETE CONTROLLERS START  =================== >>>	
	
	
	@PostMapping(value="/v1/delete", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE}, 
									 produces = "application/json")
	public ResponseEntity<String> delete( @RequestBody User user) {
		try {
			ldapClient.delete(user.getCn(), user.getUid());
	
		} catch (Exception e) {
			LOG.warn("Failed deleting account! ");
			//return  new ResponseEntity<>("{ \"message\": \" " + e.getMessage() + " \" }", HttpStatus.BAD_REQUEST);
			return  new ResponseEntity<>( "{ \"error\": "
											+ "{ \"message\": \"error deleting account \"," 
											+ " \"content\" : \"" + e.getMessage() 
											+ " \"} }", 
											HttpStatus.BAD_REQUEST);
		}		
		//return  new ResponseEntity<>("{ \"message\": \"All OK\" }", HttpStatus.OK);
		return  new ResponseEntity<>("{ \"data\": " + 
										"{ \"message\": \"successfully deleted\"," +
											"\"uid\" : \"" + user.getUid() + 
											"\"} }",
										HttpStatus.OK);
		
	}		
	
	//, headers = {"content-type=text/plain", "content-type=text/html"}
	//, consumes = "application/json", produces = "application/json"
	@DeleteMapping(value = "/v2/delete", produces = "application/json")
	public ResponseEntity<?> deleteUser	(@RequestParam(value = "cn") String cn, @RequestParam(value = "uid") String uid	)  {
//		System.out.println("Deleting: cn= " + cn + " and uid= " + uid);
		try {
			ldapClient.delete(cn, uid);

		} catch (Exception e) {
			LOG.error("Failed deleting account! ");
			//return new ResponseEntity<>("{ \"message\": \" " + e.getMessage() + " \" }", HttpStatus.BAD_REQUEST);
			return  new ResponseEntity<>( "{ \"error\": "
											+ "{ \"message\": \"error modifying account \"," 
											+ " \"content\" : \"" + e.getMessage() 
											+ " \"} }", 
											HttpStatus.BAD_REQUEST);
		}
		LOG.info("Account deleted! " + uid.toString());
		//return new ResponseEntity<>("{ \"message\": \"DELETE: All OK\" }", HttpStatus.OK);
		return  new ResponseEntity<>("{ \"data\": " + 
									"{ \"message\": \"successfully deleted\"," +
										"\"uid\" : \"" + uid.toString() + 
									"\"} }",
									HttpStatus.OK);

	}
	
//	<<< =================  DELETE CONTROLLERS END  =================== >>>
//	-----------------------------------------------------------------------	
//	<<< =================  REMOVE CONTROLLERS START  =================== >>>	
	
	@PatchMapping(value="/v1/removemember", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE}, 
			produces = "application/json")
	public ResponseEntity<String> removeMember( @RequestBody User user) {
		boolean operationStatus = false;
		try {
			operationStatus = ldapClient.removeMember(user.getUid(), user.getGroupMember());

		} catch (Exception e) {			
			LOG.error("Failed to remove member from the group ");
			return  new ResponseEntity<>( "{ \"error\": "
					+ "{ \"message\": \"Failed to remove member from the group\"," 
					+ " \"content\" : \"" + e.getMessage() 
					+ " \"} }", 
					HttpStatus.BAD_REQUEST);
		}		
		//return  new ResponseEntity<>("{ \"message\": \"All OK\" }", HttpStatus.OK);
		return  new ResponseEntity<>("{ \"data\": " + 
				"{ \"message\": \"successfully removed member from the group\"," +
				"\"uid\" : \"" + user.getUid() + "\", " +
				"\"operationStatus\" : \"" + operationStatus + "\"" + 
				"} }",
				HttpStatus.OK);

	}
	
//	<<< =================  REMOVE CONTROLLERS END  =================== >>>		
	
}


//@GetMapping("/v1")
//public String getAllUsers()throws JsonProcessingException {
//	String json = new ObjectMapper().writeValueAsString(ldapClient.searchAll());
//	System.out.println(json);
//	return json;
//}

//@GetMapping("/v1/searchuser")
//public ResponseEntity<String> searchUIDOnly(@RequestParam(value = "uid", defaultValue = "admin") String uid) throws JsonProcessingException {
//	ObjectMapper objectMapper = new ObjectMapper();				
//	String json = new ObjectMapper().writeValueAsString(ldapClient.searchUid(uid));		
//	
//	try {			
//		User[] user = objectMapper.readValue(json, User[].class);
//		System.out.println("cn = " + user[0].getCn());
//	} catch (IOException  e) {
//		e.printStackTrace();
//		return new ResponseEntity<>(null, HttpStatus.NOT_FOUND); 
//	}
//	
//	if (json.isEmpty()) {
//		System.out.println("\nEmpty result! " + json);
//		return new ResponseEntity<>(null, HttpStatus.NOT_FOUND); 			
//	}
//	return new ResponseEntity<>(json, HttpStatus.OK); 
//}	

//@PostMapping("/v1/add-user")
//public ResponseEntity<String> bindLdapPerson(@RequestBody User user) {
//  String result = ldapClient.create(user);
//  return new ResponseEntity<>(result, HttpStatus.OK);
//}	

