terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.62.0"
    }
  }

  backend "s3" {
    bucket  = "rsprox-tf-state"
    key     = "cdn.tfstate"
    region  = "eu-west-1"
    profile = "rsprox"
  }

  required_version = ">= 1.9.0"
}

provider "aws" {
  profile = "rsprox"
  region  = "eu-west-1"
}

provider "aws" {
  profile = "rsprox"
  alias   = "us-east-1"
  region  = "us-east-1"
}
