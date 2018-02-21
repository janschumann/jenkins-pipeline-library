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
    private final String id
    private final String args
    private Map<String, ?> hooks

    private DockerImage(script, String id, String args, Map hooks) {
        this.script = script
        this.id = id
        this.args = args
        this.hooks = hooks
    }

    static DockerImage create(script, Map config) {
        String args = config.remove('args')
        String id = config.remove('id')
        for (String name : HOOK_NAMES) {
            config[name] = config.get(name, {})
        }
        return new DockerImage(script, id, args, config).validated()
    }

    def args(extra = '') {
        return new DockerImage(script, id, "${args} ${extra}".trim(), new LinkedHashMap(hooks))
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
        hooks.beforeRun()
        script.docker.image(id).inside(args) {
            hooks.beforeInside()
            body()
            hooks.afterInside()
        }
        hooks.afterRun()
    }

    def around(Closure body) {
        def image = this
        hooks.beforeRun()
        script.docker.image(id).withRun(args) { container ->
            hooks.beforeAround(container.id, image)
            body(container.id, image)
            hooks.afterAround(container.id, image)
        }
        hooks.afterRun()
    }

    DockerImage validated() {
        requireNonNull(id, 'DockerImage.id')
        requireNonNull(args, 'DockerImage.args')
        for(String hook: HOOK_NAMES) {
            requireNonNull(hooks[hook], "DockerImage.hooks.${hook}")
        }
        return this
    }
}

