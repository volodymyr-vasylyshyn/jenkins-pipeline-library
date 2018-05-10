#!/usr/bin/groovy

def call(config=[:]) {
  env.BUILD_REPORT_DEPLOY_ENV = config.deployEnv   ?: "infinity and beyond"
  env.BUILD_REPORT_CONTENT    = ""
  env.BUILD_REPORT_START      = new Date()
}
