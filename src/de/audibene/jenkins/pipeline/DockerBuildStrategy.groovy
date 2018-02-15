package de.audibene.jenkins.pipeline

class DockerBuildStrategy implements BuildStrategy {

    private final def script
    private final Map<String, Closure> steps
    private final Map<String, Object> image
    private final Map<String, Object> artifact

    DockerBuildStrategy(def script, config) {
        this.script = script
        this.steps = config.steps
        this.image = config.image
        this.artifact = config.artifact
    }

    def inside(def image = this.image, body) {
        script.docker.image(image.id).inside(image.args) {
            body()
        }
    }

    def withRun(def image, Closure body) {
        script.docker.image(image.id).withRun(image.args) { container ->
            body("--link ${container.id}:${image.linkAs}")
        }
    }

    def insideWithPostgres(Map params = [:], Closure body) {
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
    String build(String tag) {
        script.node('ecs') {
            script.stage('Prepare') {
                script.deleteDir()
                script.checkout script.scm
                runStep('prepare')
            }
            script.stage('Test') {
                runStep('test')
            }
            script.stage('IT') {
                runStep('it')
            }
            script.stage('Build') {
                runStep('build')
                def dockerImage = script.docker.build(artifact.name)
                script.docker.withRegistry(artifact.registry) {
                    script.sh script.ecrLogin()
                    dockerImage.push(tag)
                }
            }
        }

        return null
    }
}