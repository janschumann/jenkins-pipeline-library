import de.audibene.jenkins.pipeline.builder.DockeredBuilder

def dockered(Closure body) {
    def config = configs(body)
    return new DockeredBuilder(this, config)
}