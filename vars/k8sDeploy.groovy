#!/usr/bin/groovy

// https://travix.io/deploying-your-application-to-kubernetes-2abaee6db222
// to rollback
// kubectl rollout undo deployment/my-app --namespace my-namespace

def call(config=[:]) {
  def envName        = config.envName
  def serviceAccount = config.serviceAccount
  def clusterName    = config.clusterName
  def imageUrl       = config.imageUrl

  def projectId             = config.projectId        ?: env.PROJECT_ID
  def applyWaitTimeout      = config.applyWaitTimeout ?: [time: 10, unit: 'MINUTES']
  def migrationsWaitTimeout = config.applyWaitTimeout ?: [time: 20, unit: 'MINUTES']
  def k8sDir                = config.k8sDir           ?: "k8s"
  def migrationTasks        = config.migrationTasks   ?: []

  def withCanary         = fileExists("${k8sDir}/canary")
  def deploymentSucceded = true
  def migrationsDone     = !migrationTasks


  if (config.serviceAccount) {
    authenticateWithServiceAccount(config.serviceAccount)
  }

  k8sSelectCluster(projectId, clusterName)

  if (withCanary) {
    k8sSubstituteImageUrl(imageUrl, "${k8sDir}/canary")
    deploymentSucceded = deployToK8sCluster(k8sDir, envName, "canary", applyWaitTimeout)
    buildReportAppendStatus(deploymentSucceded, "_canary deployment_")
  }

  if (withCanary && !deploymentSucceded) {
    timeout(time: 1, unit: 'HOURS') {
      input('Canary deployment failed. Continue?')
    }
  }

  if (withCanary && !migrationsDone) {
    performMigrationTasks(k8sDir, "canary", migrationTasks, migrationsWaitTimeout)
    migrationsDone = true
  }

  k8sSubstituteImageUrl(imageUrl, "${k8sDir}/deployment")
  deploymentSucceded = deployToK8sCluster(k8sDir, envName, "deployment", applyWaitTimeout)
  buildReportAppendStatus(deploymentSucceded, "_complete deployment_")

  if (!migrationsDone) {
    performMigrationTasks(k8sDir, "deployment", migrationTasks, migrationsWaitTimeout)
    migrationsDone = true
  }

  if (withCanary) {
    removeDeployment(k8sDir, "canary")
  }
}

def deployToK8sCluster(String k8sDir, String envName, String deploymentName, waitTimeout) {
  def configsToDeploy = [] + listK8sConfigFiles(k8sDir, envName) + listK8sDeployments(k8sDir, "deps-deployment") + listK8sDeployments(k8sDir, deploymentName)
  def fails           = []

  configsToDeploy.each {
    // echo "k8sApplyAndWait(${it})"
    def result = k8sApplyAndWait(it, waitTimeout)
    if (!result.success) {
      fails << result
    }
  }

  if (fails) {
    echo "Some of the k8s configs failed: ${fails.toString()}"
    return false
  }

  return true
}

def performMigrationTasks(String k8sDir, String deploymentName, List migrationTasks, waitTimeout) {
  def deploymentsList = k8sSelectKind(listK8sDeployments(k8sDir, deploymentName), "Deployment")

  if (!deploymentsList) {
    echo "No deployment to run the migration tasks"
    return false
  }

  def pod = k8sSelectPodFromDeployment(deploymentsList[0])
  if (!pod) {
    echo "No pods to run the migration tasks"
    buildReportAppendStatus(false, "failed to select pod for migrations")
    return false
  }

  def result = k8sPerformMigrationTasks(pod, migrationTasks, waitTimeout)
  buildReportAppendStatus(result, "performing migrations on _${pod.metadata.name}_")

  return result
}

def removeDeployment(String k8sDir, String deploymentName) {
  def deploymentPaths = listK8sDeployments(k8sDir, deploymentName)
  def deploymets      = k8sSelectKind(deploymentPaths, "Deployment")

  deploymets.each {path ->
    sh "kubectl delete -f ${path}"
  }
}

def listK8sConfigFiles(String k8sDir, String envName) {
  return expandToExistingFiles([
    "${k8sDir}/namespaces",
    "${k8sDir}/secrets/${envName}",
    "${k8sDir}/secrets",
    "${k8sDir}/configs/${envName}",
    "${k8sDir}/configs"
  ], "*.yml")
}

def listK8sDeployments(String k8sDir, String deploymentName) {
  return expandToExistingFiles(["${k8sDir}/${deploymentName}"], "*.yml")
}
