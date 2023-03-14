# Infrastructure as code with Terraform sdrgtfyhijlk
-Update Terraform template for application to add DB Security, S3 bucketUse Amazon Linux 2 as source image to create a private AMI in dev AWS account using Packer.
-Install MySQL locally in AMI and AMI builds should be set up to run in your default VPC.
-The packer template should be stored in the same repo as the web application.

### DB Security Group
1. Create an EC2 security group for RDS instances and add ingress rule to allow TCP traffic on the port 3306 for MySQL.
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

### Packer commands to initiate AWS AMI and EC2
1. packer fmt -recursive .
2. packer validate -var-file=creds.auto.pkrvars.hcl ami-packer.pkr.hcl
3. packer build -var-file=creds.auto.pkrvars.hcl ami-packer.pkr.hcl
