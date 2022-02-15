#!/bin/sh

TARGET=${1:-"-plaintext localhost:50051"}

grpcurl  -H 'X-UserId: john' \
  -protoset $(dirname $0)/../hostname-proto/target/generated-resources/protobuf/descriptor-sets/hostname-proto-1.0.0-SNAPSHOT.protobin \
  $TARGET "hostname.HostnameService/GetHostname"
