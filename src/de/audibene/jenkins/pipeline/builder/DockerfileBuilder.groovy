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

    def inside(image = this.image, body) {
        script.docker.image(image.id).inside(image.args) {
            body()
        }
    }

    def withRun(image, body) {
        script.docker.image(image.id).withRun(image.args) { container ->
            body("--link ${container.id}:${image.linkAs}")
        }
    }

    def insideWithPostgres(def params = [:], body) {
        String imageVersion = params.version ?: 'latest'
        String imageId = "postgres:$imageVersion"
        String username = params.username ?: 'postgres'
        String password = params.password ?: 'postgres'
        String args = "-e 'POSTGRES_USER=$username' -e 'POSTGRES_PASSWORD=$password'"

        withRun(id: imageId, args: args, linkAs: 'postgres') { link ->

            inside(id: imageId, args: "$args $link") {
                script.sh 'while ! pg_isready -h postgres -q; do sleep 1; done'
            }

            inside(id: image.id, args: "${image.args} $link") {
                body()
            }
        }
    }

    private def runStep(String name) {
        def body = steps[name]
        body.resolveStrategy = Closure.DELEGATE_FIRST
        body.delegate = this
        body()
    }

    @Override
    String build(Map parameters = [:]) {
        boolean verbose = parameters.get('verbose', true)
        boolean push = parameters.get('push', false)
        String tag = parameters.tag

        def imageName = null

        script.node('ecs') {
            script.buildStep('Build', !verbose) {
                script.buildStep('Prepare', verbose) {
                    script.deleteDir()
                    script.checkout script.scm
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
                            script.sh script.ecrLogin()
                            dockerImage.push(tag)
                            imageName = "${dockerImage.imageName()}:$tag"
                        }

                    }
                }
            }
        }

        return imageName
    }
}