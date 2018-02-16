private <V> V call(name = null, Closure<V> body) {
    if (script.env.NODE_NAME != null) {
        body()
    } else {
        script.node(name) {
            body()
        }
    }
}