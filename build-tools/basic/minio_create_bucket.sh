#!/bin/bash

URL=onmshs.local
PORT=9443
USERNAME=minio
PASSWORD=minio123

declare -a BUCKET_NAME=("cortex-alertmanager" "cortex-ruler" "cortex-tsdb")

# Wait until our server responds
echo "Waiting for server to come up"
until $(curl --output /dev/null -k --silent --head --fail  "https://$URL:$PORT"); do
sleep 5
done

# Login and save the cookies to /tmp/minio-cookies.txt
echo "Login to tenant console"
curl -k --location "https://$URL:$PORT/api/v1/login" \
  --silent \
  --header 'Content-Type: application/json' \
  --cookie-jar /tmp/minio-cookies.txt \
  --data "{\"accessKey\":\"$USERNAME\",\"secretKey\":\"$PASSWORD\"}"

# Create the buckets
echo "Creating buckets"
for i in "${BUCKET_NAME[@]}"
do
echo "Creating $i bucket"
curl -k -X POST -H "Host: $URL" \
   --silent \
   --cookie /tmp/minio-cookies.txt \
    -H 'content-type: application/json' \
    --data "{\"name\":\"$i\",\"versioning\":{\"enabled\":false,\"excludePrefixes\":[],\"excludeFolders\":false},\"locking\":false}" \
    "https://$URL:$PORT/api/v1/buckets"
done

echo "Clean up temporary file"
rm /tmp/minio-cookies.txt