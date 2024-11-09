data "aws_route53_zone" "rsprox-net" {
  name         = var.root_domain
  private_zone = false
}

resource "aws_route53_record" "cdn" {
  zone_id = data.aws_route53_zone.rsprox-net.zone_id
  name    = var.cdn_domain
  type    = "CNAME"
  ttl     = 300
  records = [aws_cloudfront_distribution.s3_distribution.domain_name]
}
