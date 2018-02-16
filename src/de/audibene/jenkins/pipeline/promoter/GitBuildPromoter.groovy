package de.audibene.jenkins.pipeline.promoter

class GitBuildPromoter {

    private final def script
    private final def git

    GitBuildPromoter(script, params = [:]) {
        this.script = script
        this.git = params.git
    }

    def promote(Map params) {
        def branch = params.branch

        script.approveStep("Promote to ${branch}?")

        script.buildStep("Promote to ${branch}") {
            script.checkout script.scm
            git.branch(branch)
        }
    }
}
