#!/usr/bin/groovy

def call(List confFiles, String kind) {
  def res = []

  confFiles.each { confFile ->
    if (k8sConfFileInfo(confFile)["is${kind}"]) {
      res << confFile
    }
  }

  return res
}
