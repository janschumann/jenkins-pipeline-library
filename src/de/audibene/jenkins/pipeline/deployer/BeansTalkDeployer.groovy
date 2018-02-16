package de.audibene.jenkins.pipeline.deployer

import static java.util.Objects.requireNonNull


class BeansTalkDeployer implements ArtifactDeployer {

    private final def script
    private final Map params

    BeansTalkDeployer(def script, params = [:]) {
        this.script = requireNonNull(script, 'BeansTalkDeployer.script')
        this.params = requireNonNull(params, 'BeansTalkDeployer.params') as Map
    }

    @Override
    def deploy(final Map params) {
        def artifact = requireNonNull(params.artifact, 'BeansTalkDeployer#deploy(params.artifact)')
        def environment = requireNonNull(params.environment, 'BeansTalkDeployer#deploy(params.environment)')

        script.approveStep("Deploy to ${environment}?")

        script.buildStep("Deploy to ${environment}") {
            script.echo "TODO: deploy $artifact to $environment"
        }
    }
}
