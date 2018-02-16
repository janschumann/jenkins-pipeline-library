package de.audibene.jenkins.pipeline.promoter

import static de.audibene.jenkins.pipeline.Objects.requireNonNull

class BuildPromoter {

    private final def script
    private final Map params

    BuildPromoter(script, params = [:]) {
        this.script = script
        this.params = params
    }

    def promote(Map params) {
        def branch = params.branch ?: requireNonNull('branch')

        script.approveStep("Promote to ${branch}?")

        script.buildStep("Promote to ${branch}") {
            script.echo "TODO: Promote $branch"
        }
    }
}
