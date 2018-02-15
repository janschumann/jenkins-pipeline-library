def threeFlow(body) {
    def config = configs(body)

    def tag = Long.toString(new Date().time, Character.MAX_RADIX)
    def builder = config.builder

    if (env.BRANCH_NAME.startsWith("PR-")) {
        echo 'PR Flow'
        builder.build()
    } else {
        echo 'Snapshot Flow'
        builder.build(push: true, tag: tag, verbose: false)
    }
}
