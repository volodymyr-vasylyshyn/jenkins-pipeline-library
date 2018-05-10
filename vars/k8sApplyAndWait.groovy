#!/usr/bin/groovy

import org.jenkinsci.plugins.workflow.steps.FlowInterruptedException

// https://travix.io/deploying-your-application-to-kubernetes-2abaee6db222

def call(String k8sConfFilePath, waitTimeout=[time: 5, unit: 'MINUTES']) {
  def confFile = k8sConfFileInfo k8sConfFilePath
  def meta     = [:]

  k8sApply(k8sConfFilePath)

  if (confFile.isDeployment) {
    meta = waitForDeployment(confFile.withDeployment.namespace, confFile.withDeployment.name, waitTimeout)
  } else {
    meta = waitForOther(confFile["with${confFile.kinds[0]}"].namespace, confFile["with${confFile.kinds[0]}"].name, waitTimeout)
  }

  return [
    success  : meta.success,
    confFile : confFile,
    meta     : meta
  ]
}

def waitForDeployment(String namespace, String name, waitTimeout=[time: 5, unit: 'MINUTES']) {
  Integer generation         = Integer.MAX_VALUE
  Integer observedGeneration = 0
  Integer desiredReplicas    = 1
  Integer updatedReplicas    = 0
  Integer availableReplicas  = 0
  Boolean success            = false

  try {
    timeout(waitTimeout) {

      def deployment
      while ( observedGeneration < generation      ||
              updatedReplicas    < desiredReplicas ||
              availableReplicas  < updatedReplicas ) {
        sleep 5
        deployment = readJSON(text: readFromSh("kubectl get --namespace=${namespace} deployment/${name} -o json"))

        generation         = deployment.metadata.generation       as Integer // can be read only once
        observedGeneration = deployment.status.observedGeneration as Integer ?: 0
        desiredReplicas    = deployment.spec.replicas             as Integer ?: 1
        updatedReplicas    = deployment.status.updatedReplicas    as Integer ?: 0
        availableReplicas  = deployment.status.availableReplicas  as Integer ?: 0

        echo """
          observedGeneration ${observedGeneration} < ${generation} generation
          updatedReplicas    ${updatedReplicas} < ${desiredReplicas} desiredReplicas
          availableReplicas  ${availableReplicas} < ${updatedReplicas} updatedReplicas
        """
      }
    }

    success = true
  } catch(FlowInterruptedException interruptEx) {
    echo "Apply \"${namespace}/${name}\" interrupted (${interruptEx.toString()}) with timeout: ${waitTimeout.toString()}"
  }

  return [
    success             : success,
    generation          : generation,
    observedGeneration  : observedGeneration,
    desiredReplicas     : updatedReplicas,
    availableReplicas   : availableReplicas
  ]
}

def waitForOther(String namespace, String name, waitTimeout=[time: 5, unit: 'MINUTES']) {
  return [
    success : true
  ]
}
