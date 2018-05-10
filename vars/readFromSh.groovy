#!/usr/bin/groovy

def call(String command) {
  return sh(returnStdout: true, script: command).trim()
}
