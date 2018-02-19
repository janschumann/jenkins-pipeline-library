package de.audibene.jenkins.pipeline.deployer

class BeansTalkDeployer implements ArtifactDeployer {

    private final def script
    private final Map params

    BeansTalkDeployer(def script, params) {
        this.script = script
        this.params = params
    }

    @Override
    def deploy(final Map params) {
        def artifact = params.artifact
        def environment = params.environment
        def auto = params.get('auto', false)

        if (!auto) {
            script.approveStep("Deploy to ${environment}?")
        }

        script.buildStep("Deploy to ${environment}") {
            script.echo "TODO: deploy $artifact to $environment"
        }
    }
}
