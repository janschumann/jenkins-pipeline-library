#!groovy

import de.audibene.jenkins.pipeline.builder.ArtifactBuilder
import de.audibene.jenkins.pipeline.deployer.ArtifactDeployer
import de.audibene.jenkins.pipeline.exception.ApproveStepRejected
import de.audibene.jenkins.pipeline.promoter.FlowManager
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
    def flowManager = new FlowManager(this, [scm: scm])
    def branches = config.branches ?: [snapshot: 'develop', candidate: 'candidate', release: 'master']
    def environments = config.environments ?: [snapshot: 'develop', candidate: 'staging', release: 'production']

    try {
        if (env.BRANCH_NAME.startsWith('PR-')) {
            echo 'PR Flow'
            builder.build(verbose: true, scm: scm)
        } else if (env.BRANCH_NAME == branches.snapshot) {
            echo 'Snapshot Flow'
            def tag = "snapshot-$buildTag"
            def artifact = builder.build(tag: tag, scm: scm)
            deployer.deploy(artifact: artifact, environment: environments.snapshot, auto: true)
            flowManager.promote(title: 'Create Candidate', branch: branches.candidate, start: true)
        } else if (env.BRANCH_NAME == branches.candidate) {
            echo 'Candidate Flow'
            def version = flowManager.resolveVersion()
            def tag = "candidate-$version-$buildTag"
            def artifact = builder.build(tag: tag, scm: scm, retag: ['candidate', 'snapshot'])
            deployer.deploy(artifact: artifact, environment: environments.candidate)
            flowManager.promote(title: 'Create Release', branch: branches.release, finish: true)
        } else if (env.BRANCH_NAME == branches.release) {
            echo 'Release Flow'
            def version = flowManager.resolveVersion(finished: true)
            def tag = "release-$version-$buildTag"
            def artifact = builder.build(tag: tag, scm: scm, retag: ['release', 'candidate'])
            deployer.deploy(artifact: artifact, environment: environments.release)
        }
    } catch (ApproveStepRejected ignore) {
        currentBuild.result = 'SUCCESS'
    }

}
