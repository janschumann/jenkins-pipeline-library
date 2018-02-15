def threeFlow(body) {
    def config = configs(body)

    def tag = Long.toString(new Date().time, Character.MAX_RADIX)


    if (env.BRANCH_NAME.startsWith("PR-")) {
        echo 'PR Flow'
        config.buildStrategy.build()
    } else {
        echo 'Snapshot Flow'
        config.buildStrategy.build(push: true, tag: tag, verbose: false)
    }
}
