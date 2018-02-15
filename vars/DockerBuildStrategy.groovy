def call(Closure body) {
    config = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    body(this)

    echo "DockerBuildStrategy.config: $config"
}

def run(Closure body) {
    docker.image(config.env.image).inside(config.env.args) {
        body()
    }
}

def build() {
    node('ecs') {
        stage('Prepare') {
            docker.image(config.env.image).pull()
            checkout scm
        }
        stage('Build') {
            config.steps.build()
        }
        stage('Test') {
            config.steps.test()
        }
    }
}

return this
