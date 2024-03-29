# Infrastructure as code with Terraform using DNS setup 


* Update Terraform template for application to initiate webapp on `domainname`, add DB Security, S3 bucketUse Amazon Linux 2 as source image to create a private AMI in dev AWS account using Packer.
* Install MySQL locally in AMI and AMI builds should be set up to run in your default VPC.
* The packer template should be stored in the same repo as the web application.

### DB Security Group
1. Create an EC2 security group for RDS instances and add ingress rule to allow TCP traffic on the `port 3306` for MySQL.
2. Source of the traffic is `application security group` and restrict access to the instance from the internet, this security group is referred as `database security group`

### S3 Bucket
1. Create a private S3 bucket with a randomly generated bucket name depending on the environment and enable default encryption for S3 BucketsLinks to an external site.
2. Create a lifecycle policy for the bucket to transition objects from `STANDARD storage class` to `STANDARD_IA` storage class after 30 days.
 
### RDS Instance
1. Create `DB parameter group` to match your database MySQL and its version. Then RDS DB instance must use the new parameter group and not the default parameter group.
2. `Database security group` should be attached to RDS instance, which should be created with the following configuration. 
```
Property        	    Value
Database Engine	        MySQL
DB Instance Class	    db.t3.micro
Multi-AZ deployment	    No
DB instance identifier	csye6225
Master username	        csye6225
Subnet group	        Private subnet for RDS instances
Public accessibility	No
Database name	        csye6225
```

### User Data
1. EC2 instance should be launched with user dataLinks to an external site with `Database username`, `password`, `hostname`, and `S3 bucket name` 
2. The S3 bucket name must be passed to the application via EC2 user data.

### IAM Policy and Role
1. WebAppS3 policy will allow EC2 instances to perform S3 buckets. This is required for applications for EC2 instance to talk to the S3 bucket.
2. Create an IAM role `EC2-CSYE6225` for the EC2 service and attach the WebAppS3 policy to it. 

### Application Log data in CloudWatch
1. All application log data are made available in AWS CloudWatch.
2. Metrics on API usage available in CloudWatch with custom metric data for every API
3. A counter is present to track API calls and `StatsD` is used for custom metrics

### Create Load Balancer Security Group
1. Create a security group for the load balancer to access the web application.
2. Add ingress rule to allow TCP traffic on ports `80`, and `443` from anywhere in the world.
3. This security group will be referred to as the load balancer security group.

### App Security Group Updates
1. Update the EC2 security group for your EC2 instances that will host web applications.
2. The ingress rule should allow TCP traffic on ports 22 and port on which your application runs.
3. The Source of the traffic should be the load balancer security group.
4. Restrict access to the instance from the internet.

### The Auto Scaling Application Stack
1. Web application has been accessible by the IP address of the EC2 instance on HTTP protocol. 
2. Disable direct access to our web application using the IP address of the EC2 instance.
3. The web application will now only be accessible from the load balancer.

### Setup Application Load Balancer For Your Web Application
1. Set up an Application load balancer to accept HTTPS traffic on `port 443`
2. Attach the load balancer security group to the load balancer.
3. Verify the certificate is stored in AWS Certificate Manage.

### Secure Application Endpoints
1. Secure web application endpoints with valid SSL certificates.
2. For `dev` environment, use the AWS Certificate Manager to get SSL certificates.
3. For `demo` environment,an SSL certificate from Namecheap is used
```
aws acm import-certificate --certificate fileb://D:/NEU\ MIS\ course\ first\ year/Cloud\ Computing/demo_nainilmaladkar_me/demo_nainilmaladkar_me.crt --certificate-chain fileb://D:/NEU\ MIS\ course\ first\ year/Cloud\ Computing/demo_nainilmaladkar_me/demo_nainilmaladkar_me.ca-bundle --private-key fileb://D:/NEU\ MIS\ course\ first\ year/Cloud\ Computing/demo_nainilmaladkar_me/private-key.pem --profile=demo
```
<br> Log in to the AWS Certificate Manager console. </br>
<br> Choose the “Import a certificate” option. </br>
<br> A similar form for the SSL upload will open. </br>
<br> Paste the certificate file code as the “Certificate body”. </br>
<br> Paste the CA-bundle code as the “Certificate chain”. </br>
<br> Paste the Private key. </br>
<br> Save the changes by selecting “Review and Import” </br>


### Encrypted EBS Volumes
1. All EC2 instances are launched with encrypted EBS volumes.
2. EBS volumes is encrypted with Customer managed key created as part of Terraform template.
### Encrypted RDS Instances
1. RDS instances is encrypted with (a separate) Customer managed key created as part of Terraform.

### Packer commands to initiate AWS AMI and EC2
1. packer fmt -recursive .
2. packer validate -var-file=creds.auto.pkrvars.hcl ami-packer.pkr.hcl
3. packer build -var-file=creds.auto.pkrvars.hcl ami-packer.pkr.hcl

