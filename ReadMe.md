**REST API Services for ldap users manipulation**

Current version requires the following setup:

LDAP server configuration must be placed into: /src/main/resources/application.properties

	ldap.partitionSuffix=dc=rtss,dc=qc,dc=ca
	ldap.partition=rtss
	ldap.principal=cn=Manager,dc=rtss,dc=qc,dc=ca
	ldap.password=
	ldap.port=389
	ldap.url=ldap://172.26.125.157:389
	ldap.usersFullpath=Users,o=Local
	ldap.usersOU=Users
	ldap.orgLocal=Local

Replace the values according to your LDAP server access credentials, 
DO NOT FORGET to provide a password for a superuser account (Manager in this example)!
=============================================================================

**How to use the REST API services:**

	1) Rest Call (GET) to search for a user account in LDAP:
		
		1.1) Search by user UID: 		
				https://serveraddress/ldaprestapi/api/v1/searchuid
			 Example (use GET): 
					https://localhost:8080/ldaprestapi/api/v1/searchuid?uid=mike
			
		1.2) Search by user ldap attribute cn (usually combined of first name + last name): 		
				https://serveraddress/ldaprestapi/api/v1/search
			 Example (use GET):
				https://localhost:8080/ldaprestapi/api/v1/search?name=John Doe
		
		1.3) Search by user ldap attribute mail (email): 		
				https://serveraddress/ldaprestapi/api/v1/searchmail
			 Example (use GET):
				https://localhost:8080/ldaprestapi/api/v1/searchmail?name=john.doe@hawkins.com
								
		1.4) Search by either user ldap attributes UID/CN/First Name/Surname And/Or eMail (uid/cn/givenName/sn/mail): 		
				https://serveraddress/ldaprestapi/api/v2/search
			 Example (use GET):
			  search by uid (and cn/mail):
				a)  https://localhost:8080/ldaprestapi/api/v2/search?uid=johndoe
					https://localhost:8080/ldaprestapi/api/v2/search?uid=johndoe&name=John					
					https://localhost:8080/ldaprestapi/api/v2/search?uid=johndoe&mail=joghdoe@planetexpress.com
					https://localhost:8080/ldaprestapi/api/v2/search?uid=johndoe&name=John&mail=joghdoe@planetexpress.com
				search by name(s):
				b) https://localhost:8080/ldaprestapi/api/v2/search?name=John Doe
					https://localhost:8080/ldaprestapi/api/v2/search?name=John	
					https://localhost:8080/ldaprestapi/api/v2/search?name=Doe	
			  search by name(s) and email:
				d) https://localhost:8080/ldaprestapi/api/v2/search?name=John Doe&mail=joghdoe@planetexpress.com
			  search by mail only:
				e) https://localhost:8080/ldaprestapi/api/v2/search?mail=joghdoe@planetexpress.com								
	
	
	1.5) Search with V2: There are 3 different parameters could be used in combination or sole:
			-- uid - to search for a uid attribute;
			-- name - to search for a name attribute;
			-- mail - to search for a mail attribute;
			The response will be given as an array of json objects object:
			
			Example:   http://localhost:8080/api/v2/search?mail=*planetexpress.com
			Response:
				[
				    {
				        "uid": "amy",
				        "mail": "amy@planetexpress.com",
				        "ou": "Intern",
				        "givenName": "Amy",
				        "objectClass": "top",
				        "description": "Human",
				        "sn": "Kroker",
				        "cn": "Amy Wong"
				    },
				    {				        
				        "uid": "bender",
				        "employeeType": "Ship's Robot",
				        "mail": "bender@planetexpress.com",
				        "ou": "Delivering Crew",
				        "displayName": "Bender",
				        "givenName": "Bender",
				        "objectClass": "inetOrgPerson",
				        "description": "Robot",
				        "sn": "Rodríguez",
				        "memberOf": "cn=ship_crew,ou=people,dc=planetexpress,dc=com",
				        "cn": "Bender Bending Rodríguez"
				    }
				]
	
	1.6) Search with V3: There is only one required parameter called 'searchstring'. 
			Set it to the search string you want to search, it will search among the attributes like: cn/givenName/sn/mail
			Example:  http://localhost:8080/api/v3/search?searchstring=*planetexpress.com
			Response: JSON array:
				{
				    "data": [
				        {
				            "uid": "amy",
				            "mail": "amy@planetexpress.com",
				            "ou": "Intern",
				            "givenName": "Amy",
				            "objectClass": "top",
				            "description": "Human",
				            "sn": "Kroker",
				            "cn": "Amy Wong"
				        },
				        {
				            "uid": "bender",
				            "employeeType": "Ship's Robot",
				            "mail": "bender@planetexpress.com",
				            "ou": "Delivering Crew",
				            "displayName": "Bender",
				            "givenName": "Bender",
				            "objectClass": "inetOrgPerson",
				            "description": "Robot",
				            "sn": "Rodríguez",
				            "memberOf": "cn=ship_crew,ou=people,dc=planetexpress,dc=com",
				            "cn": "Bender Bending Rodríguez"
				        }
				    ]
				}
	
	* Please note search may return more than one user object depending on what search parameters were used.

	2) Rest Call (POST) to create a user account in LDAP:
		http://serveraddress/api/v1/create
		
	   POST Load (JSON) example:
			{
				"username" 			: "John Doe" ,
				"givenName" 		: "John",
				"sn" 				:  "Doe",
				"password" 			:  "johndoe",
				"uid" 				: "johndoe",
				"mail" 				: "john.doe@hawkins.com",
				"businessCategory" 	:  "code",
				"employeeType" 		: "1",
				"employeeNumber" 	: "2",
				"departmentNumber" 	: "3"
			}
		
		v2: Create with group member (in-progress):
		
		2.1) Rest Call (POST) to create an account and make it a member of the group (supply in JSON format, group attribute is: "groupMember" ):
				http://localhost:8080/api/v2/create
				Example:
				{
					"username" 			: "John Doe" ,
					"givenName" 		: "John",
					"sn" 				:  "Doe",
					"password" 			:  "johndoe",
					"uid" 				: "johndoe",
					"mail" 				: "john.doe@hawkins.com",
					"businessCategory" 	:  "code",
					"employeeType" 		: "1",
					"employeeNumber" 	: "2",
					"departmentNumber" 	: "3"
					"groupMember" :  "students"
				}
				
		
	3) Rest call (POST) to modify ldap account:
	   https://serveraddress/ldaprestapi/api/v1/modify
	   Do not try to modify property 'uid' - this is the only identified unique for the ldap 
	   POST Load (JSON) example:
			{
				"username" 			: "John Junior Doe" ,
				"givenName" 		: "John Junior",
				"sn" 				:  "Doe",
				"password" 			:  "johndoe!",
				"uid" 				: "johndoe",
				"mail" 				: "john.doe@hawkins.com",
				"businessCategory" 	:  "code",
				"employeeType" 		: "1",
				"employeeNumber" 	: "2",
				"departmentNumber" 	: "3",
				"groupMember" 		: "students"
			}
			
		**Please note: You may specify "groupMember", so if this is a new group for a user, user account to be added to the group (member/memberOf props)
			However user will not be removed from any other groups!!!
	
	* To comply with REST best practices it has been extended with the following:
	
		3.1) Rest call (PUT) to modify ldap account: This is to modify most of attributes (except UID)
			 https://serveraddress/ldaprestapi/api/v2/modify
			 PUT Load (JSON) example:
				 {
					"username" 			: "John Junior Doe" ,
					"givenName" 		: "John Junior",
					"sn" 				:  "Doe",
					"password" 			:  "johndoe!",
					"uid" 				: "johndoe",
					"mail" 				: "john.doe@hawkins.com",
					"businessCategory" 	:  "code",
					"employeeType" 		: "1",
					"employeeNumber" 	: "2",
					"departmentNumber" 	: "3",
					"groupMember" 		: "students"
				}
			
		**Please note: You may specify "groupMember", so if this is a new group for a user, user account to be added to the group (member/memberOf props),
			However user will not be removed from any other groups!!! 
		
		3.2) Rest call (PATCH) to modify ldap account's properties: This is to modify one/two attribute(s) like givenName and sn
			 https://serveraddress/ldaprestapi/api/v2/modifyname
				 PATCH Load (JSON) example:
				 {
					"givenName" 		: "John Junior",
					"sn" 				:  "Doe",
					"uid" 				: "johndoe"
				}
	
		  
	
	4) Rest call (POST) to delete and account:
		https://serveraddress/ldaprestapi/api/v1/delete
		Must provide both 'uid' and 'cn' properties! (as simple protection).
		Also make sure cn is exactly as ldap object cn (you can use searchuid REST call to get cn).
		
		 Delete calls POST (JSON) example:
			{
				"cn" : "John Doe"
				"uid" : "johndoe"
			}
	
	* To comply with REST best practices it has been extended with the following:
	
		4.1) Rest call (DELETE) to delete an account: https://serveraddress/ldaprestapi/api/v2/delete
			 Must provide both 'uid' and 'cn' properties!.
			 Example:
				http://localhost:8080/api/v2/delete?cn=John Doe&uid=johndoe
			
			* This delete method usage looks similar to GET (but in any case it is using DELETE controller)
	
	
	5) Rest call (PATCH) to Remove member from the group:
	   As a payload please supply simple JSON object with two fileds: "uid" and "groupMember"
			 https://serveraddress/ldaprestapi/api//v1/removemember
				 PATCH Load (JSON) example:
				 {
						"uid" : "johndoe",
						"groupMember" :  "students"
				 }	 
	
	
	6) Simple rest call (GET) to receive some greetings:
		 https://serveraddress/ldaprestapi/api/v1/greet
	  Example (use GET): https://localhost:8080/ldaprestapi/api/v1/greet
	  Example (use GET): https://localhost:8080/ldaprestapi/api/v1/greet?name=John Doe
	  
	  * This call could be used for a simple live-check of the server RESTAPI
	
This implementation is not final and could be changed in the next versions.
=============================================================================
ToDo:

	1) Improve create user object feature - already implemented, may need some improvements;
	2) Implement change user object feature - Modification implemented;
	3) Implement security support (to be discussed);
	4) Implement better validation (for account creation, modification etc);
	5) add logging support (added "spring-boot-starter-logging" dependency, and this is using SLF4J logger (Regardless of the underlying framework));
	6) Add external configuration file


Updates:
 	removed import library: jdk.internal.org.jline.utils.Log