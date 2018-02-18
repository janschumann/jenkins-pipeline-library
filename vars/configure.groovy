#!groovy

def call(config = [:], Closure body) {
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    body()
    return config
}