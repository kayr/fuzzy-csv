#!/usr/bin/env bash

# fail if any commands fails
set -e

echo "check the current branch is master"
if [[ $(git rev-parse --abbrev-ref HEAD) != "master" ]]; then
  echo "Not on master branch, aborting."
  exit 1
fi

echo "check the current branch is clean"
if [[ $(git status --porcelain) ]]; then
  echo "There are uncommitted changes, aborting."
  exit 1
fi

echo 'check the current branch is up-to-date'
git fetch

if [[ $(git rev-parse HEAD) != $(git rev-parse '@{u}') ]]; then
  echo "Local branch is not up-to-date, aborting."
  exit 1
fi

# get the current version
echo "get the current version"
OUTPUT=$(./gradlew -q printVersion)
VERSION=$(echo "$OUTPUT" | awk '{print $NF}')
echo "    -> Current version is $VERSION"

NEXT_VERSION=$(echo "$VERSION" | awk -F. '{$NF = $NF + 1;} 1' | sed 's/ /./g')
echo "    -> Proposed Next version is $NEXT_VERSION"

# ask the user for the version but set the default to the next version
read -p "Enter the version [$NEXT_VERSION]: " ACTUAL_NEXT_VERSION
ACTUAL_NEXT_VERSION=${ACTUAL_NEXT_VERSION:-$NEXT_VERSION}

# check tag does not exist
if [[ $(git tag -l "$ACTUAL_NEXT_VERSION") ]]; then
  echo "Tag $ACTUAL_NEXT_VERSION already exists, aborting."
  exit 1
fi

# check branch does not exist
if [[ $(git branch -l "release/$ACTUAL_NEXT_VERSION") ]]; then
  echo "Branch release/$ACTUAL_NEXT_VERSION already exists, aborting."
  exit 1
fi

# create a new branch for the release
git checkout -b "release/$ACTUAL_NEXT_VERSION"

# escape the dots in the version
S_NEXT_VERSION=${ACTUAL_NEXT_VERSION//./\\.}

# Update all the versions in README.md and gradle.properties
sed -i -e "s/implementation 'io\.github\.kayr:fuzzy-csv:.*-groovy3'/implementation 'io.github.kayr:fuzzy-csv:$S_NEXT_VERSION-groovy3'/g" README.md
sed -i -e "s/implementation 'io\.github\.kayr:fuzzy-csv:.*-groovy4'/implementation 'io.github.kayr:fuzzy-csv:$S_NEXT_VERSION-groovy4'/g" README.md
sed -i -e "s/VERSION_NAME=.*/VERSION_NAME=$S_NEXT_VERSION/g" gradle.properties

# commit the changes
git commit -am "Release $S_NEXT_VERSION"

# run the tests
#make test
echo "MOCK make test"

# build for groovy 4
# ./gradlew clean build publish -Pvariant=4 --no-daemon
echo "MOCK ./gradlew clean build publish -Pvariant=4 --no-daemon"
#./gradlew closeAndReleaseRepository
echo "MOCK ./gradlew closeAndReleaseRepository"

# build for groovy 3 and below
#./gradlew clean build publish -Pvariant=3 --no-daemon
echo "MOCK ./gradlew clean build publish -Pvariant=3 --no-daemon"
echo "MOCK ./gradlew closeAndReleaseRepository"

#create a tag
git tag -a "$ACTUAL_NEXT_VERSION" -m "Release $ACTUAL_NEXT_VERSION"

# push the changes
#git push originin "release/$VERSION"
#git push origin "$VERSION"

# checkout master
git checkout master

# merge the changes
git merge "release/$ACTUAL_NEXT_VERSION"

# push the changes
# git push origin master
echo "MOCK git push origin master"
