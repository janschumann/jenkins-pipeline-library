#!groovy

import de.audibene.jenkins.pipeline.builder.ArtifactBuilder
import de.audibene.jenkins.pipeline.deployer.ArtifactDeployer
import de.audibene.jenkins.pipeline.exception.ApproveStepRejected
import de.audibene.jenkins.pipeline.promoter.GitBuildPromoter
import de.audibene.jenkins.pipeline.scm.Scm

import static java.util.Objects.requireNonNull


def call(body) {
    timestamps {
        pipeline(body)
    }
}

def pipeline(body) {
    def config = configure(body)
    def buildTag = Long.toString(new Date().time, Character.MAX_RADIX)
    def builder = requireNonNull(config.builder, 'threeFlowPipeline(config[builder])') as ArtifactBuilder
    def deployer = requireNonNull(config.deployer, 'threeFlowPipeline(config[deployer])') as ArtifactDeployer
    def scm = requireNonNull(config.scm, 'threeFlowPipeline(config[builder])') as Scm
    def promoter = new GitBuildPromoter(this, [scm: scm])
    def branches = config.branches ?: [snapshot: 'master', candidate: 'candidate', release: 'release']
    def environments = config.environments ?: [snapshot: 'develop', candidate: 'staging', release: 'production']

    try {
        if (env.BRANCH_NAME.startsWith('test-')) {
            echo "Test flow"
            buildNode {
                scm.checkout()
                tags = (sh('git tag --points-at HEAD')).split('\n')
                echo "Tags: $tags"

            }
        } else if (env.BRANCH_NAME.startsWith('PR-')) {
            echo 'PR Flow'
            builder.build(verbose: true, scm: scm)
        } else if (env.BRANCH_NAME == branches.snapshot) {
            echo 'Snapshot Flow'
            def tag = "snapshot-$buildTag"
            def artifact = builder.build(tag: tag, scm: scm)
            deployer.deploy(artifact: artifact, environment: environments.snapshot, auto: true)
            promoter.promote(branch: branches.candidate)
        } else if (env.BRANCH_NAME == branches.candidate) {
            echo 'Candidate Flow'
            def tag = "candidate-$buildTag"
            def artifact = builder.build(tag: tag, scm: scm)
            deployer.deploy(artifact: artifact, environment: environments.candidate)
            promoter.promote(branch: branches.release)
        } else if (env.BRANCH_NAME == branches.release) {
            echo 'Release Flow'
            def tag = "release-$buildTag"
            def artifact = builder.build(tag: tag, scm: scm)
            deployer.deploy(artifact: artifact, environment: environments.release)
        }
    } catch (ApproveStepRejected ignore) {
        currentBuild.result = 'SUCCESS'
    }

}
