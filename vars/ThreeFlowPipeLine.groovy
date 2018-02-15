def call(body) {
    def config = Config(body)
    config.buildStrategy.build()
}
