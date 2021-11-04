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
//import ca.mcgill.muhc.neel.localldapauth.service.ActiveDirectory.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.Normalizer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;
import javax.naming.ldap.LdapName;
import net.sourceforge.stripes.util.Log;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.CollectingAuthenticationErrorCallback;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.AbstractContextMapper;
import org.springframework.ldap.filter.AndFilter;
import org.springframework.ldap.filter.EqualsFilter;
import org.springframework.ldap.filter.LikeFilter;
import static org.springframework.ldap.query.LdapQueryBuilder.query;
import org.springframework.ldap.query.SearchScope;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.stereotype.Component;
import org.apache.commons.lang3.StringUtils;
import javax.naming.ldap.Rdn;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.ldap.DefaultSpringSecurityContextSource;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.naming.Context;
import javax.naming.InvalidNameException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.LdapContext;
import net.sourceforge.stripes.localization.LocalizationUtility;
import org.springframework.security.crypto.codec.Base64;
import org.springframework.security.ldap.SpringSecurityLdapTemplate;

import jcifs.util.Hexdump;
import jcifs.util.MD4;
import org.springframework.ldap.NameNotFoundException;
import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.DistinguishedName;
import org.springframework.ldap.support.LdapNameBuilder;


package ca.rtss.ldaptest.ldap.data.service;

/**
 * This class implements the @see {@link UserRepositoryIntf}.
 *
 * @author KMaji
 *
 */
@Component
public class UserLdapRepositoryImpl implements UserLdapRepository {

    private final Log log = Log.getInstance(UserLdapRepositoryImpl.class);
    private static final String BASEDN = "dc=rtss,dc=qc,dc=ca";
    private static final String OLCDBURI = "olcDbURI";
    private static final String CNCONFIG = "cn=config";
    private static final String keystore = "/etc/pki/ca-trust/extracted/pem/tls-ca-bundle.pem";
    private static final Pattern patternUrl = Pattern.compile("(ldaps?://)(.*)/");
    private static final Pattern patternCode = Pattern.compile("LDAP: error code ([0-9a-z]*) -");
    private static final Pattern patternCodeSpec = Pattern.compile("SecurityContext error, data ([0-9a-z]*),");
    private static final Pattern patternKDC = Pattern.compile("ldaps?://(\\w+).([.\\w]+)");
    private static final Pattern patternProxy = Pattern.compile("(.*)(,ou=.*,o=[P|p]roxy.*)");

    private String groupSearchFilter = "(member={0})";
    private String groupRoleAttribute = "cn";
     private boolean convertToUpperCase = true;
    public static final List<GrantedAuthority> NO_AUTHORITIES = new ArrayList<GrantedAuthority>();

    public UserLdapRepositoryImpl() {

    }

    @Autowired(required = true)
    private String groupPrefix = "";

    @Autowired(required = true)
    @Qualifier(value = "ldapTemplate")
    private LdapTemplate ldapTemplate;

    /**
     *
     * @param base
     * @param userName
     * @return
     */
    @Override
    public String getUserProfile(String base, String userName) {
        log.debug(" --- Entering GetUserProfile()");
        List<String> results = null;
        SearchControls searchcontrols = new SearchControls();
        searchcontrols.setSearchScope(SearchControls.SUBTREE_SCOPE);
        AndFilter filter = new AndFilter();
        filter.and(new EqualsFilter("objectclass", "person"));
        filter.and(new EqualsFilter("uid", userName));
        if (ldapTemplate == null) {
            log.error("Could not create ldapTemplate instance");
            return "";
        }
        log.debug("base : " + base);
        log.debug("filter :" + filter.encode());
        ldapTemplate.setIgnorePartialResultException(Boolean.TRUE);

        try {

            results = ldapTemplate.search(query().searchScope(SearchScope.SUBTREE)
                    .base(base)
                    .where("uid")
                    .is(userName),
                    new JSonAttributeMapper());
        } catch (Exception ex) {
            log.error(" Could not get user " + userName + " profil", ex);
        } finally {
            if (results != null && !results.isEmpty()) {
                log.debug(" Calling getUserProfile() give result : " + results);
                return results.get(0);
            } else {
                log.debug(" Call getUserProfile() FAILED null or empty");
            }
        }

        return null;
    }

    /**
     *
     * @param userName
     * @return
     */
    @Override
    public String getUserProfile(String userName) {
        log.debug(" --- Entering GetUserProfile()");
        List<String> results = null;
        SearchControls searchcontrols = new SearchControls();
        searchcontrols.setSearchScope(SearchControls.SUBTREE_SCOPE);
        if (ldapTemplate == null) {
            log.error("Could not create ldapTemplate instance");
            return "";
        }
        ldapTemplate.setIgnorePartialResultException(Boolean.TRUE);

        try {
            results = ldapTemplate.search(query().searchScope(SearchScope.SUBTREE)
                    .base("")
                    .where("dn")
                    .is(userName),
                    new JSonAttributeMapper());
        } catch (Exception ex) {
            log.error(" Could not get user " + userName + " profil", ex);
        } finally {
            if (results != null && !results.isEmpty()) {
                log.debug(" Calling getUserProfile() give result : " + results);
                return results.get(0);
            } else {
                log.debug(" Call getUserProfile() FAILED null or empty");
            }
        }

        return null;
    }

