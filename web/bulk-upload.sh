#!/bin/bash

folder_path="./upload/"
upload_url="https://rsprox.net/api/submit"

for file in "$folder_path"/*; do
  if [ -f "$file" ]; then
    echo "Uploading $file..."

    curl -X POST "$upload_url" \
      -F "file=@$file" \
      -F "delayed=false" \
      -H "Content-Type: multipart/form-data"

    echo "Uploaded $file."
  fi
done

echo "All files uploaded."
