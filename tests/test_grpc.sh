#!/bin/sh

TARGET=${1:-"-plaintext localhost:50051"}

grpcurl  -H 'X-UserId: john' \
  -d '{"sender":"john"}' \
  -import-path hostname-proto/src/main/proto/hostname \
  -proto hostname.proto \
  $TARGET "hostname.HostnameService/GetHostname"
