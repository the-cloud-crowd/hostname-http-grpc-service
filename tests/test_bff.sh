#!/bin/sh

TARGET=${1:-"http://localhost:9090"}

curl -H 'x-userid: john' \
  "$TARGET/bff/hostname"
