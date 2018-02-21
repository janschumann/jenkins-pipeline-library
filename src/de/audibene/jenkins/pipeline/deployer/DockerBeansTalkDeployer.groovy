package de.audibene.jenkins.pipeline.deployer

import groovy.json.JsonBuilder

import static java.util.Objects.requireNonNull

class DockerBeansTalkDeployer implements ArtifactDeployer {

    private final def script
    private final Map config

    DockerBeansTalkDeployer(def script, config) {
        this.script = script
        this.config = config
    }

    @Override
    def deploy(final Map params) {
        String application = config.application

        String artifact = requireNonNull(params.artifact, "DockerBeansTalkDeployer.deploy(params[artifact])")
        String environment = requireNonNull(params.environment, "DockerBeansTalkDeployer.deploy(params[environment])")

        boolean auto = params.get('auto', false)

        if (!auto) {
            script.approveStep("Deploy to ${environment}?")
        }

        script.lock(resource: "${script.env.JOB_NAME}:deploy:$environment") {
            script.buildNode {
                script.deleteDir()
                script.buildStep("Deploy to ${environment}") {

                    script.writeFile file: 'Dockerrun.aws.json', text: new JsonBuilder([
                            "AWSEBDockerrunVersion": "1",
                            "Image"                : ["Name": artifact, "Update": true],
                            "Ports"                : [["ContainerPort": "${config.port}"]]
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
    }

    DockerBeansTalkDeployer validated() {
        requireNonNull(script, "DockerBeansTalkDeployer.script")
        requireNonNull(config.port, "DockerBeansTalkDeployer.config.port")
        requireNonNull(config.region, "DockerBeansTalkDeployer.config.region")
        requireNonNull(config.application, "DockerBeansTalkDeployer.config.application")
        return this
    }
}
