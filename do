#!/usr/bin/env bash

DIR="$( cd "$(dirname "$0")" ; pwd -P )"

CONSUL_DEV_NAME="consul-dev"
CONSUL_DEV_ACL_NAME="consul-dev-acl"

CONSUL_ACL_CONFIG=$(cat <<EOF
{
  "acl": {
    "enabled": true,
    "default_policy": "deny",
    "tokens": {
      "master": "aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee"
    }
  }
}
EOF
)

function task_clean {
    docker rm -f "${CONSUL_DEV_NAME}"
    docker rm -f "${CONSUL_DEV_ACL_NAME}"
}

function task_test {
    task_clean

    docker run -d -p 127.0.0.1:8500:8500 --name="${CONSUL_DEV_NAME}" consul agent -dev -client 0.0.0.0 --enable-script-checks=true
    docker run -d -p 127.0.0.1:8501:8500 --name="${CONSUL_DEV_ACL_NAME}"  -e CONSUL_LOCAL_CONFIG="${CONSUL_ACL_CONFIG}" consul agent -dev -client 0.0.0.0 --enable-script-checks=true
    (
        cd "${DIR}"
        mvn test
    )

    task_clean
}

task_usage() {
  echo "Usage: $0 test | clean"
  exit 1
}

arg=${1:-}
shift || true
case ${arg} in
    test) task_test "$@" ;;
    clean) task_clean "$@" ;;
    *) task_usage ;;
esac
