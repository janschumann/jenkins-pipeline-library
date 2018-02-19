package de.audibene.jenkins.pipeline.scm

interface Scm {
    def checkout()
    def tag(String tag)
    def branch(String branch)
}