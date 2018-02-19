#!groovy

import de.audibene.jenkins.pipeline.builder.DockerfileBuilder

def call(Closure body) {
    def builder = new DockerfileBuilder(this)
    return configure(builder, body)
}