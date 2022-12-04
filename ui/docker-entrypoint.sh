#!/bin/sh
echo "{\"realm\": \"${KEYCLOAK_REALM}\", \"url\": \"${KEYCLOAK_URL}\", \"clientId\": \"${KEYCLOAK_CLIENT_ID}\"}" > /usr/share/nginx/html/config.json
