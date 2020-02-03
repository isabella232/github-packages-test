#!/usr/bin/env bash
cd $(dirname $0)/..

if [ -z "$GITHUB_MAVEN_USERNAME" ]; then
  echo "error: please set GITHUB_MAVEN_USERNAME environment variable"
  exit 1
fi

if [ -z "$GITHUB_TOKEN" ]; then
  echo "error: please set GITHUB_MAVEN_PASSWORD environment variable"
  exit 1
fi

if [ -z "$GPG_PASSPHRASE" ]; then
  echo "warning: please consider adding gpg signing and set GPG_PASSPHRASE environment variable"
fi

DTASK=":library:publishDefaultPublicationToGitHubRepository"
TARGETS="$DTASK"

if [ -n "$GITHUB_TAG_NAME" ]; then
  echo "on a tag -> deploy release version $GITHUB_TAG_NAME"
  ./gradlew "$TARGETS"
else
  echo "not on a tag -> nothing to deploy"
fi
