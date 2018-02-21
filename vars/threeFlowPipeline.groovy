#!groovy
import de.audibene.jenkins.pipeline.builder.ArtifactBuilder
import de.audibene.jenkins.pipeline.builder.DockerfileBuilder
import de.audibene.jenkins.pipeline.deployer.ArtifactDeployer
import de.audibene.jenkins.pipeline.deployer.DockerBeansTalkDeployer
import de.audibene.jenkins.pipeline.exception.ApproveStepRejected
import de.audibene.jenkins.pipeline.promoter.FlowManager
import de.audibene.jenkins.pipeline.scm.Git
import de.audibene.jenkins.pipeline.scm.Scm

import static de.audibene.jenkins.pipeline.Configurers.configure

import static java.util.Objects.requireNonNull

def call(body) {
    timestamps {
        pipeline(body)
    }
}

def pipeline(body) {
    def buildTag = Long.toString(new Date().time, Character.MAX_RADIX)

    FlowConfig config = configure(new FlowConfig(this), body).validate()

    def builder = config.builder
    def deployer = config.deployer
    def branches = config.branches
    def environments = config.environments
    def flowManager = new FlowManager(this, config.scm)

    try {
        if (env.BRANCH_NAME.startsWith('PR-')) {
            echo 'PR Flow'
            builder.build(verbose: true)
        } else if (env.BRANCH_NAME == branches.snapshot) {
            echo 'Snapshot Flow'
            def tag = "snapshot-$buildTag"
            def artifact = builder.build(tag: tag)
            deployer.deploy(artifact: artifact, environment: environments.snapshot, auto: true)
            flowManager.promote(title: 'Create Candidate', branch: branches.candidate, start: true)
        } else if (env.BRANCH_NAME == branches.candidate) {
            echo 'Candidate Flow'
            def version = flowManager.resolveVersion()
            def tag = "candidate-$version-$buildTag"
            def artifact = builder.build(tag: tag, retag: ['candidate', 'snapshot'])
            deployer.deploy(artifact: artifact, environment: environments.candidate)
            flowManager.promote(title: 'Create Release', branch: branches.release, finish: true)
        } else if (env.BRANCH_NAME == branches.release) {
            echo 'Release Flow'
            def version = flowManager.resolveVersion(finished: true)
            def tag = "release-$version-$buildTag"
            def artifact = builder.build(tag: tag, retag: ['release', 'candidate'])
            deployer.deploy(artifact: artifact, environment: environments.release)
        }
    } catch (ApproveStepRejected ignore) {
        currentBuild.result = 'SUCCESS'
    }

}

class FlowConfig {
    def script
    Scm scm
    ArtifactBuilder builder
    ArtifactDeployer deployer
    Map<String, String> branches = [snapshot: 'develop', candidate: 'candidate', release: 'master']
    Map<String, String> environments = [snapshot: 'develop', candidate: 'staging', release: 'production']


    FlowConfig(script) {
        this.script = script
    }

    def git(Closure body) {
        def config = configure(body)
        scm = new Git(script, config).validated()
    }

    def dockerfileBuilder(Closure body) {
        DockerfileBuilder builder = new DockerfileBuilder(script, scm)
        this.builder = configure(builder, body).validated()
    }

    def dockerBeansTalkDeployer(Closure body) {
        def config = configure(body)
        def deployer = new DockerBeansTalkDeployer(script, config)
        this.deployer = deployer.validated()
    }

    def validate() {
        requireNonNull(script, 'FlowConfig.script')
        requireNonNull(scm, 'FlowConfig.scm')
        requireNonNull(builder, 'FlowConfig.builder')
        requireNonNull(deployer, 'FlowConfig.deployer')
        requireNonNull(branches, 'FlowConfig.branches')
        requireNonNull(environments, 'FlowConfig.environments')
        return this
    }

}
