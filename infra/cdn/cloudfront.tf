resource "aws_cloudfront_origin_access_identity" "cdn_bucket_oai" {
  comment = var.cdn_domain
}

resource "aws_cloudfront_distribution" "s3_distribution" {
  origin {
    domain_name = aws_s3_bucket.bucket_origin.bucket_regional_domain_name
    origin_id   = aws_s3_bucket.bucket_origin.bucket_regional_domain_name

    s3_origin_config {
      origin_access_identity = aws_cloudfront_origin_access_identity.cdn_bucket_oai.cloudfront_access_identity_path
    }
  }

  enabled         = true
  is_ipv6_enabled = true

  aliases = [var.cdn_domain]

  default_cache_behavior {
    allowed_methods  = ["HEAD", "GET"]
    cached_methods   = ["HEAD", "GET"]
    target_origin_id = aws_s3_bucket.bucket_origin.bucket_regional_domain_name

    forwarded_values {
      query_string = false

      cookies {
        forward = "none"
      }
    }

    viewer_protocol_policy = "redirect-to-https"
    min_ttl                = 0
    default_ttl            = 3600
    max_ttl                = 86400
  }

  price_class = "PriceClass_All"

  restrictions {
    geo_restriction {
      restriction_type = "none"
    }
  }

  tags = {
    ManagedBy = "terraform"
  }

  viewer_certificate {
    acm_certificate_arn = data.aws_acm_certificate.cdn.arn
    ssl_support_method  = "sni-only"
  }

  depends_on = [data.aws_acm_certificate.cdn]
}
