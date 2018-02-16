def call(body) {
    def config = configs(body)

    def tag = Long.toString(new Date().time, Character.MAX_RADIX)
    def builder = config.builder
    def deployer = config.deployer

    if (env.BRANCH_NAME.startsWith("PR-")) {
        echo 'PR Flow'
        builder.build()
    } else {
        echo 'Snapshot Flow'
        def artifact = builder.build(push: true, tag: tag, verbose: false)
        deployer.deploy(artifact: artifact, environment: 'develop')
    }
}