    /**
     *
     * @param userName
     * @return
     */
    @Override
    public List<String> getUsers(String userName) {
        log.info(" --- Entering getUsers");
        List<String> results = null;
        AndFilter filter = new AndFilter();
        filter.and(new EqualsFilter("objectclass", "person"));

        if (StringUtils.isNotBlank(userName)) {

            if (!userName.contains("*")) {
                filter.and(new EqualsFilter("uid", userName));
            } else {
                log.info("**** LikeFilter");
                filter.and(new LikeFilter("uid", userName));
            }

            //filter.and(new EqualsFilter("uid", userName));
            SearchControls searchcontrols = new SearchControls();
            searchcontrols.setSearchScope(SearchControls.SUBTREE_SCOPE);
            if (ldapTemplate == null) {
                log.error("Could not create ldapTemplate instance");
                return null;
            }
            ldapTemplate.setIgnorePartialResultException(Boolean.TRUE);

            log.info(filter.encode());
            try {
                results = ldapTemplate.search(
                        "",
                        filter.encode(), searchcontrols,
                        new AttributesMapper() {
                    @Override
                    public Object mapFromAttributes(Attributes attrs)
                            throws NamingException {
                        return attrs.get("dn").get();
                    }
                });
            } catch (Exception ex) {
                log.error(" Could not get user " + userName + " profil -->", ex);
            } finally {
                if (results != null && !results.isEmpty()) {
                    log.debug(" Calling getUserProfile() give result : " + results);
                } else {
                    log.debug(" Call getUserProfile() FAILED null or empty");
                }
            }
        }
        return results;
    }

    /**
     *
     * @param uid
     * @return
     */
    @Override
    public List<String> getDnForUser(String uid) {
        log.debug(" --- Entering  function getDnForUser");
        EqualsFilter f = new EqualsFilter("uid", uid);
        ldapTemplate.setIgnorePartialResultException(Boolean.TRUE);
        List<String> result = null;
        log.debug("************* getDnForUser Query String  ****************" + f.toString());
        result = ldapTemplate.search(DistinguishedName.EMPTY_PATH, f.toString(),
                new AbstractContextMapper() {
            @Override
            protected Object doMapFromContext(DirContextOperations ctx) {
                return ctx.getNameInNamespace();
            }
        });        
        
        return result;
    }

    @Override
    public List<String> findDnForUser(String uid) {
        log.info(" --- Entering findDnForUser");
        LikeFilter filter = new LikeFilter("uid", uid);
        ldapTemplate.setIgnorePartialResultException(true);
        List<String> result = null;
        log.trace(" --- findDnForUser Query String : " + filter.toString());
        try {
            result = ldapTemplate.search("", filter.toString(),
                    new JSonAttributeMapper());
        } catch (Exception ignored) {
            log.warn("Exception ignored");
        }
        log.trace("----------", result.toString());
        
        return result;
    }

    @Override
    public String getUserMemberShip(String base, String userName ) {
        log.trace(" Entering getUserMemberShip(base,userName");
        List<String> results = null;
        AndFilter filter = new AndFilter();
        filter.and(new EqualsFilter("objectclass", "person"));
        filter.and(new EqualsFilter("uid", userName));
        log.debug("Membership search ==> " + filter.encode());
        SearchControls searchcontrols = new SearchControls();
        searchcontrols.setSearchScope(SearchControls.SUBTREE_SCOPE);
        searchcontrols.setReturningAttributes(new String[]{"memberOf"});
        if (ldapTemplate == null) {
            log.error("Could not create ldapTemplate instance");
            return "";
        }
        ldapTemplate.setIgnorePartialResultException(Boolean.TRUE);

        log.trace("base : " + base);
        log.trace("filter :" + filter.encode());

        JSonAttributeMapper jsonMemberOfAttribueMapper = new JSonAttributeMapper();
        jsonMemberOfAttribueMapper.setGroupPrefix(getGroupPrefix());

        
        try {
            results = ldapTemplate.search(base, filter.encode(), searchcontrols, jsonMemberOfAttribueMapper);
            log.debug("jsonMemberOfAttribueMapper ==> " + results);
        } catch (Exception ex) {
            log.error(" Could not get user " + userName + " membership", ex);
        } finally {
            if (results != null && !results.isEmpty()) {
                return results.get(0);
            }
        }

        return "";
    }
    
