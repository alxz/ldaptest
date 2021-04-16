package ca.rtss.ldaptest;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import ca.rtss.ldaptest.ldap.client.LdapClient;
import ch.qos.logback.core.net.server.Client;

@RestController
public class userController {

	@Autowired
	private LdapClient ldapClient;

	@GetMapping("/api/v1/")
	public String getAllUsers()throws JsonProcessingException {
		String json = new ObjectMapper().writeValueAsString(ldapClient.searchAll());
		System.out.println(json);
		return json;
	}
	
	@GetMapping("/api/v1/search")
	public String userSearch(@RequestParam(value = "name", defaultValue = "admin") String name) throws JsonProcessingException {
		String json = new ObjectMapper().writeValueAsString(ldapClient.searchPerson(name));
		System.out.println(json);
		return json;
	}
	
	@GetMapping("/api/v1/greet")
	public String showGreetings(@RequestParam(value = "name", defaultValue = "admin") String name) {
		String greets = "Hello, " + name;
		return greets;
	}
	
	
	@GetMapping("/api/v1/login")
	public void login(@RequestParam(value = "name", defaultValue = "ldapadm") String name, @RequestParam(value = "password", defaultValue = "admin") String password) {
		ldapClient.authenticate(name, password);
	}

	@GetMapping("/api/v1/loginuid")
	public void loginUID(@RequestParam(value = "uid", defaultValue = "ldapadm") String uid, @RequestParam(value = "password", defaultValue = "admin") String password) {
		ldapClient.authenticateUID(uid, password);
	}	
}
