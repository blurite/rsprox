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

resource "aws_s3_bucket_ownership_controls" "cdn" {
  bucket = aws_s3_bucket.bucket_origin.id
  rule {
    object_ownership = "ObjectWriter"
  }
}

resource "aws_s3_bucket_public_access_block" "cdn" {
  bucket = aws_s3_bucket.bucket_origin.id

  block_public_acls       = false
  block_public_policy     = false
  ignore_public_acls      = false
  restrict_public_buckets = false
}

resource "aws_s3_bucket_acl" "cdn" {
  depends_on = [
    aws_s3_bucket_ownership_controls.cdn,
    aws_s3_bucket_public_access_block.cdn,
  ]

  bucket = aws_s3_bucket.bucket_origin.id
  acl    = "public-read"
}
