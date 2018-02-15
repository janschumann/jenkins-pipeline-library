import de.audibene.jenkins.pipeline.builder.DockeredBuilder

def call(Closure body) {
    def config = configs(body)
    return new DockeredBuilder(this, config)
}