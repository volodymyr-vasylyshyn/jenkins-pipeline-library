#!/usr/bin/groovy

def call(String path) {
  sh "kubectl apply -f ${path}"
}
