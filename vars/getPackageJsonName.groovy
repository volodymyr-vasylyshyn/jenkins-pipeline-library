#!/usr/bin/groovy

def call() {
  return readJSON(file: 'package.json').name
}
