#!/usr/bin/groovy

import org.jenkinsci.plugins.workflow.steps.FlowInterruptedException

def call(Map pod, List migrationTasks, waitTimeout) {
  def done = false

  try {
    timeout(waitTimeout) {
      migrationTasks.each { script ->
        try {
          sh "kubectl --namespace=${pod.metadata.namespace} exec -t ${pod.metadata.name} ${script}"
          buildReportAppendStatus(true, "_migrations_ `${script}`")
        } catch (e) {
          echo "Migration task \"${script}\" failed with error ${e.toString()}"
          buildReportAppendStatus(false, "_migrations_ `${script}`")
        }
      }
    }
    done = true
  } catch(FlowInterruptedException interruptEx) {
    echo "Migration tasks interrupted (${interruptEx.toString()}) with timeout: ${waitTimeout.toString()}"
    buildReportAppendStatus(false, "_migrations_ *interrupted* after ${waitTimeout.toString()}")
  }

  return done
}
