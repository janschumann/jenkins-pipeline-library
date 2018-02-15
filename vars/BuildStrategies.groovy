import de.audibene.jenkins.pipeline.BuildStrategy
import de.audibene.jenkins.pipeline.DockerBuildStrategy

BuildStrategy call(Closure body){

    def config = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    body()

    echo "Config $config"
    return new DockerBuildStrategy(this, config)
}


return this
