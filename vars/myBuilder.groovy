def call(Closure body) {
    return new MyBuilder(this, body)
}


class MyBuilder implements Serializable {
    private final def script
    private final Closure body

    @NonCPS
    MyBuilder(final script, final Closure body) {
        this.script = script
        this.body = body
    }

    def myRun() {
        script.node('ecs') {
            script.stage('test') {
                script.checkout script.scm
                body()
            }
        }
    }
}
