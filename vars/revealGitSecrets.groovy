#!/usr/bin/groovy

def call(body) {
  sh "cp -r /root/.gnupg/ /home/jenkins && chmod 700 /home/jenkins/.gnupg/"
  sh "git secret reveal"
}
