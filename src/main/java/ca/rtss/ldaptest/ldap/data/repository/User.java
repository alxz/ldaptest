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
    private @Attribute(name = "givenName") String givenname;
    private @Attribute(name = "sn") String sn;
    private @Attribute(name = "userPassword") String password;
    private @Attribute(name = "uid") String uid;
    private @Attribute(name = "mail") String email;
    private @Attribute(name = "description") String description;
    
    public User() {
    }

    public User(String username, String givenname, String sn, String password, String uid, String email, String description) {
        this.username = username;
        this.givenname = givenname;
        this.sn = sn;
        this.password = password;
        this.uid = uid;
        this.email = email;
        this.description = description;
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

    public void setGivenname(String givenname) {
        this.givenname = givenname;
    }
    
    public String getGivenname() {
        return givenname;
    }

    public void setSn(String sn) {
        this.sn = sn;
    }

    public String getSn() {
        return sn;
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
	
    public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.uid = email;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public String toString() {
		return "User [id=" + id + ", username=" + username + ", givenname=" + givenname + ", sn=" + sn + ", password="
				+ password + ", uid=" + uid + ", email=" + email + ", description=" + description + "]";
	}


}