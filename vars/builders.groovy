import de.audibene.jenkins.pipeline.DockerBuilder

def docker(Closure body) {
    def config = configs(body)
    return new DockerBuilder(this, config)
}