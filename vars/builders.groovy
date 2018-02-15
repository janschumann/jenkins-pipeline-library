import de.audibene.jenkins.pipeline.builder.DockerBuilder

def call(Closure body) {
    def config = configs(body)
    return new DockerBuilder(this, config)
}