variable "ami_id" {
  type    = string
  default = "ami-0dfcb1ef8550277af"
}

variable "jar_source" {
  type    = string
  default = "./target/webapp-0.0.1-SNAPSHOT.jar"
}

variable "config_source" {
  type    = string
  default = "./scripts/app2.service"
}

variable "app_name" {
  type    = string
  default = "webapp"
}

variable "instance_type" {
  type    = string
  default = "t2.micro"
}

variable "region" {
  type    = string
  default = "us-east-1"
}

variable "ssh_username" {
  type    = string
  default = "ec2-user"
}

variable "env" {
  type    = string
  default = "dev"
}

variable "ami_users" {
  type    = list(string)
  default = []
}

variable "delete_on_termination" {
  type    = bool
  default = true
}

variable "volume_size" {
  type    = number
  default = 50
}

variable "volume_type" {
  type    = string
  default = "gp2"
}

variable "access_key" {
  type    = string
  // default = true
}

variable "secret_key" {
  type    = string
  // default = true
}

locals {
  timestamp_val = formatdate("YYYYMMDDhhmmss", timestamp())
}

source "amazon-ebs" "nainil_ami" {
  ami_name      = "packer-${var.app_name}-${local.timestamp_val}"
  instance_type = "${var.instance_type}"
  region        = "${var.region}"
  source_ami    = "${var.ami_id}"
  ssh_username  = "${var.ssh_username}"
  ssh_timeout   = "20m"
  access_key    = "${var.access_key}"
  secret_key    = "${var.secret_key}"

  ami_users = var.ami_users

  tags = {
    Env  = "${var.env}"
    Name = "packer-${var.app_name}-${local.timestamp_val}"
  }

  ssh_agent_auth = false
  launch_block_device_mappings {
    delete_on_termination = "${var.delete_on_termination}"
    device_name           = "/dev/xvda"
    volume_size           = var.volume_size
    volume_type           = "${var.volume_type}"
  }
}

build {

  sources = ["source.amazon-ebs.nainil_ami"]

  provisioner "file" {
    source      = "${var.jar_source}"
    destination = "/tmp/"
  }

  provisioner "shell" {
    script = "./scripts/shellScript.sh"
  }

  post-processor "manifest" {
    output     = "manifest.json"
    strip_path = true
  }
}