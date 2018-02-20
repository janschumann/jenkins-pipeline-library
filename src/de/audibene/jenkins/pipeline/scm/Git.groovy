package de.audibene.jenkins.pipeline.scm

class Git implements Scm {
    private final def script
    private final Map config

    Git(script, config) {
        this.script = script
        this.config = config
    }

    @Override
    def checkout() {
        script.buildNode {
            script.checkout script.scm
        }
    }

    @Override
    def tag(String tag) {
        execute "git tag -a $tag -m 'create tag: $tag'"
        execute "git push origin --tags"
    }

    @Override
    def branch(String branch) {
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
