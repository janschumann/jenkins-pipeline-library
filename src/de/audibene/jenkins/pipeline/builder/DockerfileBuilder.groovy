package de.audibene.jenkins.pipeline.builder

import de.audibene.jenkins.pipeline.scm.Scm

class DockerfileBuilder implements ArtifactBuilder {

    private final def script
    private final Map<String, Closure> steps
    private final Map<String, Object> artifact

    DockerfileBuilder(script, steps = [:], artifact = [:]) {
        this.script = script
        this.steps = steps
        this.artifact = artifact
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
    String build(Map params = [:]) {
        boolean verbose = params.get('verbose', false)
        boolean push = params.get('push', false)
        String tag = params.tag
        Scm scm = params.scm

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
        script.sh script.ecrLogin()
    }

    def artifact(Closure body) {
        script.configure(artifact, body)
    }

    def steps(Closure body) {
        script.configure(steps, body)
    }
}