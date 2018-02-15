package de.audibene.jenkins.pipeline

interface BuildStrategy extends Serializable{

    /**
     * @return built artifact id
     */
    String build()

}
