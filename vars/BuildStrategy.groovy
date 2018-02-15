import de.audibene.jenkins.pipeline.DockerBuildStrategy

def call(String type, Closure body) {
    def config = Config(body)
    switch (type) {
        case 'docker': return new DockerBuildStrategy(this, config)
        default: throw new IllegalArgumentException("Unknown type $type")
    }
}
