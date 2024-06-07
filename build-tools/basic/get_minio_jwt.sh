#!/usr/bin/env bash

$(which kubectl) get secret/console-sa-secret -o jsonpath='{.data.token}' |$(which base64) -d
