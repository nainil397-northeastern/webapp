# webapp
 Web App for Cloud Course - Nainil Maladkar

 This Web Application is able to create new account to mySQL database and can read from and update based on authentications and access.
Further We Application makes use of CI workflow under github Actions.

Associated Tech:

Framework: Spring Boot 
Database: MySQL
API Platform: Postman


Spring supported libraries:

Spring Web
Spring Data JPA
JDBC API
MySQL Driver
Spring Security
H2 Database (used for test) 


Prerequisites for local -
* Creating a .gitignore file using the ready template while creating the repository. In my case I used the Java template
* Write the workflow in .github/worflows to be executed before merging the pull request to organization/main
* Set the required branch protections in organization/main to avoid merging of pull request if the PR fails

Build and Deploy instructions for the web application -

* Build application using - mvn clean install
* Deploy the application to organization repository and run the tests using workflow - mvn test

Git commands executed for initalizing web application over branch and main organization

* List the remotes -- git remote -v
* Remove the remotes -- git remote remove
* Add new remote for forked repo -- git remote add ameya sshForForked
* Add new remote for organization repo (upstream)-- git remote add nainil sshForOrganization
* Create a branch -- git checkout -b branchName
* Add all files to staging -- git add .
* Add a commit message -- git commit -m ""
* push the local feature branch to the feature branch in forked repository -- git push nainil featureBranchName
* Raise a pull request from featureBranch in forked repository to organization/main

REST API implemented --
* GET - v1/user/{userId} -- for retrieving user account information
* PUT - v1/user/{userId} -- for updating user account information
* POST - v1/user -- for creating a new user account
* GET - healthz -- simple health check

