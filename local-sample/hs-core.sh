#!/bin/bash

cd ../platform/

# The |& does not automatically create this file.
touch ../local-sample/tmp/HS_CORE.log
#rm ../local-sample/tmp/HS_CORE.log

# TODO: This needs work, '1>&2' does not catch everything, tried various combinations.

mvn install -Pbuild-docker-images-enabled -DskipTests | grep '*' 1>&2 ../local-sample/tmp/HS_CORE.log

echo 1 > ../local-sample/tmp/HS_CORE