    @Override
    public Set<String> getUserRoles(String base, String userName) {
        LdapName memberRdn;
        log.debug(" Entering getUserRoles(base,userName)");
        List<String> jsonAttributs = null;
        Set<String> roles = new HashSet();
        AndFilter filter = new AndFilter();
        filter.and(new EqualsFilter("objectclass", "person"));
        filter.and(new EqualsFilter("uid", userName));
        log.debug("Roles search ==> " + filter.encode());
        SearchControls searchcontrols = new SearchControls();
        searchcontrols.setSearchScope(SearchControls.SUBTREE_SCOPE);
        searchcontrols.setReturningAttributes(new String[]{"memberOf"});
        if (ldapTemplate == null) {
            log.error("Could not create ldapTemplate instance");
            return roles;
        }
        ldapTemplate.setIgnorePartialResultException(Boolean.TRUE);
        JSonAttributeMapper jsonMemberOfAttribueMapper = new JSonAttributeMapper();
        jsonMemberOfAttribueMapper.setGroupPrefix(getGroupPrefix());

        try {
            jsonAttributs = ldapTemplate.search(base, filter.encode(), searchcontrols, jsonMemberOfAttribueMapper);
            if (jsonAttributs != null && !jsonAttributs.isEmpty()) {
                HashMap<String, String> memberOfAttribut = new ObjectMapper().readValue(jsonAttributs.get(0), HashMap.class);
                Set memberOfSet = memberOfAttribut.entrySet();
                Iterator memberOfIterator = memberOfSet.iterator();

                while (memberOfIterator.hasNext()) {
                    Map.Entry membership = (Map.Entry) memberOfIterator.next();
                    ArrayList<String> memberValues;
                    if (membership.getValue() instanceof String) {
                        memberValues = new ArrayList();
                        memberValues.add((String) membership.getValue());
                    } else {
                        memberValues = (ArrayList) membership.getValue();
                    }
                    for (String memberDN : memberValues) {
                        memberRdn = new LdapName(memberDN);
                        String memberCN = (String) memberRdn.getRdn(memberRdn.getRdns().size() - 1).getValue();
                        roles.add(memberCN.toUpperCase());
                    }
                }
            }

        } catch (Exception ex) {
            log.trace(" Could not get user " + userName + " roles", ex);
        }

        log.info("getUserRoles  ==> " + roles);
        return roles;
    }
    

    
    
    @Override
    public List<String> getUserMemberShip(String userName) {
        log.debug(" Entering getUserMemberShip()");
        List<String> results = null;
        AndFilter filter = new AndFilter();
        
        
        filter.and(new EqualsFilter("objectclass", "groupOfNames"));
        filter.and(new EqualsFilter("member", userName));
        log.debug("Membership search ==> " + filter.encode());
        SearchControls searchcontrols = new SearchControls();
        searchcontrols.setSearchScope(SearchControls.SUBTREE_SCOPE);
        searchcontrols.setReturningAttributes(new String[]{"cn"});
        if (ldapTemplate == null) {
            log.error("Could not create ldapTemplate instance");
            return results;
        }
        ldapTemplate.setIgnorePartialResultException(Boolean.TRUE);

        JSonAttributeMapper jsonMemberOfAttribueMapper = new JSonAttributeMapper();
        jsonMemberOfAttribueMapper.setGroupPrefix(getGroupPrefix());

        
        try {
            results = ldapTemplate.search("", filter.encode(), searchcontrols, jsonMemberOfAttribueMapper);
            log.info("getUserMemberShip ==> " + results);
        } catch (Exception ex) {
            log.error(" Could not get user " + userName + " membership", ex);
        } 

        return results;
    }

    /**
     *
     * @param base
     * @param userName
     * @param password
     * @param errorCallback
     * @return
     */
    @Override
    public boolean authenticate(String base, String userName,
            String password, CollectingAuthenticationErrorCallback errorCallback
    ) {
        boolean result = false;
        log.debug(" Calling  authenticate(String base, String userName, String password)");
        AndFilter filter = new AndFilter();
        filter.and(new EqualsFilter("objectclass", "person"));
        filter.and(new EqualsFilter("uid", userName));

        log.debug("base : " + base);
        log.debug("filter :" + filter.encode());

        if (ldapTemplate == null) {
            log.error("Could not create ldapTemplate instance");
        } else {
            ldapTemplate.setIgnorePartialResultException(Boolean.TRUE);
            errorCallback = new CollectingAuthenticationErrorCallback();
            result = ldapTemplate.authenticate(base, filter.encode(), password, errorCallback);
            if (errorCallback.hasError()) {
                log.info("Callback:" + errorCallback.getError().getMessage());
                throw new AuthenticationServiceException(errorCallback.getError().getMessage());
            }
            log.debug("Logon " + result + "  for " + userName);
        }
        return result;
    }

