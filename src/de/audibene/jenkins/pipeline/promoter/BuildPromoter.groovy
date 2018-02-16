package de.audibene.jenkins.pipeline.promoter

class BuildPromoter {

    private final def script
    private final Map params

    BuildPromoter(script, params = [:]) {
        this.script = script
        this.params = params
    }

    def promote(Map params) {
        def branch = Objects.requireNonNull(params.branch, 'BuildPromoter.branch')

        script.approveStep("Promote to ${branch}?")

        script.buildStep("Promote to ${branch}") {
            script.echo "TODO: Promote $branch"
        }
    }
}
