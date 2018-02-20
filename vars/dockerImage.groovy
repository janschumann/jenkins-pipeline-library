import de.audibene.jenkins.pipeline.docker.DockerImage

def call(Map params = [:], Closure body) {
    def config = configure(params, body)
    return new DockerImage(this, config)
}

