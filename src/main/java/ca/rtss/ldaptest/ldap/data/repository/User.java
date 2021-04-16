package ca.rtss.ldaptest.ldap.data.repository;

import javax.naming.Name;

import org.springframework.ldap.odm.annotations.Attribute;
import org.springframework.ldap.odm.annotations.Entry;
import org.springframework.ldap.odm.annotations.Id;


//@Entry(base = "ou=users", objectClasses = { "person", "inetOrgPerson", "top" })
@Entry(base = "ou=people", objectClasses = { "person", "organizationalPerson", "inetOrgPerson", "top" })
public class User  {
        
    @Id
    private Name id; 

    private @Attribute(name = "cn") String username;
    private @Attribute(name = "sn") String password;
    private @Attribute(name = "uid") String uid;

    public User() {
    }

    public User(String username, String password, String uid) {
        this.username = username;
        this.password = password;
        this.uid = uid;
    }

    public Name getId() {
        return id;
    }

    public void setId(Name id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	@Override
    public String toString() {
        return "userName: " + (String) username + " uid: " + (String) uid;
    }
    

}