#!/usr/bin/groovy

def call(config=[:]) {
  def tags = config.tags
  def tag  = tags[0]

  if (config.serviceAccount) {
    authenticateWithServiceAccount(config.serviceAccount)
  }

  sh("docker build -t ${tag} .")

  sh "gcloud docker -- push ${tag}"
  tags.each { extraTag ->
    sh "gcloud container images add-tag ${tag} ${extraTag}"
  }
}
