#!/usr/bin/groovy

def call() {
  def fileContent      = readFile('config/initializers/version.rb')
  def canonicalVersion = (fileContent =~ /VERSION\s*=\s*['"]?([^'"]*)?/)[0][1]
  def buildNumber      = env.BUILD_NUMBER
  def version          = "${canonicalVersion}-${buildNumber}"

  if (env.BRANCH_NAME != 'master') {
    version = "${version}-${env.BRANCH_NAME}"
  }

  return version
}
