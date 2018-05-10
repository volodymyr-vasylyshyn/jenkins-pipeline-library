#!/usr/bin/groovy

def call(Boolean status, String message) {
  def statusString = status ? "`[  OK  ]`" : "`[ FAIL ]`"
  buildReportAppend("${statusString} – ${message}")
}
