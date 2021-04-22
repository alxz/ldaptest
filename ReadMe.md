REST API Services for ldap users manipulation

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

How to use the REST API services:

	1) Rest Call (GET) to search for a user account in LDAP: 
		http://serveraddresss/api/v1/searchuid
				
		Example:
			http://localhost:8080/api/v1/searchuser?uid=mike

	2) Rest Call (POST) to create a user account in LDAP: 
		http://serveraddresss/api/v1/create
		
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
		
	3) Rest call (POST) to modify ldap account:
	   http://serveraddresss/api/v1/modify
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
	
	4) Rest call (POST) to delete and account:
		http://serveraddresss/api/v1/delete
		Must provide both 'uid' and 'cn' properties! (as simple protection).
		Also make sure cn is exactly as ldap object cn (you can use searchuid REST call to get cn).
		
		 Delete call  POST (JSON) example:
			{
				"cn" : "John Doe"
				"uid" : "johndoe"
			}
	
This implementation is not final and could be changed in the next versions.
=============================================================================
ToDo:

	1) Improve create user object feature - already implemented, may need some improvements;
	2) Implement change user object feature - Modification implemented;
	3) Implement security support (to be discussed);
	4) Implement better validation (for account creation, modification etc);
	5) add logging support;
	6) Add external configuration file


