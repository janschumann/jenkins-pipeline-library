#!groovy

def call(String name, boolean visible = true, Closure body) {
    if (env.BUILD_FASE == null) {
        env.BUILD_FASE = 'STARTED'
    }
    if (env.BUILD_FASE == 'STARTED') {
        if (visible) {
            stage(name, body)
        } else {
            body()
        }
    }
}