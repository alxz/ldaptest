REST API Services for ldap users manipulation

Current version requires the following setup:

LDAP server configuration must be placed into: /src/main/resources/application.properties

	ldap.partitionSuffix=dc=planetexpress,dc=com
	ldap.partition=planetexpress
	ldap.principal=cn=admin,dc=planetexpress,dc=com
	ldap.password=GoodNewsEveryone
	ldap.port=10389
	ldap.url=ldap://127.0.0.1:10389

Replace the values according to your LDAP server access credentials!

How to use the REST API services:

	1) Rest Call (GET) to search for a user account in LDAP: 
		http://serveraddresss/api/v1/searchuid
		
		GET request:
		uid=<String uid>

	2) Rest Call (POST) to create a user account in LDAP: 
		http://serveraddresss/api/v1/create
		
		POST Load:
		username=<String value>,givenname=<String value>,sn=<String value>,
		password=<String value>,uid=<String value>,mail=<String value>,
		businessCategory=<String value>,employeeType=<String value>,
		employeeNumber=<String value>,departmentNumber=<String value>
		
	3) Rest call (POST) to modify ldap account:
	   http://serveraddresss/api/v1/modify
	   
	   POST Load:
	   
	   	username=<String value>,givenname=<String value>,sn=<String value>,
		password=<String value>,uid=<String value>,mail=<String value>,
		businessCategory=<String value>,employeeType=<String value>,
		employeeNumber=<String value>,departmentNumber=<String value>
	
This implementation is not final and could be changed in the next versions.
=============================================================================
ToDo:

	1) Improve create user object feature;
	2) Implement change user object feature - Modification implemented;
	3) Implement security support (to be discussed);
	4) Implement better validation (for account creation, modification etc);


