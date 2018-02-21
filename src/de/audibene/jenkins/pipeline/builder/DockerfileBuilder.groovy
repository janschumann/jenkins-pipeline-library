package de.audibene.jenkins.pipeline.builder

import de.audibene.jenkins.pipeline.scm.Scm

import static de.audibene.jenkins.pipeline.Configurers.configure
import static de.audibene.jenkins.pipeline.Configurers.configure
import static java.util.Objects.requireNonNull

class DockerfileBuilder implements ArtifactBuilder {

    private final def script
    private final Scm scm

    private final Map<String, Object> artifact
    private final Map<String, Closure> steps

    DockerfileBuilder(def script, Scm scm, Map steps = [:], Map artifact = [:]) {
        this.script = script
        this.scm = scm
        this.artifact = artifact
        this.steps = steps
    }

    @Override
    String build(Map params = [:]) {
        String tag = params.tag
        List<String> retagPrefixes = params.get('retag', []) as List<String>
        boolean verbose = params.get('verbose', false)

        def imageName = null

        script.buildNode('ecs') {
            script.buildStep('Build', !verbose) {
                scm.checkout {
                    def existed = retagPrefixes ? existedTag(retagPrefixes) : null
                    imageName = existed ? doRetag(tag, verbose, existed) : null
                    imageName = imageName ?: doBuild(tag, verbose)
                    scm.tag(tag)
                }
            }
        }
        script.echo "Build result $imageName"
        return imageName
    }

    private String existedTag(List<String> prefixes) {
        List<String> tags = scm.headTags()
        for (def candidate : prefixes) {
            def tag = tags.findAll { it.startsWith(candidate) }.max()
            if (tag) return tag
        }
        return null
    }

    private String doRetag(String tag, boolean verbose, String existed) {
        def imageName = null
        if (existed) {
            script.buildStep('Retag', verbose) {
                script.docker.withRegistry(artifact.registry) {
                    loginEcrRepository()
                    def existedImage = script.docker.image("${artifact.name}:$existed").imageName()
                    script.docker.image(existedImage).pull()
                    imageName = script.docker.image(existedImage).tag(tag)
                    script.docker.image(imageName).push()
                }
            }
        }
        return imageName
    }

    private String doBuild(String tag, boolean verbose) {
        if (steps['prepare']) {
            script.buildStep('Prepare', verbose, steps['prepare'])
        }
        if (steps['unitTest']) {
            script.buildStep('Test', verbose, steps['unitTest'])
        }
        if (steps['itTest']) {
            script.buildStep('IT', verbose, steps['itTest'])
        }

        def imageName = null
        script.buildStep('Build', verbose) {
            if (steps['build']) {
                steps['build']()
            }
            def dockerImage = script.docker.build("${artifact.name}:$tag")
            if (tag != null) {
                script.docker.withRegistry(artifact.registry) {
                    loginEcrRepository()
                    dockerImage.push(tag)
                    imageName = dockerImage.imageName()
                }
            }
        }
        return imageName
    }

    private void loginEcrRepository() {
        script.sh script.ecrLogin()
    }

    def artifact(Closure artifactBody) {
        configure(artifact, artifactBody)
    }

    def steps(Closure stepsBody) {
        configure(steps, stepsBody)
    }

    DockerfileBuilder validated() {
        requireNonNull(script, 'DockerfileBuilder.script')
        requireNonNull(scm, 'DockerfileBuilder.scm')
        requireNonNull(steps, 'DockerfileBuilder.steps')

        requireNonNull(artifact, 'DockerfileBuilder.artifact')
        requireNonNull(artifact.name, 'DockerfileBuilder.artifact.name')
        requireNonNull(artifact.registry, 'DockerfileBuilder.artifact.registry')

        return this
    }
}