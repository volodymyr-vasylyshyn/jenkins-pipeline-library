#!/usr/bin/groovy

def call(String configFile) {
  def yaml   = readYaml(file: configFile)
  def result = [
    kinds : []
  ]

  if (!(yaml in List)) {
    yaml = [yaml]
  }

  yaml.each { root ->
    result.kinds << root.kind
    result["is${root.kind}"]   = true
    result["with${root.kind}"] = [
      namespace : root.metadata.namespace,
      name      : root.metadata.name
    ]

    if (root.kind == "Namespace") {
      result["with${root.kind}"].namespace = root.metadata.name
    }
  }

  return result
}
