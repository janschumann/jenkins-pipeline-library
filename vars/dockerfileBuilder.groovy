import de.audibene.jenkins.pipeline.builder.DockerfileBuilder

def call(Closure body) {
    def config = configs(body)
    return new DockerfileBuilder(this, config)
}