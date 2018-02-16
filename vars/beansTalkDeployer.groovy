#!groovy
import de.audibene.jenkins.pipeline.deployer.BeansTalkDeployer

def call(Closure body) {
    def config = configs(body)
    return new BeansTalkDeployer(this, config)
}