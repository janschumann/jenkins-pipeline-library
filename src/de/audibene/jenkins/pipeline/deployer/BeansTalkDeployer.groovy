package de.audibene.jenkins.pipeline.deployer

class BeansTalkDeployer implements ArtifactDeployer {

    private final def script
    private final Map config

    BeansTalkDeployer(def script, config) {
        this.script = script
        this.config = config
    }

    @Override
    def deploy(final Map params) {
        script.echo "beanstalk config $config"
        script.echo "beanstalk params $params"
        String artifact = params.artifact
        String environment = params.environment
        boolean auto = params.get('auto', false)
        String application = config.application
        String region = config.region
        Integer port = config.port
        String simpleArtifact = artifact.split('/').last()

        if (!auto) {
            script.approveStep("Deploy to ${environment}?")
        }

        script.buildNode {
            script.deleteDir()
            script.buildStep("Deploy to ${environment}") {

                script.writeFile file: 'Dockerrun.aws.json', text: """
                |{
                |  "AWSEBDockerrunVersion": "1",
                |  "Image": {
                |    "Name": "$artifact",
                |    "Update":true
                |  },
                |  "Ports": [
                |    {
                |      "ContainerPort": "${port}"
                |    }
                |  ]
                |}""".stripMargin()


                def stepParams = [
                        $class                  : 'AWSEBDeploymentBuilder',
                        credentialId            : '',
                        awsRegion               : region,
                        applicationName         : application,
                        environmentName         : environment,
                        bucketName              : application,
                        keyPrefix               : '',
                        versionLabelFormat      : simpleArtifact,
                        versionDescriptionFormat: artifact,
                        rootObject              : '',
                        includes                : '',
                        excludes                : '',
                        zeroDowntime            : false,
                        checkHealth             : true,
                        sleepTime               : 10,
                        maxAttempts             : 60
                ]

                script.echo "Do deploy $stepParams"
                script.step(stepParams)
            }
        }
    }
}
