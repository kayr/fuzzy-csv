#!/usr/bin/env bash

# fail if any commands fails
set -ex

# check the current branch is master
if [[ $(git rev-parse --abbrev-ref HEAD) != "master" ]]; then
    echo "Not on master branch, aborting."
    exit 1
fi

# check the current branch is clean
#if [[ $(git status --porcelain) ]]; then
#    echo "There are uncommitted changes, aborting."
#    exit 1
#fi

# check the current branch is up-to-date
git fetch

#if [[ $(git rev-parse HEAD) != $(git rev-parse '@{u}') ]]; then
#    echo "Local branch is not up-to-date, aborting."
#    exit 1
#fi



# get the current version
OUTPUT=$(./gradlew -q printVersion)

# the version will be the last string of the output of the command
VERSION=$(echo $OUTPUT | awk '{print $NF}')

# get the next version
NEXT_VERSION=$(echo $VERSION | awk -F. '{$NF = $NF + 1;} 1' | sed 's/ /./g')

# ask the user for the version
#TEMP read -p "Current version is $VERSION, next version is $NEXT_VERSION, please enter the version you want to release: " VERSION

# check the version is not empty
if [[ -z "$VERSION" ]]; then
    echo "Version cannot be empty, aborting."
    exit 1
fi

VERSION=$NEXT_VERSION

# create a new branch for the release
#TEMP git checkout -b "release/$VERSION"

# escape the dots in the version
S_NEXT_VERSION=$(echo $NEXT_VERSION | sed 's/\./\\\./g')

# Update the version in the README.md
sed -i -e  "s/implementation 'io\.github\.kayr:fuzzy-csv:.*-groovy3'/implementation 'io.github.kayr:fuzzy-csv:$S_NEXT_VERSION-groovy3'/g" README.md
sed -i -e  "s/implementation 'io\.github\.kayr:fuzzy-csv:.*-groovy4'/implementation 'io.github.kayr:fuzzy-csv:$S_NEXT_VERSION-groovy4'/g" README.md

# set the version in gradle.properties
sed -i  -e "s/VERSION_NAME=.*/VERSION_NAME=$S_NEXT_VERSION/g" gradle.properties

# commit the changes
git commit -am "Release $S_NEXT_VERSION"


# run the tests
make test

# build for groovy 4
./gradlew clean build publish -Pvariant=4 --no-daemon
#./gradlew closeAndReleaseRepository
echo "MOCK ./gradlew closeAndReleaseRepository"


# build for groovy 3 and below
./gradlew clean build publish -Pvariant=3 --no-daemon
echo "MOCK ./gradlew closeAndReleaseRepository"

#create a tag
git tag -a "$VERSION" -m "Release $VERSION"

# push the changes
#git push originin "release/$VERSION"
#git push origin "$VERSION"

# checkout master
git checkout master

# merge the changes
git merge "release/$VERSION"

# push the changes
# git push origin master
echo "MOCK git push origin master"




