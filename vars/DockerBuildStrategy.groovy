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

//    def run(body) {
//        script.docker.image(env.image).inside(env.args) {
//            body()
//        }
//    }

    def build(body) {
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
////            script.stage('Build') {
////                run {
////                    script.sh 'gradle assemble'
////                }
////            }
////            script.stage('Test') {
////                run {
////                    script.sh 'gradle test'
////                }
////            }
//        }

        script.node('ecs') {
            script.stage('test') {
                script.checkout script.scm
                body()
            }
        }
    }
}