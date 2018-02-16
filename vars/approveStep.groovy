def call(String name, Map params = [:]) {
    def time = params.get('time', Long.MAX_VALUE)
    def unit = params.get('unit', Long.DAYS)
    def message = params.message ?: name
    def timeoutAs = params.get('timeoutAs', false)

    def approve = getApprove(name, time, unit, message, timeoutAs)

    if (!approve) {
        env.BUILD_FASE = 'STOPPED'
    }
}

private def getApprove(String name, time, unit, message, timeoutAs) {
    buildStep(name) {
        try {
            timeout(time: time, unit: unit) {
                input(message)
            }

            return [result: true, userName: getApprover()]
        } catch (FlowInterruptedException e) {
            def rejectedBy = getRejectedBy(e)
            def result = rejectedBy != "SYSTEM" || timeoutAs
            return [result: result, userName: rejectedBy]
        }
    }
}


@NonCPS
def getRejectedBy(FlowInterruptedException e) {
    def rejection = e.causes[0]

    return rejection.user?.displayName ?: "SYSTEM"
}

//http://stackoverflow.com/questions/32858782/get-input-approver-user-name-in-jenkins-workflow-plugin
//todo approving "method org.jenkinsci.plugins.workflow.support.steps.build.RunWrapper getRawBuild" may introduce security vulnerability
@NonCPS
def getApprover() {
    def latest = null

    // this returns a CopyOnWriteArrayList, safe for iteration
    def acts = currentBuild.rawBuild.getAllActions()
    for (act in acts) {
        if (act instanceof org.jenkinsci.plugins.workflow.support.steps.input.ApproverAction) {
            latest = act.userName
        }
    }
    return latest
}

