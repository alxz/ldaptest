package ca.rtss.ldaptest.ldap.data.service;


import ca.rtss.ldaptest.ldap.data.repository.User;
import ca.rtss.ldaptest.ldap.data.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.support.LdapUtils;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public Boolean authenticate(final String username, final String password) {    	
    	User user = userRepository.findByUsernameAndPassword(username, password);    	        
        return user != null;
    }

    public Boolean authenticateUID(final String uid, final String password) {
        User user = userRepository.findByUsernameAndPassword(uid, password);       
        return user != null;
    }

    public List<String> search(final String username) {
        List<User> userList = userRepository.findByUsernameLikeIgnoreCase(username);
        if (userList == null) {
            return Collections.emptyList();
        }

        return userList.stream()
          .map(User::getUsername)
          .collect(Collectors.toList());
    }

    public List<String> findByUid(String uid) {
    	List<User> userList = userRepository.findByUid(uid);
        if (userList == null) {
            return Collections.emptyList();
        }
        return userList.stream()
                .map(User::getUid)
                .collect(Collectors.toList());
    }    
    
    public List<String> findAllUserObjects() {
        Iterable<User> userList = userRepository.findAll();
        if (userList == null) {
            return Collections.emptyList();
        }

        return ((Collection<User>) userList).stream()
          .map(User::getUsername)
          .collect(Collectors.toList());
    }
    
    public void create(final String cn, final String username, 
    					final String givenName, final String sn,
    					final String password, final String uid, final String mail, final String description,	
    					final String employeeType, final String employeeNumber, 
    					final String businessCategory, final String departmentNumber,
    					final String groupMember) {
    	
        User newUser = new User(cn, username, 
        						givenName, sn, 
        						digestSHA(password), uid, mail, description,
        						employeeType, employeeNumber, 
        						businessCategory, departmentNumber,
        						groupMember);
        
        newUser.setId(LdapUtils.emptyLdapName());
        userRepository.save(newUser);
        // User(String username, String givenName, String sn, String password, String uid, String email)
        /*
         * User(String cn, String username, String givenName, String sn, String password, String uid, String mail,
    	 * 		String description, String employeeType, String employeeNumber, String businessCategory, String departmentNumber)
		*/
    }

    public void modify(final String username, final String password) {
        User user = userRepository.findByUsername(username);
        user.setPassword(password);
        userRepository.save(user);
    }

    private String digestSHA(final String password) {
        String base64;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA");
            digest.update(password.getBytes());
            base64 = Base64.getEncoder()
                .encodeToString(digest.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        return "{SHA}" + base64;
    }
    
    
    
}


/*

    public Boolean authenticate(final String username, final String password) {
        User user = userRepository.findByUsernameAndPassword(username, password);
        if (user.getUid() != null) {
        	System.out.println("\nAuth with Name success!\n");
        } else {
        	System.out.println("\nAuth with Name  FAILED!\n");
        }
        return user != null;
    }
    
*/