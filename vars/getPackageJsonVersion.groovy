#!/usr/bin/groovy

def call() {
  def packageJson = readJSON(file: 'package.json')
  def buildNumber = env.BUILD_NUMBER
  def version     = "${packageJson.version}-${buildNumber}"

  if (env.BRANCH_NAME != 'master') {
    version = "${version}-${env.BRANCH_NAME}"
  }

  return version
}
