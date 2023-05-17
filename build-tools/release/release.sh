#!/bin/sh
########################################################################################################################
##
## FOR RELEASES
##
## RUN: ./release.sh v0.0.38-dev v0.0.37-dev
## - The format is 'release.sh <current_tag> <previous_tag>'
## - The <previous_tag> is used for 
##
########################################################################################################################

set -e

CURRENT_TAG=$1
PREVIOUS_TAG=$2

# Update Branch develop
git checkout develop
git pull -r origin develop

# Update Branch release
git checkout release
git pull -r origin release
git merge develop

echo "release-"$CURRENT_TAG

git log $PREVIOUS_TAG..HEAD --oneline --graph --all | grep 'Merge pull request' | sed 's/| //g' > CHANGELOG/changelog-$CURRENT_TAG.md

#git checkout release-$CURRENT_TAG
