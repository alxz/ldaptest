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
				https://serveraddress/api/v1/searchuid
			 Example (use GET): 
					https://localhost:8080/api/v1/searchuid?uid=mike
			
		1.2) Search by user ldap attribute cn (usually combined of first name + last name): 		
				https://serveraddress/api/v1/search
			 Example (use GET):
				https://localhost:8080/api/v1/search?name=John Doe
		
		1.3) Search by user ldap attribute mail (email): 		
				https://serveraddress/api/v1/searchmail
			 Example (use GET):
				https://localhost:8080/api/v1/searchmail?name=john.doe@hawkins.com
								
		1.4) Search by either user ldap attributes UID/CN/First Name/Surname And/Or eMail (uid/cn/givenName/sn/mail): 		
				https://serveraddress/api/v2/search
			 Example (use GET):
			  search by uid (and cn/mail):
				a)  https://localhost:8080/api/v2/search?uid=johndoe
					https://localhost:8080/api/v2/search?uid=johndoe&name=John					
					https://localhost:8080/api/v2/search?uid=johndoe&mail=joghdoe@planetexpress.com
					https://localhost:8080/api/v2/search?uid=johndoe&name=John&mail=joghdoe@planetexpress.com
				search by name(s):
				b) https://localhost:8080/api/v2/search?name=John Doe
					https://localhost:8080/api/v2/search?name=John	
					https://localhost:8080/api/v2/search?name=Doe	
			  search by name(s) and email:
				d) https://localhost:8080/api/v2/search?name=John Doe&mail=joghdoe@planetexpress.com
			  search by mail only:
				e) https://localhost:8080/api/v2/search?mail=joghdoe@planetexpress.com								

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
		
		v2: Bulk create (in-progress):
		2.1) Rest Call (POST) to create multiple accounts (supply in JSON format, i.e. JSON array [{x},[y],..,{z}]):
				http://localhost:8080/api/v2/createusers
				
		
	3) Rest call (POST) to modify ldap account:
	   https://serveraddress/api/v1/modify
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
				"departmentNumber" 	: "3"
			}
	
	* To comply with REST best practices it has been extended with the following:
	
		3.1) Rest call (PUT) to modify ldap account: This is to modify most of attributes (except UID)
			 https://serveraddress/api/v2/modify
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
					"departmentNumber" 	: "3"
				}
			** value username - is simply the combination of givenName and sn.. and it is in ldap called cn (very important attribute anyway) 
		
		3.2) Rest call (PATCH) to modify ldap account's properties: This is to modify one/two attribute(s) like givenName and sn
			 https://serveraddress/api/v2/modifyname
				 PATCH Load (JSON) example:
				 {
					"givenName" 		: "John Junior",
					"sn" 				:  "Doe",
					"uid" 				: "johndoe"
				}
	
		  
	
	4) Rest call (POST) to delete and account:
		https://serveraddress/api/v1/delete
		Must provide both 'uid' and 'cn' properties! (as simple protection).
		Also make sure cn is exactly as ldap object cn (you can use searchuid REST call to get cn).
		
		 Delete calls POST (JSON) example:
			{
				"cn" : "John Doe"
				"uid" : "johndoe"
			}
	
	* To comply with REST best practices it has been extended with the following:
	
		4.1) Rest call (DELETE) to delete an account: https://serveraddress/api/v2/delete
			 Must provide both 'uid' and 'cn' properties!.
			 Example:
				http://localhost:8080/api/v2/delete?cn=John Doe&uid=johndoe
			
			* This delete method usage looks similar to GET (but in any case it is using DELETE controller)
	
	
	
	5) Simple rest call (GET) to receive some greetings:
		 https://serveraddress/api/v1/greet
	  Example (use GET): https://localhost:8080/api/v1/greet
	  Example (use GET): https://localhost:8080/api/v1/greet?name=John Doe
	  
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


