#!/usr/bin/groovy

def call(config=[:]) {
  def channel     = config.channel     ?: env.SLACK_CHANNEL
  def appName     = config.appName     ?: env.BUILD_APP_NAME
  def version     = config.version     ?: env.BUILD_VERSION
  def buildNumber = config.buildNumber ?: env.BUILD_NUMBER_FULL ?: env.BUILD_NUMBER

  def color       = (config.success.equals(true)) ? "good"      : (config.success.equals(false)) ? "danger" : "warning"
  def resolution  = (config.success.equals(true)) ? "succeeded" : (config.success.equals(false)) ? "failed" : "entered an interesting state"
  def mainMessage = "*${appName}@${version}* build to ${env.BUILD_REPORT_DEPLOY_ENV} ${resolution}"

  slackSend channel: channel, color: color, message: "${mainMessage}\n\n${env.BUILD_REPORT_CONTENT}"
}
