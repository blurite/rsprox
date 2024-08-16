resource "aws_s3_bucket" "bucket_origin" {
  bucket = var.cdn_domain

  tags = {
    Name      = var.cdn_domain
    ManagedBy = "terraform"
  }
}

resource "aws_s3_bucket_policy" "allow_access_from_another_account" {
  bucket = aws_s3_bucket.bucket_origin.id
  policy = data.aws_iam_policy_document.allow_cloudfront.json
}

data "aws_iam_policy_document" "allow_cloudfront" {
  statement {
    principals {
      type        = "AWS"
      identifiers = [aws_cloudfront_origin_access_identity.cdn_bucket_oai.iam_arn]
    }

    actions = [
      "s3:GetObject"
    ]

    resources = [
      aws_s3_bucket.bucket_origin.arn,
      "${aws_s3_bucket.bucket_origin.arn}/*",
    ]
  }
}
