package de.audibene.jenkins.pipeline.promoter

import de.audibene.jenkins.pipeline.scm.Scm

import static java.util.Comparator.reverseOrder
import static java.util.Objects.requireNonNull

class FlowManager {

    private final def script
    private final Scm scm

    FlowManager(script, config = [:]) {
        this.script = script
        this.scm = requireNonNull(config.scm, 'FlowManager.init(config[scm]') as Scm

    }

    def promote(Map params) {
        String branch = requireNonNull(params.branch, 'FlowManager.promote(params[branch])')
        String title = params.get('title', "Promote to ${branch}")

        script.approveStep("$title?")

        script.buildStep(title) {
            scm.checkout {
                if (params.get('start', false)) {
                    boolean allowStarted = params.get('allowStarted', true)
                    String increment = params.get('increment', '0.1.0')
                    startVersion(allowStarted, increment)
                }

                if (params.get('finish', false)) {
                    boolean allowFinished = params.get('allowFinished', true)
                    finishVersion(allowFinished)
                }

                scm.branch(branch)
            }
        }
    }

    def resolveVersion(Map params = [:]) {
        scm.checkout {
            String versionTag = resolveVersionTag()
            String version = extractVersion(versionTag)

            if (params.get('started', false)) {
                if (!versionTag.endsWith('-started')) {
                    throw new IllegalArgumentException("Version $version already finished")
                }
            }
            if (params.get('finished', false)) {
                if (!versionTag.endsWith('-end')) {
                    throw new IllegalArgumentException("Version $version not yet finished")
                }
            }

            return version
        }
    }

    private resolveVersionTag() {
        List<String> versionTags = scm.branchTags().findAll { it.startsWith('version') }.toSorted(reverseOrder())
        String versionTag = versionTags.find() ?: 'initial'
        return versionTag
    }

    private void finishVersion(boolean allowFinished) {
        String versionTag = resolveVersionTag()
        if (versionTag.endsWith('-begin')) {
            scm.tag(versionTag.replace('-begin', '-end'))
        } else {
            if (!allowFinished) {
                def version = extractVersion(versionTag)
                throw new IllegalArgumentException("Version $version already finished")
            }
        }
    }

    private void startVersion(boolean allowStarted, String increment) {
        String versionTag = resolveVersionTag()
        String version = extractVersion(versionTag)

        if (versionTag.endsWith('-begin')) {
            if (!allowStarted) {
                throw new IllegalArgumentException("Version $version already started")
            }
        } else if (versionTag.endsWith('-end')) {
            def next = nextVersion(version, increment)
            scm.tag("version-$next-begin")
        } else {
            scm.tag('version-1.0.0-begin')
        }
    }

    private static String extractVersion(String versionTag) {
        return versionTag.replace('version-', '').replace('-begin', '').replace('-end', '')
    }

    private static String nextVersion(String version, String increment) {
        def versions = version.tokenize('.')
        def increments = increment.tokenize('.')

        for (int index : 0..2) {
            int versionValue = Integer.parseInt(versions[index])
            int incrementValue = Integer.parseInt(increments[index])
            versions[index] = "${versionValue + incrementValue}"
        }

        return versions.join('.')
    }

}
