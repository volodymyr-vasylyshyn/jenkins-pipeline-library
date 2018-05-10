#!/usr/bin/groovy

def call(String serviceAccountCredentialsId) {
  withCredentials([file(credentialsId: serviceAccountCredentialsId, variable: 'SERVICE_ACCOUNT_KEY_PATH')]) {
    sh "gcloud auth activate-service-account --key-file=\$SERVICE_ACCOUNT_KEY_PATH"
  }
}
