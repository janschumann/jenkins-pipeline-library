def call(body) {
    return new Git(this, configs(body))
}

class Git {
    private final def script
    private final Map config
    private String upstream

    Git(script, config) {
        this.script = script
        this.config = config
        this.script.git = this
    }

    def initialize() {
        script.sh "git config user.name '${config.username}'"
        script.sh "git config user.email '${config.email}'"
        upstream = "git@github.com:Audibene-GMBH/audibene-microservice.git"
    }

    def tag(tag) {
        execute {
            script.sh "git tag -a $tag -m 'create tag: $tag'"
            script.sh "git push $upstream --tags"
        }
    }

    def branch(branch) {
        execute {
            script.sh "git push $upstream HEAD:$branch"
        }
    }

    def execute(body) {
        initialize()
        script.sshagent([config.credentials]){
            body
        }
    }

}