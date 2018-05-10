#!/usr/bin/groovy

def call(config=[:]) {
  def uploadCredentialsId   = config.uploadCredentialsId
  def fileName              = config.fileName
  def packageName           = config.packageName    ?: env.BUILD_APP_NAME
  def packageVersion        = config.packageVersion ?: env.BUILD_VERSION
  def pathPrefix            = config.pathPrefix     ?: ""
  def branchName            = config.branchName     ?: env.BRANCH_NAME
  def bucketName            = config.bucketName
  def upServerRoot          = config.upServerRoot   ?: "https://up.icacs.io"
  def upServerCredentialsId = config.upServerCredentialsId

  def commitMessage         = sh(script: "git log -1 --pretty=%B", returnStdout: true).trim()
  def checksum              = md5Sum("${pathPrefix}${fileName}")
  def channelInfo

  withCredentials([string(credentialsId: upServerCredentialsId, variable: 'UP_SERVER_AUTH_TOKEN')]) {
    channelInfo = jsonRequest([
      url                : "${upServerRoot}/api/v2/ci/packages/${branchName}/channel",
      authorizationToken : env.UP_SERVER_AUTH_TOKEN
    ])
  }

  def uploadOptions = [
    credentialsId  : uploadCredentialsId,
    pathPrefix     : pathPrefix,
    pattern        : "${pathPrefix}${fileName}",
    bucket         : "gs://${bucketName}/packages/${packageName}/${channelInfo.channelName}/",
    sharedPublicly : true
  ]
  def packageUrl    = "https://storage.googleapis.com/${bucketName}/packages/${packageName}/${channelInfo.channelName}/${fileName}"

  googleStorageUpload(uploadOptions)

  buildReportAppend("`PACKAGE_URL` = ${packageUrl}")
  buildReportAppend("`     COMMIT` = `${env.GIT_COMMIT}`")

  withCredentials([string(credentialsId: upServerCredentialsId, variable: 'UP_SERVER_AUTH_TOKEN')]) {
    jsonRequest([
      url                : "${upServerRoot}/api/v2/ci/packages",
      method             : "POST",
      authorizationToken : env.UP_SERVER_AUTH_TOKEN,
      body               : [
        name         : packageName,
        url          : packageUrl,
        version      : packageVersion,
        sourceBranch : branchName,
        checksum     : checksum,
        notes        : commitMessage,
        commit       : env.GIT_COMMIT
      ]
    ])
  }
}
