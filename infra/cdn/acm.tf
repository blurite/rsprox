data "aws_acm_certificate" "cdn" {
  domain      = var.cdn_domain
  types       = ["AMAZON_ISSUED"]
  most_recent = true
  provider    = aws.us-east-1
}
