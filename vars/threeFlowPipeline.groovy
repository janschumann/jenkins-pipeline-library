import de.audibene.jenkins.pipeline.promoter.BuildPromoter

import static de.audibene.jenkins.pipeline.Objects.requireNonNull

def call(body) {
    def config = configs(body)

    def tag = Long.toString(new Date().time, Character.MAX_RADIX)
    def builder = config.builder ?: requireNonNull('builder')
    def deployer = config.deployer ?: requireNonNull('deployer')
    def promoter = config.promoter ?: new BuildPromoter(this)

    if (env.BRANCH_NAME.startsWith('PR-')) {
        echo 'PR Flow'
        builder.build()
    } else if (env.BRANCH_NAME == 'master' || env.BRANCH_NAME.startsWith('test-')) {
        echo 'Snapshot Flow'
        def artifact = builder.build(push: true, tag: "snapshot-$tag", verbose: false)
        deployer.deploy(artifact: artifact, environment: 'develop')
        promoter.promote(branch: 'candidate')
    } else if (env.BRANCH_NAME == 'candidate') {
        echo 'Snapshot Flow'
        def artifact = builder.build(push: true, tag: "candidate-$tag", verbose: false)
        deployer.deploy(artifact: artifact, environment: 'staging')
        promoter.promote(branch: 'release')
    } else if (env.BRANCH_NAME == 'release') {
        echo 'Snapshot Flow'
        def artifact = builder.build(push: true, tag: "release-$tag", verbose: false)
        deployer.deploy(artifact: artifact, environment: 'production')
    }
}
