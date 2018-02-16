#!groovy

def call(String name, boolean visible = true, Closure body) {
    if (visible) {
        stage(name, body)
    } else {
        body()
    }
}