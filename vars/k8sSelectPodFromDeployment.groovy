#!/usr/bin/groovy

def call(String deploymentPath) {
  def confFile = k8sConfFileInfo deploymentPath

  if (!confFile.isDeployment) return null

  def selector   = []
  def deployment = readJSON(text: readFromSh("kubectl get --namespace=${confFile.withDeployment.namespace} deployment/${confFile.withDeployment.name} -o json"))
  def pods       = []

  if (deployment.spec.selector && deployment.spec.selector.matchLabels) {
    deployment.spec.selector.matchLabels.each { key, value -> selector << "${key}=${value}" }
  }

  if (selector) {
    pods = readJSON(text: readFromSh("kubectl get pods --namespace=${confFile.withDeployment.namespace} --selector=${selector.join(",")} --output=json")).items
  }

  return pods[0]
}
