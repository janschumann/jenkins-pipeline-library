package de.audibene.jenkins.pipeline.builder

import de.audibene.jenkins.pipeline.scm.Scm

import static java.util.Objects.requireNonNull

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
        script.echo "DockerfileBuilder.build with arttifact: $artifact, steps: $steps, params: $params"

        String tag = params.tag
        boolean verbose = params.get('verbose', true)
        Scm scm = requireNonNull(params.scm, 'DockerfileBuilder.build(params[scm]') as Scm

        def imageName = null

        script.buildNode('ecs') {
            script.buildStep('Build', !verbose) {
                script.buildStep('Prepare', verbose) {
                    scm.checkout()
                    runStep('prepare')
                }
                script.buildStep('Test', verbose) {
                    runStep('unitTest')
                }
                script.buildStep('IT', verbose) {
                    runStep('itTest')
                }
                script.buildStep('Build', verbose) {
                    runStep('build')
                    def dockerImage = script.docker.build(artifact.name)
                    if (tag != null) {
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
        requireNonNull(artifact.name, 'DockerfileBuilder.init(artifact[name]')
        requireNonNull(artifact.registry, 'DockerfileBuilder.init(artifact[registry]')

    }

    def steps(Closure body) {
        script.configure(steps, body)
    }
}