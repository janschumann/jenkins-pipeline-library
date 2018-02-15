package de.audibene.jenkins.pipeline

class DockerBuildStrategy implements BuildStrategy {

    private final def script
    private final Map<String, Closure> steps
    private final Map<String, Object> env

    DockerBuildStrategy(def script, config) {
        this.script = script
        this.steps = config.steps
        this.env = config.env
    }

    def inside(body) {
        script.docker.image(env.image).inside(env.args) {
            body()
        }
    }

    private def runStep(String name) {
        def body = steps[name]
        body.resolveStrategy = Closure.DELEGATE_FIRST
        body.delegate = this
        body()
    }

    @Override
    String build() {
        script.node('ecs') {
            script.stage('Prepare') {
                script.deleteDir()
                script.checkout script.scm
                runStep('prepare')
            }
            script.stage('Build') {
                runStep('build')
            }
            script.stage('Test') {
                runStep('test')
            }
        }

        return null
    }
}