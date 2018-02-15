import de.audibene.jenkins.pipeline.DockerBuildStrategy

def docker(Closure body) {
    def config = configs(body)
    return new DockerBuildStrategy(this, config)
}