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
		(v1 is considered obsolete):
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
			
			Example:   https://localhost:8080/ldaprestapi/apiv2/search?mail=*planetexpress.com
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
			Example:  https://localhost:8080/ldaprestapi/apiv3/search?searchstring=*planetexpress.com
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

	1.7) Search with V4: There is only one required parameter called 'searchstring' 
			Example: http://localhost:8080/api/v4/search?searchstring=jdoe0001
			- This search also returns a membership information using "memberOf" attribute of the user account...
	
	1.8) Search with V5: There is only one required parameter called 'searchstring' 
			Example: http://localhost:8080/api/v5/search?searchstring=jdoe001
			- This search also returns a membership information using "member" attribute of the group that has this account as member... 
			*** - this version is specifically for those 'misconfigured' version of LDAP 
					where user account may not have 'memberOf' attribute even if it is a member of a group/
			
	1.9) Search with V6: Almost the same as V4/V5 with parameter called 'searchstring'.
		This search V6 is used without filtering by ldap objectClass 'person', however there still some validation:
		an object is not added to the search result output if it is not one of the following: 
					objectClass: person
					objectClass: organizationalPerson
					objectClass: inetOrgPerson
		Also added a count of found objects to the search result output:
			Example (for empty result, non empty should have the same structure):
					{
					    "data": [],
					    "count": 0
					}
	1.91) Search with V8: as previous (V6) with parameter called 'searchstring'. 
		(- still in progress: sometimes first search could not find an objects in AD, next try it works -)
		This search V8 is searching with basic base DN, searching also in all referred OUs starting from the LDAP RootDSE...
		Also requires the follwoing parameters in the application.properties file:
			ldap.adproxyORG=proxy
			ldap.muhcadOU=muhca
			ldap.muhcproxy=ou=muhcad,o=proxy	
				
		Validation:
		an object is not added to the search result output if it is not one of the following: 
					objectClass: person
					objectClass: organizationalPerson
					objectClass: inetOrgPerson
		Also added a count of found objects to the search result output:
			Example (for empty result, non empty should have the same structure):
					{
					    "data": [],
					    "count": 0
					}
						
	2) Rest Call (POST) to create a user account in LDAP:
		(obsolete)	http://serveraddress/api/v1/create
		
		v2: Create with group member (gruop support - groups as array of strings):
		
		2.1) Rest Call (POST) to create an account and make it a member of the group (supply in JSON format, group attribute is: "groupMember" ):
			(obsolete)	https://localhost:8080/ldaprestapi/apiv2/create
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
		
		** Please note:
		As of v.0.6.8: Once there is a duplicate in CN found (same user first and Last Name), we check for UID duplication for this account also,
						if the UID is still unique there will be an attempt to create a user with suggested First Name and Last Name extended with user UID:
						Example: 
							if John Doe is already existing account and its ldap uid = johndoe;
							Given a new user with the same name and uid=jdoe0001 we create a user with CN like: "John Doe (jdoe0001)"  
		
		2.3) Rest Call (POST) to create an account and make it a member of the group (supply in JSON format, group attribute is: "groupMember" ):
				https://localhost:8080/ldaprestapi/apiv3/create
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
				https://localhost:8080/ldaprestapi/apiv3/createusers
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
	   (obsolete) https://serveraddress/ldaprestapi/api/v1/modify
	   Do not try to modify property 'uid' - this is the only identified unique for the ldap 	   
	
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


Updates:
 	removed import library: jdk.internal.org.jline.utils.Log
 	
Search since V3 uses the following approach:
	LdapQueryBuilder.query()....
						.and(LdapQueryBuilder.query().where("cn").like(queryStr)
						    					.or("uid").like(queryStr)
						    					.or("givenName").like(queryStr)
						    					.or("sn").like(queryStr)
						    					.or("mail").like(queryStr) 	

Why memberOf may not work:
	1) If you have the memberof overlay configured and the users were added to their groups 
		after memberof was configured, memberOf will work. If it doesn't, one of those 
		conditions isn't true. In particular, memberof isn't retrospective.

	2) If those conditions don't or can't hold, you will need to conduct a subtree search 
		starting from the groups subtree, using a filter like uniqueMember={0} (or member)
		where the parameter is provided as the DN of the user.
=============================================================================
