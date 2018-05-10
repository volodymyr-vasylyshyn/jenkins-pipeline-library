#!/usr/bin/groovy

def call(static_version='') {
  def buildNumber = env.BUILD_NUMBER
  def version     = ''
  if (static_version != '') {
    version = "${static_version}-${buildNumber}"
  } else {
    version = "${buildNumber}"
  } 
  

  if (env.BRANCH_NAME != 'master') {
    version = "${version}-${env.BRANCH_NAME}"
  }

  return version
}
