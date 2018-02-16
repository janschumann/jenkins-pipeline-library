package de.audibene.jenkins.pipeline

class Git {

    private final def script

    Git(script) {
        this.script = script
    }

    def initialize() {
        def upstream = script.sh "git remote show  | grep upstream"
        echo "Upstream: $upstream"
    }

    def tag(tag) {
        initialize()
    }
}
