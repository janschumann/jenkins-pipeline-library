package de.audibene.jenkins.pipeline.docker

import static java.util.Objects.requireNonNull

class DockerImage {

    public static final def HOOK_NAMES = [
            'beforeRun',
            'afterRun',
            'beforeAround',
            'afterAround',
            'beforeInside',
            'afterInside'
    ]

    private def script
    private Map<String, ?> config

    private DockerImage(def script, Map config) {
        this.script = script
        this.config = config
        requireNonNull(config.id, 'DockerImage.init(config[id])')
        requireNonNull(config.args, 'DockerImage.init(config[args])')
        for(String hook: HOOK_NAMES) {
            requireNonNull(config[hook], "DockerImage.init(config[${hook}])")
        }
    }

    static DockerImage create(def script, Map config) {
        config.args = config.args ?: ''
        for (String name : HOOK_NAMES) {
            config[name] = config.get(name, {})
        }
        return new DockerImage(script, config)
    }

    def args(args = '') {
        Map config = this.config.clone() as Map
        config.args = "${config.args} ${args}".trim()
        return new DockerImage(script, config)
    }

    def link(String name, String id) {
        return args("--link $id:$name")
    }

    def link(String name, String id, Closure body) {
        link(name, id).inside(body)
    }

    def with(String name, DockerImage image, Closure body) {
        image.around { String id, _ -> link(name, id, body) }
    }

    def call(Closure body) {
        inside(body)
    }

    def inside(Closure body) {
        config.beforeRun()
        script.docker.image(config.id).inside(config.args) {
            config.beforeInside()
            body()
            config.afterInside()
        }
        config.afterRun()
    }

    def around(Closure body) {
        def image = this
        config.beforeRun()
        script.docker.image(config.id).withRun(config.args) { container ->
            config.beforeAround(container.id, image)
            body(container.id, image)
            config.afterAround(container.id, image)
        }
        config.afterRun()
    }
}

