#!groovy

import de.audibene.jenkins.pipeline.builder.ArtifactBuilder
import de.audibene.jenkins.pipeline.deployer.ArtifactDeployer
import de.audibene.jenkins.pipeline.exception.ApproveStepRejected
import de.audibene.jenkins.pipeline.promoter.GitBuildPromoter


def call(body) {
    timestamps {
        pipeline(body)
    }
}

def pipeline(body) {
    def config = configure(body)

    def build = Long.toString(new Date().time, Character.MAX_RADIX)
    def builder = config.builder as ArtifactBuilder
    def deployer = config.deployer as ArtifactDeployer
    def scm = config.scm
    def promoter = new GitBuildPromoter(this, [scm: scm])

    try {
        if (env.BRANCH_NAME.startsWith('PR-')) {
            echo 'PR Flow'
            builder.build(verbose: true, scm: scm)
        } else if (env.BRANCH_NAME == 'master' || env.BRANCH_NAME.startsWith('test-')) {
            echo 'Snapshot Flow'
            def tag = "snapshot-$builder"
            def artifact = builder.build(push: true, tag: tag, scm: scm)
            deployer.deploy(artifact: artifact, environment: 'develop', tag: tag, auto: true)
            promoter.promote(branch: 'candidate')
        } else if (env.BRANCH_NAME == 'candidate') {
            echo 'Candidate Flow'
            def tag = "candidate-$builder"
            def artifact = builder.build(push: true, tag: tag, scm: scm)
            deployer.deploy(artifact: artifact, environment: 'staging', tag: tag)
            promoter.promote(branch: 'release')
        } else if (env.BRANCH_NAME == 'release') {
            echo 'Release Flow'
            def tag = "release-$build"
            def artifact = builder.build(push: true, tag: tag, scm: scm)
            deployer.deploy(artifact: artifact, environment: 'production', tag: tag)
        }
    } catch (ApproveStepRejected ignore) {
        currentBuild.result = 'SUCCESS'
    }

}
