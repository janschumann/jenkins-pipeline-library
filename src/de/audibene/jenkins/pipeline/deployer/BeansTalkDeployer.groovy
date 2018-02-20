package de.audibene.jenkins.pipeline.deployer

import groovy.json.JsonBuilder

import static java.util.Objects.requireNonNull

class BeansTalkDeployer implements ArtifactDeployer {

    private final def script
    private final Map config

    BeansTalkDeployer(def script, config) {
        this.script = script
        this.config = config

        requireNonNull(config.port, "BeansTalkDeployer.init(config[port])")
        requireNonNull(config.region, "BeansTalkDeployer.init(config[region])")
        requireNonNull(config.application, "BeansTalkDeployer.init(config[application])")
    }

    @Override
    def deploy(final Map params) {
        script.echo "BeansTalkDeployer.build($params) on ${this.toString()}"
        def port = requireNonNull(config.port, "BeansTalkDeployer.init(config[port])")
        String application = requireNonNull(config.application, "BeansTalkDeployer.init(config[application])")
        String artifact = requireNonNull(params.artifact, "BeansTalkDeployer.deploy(params[artifact])")
        String environment = requireNonNull(params.environment, "BeansTalkDeployer.deploy(params[environment])")

        boolean auto = params.get('auto', false)

        if (!auto) {
            script.approveStep("Deploy to ${environment}?")
        }

        script.buildNode {
            script.deleteDir()
            script.buildStep("Deploy to ${environment}") {

                script.writeFile file: 'Dockerrun.aws.json', text: new JsonBuilder([
                        "AWSEBDockerrunVersion": "1",
                        "Image"                : ["Name": artifact, "Update": true],
                        "Ports"                : [["ContainerPort": "$port"]]
                ]).toPrettyString()

                script.step([
                        $class                  : 'AWSEBDeploymentBuilder',
                        credentialId            : config.get('credentialId', ''),
                        awsRegion               : config.get('region'),
                        applicationName         : application,
                        environmentName         : environment,
                        bucketName              : config.get('bucket', ''),
                        keyPrefix               : config.get('keyPrefix', application),
                        versionLabelFormat      : params.get('label', artifact.split('/').last()),
                        versionDescriptionFormat: params.get('description', artifact),
                        rootObject              : config.get('root', ''),
                        includes                : config.get('includes', ''),
                        excludes                : config.get('excludes', ''),
                        zeroDowntime            : config.get('zeroDowntime', false),
                        checkHealth             : config.get('checkHealth', true),
                        sleepTime               : config.get('sleepTime', 10),
                        maxAttempts             : config.get('maxAttempts', 10)
                ])
            }
        }
    }


    @Override
    String toString() {
        return "BeansTalkDeployer{config=$config}"
    }
}
