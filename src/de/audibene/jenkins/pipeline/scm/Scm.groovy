package de.audibene.jenkins.pipeline.scm

interface Scm {
    void checkout()
    void checkout(Closure body)
    void tag(String tag)
    void branch(String branch)
    List<String> headTags()
    List<String> branchTags()
}