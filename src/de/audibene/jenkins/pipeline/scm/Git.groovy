package de.audibene.jenkins.pipeline.scm

class Git implements Scm {
    private final def script
    private final Map config

    Git(script, config) {
        this.script = script
        this.config = config
    }

    @Override
    void checkout() {
        script.buildNode {
            script.checkout script.scm
        }
    }

    @Override
    void checkout(final Closure body) {
        script.buildNode {
            script.checkout script.scm
            try {
                body()
            } finally {
                script.deleteDir()
            }
        }
    }

    @Override
    void tag(String tag) {
        execute {
            script.sh "git tag -a $tag -m 'create tag: $tag'"
            script.sh "git push origin --tags"
        }
    }

    @Override
    void branch(String branch) {
        execute {
            script.sh "git push origin HEAD:$branch"
        }
    }

    @Override
    List<String> headTags() {
        execute {
            script.sh 'git tag -l | xargs git tag -d'
            script.sh 'git fetch --tags'
            String output = script.sh(returnStdout: true, script: 'git tag --list --points-at HEAD')
            output.tokenize("\n").collect { it.trim() }
        }
    }

    @Override
    List<String> branchTags() {
        execute {
            script.sh 'git tag -l | xargs git tag -d'
            script.sh 'git fetch --tags'
            String output = script.sh(returnStdout: true, script: 'git tag --list --merged HEAD')
            output.tokenize("\n").collect { it.trim() }
        }
    }

    private <T> T execute(body) {
        script.buildNode {
            script.sh "git config user.name '${config.username}'"
            script.sh "git config user.email '${config.email}'"

            script.sshagent([config.credentials]) {
                body()
            }
        }
    }
}