    @Override
    public boolean authenticate(String base, String userName,
            String password
    ) {
        log.debug(" Calling  authenticate(base, userName, password, errorCallback)");
        AndFilter filter = new AndFilter();
        filter.and(new EqualsFilter("objectclass", "person"));
        filter.and(new EqualsFilter("cn", userName));

        if (ldapTemplate == null) {
            log.error("Could not create ldapTemplate instance");
            return false;
        }
        ldapTemplate.setIgnorePartialResultException(Boolean.TRUE);
        try {
            return ldapTemplate.authenticate(base, filter.encode(), password);
        } catch (Exception ex) {
            log.error(" Could not authenticate " + userName, ex);
            return false;
        }
    }

    @Override
    public HashMap<String, Object> authenticate(String userDn, String password) {

        log.debug(" Executing authenticate(String userDn, String password)");
        Locale locale = new Locale("fr_CA");
        String username = "";
        HashMap<String, Object> status = new HashMap<>();
        status.put("Authenticated", Boolean.FALSE);
        status.put("Errmsg", "");
        SpringSecurityLdapTemplate template = null;
        HashMap<String, Object> values = getDnInfos(userDn);
        String commonname = (String) values.get("commonname");
        String basesearch = (String) values.get("basesearch");
                

        try {
            // Logon current userid

            DefaultSpringSecurityContextSource contextSource = getDefaultContextSource();            
            log.info("Logon params: ", userDn);
            contextSource.setUserDn(userDn);
            contextSource.setPassword(password);
            contextSource.setBase(contextSource.getBaseLdapPathAsString());
            SearchControls searchControls = new SearchControls();
            searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
            searchControls.setDerefLinkFlag(true);
            
            template = new SpringSecurityLdapTemplate(contextSource);
            template.setSearchControls(searchControls);
            template.afterPropertiesSet();
            log.info("CONTEXTSOURCE ",template.getContextSource().getReadOnlyContext());
            
            status.put("Authenticated", Boolean.TRUE);
            
        } catch (Exception e) {
            log.error("Error auth: ", e.toString());
            Matcher matcherCode = patternCode.matcher(e.toString());
            Matcher matcherCodeSpec = patternCodeSpec.matcher(e.toString());
            String code;
            if (matcherCode.find()) {
                if (matcherCodeSpec.find()) {
                    log.error("*** Matches: ", matcherCodeSpec);
                    log.error("*** Matches: ", matcherCodeSpec.group(1));
                    code = "Login." + matcherCodeSpec.group(1);
                } else {
                    log.error("*** Matches: ", matcherCode);
                    log.error("*** Matches: ", matcherCode.group(1));
                    code = "Login." + matcherCode.group(1);
                }
                log.error("*** Matches CODE: ", code);
                String error = LocalizationUtility.getErrorMessage(locale, code);
                log.error("Ldap error: ", error.replace("{2}", userDn));
                status.put("Errmsg", error.replace("{2}", userDn));
            } else {
                status.put("Errmsg", "Traitement impossible actuellement.");
            }
        }

        return status;
    }

    @Override
    public HashMap<String, Object> changeMail(String userDn, String password, String email) {
        log.debug(" Executing changePwd(String userDn, String password, String newpwd)");
        String protocol = "", server = "", username = "";
        Boolean isProxyfied = Boolean.FALSE;
        HashMap<String, Object> status = new HashMap<>();
        status.put("Authenticated", Boolean.FALSE);
        status.put("MailUpdated", Boolean.FALSE);
        status.put("Errmsg", "");
        Boolean mailUpdated = Boolean.FALSE;

        status.put("MailUpdated", mailUpdated);
        return status;
    }

    @Override
    public HashMap<String, Object> resetPwd(String userDn, String newpwd) {

        log.debug(" Executing resetPwd(String userDn, String password, String newpwd)");

        HashMap<String, Object> status = new HashMap<>();

        status.put("Authenticated", Boolean.FALSE);
        status.put("PasswordUpdated", Boolean.FALSE);
        status.put("Errmsg", "");
        Boolean passwordUpdated = Boolean.FALSE;
        Locale locale = new Locale("fr_CA");

        SpringSecurityLdapTemplate template = null;
        try {
            HashMap<String, Object> values = getDnInfos(userDn);
            String commonname = (String) values.get("commonname");
            String basesearch = (String) values.get("basesearch");
            Boolean isProxyfied = (Boolean) values.get("isProxyfied");
            LdapName person = new LdapName("cn=" + commonname);
            DirContextAdapter ctx = (DirContextAdapter) template.lookup(person);
            if (!isProxyfied) {
                ctx.setAttributeValue("userPassword", hashMD5Password(newpwd));
                template.modifyAttributes(ctx);
                passwordUpdated = Boolean.TRUE;
                log.info("Passowrd has been updated");
            } else {
                status.put("Errmsg", "Fonction non disponible pour cette instance.");
            }

            status.put("PasswordUpdated", passwordUpdated);
            return status;
        } catch (InvalidNameException | NoSuchAlgorithmException | UnsupportedEncodingException ex) {
            log.error("Error", ex.getMessage());
        }
        status.put("PasswordUpdated", passwordUpdated);
        return status;
    }

