/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
//package ca.mcgill.muhc.neel.localldapauth.service;

/**
 *
 * @author Mohamed
 */
package ca.rtss.ldaptest.ldap.data.service;

import ca.rtss.ldaptest.ldap.data.repository.ADUser;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import org.springframework.ldap.core.CollectingAuthenticationErrorCallback;

/**
 * <pre>
 * This interface is used for
 * 	 a) fetch all the user details as a list of String
 *   b) fetch all the user details as a list of User object
 *   c) fetch user details of particular user.
 * </pre>
 *
 * @author KMaji
 *
 */
public interface UserLdapRepository {

    /**
     * This method is responsible to fetch user details of particular user as a
     * string.
     *
     * @return user detail {@link User}
     */
    public String getUserProfile(String base, String userName);
    public String getUserProfile(String userName);
    
    public List<String> getUsers(String userName);    
    public List<String> getDnForUser(String uid);
    public List<String> findDnForUser(String uid);
    
    public String getUserMemberShip(String base,String userName);
    public Set<String> getUserRoles(String base, String userName ) ;
    public List<String> getUserMemberShip(String userName);

    /**
     * This method is responsible to authenticate user.
     *
     * @return boolean true|false
     */
    public boolean authenticate(String base, String userName, String password);

    public boolean authenticate(String string, String cn, String password, CollectingAuthenticationErrorCallback errorCallback);
    public HashMap<String, Object> authenticate(String userid, String password ) ;
    public HashMap<String, Object> changePwd(String userDn, String password, String newpwd ) ;
    public HashMap<String, Object> resetPwd(String userDn, String newpwd ) ;
    public HashMap<String, Object> changeMail(String userDn, String password, String newpwd ) ;
    

}

