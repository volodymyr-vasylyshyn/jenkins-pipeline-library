#!/usr/bin/groovy

import groovy.json.JsonOutput

def call(config=[:]) {
  def method             = config.method ?: "GET"
  def authorizationToken = config.authorizationToken
  def body               = config.body
  def url                = config.url

  def script = "curl -X ${method} -Ls"
  if (authorizationToken) {
    script += "H \"Authorization: ${authorizationToken}\""
  }
  if (body) {
    script += " -H \"Content-Type: application/json; encoded=base64\" -d '${encodeJson(body)}'"
  }
  script += " \"${url}\""
  def response = sh(script: script, returnStdout: true).trim()
  try {
    response = readJSON(text: response)
  } catch (err) {
    echo "ERROR ${err}"
  }

  return response
}

String encodeJson(obj) {
  def res = [__base64Encoded: []]
  obj.each {k, v ->
    res["__base64Encoded"] << k
    res[k] = v.toString().bytes.encodeBase64().toString()
  }

  return JsonOutput.toJson(res)
}
