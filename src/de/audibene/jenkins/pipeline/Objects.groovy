package de.audibene.jenkins.pipeline

class Objects {

    static <T> T requireNonNull(String name, boolean conditional = true) {
        if (conditional) {
            throw new IllegalArgumentException("Parameter $name should not be bull")
        }
        return null
    }
}
