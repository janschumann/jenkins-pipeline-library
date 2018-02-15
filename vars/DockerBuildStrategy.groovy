MyDelegate call(Closure body){

    def config = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    body()

    return new MyDelegate(this, config)
}

class MyDelegate implements Serializable {

    private final def script
    private final def steps
    private final def env

    MyDelegate(def script, config) {
        this.script = script
        this.steps = config.steps
        this.env = config.env
        script.echo "$this"
    }

    def run(body) {
        script.docker.image(env.image).inside(env.args) {
            body()
        }
    }

    def build() {
//        script.node('ecs') {
//            script.stage('Prepare') {
//                script.docker.image(env.image).pull()
//
//                script.deleteDir()
//
//                script.checkout script.scm
//
//                script.sh 'ls -lah'
//            }
//            script.stage('Build') {
//                script.docker.image(env.image).inside(env.args) {
//                    script.sh 'gradle assemble'
//                }
//            }
//            script.stage('Test') {
//                script.docker.image(env.image).inside(env.args) {
//                    script.sh 'gradle test'
//                }
//            }
//        }
    }


    String toString() {
        return "MyDelegate{steps=$steps, env=$env}"
    }
}