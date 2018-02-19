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
        def artifact = params.artifact
        def environment = params.environment
        def auto = params.get('auto', false)
        def application = config.application
        def tag = params.tag

        if (!auto) {
            script.approveStep("Deploy to ${environment}?")
        }

        script.buildNode('ecs') {
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
                |      "ContainerPort": "${config.port}"
                |    }
                |  ]
                |}""".stripMargin()


                script.step([
                        $class:'AWSEBDeploymentBuilder',
                        credentialId: '',
                        awsRegion: config.region,
                        applicationName: application,
                        environmentName: environment,
                        bucketName: '',
                        keyPrefix: application,
                        versionLabelFormat: "$application:$tag",
                        versionDescriptionFormat: '',
                        rootObject: '',
                        includes: '',
                        excludes: '',
                        zeroDowntime: false,
                        checkHealth: true,
                        sleepTime: 10,
                        maxAttempts: 60
                ])
            }
        }
    }
}
