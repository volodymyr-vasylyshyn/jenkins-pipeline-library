#!/usr/bin/groovy

def call(String projectId, String clasterName) {
  sh "gcloud container clusters get-credentials '${clasterName}' --project '${projectId}' --zone us-central1-a"
  sh "kubectl config use-context \"\$(kubectl config get-contexts | grep -oe \"gke[^ ]\\+${clasterName}\" | head -n1)\""
}
