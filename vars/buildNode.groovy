#!groovy

def <V> V call(name = null, Closure<V> body) {
    if (env.NODE_NAME != null) {
        body()
    } else {
        node(name) {
            body()
        }
    }
}