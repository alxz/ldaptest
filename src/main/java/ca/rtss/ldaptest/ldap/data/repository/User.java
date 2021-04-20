package ca.rtss.ldaptest.ldap.data.repository;

import javax.naming.Name;

import org.springframework.ldap.odm.annotations.Attribute;
import org.springframework.ldap.odm.annotations.Entry;
import org.springframework.ldap.odm.annotations.Id;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


//@Entry(base = "ou=users", objectClasses = { "person", "inetOrgPerson", "top" })
@JsonIgnoreProperties(ignoreUnknown = true)
@Entry(base = "ou=people", objectClasses = { "person", "organizationalPerson", "inetOrgPerson", "top" })
public class User  {
        
    @Id
    private Name id; 

    private @Attribute(name = "cn") String username;
    private @Attribute(name = "givenName") String givenName;
    private @Attribute(name = "sn") String sn;
    private @Attribute(name = "userPassword") String password;
    private @Attribute(name = "uid") String uid;
    private @Attribute(name = "mail") String mail;
    
    private @Attribute(name = "description") String description;  
	private @Attribute(name = "employeeType") String employeeType;
    private @Attribute(name = "employeeNumber") String employeeNumber;
    private @Attribute(name = "businessCategory") String businessCategory;
    private @Attribute(name = "departmentNumber") String departmentNumber;
    
    private @Attribute(name = "objectClass") String objectClass;  
    private @Attribute(name = "cn") String cn;  
    
    public User() {
    }

    public User(String username, String givenName, String sn, String password, String uid, String mail, String description) {
        this.username = username;
        this.givenName = givenName;
        this.sn = sn;
        this.password = password;
        this.uid = uid;
        this.mail = mail;
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


    public String getGivenName() {
		return givenName;
	}

	public void setGivenName(String givenName) {
		this.givenName = givenName;
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
	

	public String getMail() {
		return mail;
	}

	public void setMail(String mail) {
		this.mail = mail;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	public String getEmployeeType() {
		return employeeType;
	}

	public void setEmployeeType(String employeeType) {
		this.employeeType = employeeType;
	}

	public String getEmployeeNumber() {
		return employeeNumber;
	}

	public void setEmployeeNumber(String employeeNumber) {
		this.employeeNumber = employeeNumber;
	}

	public String getBusinessCategory() {
		return businessCategory;
	}

	public void setBusinessCategory(String businessCategory) {
		this.businessCategory = businessCategory;
	}

	public String getDepartmentNumber() {
		return departmentNumber;
	}

	public void setDepartmentNumber(String departmentNumber) {
		this.departmentNumber = departmentNumber;
	}

	public String getObjectClass() {
		return objectClass;
	}

	public void setObjectClass(String objectClass) {
		this.objectClass = objectClass;
	}

	public String getCn() {
		return cn;
	}

	public void setCn(String cn) {
		this.cn = cn;
	}

	@Override
	public String toString() {
		return "User [id=" + id + ", username=" + username + ", givenName=" + givenName + ", sn=" + sn + ", password="
				+ password + ", uid=" + uid + ", mail=" + mail + ", description=" + description + "]";
	}


}