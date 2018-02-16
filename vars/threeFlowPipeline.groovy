#!groovy

import de.audibene.jenkins.pipeline.builder.ArtifactBuilder
import de.audibene.jenkins.pipeline.deployer.ArtifactDeployer
import de.audibene.jenkins.pipeline.exception.ApproveStepRejected
import de.audibene.jenkins.pipeline.promoter.BuildPromoter


def call(body) {
    def config = configs(body)

    def tag = Long.toString(new Date().time, Character.MAX_RADIX)
    def builder = config.builder as ArtifactBuilder
    def deployer = config.deployer as ArtifactDeployer
    def promoter = new BuildPromoter(this)

    try {
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
    } catch (ApproveStepRejected ignore) {
        currentBuild.result = 'SUCCESS'
    }
}
