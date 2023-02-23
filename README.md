## Building Custom Application AMI using Packer
-Use Amazon Linux 2 as source image to create a private AMI in dev AWS account using Packer.
-Install MySQL locally in AMI and AMI builds should be set up to run in your default VPC.
-The packer template should be stored in the same repo as the web application.

## Continuous Integration
**When a pull request is merged, a GitHub Actions workflow should be triggered to do the following:**
1.Run the unit test to validate Packer.
2.Build the application artifact (war, jar, zip, etc.).
3.Build the AMI with application dependencies and configure the application to start automatically when VM is launched.
4.AMI template should be validated in the pull request status check.
5.The application artifact is built for copying to AMI and AMI is built when PR is merged.
6.AMI is automatically shared with the DEMO account. The AWS account id is provided in the Packer template.
 
## App Security Group
Create an EC2 security group for your EC2 instances that will host web applications.
Add ingress rule to allow TCP traffic on ports `22`, `80`, `443`, and `port` on which your application runs from anywhere in the world.

## EC2 Instance
Create an EC2 instance with the following specifications. For any parameter not provided in the table below, you may go with default values. The EC2 instance should belong to the VPC you have created.

**Parameter Value**
Amazon Machine Image (AMI)             : 	Your custom AMI
Instance Type                          : 	t2.micro
Protect against accidental termination :	No
Root Volume Size                       : 	50
Root Volume Type                       :	General Purpose SSD (GP2)
 
### Deploying Application by Launching the AMI
To demo a properly built AMI, launch the EC2 instance with the custom AMI using the Terraform template.
The application should work when the EC2 instance is in a "running" state.
