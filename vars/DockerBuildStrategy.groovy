MyDelegate call(Closure body){

    def config = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    body()

    echo "Config $config"
    return new MyDelegate(this, config)
}

class MyDelegate implements Serializable {

    private final def script
    private final Map<String, Closure> steps
    private final Map<String, Object> env

    MyDelegate(def script, config) {
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

    def build() {
        script.node('ecs') {
            script.stage('Prepare') {
                script.sh 'ls -lah /root/.gradle'
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
    }


    String toString() {
        return "MyDelegate{steps=$steps, env=$env}"
    }
}