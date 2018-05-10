#!/usr/bin/groovy

def call(List paths, String pattern) {
  def files = []
  paths.each {
    files += findFiles(glob: "${it}/${pattern}").collect { it.toString() }
  }

  return files
}
