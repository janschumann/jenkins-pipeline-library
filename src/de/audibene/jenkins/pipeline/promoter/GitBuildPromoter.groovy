package de.audibene.jenkins.pipeline.promoter

class GitBuildPromoter {

    private final def script
    private final def scm

    GitBuildPromoter(script, params = [:]) {
        this.script = script
        this.scm = params.scm
    }

    def promote(Map params) {
        def branch = params.branch

        script.approveStep("Promote to ${branch}?")

        script.buildStep("Promote to ${branch}") {
            script.buildNode {
                scm.checkout()
                scm.branch(branch)
            }
        }
    }
}
