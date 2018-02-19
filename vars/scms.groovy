#!groovy
import de.audibene.jenkins.pipeline.scm.Git

def call(type, Closure body) {
    switch (type) {
        case 'git':
            return new Git(this, configure(body))
        default:
            throw new IllegalArgumentException("Unknown scm type: $type")
    }
}