package ca.rtss.ldaptest.ldap.data.repository;

import java.util.List;

import org.springframework.data.ldap.repository.LdapRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends LdapRepository<User> {

    User findByUsername(String username);

    User findByUsernameAndPassword(String username, String password);

    List<User> findByUsernameLikeIgnoreCase(String username);
    

    
//    User findByFullName(String fullName);

//    User findByEmail(String email);
//
    List<User> findByUid(String uid);
//
//    List<User> findByEmailLikeIgnoreCase(String email);
//
//    List<User> findByEmailNotLike(String email);

}