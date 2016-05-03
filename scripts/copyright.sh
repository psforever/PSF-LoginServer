#!/bin/bash
set -u

COPYRIGHT="// Copyright (c) 2016 PSForever.net to present"

FILES=$(find . -name '*.scala')

for f in $FILES; do

  LINESPEC=$(grep -n "^${COPYRIGHT}$" "$f")
  LINE=$(echo "$LINESPEC" | cut -d: -f1)

  if [ ! "$LINE" = "1" ]; then
    LINESPEC_NOWS=$(grep -n "${COPYRIGHT}" "$f")

    if [ ! "$LINESPEC_NOWS" = "" ]; then
      echo "$f: Found but malformed"
    else
      LINESPEC_OTHER_COPY=$(head -n20 "$f" | grep -ni "copyright")

      if [ ! "$LINESPEC_OTHER_COPY" = "" ]; then
        echo "$f: Other copyright found. Skipping..."
      else
        echo "$f: Not found"

        read -p "Fix? (y/n) " choice

        case $choice in
          [yY]) sed -i -b '1i '"$COPYRIGHT"'' "$f" ;;
          [nN]) ;;
          *) echo "Invalid choice"; exit 1;;
        esac
      fi
    fi
  else
    echo "$f: Okay!"
  fi
done
