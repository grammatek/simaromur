#! /bin/bash
# Downloads all necessary build requisites from required projects via Github API.
#
# The following Android Release artifacts are needed:
#   - OpenFst
#   - Thrax
#
# These are downloaded from their corresponding Github Release pages according to the following
# environment variables:
#   OPENFST_TAG, THRAX_TAG, FLITE_THRAX_TAG

set -o pipefail
#set -x
set -e

if [ -z "$OPENFST_TAG" ] || [ -z "$THRAX_TAG" ]; then
  echo "At least one variable of [OPENFST_TAG, THRAX_TAG] is not set! Giving up."
  exit 1
fi

echo "Using OPENFST_TAG: $OPENFST_TAG"
echo "Using THRAX_TAG: $THRAX_TAG"

# Download release infos
OPENFST_URL=https://api.github.com/repos/grammatek/openfst/releases
OPENFST_REL_JSON=$(curl -sH "Accept: application/vnd.github.v3+json" "$OPENFST_URL")

THRAX_URL=https://api.github.com/repos/grammatek/thrax/releases
THRAX_REL_JSON=$(curl -sH "Accept: application/vnd.github.v3+json" "$THRAX_URL")

# Checks given version against version in given json string
#
# $1: version to check
# $2: json string as returned from github API for the Releases page
check_version() {
    local tag="$1"
    local json="$2"
    echo "$json" | ruby -e "\
        require 'json'; s=STDIN.read; j = JSON.parse(s); \
        j[0]['tag_name'] == '$tag'"
    return $?
}

# Retrieves download URL of first asset of a release
# $1: json string as returned from github API for Releases page
# @todo: we need to search for the tag in the assets, to make sure, that we get always the required one
first_asset_download_url() {
    local json="$1"
    rv=$(echo "$json" | ruby -e "\
        require 'json'; s=STDIN.read; j = JSON.parse(s); \
        puts j[0]['assets'][0]['browser_download_url']")
    echo "$rv"
}

# Download asset to given directory
# $1: tag of asset
# $2: JSON string returned from Github Releases API for a specific project
dl_asset() {
    local tag="$1"
    local json="$2"
    local url
    check_version "$tag" "$json" || exit 1
    url=$(first_asset_download_url "$json")
    echo "Downloading $url ..."
    curl -L "$url" --output "$tag.tgz" || exit 1
}

# Create directories
THIRD_PARTY_DIR=$(pwd)/3rdparty
mkdir -p "$THIRD_PARTY_DIR/archives"
mkdir -p "$THIRD_PARTY_DIR/ndk"
pushd "$THIRD_PARTY_DIR/archives" >/dev/null

# Download and extract assets. Use only subfolder for extraction
if [ ! -f ".$OPENFST_TAG.tgz.extracted" ]; then
    dl_asset "$OPENFST_TAG" "$OPENFST_REL_JSON"
    tar xf "$OPENFST_TAG.tgz" --strip-components=1 -C ../ndk && touch ".$OPENFST_TAG.tgz.extracted"
    rm -f "$OPENFST_TAG.tgz"
fi
if [ ! -f ".$THRAX_TAG.tgz.extracted" ]; then
    dl_asset "$THRAX_TAG" "$THRAX_REL_JSON"
    tar xf "$THRAX_TAG.tgz" --strip-components=1 -C ../ndk && touch ".$THRAX_TAG.tgz.extracted"
    rm -f "$THRAX_TAG.tgz"
fi

popd >/dev/null
