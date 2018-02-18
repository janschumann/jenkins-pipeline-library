#!groovy

import de.audibene.jenkins.pipeline.builder.DockerfileBuilder

def call(Closure body) {
    def config =  configure(body)
    return new DockerfileBuilder(this, config)
}