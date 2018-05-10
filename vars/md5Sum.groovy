#!/usr/bin/groovy

def call(String fileName) {
  return sh(script: "md5sum ${fileName} | awk '{ print \$1 }'", returnStdout: true).trim()
}
