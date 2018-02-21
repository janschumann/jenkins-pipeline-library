import de.audibene.jenkins.pipeline.docker.DockerImage
import static de.audibene.jenkins.pipeline.Configurers.configure

def call(Map params = [:], Closure body = {}) {
    Map config = configure(params, body)
    return DockerImage.create(this, config)
}

