**REST API Services for ldap users manipulation**

Current version requires the following setup:

LDAP server configuration must be placed into: /src/main/resources/application.properties

	ldap.partitionSuffix=
	ldap.partition=
	ldap.principal=
	ldap.password=
	ldap.port=
	ldap.url=
	ldap.usersFullpath=
	ldap.usersOU=
	ldap.orgLocal=

Replace the values according to your LDAP server access credentials, 
DO NOT FORGET to provide a password for a superuser account (Manager in this example)!
=============================================================================

**How to use the REST API services:**

	0) Authentication (including following Authorization) with so called "call-back":
		Starting from version 0.6.x added a requirements for every REST Call to have a header like:
		name: authorization 
		value: institution-hash-key
		
			-- where: 
			<authorization> - is a name of the token, literally authorization 
							(also accepting: authentication, institution_hash);
			<institution-hash-key> - is a secret key saved in the central server.
			
			the key must have expiration date (this is validated by central server)
			Without this header no other rest call will be accepted, but the error returned: BAD_REQUEST
		

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
				d) https://localhost:8080/ldaprestapi/api/v2/search?name=John Doe&mail=johndoe@planetexpress.com
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
				            "title"	: "Employee at the SarCourt mall",
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
				"title"				: "Employee at the SarCourt mall",
				"businessCategory" 	: "code",
				"employeeType" 		: "1",
				"employeeNumber" 	: "2",
				"departmentNumber" 	: "3"
			}
		
		v2: Create with group member (gruop support - groups as array of strings):
		
		2.1) Rest Call (POST) to create an account and make it a member of the group (supply in JSON format, group attribute is: "groupMember" ):
				http://localhost:8080/api/v2/create
				Example (place user in a group):
				{
					"username" 			: "John Doe" ,
					"givenName" 		: "John",
					"sn" 				:  "Doe",
					"password" 			:  "johndoe",
					"uid" 				: "johndoe",
					"mail" 				: "john.doe@hawkins.com",
					"title"				: "Employee at the SarCourt mall",
					"businessCategory" 	:  "code",
					"employeeType" 		: "1",
					"employeeNumber" 	: "2",
					"departmentNumber" 	: "3"
					"groupMember" 		:  ["students"]
				}
				Example (make user a member of more than one group):
				{
					"username" 			: "John Doe" ,
					"givenName" 		: "John",
					"sn" 				:  "Doe",
					"password" 			:  "johndoe",
					"uid" 				: "johndoe",
					"mail" 				: "john.doe@hawkins.com",
					"title"				: "Employee at the SarCourt mall",
					"businessCategory" 	:  "code",
					"employeeType" 		: "1",
					"employeeNumber" 	: "2",
					"departmentNumber" 	: "3"
					"groupMember" 		:  ["students","teachers"]
				}
		
		
		v3: Create with group member and get JSON with detailed results (gruop support - groups as array of strings):
		
		2.3) Rest Call (POST) to create an account and make it a member of the group (supply in JSON format, group attribute is: "groupMember" ):
				http://localhost:8080/api/v3/create
				Example (make user a member of more than one group):
				{
					"username" 			: "John Doe" ,
					"givenName" 		: "John",
					"sn" 				:  "Doe",
					"password" 			:  "johndoe",
					"uid" 				: "johndoe",
					"mail" 				: "john.doe@hawkins.com",
					"title"				: "Employee at the SarCourt mall",
					"businessCategory" 	:  "code",
					"employeeType" 		: "1",
					"employeeNumber" 	: "2",
					"departmentNumber" 	: "3"
					"groupMember" 		:  ["students","teachers"]
				}
		
		2.4) Rest Call (POST) to create multiple accounts, with the group membership, same as create one user (supply in JSON format, group attribute is: "groupMember" ):
				http://localhost:8080/api/v3/createusers
				Example (make user a member of more than one group):
				[
					{
						"username" 			: "John Doe" ,
						"givenName" 		: "John",
						"sn" 				:  "Doe",
						"password" 			:  "johndoe",
						"uid" 				: "johndoe",
						"mail" 				: "john.doe@hawkins.com",
						"title"				: "Employee at the SarCourt mall",
						"businessCategory" 	:  "code",
						"employeeType" 		: "1",
						"employeeNumber" 	: "2",
						"departmentNumber" 	: "3"
						"groupMember" 		:  ["students","teachers"]
					},
					{
						"username" 			: "Jane Doe" ,
						"givenName" 		: "Jane",
						"sn" 				:  "Doe",
						"password" 			:  "janedoe",
						"uid" 				: "janedoe",
						"mail" 				: "jane.doe@hawkins.com",
						"title"				: "Employee at the SarCourt mall",
						"businessCategory" 	:  "code",
						"employeeType" 		: "1",
						"employeeNumber" 	: "2",
						"departmentNumber" 	: "3"
						"groupMember" 		:  ["students","teachers"]
					}
				]
				
			*Note: The reply status could be:
			==  [200] OK - when all accounts created and all group membership changed without any warnings or errors
			==  [206] PARTIAL - when some account were not created (already existing0 or some issues with Groups (warnings)
			==  [400] BAD REQUEST - failed operation
		
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
				"title"				: "Employee at the SarCourt mall",
				"businessCategory" 	:  "code",
				"employeeType" 		: "1",
				"employeeNumber" 	: "2",
				"departmentNumber" 	: "3",
				"groupMember" 		: ["students"]
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
					"title"				: "Employee at the SarCourt mall",
					"businessCategory" 	:  "code",
					"employeeType" 		: "1",
					"employeeNumber" 	: "2",
					"departmentNumber" 	: "3",
					"groupMember" 		: ["students"]
				}
			
		**Please note: You may specify "groupMember", so if this is a new group for a user, user account to be added to the group (member/memberOf props),
			However user will not be removed from any other groups!!! 
			
		V3: modify - with extended JSON response:			
		3.2) Rest call (PUT) to modify ldap account: This is to modify most of attributes (except UID)
			 https://serveraddress/ldaprestapi/api/v3/modify
			 PUT Load (JSON) example:
				 {
					"username" 			: "John Junior Doe" ,
					"givenName" 		: "John Junior",
					"sn" 				:  "Doe",
					"password" 			:  "johndoe!",
					"uid" 				: "johndoe",
					"mail" 				: "john.doe@hawkins.com",
					"title"				: "Employee at the SarCourt mall",
					"businessCategory" 	:  "code",
					"employeeType" 		: "1",
					"employeeNumber" 	: "2",
					"departmentNumber" 	: "3",
					"groupMember" 		: ["students"]
				}			
				
				You get a responce:
					{
					  "data": [
					    {
					      "uid": "johndoe",
					      "status": "OK",
					      "messages": [
					        {
					          "name": "ship_crew",
					          "status": true,
					          "messageString": "Success"
					        }
					      ]
					    }
					  ]
					}
				
		* Please make sure you do not send empty values as those will be skipped from the change.
		
		3.3) Rest call (PATCH) to modify ldap account's properties: This is to modify one/two attribute(s) like givenName and sn
			 https://serveraddress/ldaprestapi/api/v2/modifyname
				 PATCH Load (JSON) example:
				 {
					"givenName" 		: "John Junior",
					"sn" 				:  "Doe",
					"uid" 				: "johndoe"
				}
	
		  		
		3.4) Rest call (PATCH) to modify ldap account's password: This is to modify the password for an account
			 https://serveraddress/ldaprestapi/api/v2/modifypassword
				 PATCH Load (JSON) example:
				 {
					"uid" 				: "johndoe",
					"password" 			: "johnsPassword"
				}
	
	4) Rest call (POST) to delete and account:
		https://serveraddress/ldaprestapi/ldaprestapi/api/v1/delete
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
				http://localhost:8080/ldaprestapi/api/v2/delete?cn=John Doe&uid=johndoe
			
			* This delete method usage looks similar to GET (but in any case it is using DELETE controller)
	
	
	5) Rest call (PATCH) to Remove member from the group:
	   As a payload please supply simple JSON object with two fileds: "uid" and "groupMember"
			 https://serveraddress/ldaprestapi/api//v1/removemember
				 PATCH Load (JSON) example:
				 {
						"uid" : "johndoe",
						"groupMember" :  ["students"]
				 }	 
	
	
	6) Simple rest call (GET) to receive some greetings:
		 https://serveraddress/ldaprestapi/api/v1/greet
	  Example (use GET): https://localhost:8080/ldaprestapi/api/v1/greet
	  Example (use GET): https://localhost:8080/ldaprestapi/api/v1/greet?name=John Doe
	  
	  * This call could be used for a simple live-check of the server RESTAPI
	
	7) Simple rest call (GET) to receive application version:
		 https://serveraddress/ldaprestapi/api/v1/status
	  Example (use GET): https://localhost:8080/ldaprestapi/api/v1/status
	  Response (JSON):
			{
			    "data": {
			        "version": "0.4.19"
			    }
			}
			  
	  * This call could be used for a simple live-check of the server RESTAPI
	

* Please Note:
	Password is encoded with CRYPT-SSHA-512 (Seeded salted SHA, using Crypt Apache library)!
	
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