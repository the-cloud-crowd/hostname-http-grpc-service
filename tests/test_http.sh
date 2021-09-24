#!/bin/sh

TARGET=${1:-"http://localhost:8080"}

curl -H 'X-UserId: john' \
  "$TARGET/hostname"
