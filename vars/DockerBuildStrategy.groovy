def call(Closure body) {
    return new MyDelegate(this, body)
}

class MyDelegate implements Serializable {

    private final def script
    private final def steps = [:]
    private final def env = [:]

    MyDelegate(def script, Closure body) {
        this.script = script
//        body.resolveStrategy = Closure.DELEGATE_FIRST
//        body.delegate = this
//        body()
//
//        script.echo this
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