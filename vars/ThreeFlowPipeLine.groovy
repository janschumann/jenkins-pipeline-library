def call(body) {
    config = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    body()

    echo "ThreeFlowPipeLine.config: $config"

    config.buildStrategy.build()
}

return this