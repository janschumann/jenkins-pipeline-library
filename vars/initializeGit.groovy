def call(body) {
    this.git = new Git(this, configs(body))
    git.initialize()
    return git
}

class Git {
    private final def script
    private final Map config
    private String upstream

    Git(script, config) {
        this.script = script
        this.config = config
    }

    def initialize() {
        script.sh "git config user.name '${config.username}'"
        script.sh "git config user.email '${config.email}'"
        upstream = "git@github.com:Audibene-GMBH/audibene-microservice.git"
    }

    def tag(tag) {
        script.sh "git tag -a $tag -m 'create tag: $tag'"
        script.sh "git push $upstream --tags"
    }
}