    /**
     *
     * @param userDn
     * @param password
     * @return
     */
    @Override
    public HashMap<String, Object> changePwd(String userDn, String password, String newpwd) {

        log.debug(" Executing changePwd(String userDn, String password, String newpwd)");
        String username = "";
        HashMap<String, Object> status = new HashMap<>();
        status.put("Authenticated", Boolean.FALSE);
        status.put("PasswordUpdated", Boolean.FALSE);
        status.put("Errmsg", "");
        Boolean passwordUpdated = Boolean.FALSE;
        Locale locale = new Locale("fr_CA");
        SpringSecurityLdapTemplate template = null;
        HashMap<String, Object> values = getDnInfos(userDn);
        String commonname = (String) values.get("commonname");
        String basesearch = (String) values.get("basesearch");
        Boolean isProxyfied = (Boolean) values.get("isProxyfied");

        try {

            // Logon current userid
            DefaultSpringSecurityContextSource contextSource = getDefaultContextSource();
            contextSource.setUserDn(userDn);
            contextSource.setPassword(password);
            contextSource.setBase(basesearch);
            template = new SpringSecurityLdapTemplate(contextSource);
            template.afterPropertiesSet();
            log.debug("Set logon params: ", userDn);
            LdapName person = new LdapName("cn=" + commonname);
            DirContextAdapter ctx = (DirContextAdapter) template.lookup(person);
            // Get current user
            username = ctx.getStringAttribute("uid");
            log.info("username: ", username);

            if (newpwd != null && newpwd.length() > 2) {
                if (!isProxyfied) {
                    ctx.setAttributeValue("userPassword", hashMD5Password(newpwd));
                    template.modifyAttributes(ctx);
                    passwordUpdated = Boolean.TRUE;
                    log.info("Passowrd has been updated");
                } else {

                    Hashtable env = new Hashtable();
                    env.put(DirContext.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
                    env.put(DirContext.PROVIDER_URL, contextSource.getUrls());
                    env.put(DirContext.SECURITY_AUTHENTICATION, "Simple");
                    env.put(DirContext.SECURITY_PRINCIPAL, "cn=Manager,cn=config");
                    env.put(DirContext.SECURITY_CREDENTIALS, "Password1!");

                    SearchControls sc1 = new SearchControls();
                    sc1.setSearchScope(SearchControls.ONELEVEL_SCOPE);
                    sc1.setReturningAttributes(new String[]{OLCDBURI});

                    String KDCs = "";
                    try {
                        DirContext dc = new InitialDirContext(env);
                        NamingEnumeration directoryNE = null;
                        AndFilter filter = new AndFilter();
                        filter.and(new EqualsFilter("olcDatabase", "ldap"));
                        directoryNE = dc.search("cn=config", filter.encode(), sc1);

                        while (directoryNE.hasMore()) {
                            SearchResult result1 = (SearchResult) directoryNE.next();
                            Attributes attrs = result1.getAttributes();
                            Attribute attr = attrs.get(OLCDBURI);
                            KDCs = (String) attr.get();
                        }
                        dc.close();

                    } catch (javax.naming.AuthenticationException e) {
                        log.error("Bad configurtation");
                    } catch (NamingException e) {
                        log.error(e.getCause()
                                + "No Results for: " + "\nProblem: " + e.getLocalizedMessage() + "  ");
                    }

                    Boolean trying = Boolean.TRUE;

                    for (String kdc : StringUtils.split(StringUtils.remove(KDCs, '"'), ',')) {
                        log.debug("KDC: ", kdc);

                        Matcher matcherKDC = patternKDC.matcher(kdc);
                        if (matcherKDC.find()) {
                            log.info("hostname: ", matcherKDC.group(1));
                            log.info("domain: ", matcherKDC.group(2));

                            log.info("Windows change password: ", username, ", ", newpwd);
                            if (trying) {
                                LdapContext conn = ActiveDirectory.getConnection(username, password, matcherKDC.group(2), matcherKDC.group(1));
                                log.info("Windows AD Connected");
                                ActiveDirectory.getUser(username, conn).changePassword(password, newpwd, true, conn);
                                log.info("Windows password changed");
                                passwordUpdated = Boolean.TRUE;
                                if (conn != null) {
                                    conn.close();
                                }
                                trying = Boolean.FALSE;
                                log.info("Passowrd has been updated");
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error auth: ", e.toString());
            Matcher matcherCode = patternCode.matcher(e.toString());
            Matcher matcherCodeSpec = patternCodeSpec.matcher(e.toString());
            String code;
            if (matcherCode.find()) {
                if (matcherCodeSpec.find()) {
                    log.error("*** Matches: ", matcherCodeSpec);
                    log.error("*** Matches: ", matcherCodeSpec.group(1));
                    code = "Login." + matcherCodeSpec.group(1);
                } else {
                    log.error("*** Matches: ", matcherCode);
                    log.error("*** Matches: ", matcherCode.group(1));
                    code = "Login." + matcherCode.group(1);
                }
                log.error("*** Matches CODE: ", code);
                String error = LocalizationUtility.getErrorMessage(locale, code);
                log.error("Ldap error: ", error.replace("{2}", commonname));
                status.put("Errmsg", error.replace("{2}", commonname));
            } else {
                status.put("Errmsg", "Traitement impossible actuellement.");
            }
        }
        status.put("PasswordUpdated", passwordUpdated);
        return status;
    }

    private class JSonAttributeMapper implements AttributesMapper<String> {

        String groupPrefix = "";

        public String getGroupPrefix() {
            return groupPrefix;
        }

        public void setGroupPrefix(String groupPrefix) {
            this.groupPrefix = groupPrefix;
        }

        @Override
        public String mapFromAttributes(Attributes attributes) throws NamingException {
            Map<String, Object> map = new HashMap<>();
            String profile = "";

            if (attributes != null) {
                for (NamingEnumeration<Attribute> atts = (NamingEnumeration<Attribute>) attributes.getAll(); atts.hasMore();) {
                    Attribute att = atts.nextElement();

                    log.trace(" Att: ID:", att.getID(), ", value:", att.get(0));
                    ArrayList<Object> list = null;
                    if (att.getAll().hasMore()) {
                        list = new ArrayList<>();
                        for (NamingEnumeration attsChild = att.getAll(); attsChild.hasMore();) {
                            Object attChild = attsChild.nextElement();
                            if (att.getID().equalsIgnoreCase("cn")) {
                                String newCN = attChild.toString()
                                        .replaceAll("[^a-zA-Z0-9áàâäãåçéèêëíìîïñóòôöõúùûüýÿæœÁÀÂÄÃÅÇÉÈÊËÍÌÎÏÑÓÒÔÖÕÚÙÛÜÝŸÆŒ\\.\\s\\-\\_]", "");
                                log.trace("cut space tmp ====>   " + newCN);
                                list.add(newCN);
                            } else if ((att.getID().equalsIgnoreCase("accountExpires")) || (att.getID().equalsIgnoreCase("lastLogon"))) {
                                long fileTime = (Long.parseLong(attChild.toString()) / 10000L) - +11644473600000L;
                                Date inputDate = new Date(fileTime);
                                list.add(attChild);
                                log.trace(" -->> Att: ID:", att.getID(), "  prefix:", inputDate.toLocaleString());
                            } else if ((!groupPrefix.isEmpty()) && (att.getID().equalsIgnoreCase("memberof"))) {
                                String[] prefixes = groupPrefix.split(",");
                                int i = 0;
                                boolean found = false;
                                while ((i < prefixes.length) && (!found)) {
                                    if (attChild.toString().toUpperCase().contains(prefixes[i].toUpperCase())) {
                                        log.trace(" -->> Att: ID:", attChild.toString(), "  prefix:", prefixes[i]);
                                        found = Boolean.TRUE;
                                        list.add(attChild);
                                    }
                                    i++;
                                }
                            } else if ((!groupPrefix.isEmpty()) && (att.getID().equalsIgnoreCase("member"))) {
                                String[] prefixes = groupPrefix.split(",");
                                int i = 0;
                                boolean found = false;
                                while ((i < prefixes.length) && (!found)) {
                                    if (attChild.toString().toUpperCase().contains(prefixes[i].toUpperCase())) {
                                        log.trace(" -->> Att: ID:", attChild.toString(), "  prefix:", prefixes[i]);
                                        found = Boolean.TRUE;
                                        list.add(attChild);
                                    }
                                    i++;
                                }
                            } else if (att.getID().equalsIgnoreCase("dnQualifier") || att.getID().equalsIgnoreCase("DN") || att.getID().equalsIgnoreCase("mail") || att.getID().equalsIgnoreCase("secretary") || att.getID().equalsIgnoreCase("manager") || att.getID().equalsIgnoreCase("description") || att.getID().equalsIgnoreCase("objectCategory")) {
                                list.add(attChild);
                            } else if (att.getID().equalsIgnoreCase("physicalDeliveryOfficeName")) {
                                if (StringUtils.isBlank((String) attChild)) {
                                    list.add(new String(" "));
                                }
                            } else {
                                String newChild = attChild.toString()
                                        .replaceAll("[^a-zA-Z0-9áàâäãåçéèêëíìîïñóòôöõúùûüýÿæœÁÀÂÄÃÅÇÉÈÊËÍÌÎÏÑÓÒÔÖÕÚÙÛÜÝŸÆŒ\\.\\s\\-\\_\\(\\)]", "");
                                log.trace("cut space tmp ====>   " + newChild);
                                list.add(newChild);
                            }
                        }

                        if (list.size() == 1) {
                            map.put(att.getID(), list.get(0)); // add value for an attribute as as object NOT as an array
                        } else if (!list.isEmpty()) {
                            map.put(att.getID(), list); //add many values for an attribute as array of elements
                        }
                    }
                }
            }
            try {

                if (!map.isEmpty()) {
                    profile = new ObjectMapper().writeValueAsString(map);
                }
            } catch (IOException ex) {
                log.error(" Could not Serialize LDAP search result attributes to Json", ex);
            }
            log.debug(" --> Json:" + profile);
            return profile;
        }

    }

    /**
     * This class is responsible to print only cn .
     *
     * @author KMaji
     *
     */
    private class SingleAttributesMapper implements AttributesMapper<String> {

        @Override
        public String mapFromAttributes(Attributes attrs) throws NamingException {
            Attribute cn = attrs.get("cn");
            return cn.toString();
        }
    }

    /**
     * This class is responsible to print all the content in string format.
     *
     * @author KMaji
     *
     */
    private class MultipleAttributesMapper implements AttributesMapper<String> {

        @Override
        public String mapFromAttributes(Attributes attrs) throws NamingException {
            NamingEnumeration<? extends Attribute> all = attrs.getAll();
            StringBuffer result = new StringBuffer();
            result.append("\n Result { \n");
            while (all.hasMore()) {
                Attribute id = all.next();
                result.append(" \t |_  #" + id.getID() + "= [ " + id.get() + " ]  \n");
                log.info(id.getID() + "\t | " + id.get());
            }
            result.append("\n } ");
            return result.toString();
        }
    }

    public LdapTemplate getLdapTemplate() {
        return ldapTemplate;
    }

    public void setLdapTemplate(LdapTemplate ldapTemplate) {
        this.ldapTemplate = ldapTemplate;
    }

    public String getGroupPrefix() {
        return groupPrefix;
    }

    public void setGroupPrefix(String groupPrefix) {
        this.groupPrefix = groupPrefix;
    }

    public static String removeAccents(String text) {
        return text == null ? null
                : Normalizer.normalize(text, Normalizer.Form.NFD)
                        .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
    }

    /**
     * Generate an NTLM password hash Uses jcifs
     *
     * @param plain text password
     * @return NTLM 24-bit ANSI hash
     */
    private String hashNTPassword(String password) throws UnsupportedEncodingException {
        MD4 md4 = new MD4();
        byte[] bpass = password.getBytes("UnicodeLittleUnmarked");
        md4.engineUpdate(bpass, 0, bpass.length);
        byte[] hashbytes = md4.engineDigest();
        String ntHash = Hexdump.toHexString(hashbytes, 0, hashbytes.length * 2);
        return ntHash;
    }

    /**
     * Generate an MD5 password hash for LDAP First create a digest from the
     * plain text password, then append the LDAP crypto-prefix
     *
     * @param plaintext password
     * @return MD5 hashed string
     */
    private String hashMD5Password(final String password) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        //https://docs.oracle.com/javase/6/docs/technotes/guides/intl/encoding.doc.html
        MessageDigest digest = MessageDigest.getInstance("MD5");
        Charset csets = Charset.forName("UTF8");
        digest.update(password.getBytes(csets));
        String md5Password = new String(Base64.encode(digest.digest()));
        return "{MD5}" + md5Password;
    }

    /**
     * Escapes any special chars (RFC 4515) from a string representing a a
     * search filter assertion value. http://blog.dzhuvinov.com/?p=585
     *
     * @param input The input string.
     *
     * @return A assertion value string ready for insertion into a search filter
     * string.
     */
    public static String sanitize(final String input) {

        String s = "";

        for (int i = 0; i < input.length(); i++) {

            char c = input.charAt(i);

            if (c == '*') {
                // escape asterisk
                s += "\\2a";
            } else if (c == '(') {
                // escape left parenthesis
                s += "\\28";
            } else if (c == ')') {
                // escape right parenthesis
                s += "\\29";
            } else if (c == '\\') {
                // escape backslash
                s += "\\5c";
            } else if (c == '\u0000') {
                // escape NULL char
                s += "\\00";
            } else if (c <= 0x7f) {
                // regular 1-byte UTF-8 char
                s += String.valueOf(c);
            } else if (c >= 0x080) {

                // higher-order 2, 3 and 4-byte UTF-8 chars
                try {
                    byte[] utf8bytes = String.valueOf(c).getBytes("UTF8");

                    for (byte b : utf8bytes) {
                        s += String.format("\\%02x", b);
                    }

                } catch (UnsupportedEncodingException e) {
                    // ignore
                }
            }
        }

        return s;
    }

    private HashMap<String, Object> getDnInfos(String userDn) {
        HashMap<String, Object> values = new HashMap<>();
        values.put("isProxyfied", Boolean.FALSE);
        values.put("commonname", "");
        values.put("basesearch", "");
        try {
            LdapName ln = new LdapName(userDn);
            ArrayList<String> elt = new ArrayList();
            for (Rdn rdn : ln.getRdns()) {
                if (!"CN".equalsIgnoreCase(rdn.getType()) && !"DC".equalsIgnoreCase(rdn.getType())) {
                    log.trace("Element: ", rdn.getType(), " ", rdn.getValue());
                    elt.add(rdn.toString());
                    if ("O".equalsIgnoreCase(rdn.getType())) {
                        String sub = rdn.getValue().toString();
                        log.debug("Substring: ", sub.substring(0, 5));
                        if ("Proxy".equalsIgnoreCase(sub.substring(0, 5))) {
                            values.put("isProxyfied", Boolean.TRUE);
                        }
                    }
                } else if ("CN".equalsIgnoreCase(rdn.getType())) {
                    values.put("commonname", (String) rdn.getValue());
                }
            }
            Collections.reverse(elt);
            elt.add(BASEDN);
            values.put("basesearch", StringUtils.join(elt, ","));

        } catch (InvalidNameException ex) {
            log.error("Error: ", ex.getMessage());
        }
        return values;
    }

    private DefaultSpringSecurityContextSource getDefaultContextSource() throws ClassNotFoundException, NamingException {
        Hashtable<String, String> map = (Hashtable<String, String>) ldapTemplate.getContextSource().getReadWriteContext().getEnvironment();

        System.setProperty("javax.net.ssl.trustStore", keystore);
        System.setProperty("javax.net.ssl.trustStorePassword", "");

        // Get LocalAuth OpenLdap configuration
        DefaultSpringSecurityContextSource contextSource = new DefaultSpringSecurityContextSource(map.get("java.naming.provider.url"));
        Matcher url = patternUrl.matcher((String) map.get("java.naming.provider.url"));
        if (url.find()) {
            String protocol = url.group(1);
            String server = url.group(2);
            contextSource.setUrl(protocol + server);
        } else {
            log.info("Cannot get default Url: ", map.get("java.naming.provider.url"));
        }

        contextSource.setReferral(map.get("java.naming.referral"));
        contextSource.setPooled(Boolean.getBoolean(map.get("com.sun.jndi.ldap.connect.pool")));
        contextSource.setDirObjectFactory(Class.forName(map.get("java.naming.factory.object")));
        
          contextSource.setAnonymousReadOnly(false);
        contextSource.afterPropertiesSet();
        log.trace("Context Done ");
        return contextSource;
    }
    
    private String listKDC(DefaultSpringSecurityContextSource contextSource) {

        String KDCs = "";
        DirContext dc = null;
        NamingEnumeration directoryNE = null;
        SearchControls sc1 = new SearchControls();
        Hashtable<String, String> env = new Hashtable<String, String>();

        env.put(DirContext.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(DirContext.SECURITY_AUTHENTICATION, "Simple");
        env.put(DirContext.SECURITY_PRINCIPAL, "cn=admin,cn=config");
        env.put(DirContext.SECURITY_CREDENTIALS, "Password1!");

        try {
            Hashtable<String, String> map = (Hashtable<String, String>) contextSource.getReadWriteContext().getEnvironment();
            Matcher url = patternUrl.matcher((String) map.get("java.naming.provider.url"));
            if (url.find()) {
                String protocol = url.group(1);
                String server = url.group(2);
                contextSource.setUrl(protocol + server);
                
                env.put(DirContext.PROVIDER_URL, protocol + server);
            } else {
                log.debug("Cannot get default Url: ", map.get("java.naming.provider.url"));
            }
            
            sc1.setSearchScope(SearchControls.ONELEVEL_SCOPE);
            sc1.setReturningAttributes(new String[]{OLCDBURI});
            
            dc = new InitialDirContext(env);
            
            
            AndFilter filter = new AndFilter();
            filter.and(new EqualsFilter("olcDatabase", "ldap"));
            directoryNE = dc.search("cn=config", filter.encode(), sc1);

            log.debug("filter.encode()", filter.encode());

            while (directoryNE.hasMore()) {
                SearchResult result1 = (SearchResult) directoryNE.next();
                Attributes attrs = result1.getAttributes();
                Attribute attr = attrs.get(OLCDBURI);
                KDCs = (String) attr.get();
            }
            dc.close();

        } catch (javax.naming.AuthenticationException e) {
            log.error("Bad configuration", e.getMessage());
        } catch (NamingException e) {
            log.error(e.getCause()
                    + "No Results for: " + "\nProblem: " + e.getLocalizedMessage() + "  ");
        }
        
        return KDCs;
    }
}
