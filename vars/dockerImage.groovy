def call(Map params = [:], Closure body) {
    def config = configure(params, body)
    return new DockerImage(docker, config)
}

class DockerImage {
    private def docker
    private def config

    DockerImage(docker, config) {
        this.docker = docker
        this.config = config
    }

    def withArgs(args) {
        def newConfig = config.clone()
        newConfig.args = "${newConfig.args?:''} $args".trim()
        return new DockerImage(docker, newConfig)
    }

    def withLink(name, container) {
        return withArgs("--link ${container.id}:$name")
    }

    def inside(Closure body) {
        if (config.beforeRun) {
            config.beforeRun()
        }
        docker.image(config.id).inside(config.args) {
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

    def withRun(Closure body) {
        def image = this
        if (config.beforeRun) {
            config.beforeRun()
        }
        docker.image(config.id).withRun(config.args) { c ->
            def container = [image: image, id: c.id]
            if (config.beforeWithRun) {
                config.beforeWithRun(container)
            }
            body(container)
            if (config.afterWithRun) {
                config.afterWithRun(container)
            }
        }
        if (config.afterRun) {
            config.afterRun()
        }
    }
}