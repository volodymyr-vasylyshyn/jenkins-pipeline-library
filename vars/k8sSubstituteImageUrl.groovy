#!/usr/bin/groovy

def call(String imageUrl, String directory) {
  def versionlessImageUrl = imageUrl.split(":")[0]
  def searchPattern       = "[\"'\"'\"']\\?${versionlessImageUrl}:.*\$"

  expandToExistingFiles([directory], "*.yml").each { path ->
    sh "sed -i 's#${searchPattern}#${imageUrl}#' ${path}"
  }
}
