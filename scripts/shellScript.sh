#Shell Script For Installing Everything
#Shell Script For Installing Everything

#Update the base image
sudo yum update -y
sudo yum upgrade -y

#Install Apache Http Server
sudo yum install httpd -y

#Install Java
sudo rpm --import https://yum.corretto.aws/corretto.key
sudo curl -L -o /etc/yum.repos.d/corretto.repo https://yum.corretto.aws/corretto.repo
sudo yum install -y java-19-amazon-corretto-devel

#Install Maven
# cd /opt
# sudo wget https://downloads.apache.org/maven/maven-3/3.8.7/binaries/apache-maven-3.8.7-bin.tar.gz
# sudo tar xzf apache-maven-3.8.7-bin.tar.gz
# sudo ln -s apache-maven-3.8.7 maven

# sudo touch /etc/profile.d/maven.sh
# sudo sh -c "echo 'M2_HOME=/opt/apache-maven-3.8.7' >> /etc/profile.d/maven.sh"
# sudo sh -c "echo 'PATH=\"\$M2_HOME/bin:\$PATH\"' >> /etc/profile.d/maven.sh"
# sudo sh -c "echo 'export PATH' >> /etc/profile.d/maven.sh"
# source /etc/profile.d/maven.sh
# mvn -version

#Install MySql Client
yes | sudo yum install mysql

# sudo amazon-linux-extras install epel -y
# yes | sudo yum install https://dev.mysql.com/get/mysql80-community-release-el7-5.noarch.rpm
# yes | sudo yum install mysql-community-server -y

# sudo systemctl start mysqld
# sudo systemctl status mysqld

# DEFAULT_PASS=$(sudo cat ../../var/log/mysqld.log | grep "A temporary password" | awk '{print $13}')

# yes | sudo yum install expect

# SECURE_MYSQL=$(expect -c "

#   set timeout 10
#   spawn sudo mysql -u root -p

#   expect \"Enter password:\"
#   send \"$DEFAULT_PASS\r\"

#   expect \"mysql>\"
#   send \"ALTER USER 'root'@'localhost' IDENTIFIED BY 'Abcde@12345';\r\"

#   expect \"mysql>\"
#   send \"CREATE DATABASE webapp2;\r\"

#   expect \"mysql>\"
#   send \"exit\r\"

#   expect eof
#   ")

# echo "$SECURE_MYSQL"

#Write Daemon file
sudo touch /etc/systemd/system/app1.service
sudo sh -c "echo '[Unit]' >> /etc/systemd/system/app2.service"
sudo sh -c "echo 'Description=Spring init sample' >> /etc/systemd/system/app2.service"
sudo sh -c "echo 'After=syslog.target' >> /etc/systemd/system/app2.service"

sudo sh -c "echo '[Service]' >> /etc/systemd/system/app2.service"
sudo sh -c "echo 'User=ec2-user' >> /etc/systemd/system/app2.service"
sudo sh -c "echo 'EnvironmentFile=/etc/systemd/system/service.env' >> /etc/systemd/system/app2.service"
sudo sh -c "echo 'SuccessExitStatus=143' >> /etc/systemd/system/app2.service"
sudo sh -c "echo 'Restart=always' >> /etc/systemd/system/app2.service"
sudo sh -c "echo 'RestartSec=30s' >> /etc/systemd/system/app2.service"
sudo sh -c "echo 'StandardOutput=syslog' >> /etc/systemd/system/app2.service"
sudo sh -c "echo 'StandardError=syslog' >> /etc/systemd/system/app2.service"
sudo sh -c "echo 'ExecStart=/usr/bin/java -jar /tmp/webapp-0.0.1-SNAPSHOT.jar' >> /etc/systemd/system/app2.service"

sudo sh -c "echo '[Install]' >> /etc/systemd/system/app2.service"
sudo sh -c "echo 'WantedBy=multi-user.target' >> /etc/systemd/system/app2.service"

#sudo systemctl start app1.service
#sudo systemctl enable app1.service
