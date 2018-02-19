package de.audibene.jenkins.pipeline.builder

class DockerfileBuilder implements ArtifactBuilder {

    private final def script
    private final Map<String, Closure> steps
    private final Map<String, Object> image
    private final Map<String, Object> artifact

    DockerfileBuilder(script, config) {
        this.script = script
        this.steps = config.steps
        this.image = config.image
        this.artifact = config.artifact
    }

    private def runStep(String name) {
        def stepBody = steps[name]
        if (stepBody) {
            stepBody()
        } else {
            script.echo "TBD: step $name"
        }
    }

    @Override
    String build(Map parameters = [:]) {
        boolean verbose = parameters.get('verbose', true)
        boolean push = parameters.get('push', false)
        String tag = parameters.tag
        def scm = parameters.scm

        def imageName = null

        script.buildNode('ecs') {
            script.buildStep('Build', !verbose) {
                script.buildStep('Prepare', verbose) {
                    scm.checkout()
                    runStep('prepare')
                }
                script.buildStep('Test', verbose) {
                    runStep('test')
                }
                script.buildStep('IT', verbose) {
                    runStep('it')
                }
                script.buildStep('Build', verbose) {
                    runStep('build')
                    def dockerImage = script.docker.build(artifact.name)
                    if (push) {
                        script.docker.withRegistry(artifact.registry) {
                            loginEcrRepository()
                            dockerImage.push(tag)
                            scm.tag(tag)
                            imageName = "${dockerImage.imageName()}:$tag"
                        }

                    }
                }
            }
        }

        return imageName
    }

    private void loginEcrRepository() {
        script.sh script.ecrLogin() //TODO hide credentials
    }
}