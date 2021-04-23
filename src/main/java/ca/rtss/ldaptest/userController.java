package ca.rtss.ldaptest;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jackson.JsonObjectSerializer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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

@RestController
@RequestMapping("/api")
public class userController {

	@Autowired
	private LdapClient ldapClient;
	private static final Logger LOG = LoggerFactory.getLogger(userController.class);
	
	@GetMapping("/v1/search")
	public String userSearch(@RequestParam(value = "name", defaultValue = "Stranger") String name) throws JsonProcessingException {
		String json = new ObjectMapper().writeValueAsString(ldapClient.searchPerson(name));
//		System.out.println(json);
		return json;
	}
	
	@GetMapping("/v1/searchuid")
	public ResponseEntity<String> serachUid(@RequestParam(value = "uid", defaultValue = "user0000") String uid) throws JsonProcessingException {
		 String json = new ObjectMapper().writeValueAsString(ldapClient.searchUid(uid));
		if (json.isEmpty()) {
//			System.out.println("\nEmpty result! " + json);
			return new ResponseEntity<>("{ \"message\": \" Not Found \" }", HttpStatus.NOT_FOUND); 			
		}		
		return new ResponseEntity<>(json, HttpStatus.OK); 
	}	
	
	
	@GetMapping("/v1/greet")
	public String showGreetings(@RequestParam(value = "name", defaultValue = "Stranger") String name) {
		String greets = "This is the test message: Hello, " + name;
		return greets;
	}
	
	
	@GetMapping("/v1/login")
	public void login(@RequestParam(value = "name", defaultValue = "ldapadm") String name, @RequestParam(value = "password", defaultValue = "admin") String password) {
		ldapClient.authenticate(name, password);
	}

	@GetMapping("/v1/loginuid")
	public void loginUID(@RequestParam(value = "uid", defaultValue = "ldapadm") String uid, @RequestParam(value = "password", defaultValue = "admin") String password) {
		ldapClient.authenticateUID(uid, password);
	}	
	
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
					user.getBusinessCategory(), user.getEmployeeType(), user.getEmployeeNumber(), user.getDepartmentNumber());
//			ldapClient.create(givenName, sn, password, uid, mail, 
//					businessCategory, employeeType, employeeNumber, departmentNumber);
//			LOG.info("Created account with: " + user.toString());
		} catch (Exception e) {
			LOG.info("Failed account creation! ");
			return  new ResponseEntity<>("{ \"message\": \" " + e.getMessage() + " \" }", HttpStatus.BAD_REQUEST);
		}		
		return  new ResponseEntity<>("{ \"message\": \"All OK\" }", HttpStatus.OK);
	}	
	

	@PostMapping(value="/v1/modify", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE}, 
									produces = "application/json") //consumes = "application/json"
	public ResponseEntity<String> modifyUser(@RequestBody User user) {
		try {
			ldapClient.modifyUser(user.getCn(), user.getUsername(), user.getGivenName(), user.getSn(), user.getPassword(), user.getUid(), user.getMail(), 
					user.getBusinessCategory(), user.getEmployeeType(), user.getEmployeeNumber(), user.getDepartmentNumber());
//			ldapClient.modifyUser(givenName, sn, password, uid, mail, 
//					businessCategory, employeeType, employeeNumber, departmentNumber);
	
		} catch (Exception e) {
			return  new ResponseEntity<>("{ \"message\": \" " + e.getMessage() + " \" }", HttpStatus.BAD_REQUEST);
		}		
		return  new ResponseEntity<>("{ \"message\": \"Post: All OK\" }", HttpStatus.OK);
		//final String username, final String givenName,final String sn,final String password,final String uid,final String mail
		//@RequestParam(value = "username", defaultValue = "username") String username,
	}	

	@PutMapping(value = "/v2/modify", consumes = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE,
			MediaType.MULTIPART_FORM_DATA_VALUE }, produces = "application/json") // consumes = "application/json"
	public ResponseEntity<String> modifyUserAll(@RequestBody User user) {
		try {
			ldapClient.modifyUser(user.getCn(), user.getUsername(), user.getGivenName(), user.getSn(),
					user.getPassword(), user.getUid(), user.getMail(), user.getBusinessCategory(),
					user.getEmployeeType(), user.getEmployeeNumber(), user.getDepartmentNumber());

		} catch (Exception e) {
			return new ResponseEntity<>("{ \"message\": \" " + e.getMessage() + " \" }", HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<>("{ \"message\": \"Put: All OK\" }", HttpStatus.OK);
		//final String username, final String givenName,final String sn,final String password,final String uid,final String mail
		//@RequestParam(value = "username", defaultValue = "username") String username,
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
		try {
			ldapClient.modifyUserPassword(user.getPassword(), user.getUid());

		} catch (Exception e) {
			return new ResponseEntity<>("{ \"message\": \" " + e.getMessage() + " \" }", HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<>("{ \"message\": \"PatchName: All OK\" }", HttpStatus.OK);
	}

	@PostMapping(value="/v1/delete", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE}, 
									 produces = "application/json")
	public ResponseEntity<String> delete( @RequestBody User user) {
		try {
			ldapClient.delete(user.getCn(), user.getUid());
	
		} catch (Exception e) {
			LOG.warn("Failed deleting account! ");
			return  new ResponseEntity<>("{ \"message\": \" " + e.getMessage() + " \" }", HttpStatus.BAD_REQUEST);
		}		
		return  new ResponseEntity<>("{ \"message\": \"All OK\" }", HttpStatus.OK);
		
	}		
	
	//, headers = {"content-type=text/plain", "content-type=text/html"}
	//, consumes = "application/json", produces = "application/json"
	@DeleteMapping(value = "/v2/delete", produces = "application/json")
	public ResponseEntity<?> deleteUser	(@RequestParam(value = "cn") String cn, @RequestParam(value = "uid") String uid	)  {
//		System.out.println("Deleting: cn= " + cn + " and uid= " + uid);
		try {
			ldapClient.delete(cn, uid);

		} catch (Exception e) {
			LOG.warn("Failed deleting account! ");
			return new ResponseEntity<>("{ \"message\": \" " + e.getMessage() + " \" }", HttpStatus.BAD_REQUEST);
		}
//		LOG.info("Account deleted! " + uid.toString());
		return new ResponseEntity<>("{ \"message\": \"DELETE: All OK\" }", HttpStatus.OK);

	}
	
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

