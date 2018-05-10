#!/usr/bin/groovy

def call(String message) {
  env.BUILD_REPORT_CONTENT += "\n${message}"
}
