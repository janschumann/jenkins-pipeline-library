#!groovy
import de.audibene.jenkins.pipeline.deployer.BeansTalkDeployer

def call(Closure body) {
    def config = configure(body)
    echo "beanstalk config $config"
    return new BeansTalkDeployer(this, config)
}