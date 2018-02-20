package de.audibene.jenkins.pipeline.promoter

import de.audibene.jenkins.pipeline.scm.Scm

import static java.util.Objects.requireNonNull

class GitBuildPromoter {

    private final def script
    private final Scm scm

    GitBuildPromoter(script, config = [:]) {
        this.script = script
        this.scm = requireNonNull(config.scm, 'GitBuildPromoter.init(config[scm]') as Scm

    }

    def promote(Map params) {
        String branch = requireNonNull(params.branch, 'GitBuildPromoter.promote(params[branch])')

        script.approveStep("Promote to ${branch}?")

        script.buildStep("Promote to ${branch}") {
            script.buildNode {
                scm.checkout()
                scm.branch(branch)
            }
        }
    }
}
