package de.audibene.jenkins.pipeline.docker

import static java.util.Objects.requireNonNull

class DockerImage {
    private def script
    private def config

    DockerImage(script, config) {
        this.script = script
        requireNonNull(config.id, 'DockerImage.init(config[id])')
    }

    def withArgs(args) {
        Map config = this.config.clone() as Map
        config.args = "${config.args ?: ''} ${args ?: ''}".trim()
        return new DockerImage(script, config)
    }

    def withLink(name, container) {
        return withArgs("--link ${container.id}:$name")
    }

    def inside(Closure body) {
        script.echo "DockerImage.inside with config: $config"
        if (config.beforeRun) {
            config.beforeRun()
        }
        script.docker.image(config.id).inside(config.args) {
            if (config.beforeInside) {
                config.beforeInside()
            }
            body()
            if (config.afterInside) {
                config.afternside()
            }
        }
        if (config.afterRun) {
            config.afterRun()
        }
    }

    def around(Closure body) {
        script.echo "DockerImage.around with config: $config"
        def image = this
        if (config.beforeRun) {
            config.beforeRun()
        }
        script.docker.image(config.id).withRun(config.args) {
            def container = [image: image, id: it.id]
            if (config.beforeAround) {
                config.beforeAround(container)
            }
            body(container)
            if (config.afterAround) {
                config.afterAround(container)
            }
        }
        if (config.afterRun) {
            config.afterRun()
        }
    }
}

