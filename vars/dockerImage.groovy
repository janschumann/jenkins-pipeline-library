import de.audibene.jenkins.pipeline.docker.DockerImage


def call(Map params = [:], Closure body = {}) {
    Map config = configure(params, body)
    return DockerImage.create(this, config)
}

