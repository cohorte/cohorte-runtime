#!/bin/bash

# Get the PSEM2M Root directory
cd $(dirname $0)
PSEM2M_ROOT=$(pwd)

# Output name
ZIP_FILE="small-install.zip"

# Prepare paths
PLATFORMS_DIR="$PSEM2M_ROOT/platforms"
SMALL_INSTALL_NAME="small-install"
SMALL_HOME="small.home"

BASE_MODULE="base"
BASE_SRC="$PSEM2M_ROOT/python/psem2m.base/src"
FORKER_MODULE="psem2m"
FORKER_SRC="$PSEM2M_ROOT/python/psem2m.forker/src"

# - Script -
echo "Mise à jour de Small-Install..."
pushd "$PLATFORMS_DIR" >/dev/null || exit 1

pushd "$SMALL_INSTALL_NAME/$SMALL_HOME/bin" >/dev/null || exit 1
echo "> Mise à jour de PSEM2M base..."
rm -fr BASE_MODULE
cp -r "$BASE_SRC/$BASE_MODULE" .

echo "> Mise à jour de PSEM2M forker..."
rm -fr "psem2m"
cp -r "$FORKER_SRC/$FORKER_MODULE" .

echo "> Suppression des fichiers .pyc"
find . -name "*.pyc" -exec rm -f {} \;
popd >/dev/null

echo "Suppression de l'archive précédente..."
rm -f "$ZIP_FILE"

echo "Création de l'archive..."
zip -r "$PSEM2M_ROOT/$ZIP_FILE" "$SMALL_INSTALL_NAME" || exit 1
popd >/dev/null

echo "Copie de l'archive dans le dossier personnel..."
cp "$PSEM2M_ROOT/$ZIP_FILE" "$HOME/$ZIP_FILE"

echo "Fini."
