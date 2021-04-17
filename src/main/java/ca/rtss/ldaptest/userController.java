package ca.rtss.ldaptest;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.beans.factory.annotation.Autowired;
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
	public String serachUid(@RequestParam(value = "uid", defaultValue = "admin") String uid) throws JsonProcessingException {
		String json = new ObjectMapper().writeValueAsString(ldapClient.searchUid(uid));
		System.out.println(json);
		return json;
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
								@RequestParam(value = "givenname", defaultValue = "given") String givenname,
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
		ldapClient.create(givenname, sn, password, uid, mail, 
							businessCategory, employeeType, employeeNumber, departmentNumber);

		return  new ResponseEntity<>("redirect:/v1/searchuid", HttpStatus.OK);
		//final String username, final String givenname,final String sn,final String password,final String uid,final String mail
		//@RequestParam(value = "username", defaultValue = "username") String username,
	}	
	
//	@PostMapping("/v1/add-user")
//    public ResponseEntity<String> bindLdapPerson(@RequestBody User user) {
//        String result = ldapClient.create(user);
//        return new ResponseEntity<>(result, HttpStatus.OK);
//    }	
	
}
