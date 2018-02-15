def call(body) {
    def config = configs(body)
    config.buildStrategy.build()
}
