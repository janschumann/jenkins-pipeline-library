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

        if (!auto) {
            script.approveStep("Deploy to ${environment}?")
        }

        script.buildNode('ecs') {
            script.sh 'ls -lah'
            script.buildStep("Deploy to ${environment}") {
                script.sh 'mkdir -p .elasticbeanstalk/'
                script.writeFile file: '.elasticbeanstalk/config.yml', text: """
                |global:
                |  application_name: ${config.application}
                |  default_platform: ${config.platform}
                |  default_region: ${config.region}
                """.stripMargin()

                script.sh 'cat .elasticbeanstalk/config.yml'
                
                
                script.writeFile file: 'Dockerrun.aws.json', text: """
                |{
                |  "AWSEBDockerrunVersion": "1",
                |  "Image": {
                |    "Name": "$artifact,
                |    "Update":true
                |  },
                |  "Ports": [
                |    {
                |      "ContainerPort": "${config.port}"
                |    }
                |  ]
                |}""".stripMargin()

                script.sh 'cat Dockerrun.aws.json'

                script.echo "TODO: deploy $artifact to $environment"
            }
        }
    }
}
