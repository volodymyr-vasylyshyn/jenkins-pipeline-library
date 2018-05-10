#!/usr/bin/groovy

def call() {
  def buildNumber = env.BUILD_NUMBER

  if (env.BRANCH_NAME != 'master') {
    buildNumber = "${env.BRANCH_NAME}-${env.BUILD_NUMBER}"
  }

  return buildNumber
}
