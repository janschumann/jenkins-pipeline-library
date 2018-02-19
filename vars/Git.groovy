#!groovy

def call(Closure body) {
    return new Delegate(this, configure(body))
}

class Delegate {
    private final def script
    private final Map config

    Delegate(script, config) {
        this.script = script
        this.config = config
        this.script.git = this
    }

    def checkout() {
        script.buildNode {
            script.checkout script.scm
        }
    }

    def tag(tag) {
        execute "git tag -a $tag -m 'create tag: $tag'"
        execute "git push origin --tags"
    }

    def branch(branch) {
        execute "git push origin HEAD:$branch"
    }

    def execute(command) {
        script.buildNode {
            script.sh "git config user.name '${config.username}'"
            script.sh "git config user.email '${config.email}'"

            script.sshagent([config.credentials]) {
                script.sh command
            }
        }
    }
}