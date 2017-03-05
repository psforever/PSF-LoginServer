#!/bin/bash
set -u

COPYRIGHT="// Copyright (c) 2017 PSForever"

YEAR=$(date +"%Y")
FILES=$(find . -name '*.scala')
CHOICE=""

ask() {
  if [ "$CHOICE" = "a" ]; then
    return
  fi

  read -p "Fix? (y/n/a) " choice

  case $choice in
    [yY]) CHOICE="y" ;;
    [nN]) CHOICE="n" ;;
    [aA]) CHOICE="a" ;;
    *) echo "Invalid choice"; exit 1;;
  esac
}

for f in $FILES; do

  LINESPEC=$(grep -n "^${COPYRIGHT}$" "$f")
  LINE=$(echo "$LINESPEC" | cut -d: -f1)

  if [ ! "$LINE" = "1" ]; then
    LINESPEC_NOWS=$(grep -n "${COPYRIGHT}" "$f")

    if [ ! "$LINESPEC_NOWS" = "" ]; then
      echo "$f: Found but malformed"
    else
      LINESPEC_EXISTING_COPY=$(head -n20 "$f" | grep -i "copyright" | grep 'PSForever')
      LINESPEC_OTHER_COPY=$(head -n20 "$f" | grep -ni "copyright" | grep -v 'PSForever')

      if [ ! "$LINESPEC_OTHER_COPY" = "" ]; then
        echo "$f: Other copyright found. Skipping..."
      elif [ ! "$LINESPEC_EXISTING_COPY" = "" ]; then
        FOUND_YEAR=$(echo "$LINESPEC_EXISTING_COPY" | egrep -o '[0-9]{4}')
        if [ "$YEAR" = "$FOUND_YEAR" ]; then
          echo "$f: Found malformed copyright"
        else
          echo "$f: Found old copyright ($FOUND_YEAR)"
        fi

        ask

        if [ $CHOICE = "n" ]; then
          :
        else
          SED_CMD='s#'"$LINESPEC_EXISTING_COPY"'#'"$COPYRIGHT"'#'
          echo "Replacing '$LINESPEC_EXISTING_COPY' --> '$COPYRIGHT'"
          sed -i -b "$SED_CMD" "$f" 
        fi
      else
        echo "$f: Not found"

        ask

        if [ $CHOICE = "n" ]; then
          :
        else
          sed -i -b '1i '"$COPYRIGHT"'' "$f" 
        fi
      fi
    fi
  else
    echo "$f: Okay!"
  fi
done
