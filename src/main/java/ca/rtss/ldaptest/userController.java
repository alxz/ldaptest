package ca.rtss.ldaptest;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jackson.JsonObjectSerializer;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
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

	@GetMapping("/v1")
	public String getAllUsers()throws JsonProcessingException {
		String json = new ObjectMapper().writeValueAsString(ldapClient.searchAll());
		System.out.println(json);
		return json;
	}
	
	@GetMapping("/v1/search")
	public String userSearch(@RequestParam(value = "name", defaultValue = "admin") String name) throws JsonProcessingException {
		String json = new ObjectMapper().writeValueAsString(ldapClient.searchPerson(name));
		System.out.println(json);
		return json;
	}
	
	@GetMapping("/v1/searchuid")
	public ResponseEntity<String> serachUid(@RequestParam(value = "uid", defaultValue = "admin") String uid) throws JsonProcessingException {
		String json = new ObjectMapper().writeValueAsString(ldapClient.searchUid(uid));
		//System.out.println(json);
		if (json.isEmpty()) {
			System.out.println("\nEmpty result! " + json);
			return new ResponseEntity<>(null, HttpStatus.NOT_FOUND); 			
		}
		return new ResponseEntity<>(json, HttpStatus.OK); 
	}	
	
	
	@GetMapping("/v1/searchuser")
	public ResponseEntity<String> searchUIDOnly(@RequestParam(value = "uid", defaultValue = "admin") String uid) throws JsonProcessingException {
		ObjectMapper objectMapper = new ObjectMapper();
				
		String json = new ObjectMapper().writeValueAsString(ldapClient.searchUid(uid));
		
//		String resultString = (ldapClient.searchUid(uid)).get(0).toString();
//		System.out.println("Result string: " + resultString);
		
		try {			
			User[] user = objectMapper.readValue(json, User[].class);
			System.out.println("cn = " + user[0].getCn());
		} catch (IOException  e) {
			e.printStackTrace();
			return new ResponseEntity<>(null, HttpStatus.NOT_FOUND); 
		}
		
		if (json.isEmpty()) {
			System.out.println("\nEmpty result! " + json);
			return new ResponseEntity<>(null, HttpStatus.NOT_FOUND); 			
		}
		return new ResponseEntity<>(json, HttpStatus.OK); 
	}	
	
	@GetMapping("/v1/greet")
	public String showGreetings(@RequestParam(value = "name", defaultValue = "admin") String name) {
		String greets = "Hello, " + name;
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
	
	@PostMapping("/v1/create")
	public ResponseEntity<String> createUser( 
								@RequestParam(value = "givenname", defaultValue = "FirstName") String givenName,
								@RequestParam(value = "sn", defaultValue = "sn") String sn,
								@RequestParam(value = "password", defaultValue = "admin") String password,
								@RequestParam(value = "uid", defaultValue = "uid") String uid,
								@RequestParam(value = "mail", defaultValue = "mail") String mail,
								@RequestParam(value = "description", defaultValue = "description") String description,
								@RequestParam(value = "businessCategory", defaultValue = "code") String businessCategory,
								@RequestParam(value = "employeeType", defaultValue = "0") String employeeType,
								@RequestParam(value = "employeeNumber", defaultValue = "1") String employeeNumber,
								@RequestParam(value = "departmentNumber", defaultValue = "1") String departmentNumber
							) {
		try {
			ldapClient.create(givenName, sn, password, uid, mail, 
					businessCategory, employeeType, employeeNumber, departmentNumber);
	
		} catch (Exception e) {
			return  new ResponseEntity<>("Error creating object: \n" + e.getMessage(), HttpStatus.BAD_REQUEST);
		}		
		return  new ResponseEntity<>("All OK", HttpStatus.OK);
		//final String username, final String givenName,final String sn,final String password,final String uid,final String mail
		//@RequestParam(value = "username", defaultValue = "username") String username,
	}	
	
	
	@PostMapping("/v1/modify")
	public ResponseEntity<String> modifyUser( 
								@RequestParam(value = "givenname", defaultValue = "given") String givenName,
								@RequestParam(value = "sn", defaultValue = "sn") String sn,
								@RequestParam(value = "password", defaultValue = "admin") String password,
								@RequestParam(value = "uid", defaultValue = "uid") String uid,
								@RequestParam(value = "mail", defaultValue = "mail") String mail,
								@RequestParam(value = "description", defaultValue = "description") String description,
								@RequestParam(value = "businessCategory", defaultValue = "code") String businessCategory,
								@RequestParam(value = "employeeType", defaultValue = "0") String employeeType,
								@RequestParam(value = "employeeNumber", defaultValue = "1") String employeeNumber,
								@RequestParam(value = "departmentNumber", defaultValue = "1") String departmentNumber
							) {
		try {
			ldapClient.modifyUser(givenName, sn, password, uid, mail, 
					businessCategory, employeeType, employeeNumber, departmentNumber);
	
		} catch (Exception e) {
			return  new ResponseEntity<>("Error modifying an object: \n" + e.getMessage(), HttpStatus.BAD_REQUEST);
		}		
		return  new ResponseEntity<>("All OK", HttpStatus.OK);
		//final String username, final String givenName,final String sn,final String password,final String uid,final String mail
		//@RequestParam(value = "username", defaultValue = "username") String username,
	}	
	
//	@PostMapping("/v1/add-user")
//    public ResponseEntity<String> bindLdapPerson(@RequestBody User user) {
//        String result = ldapClient.create(user);
//        return new ResponseEntity<>(result, HttpStatus.OK);
//    }	
	
}
