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

    def tag(tag) {
        execute "git tag -a $tag -m 'create tag: $tag'"
        execute "git push $upstream --tags"
    }

    def branch(branch) {
        execute "git push $upstream HEAD:$branch"
    }

    def execute(command) {
        script.sh "git config user.name '${config.username}'"
        script.sh "git config user.email '${config.email}'"
        script.sh command
    }

}