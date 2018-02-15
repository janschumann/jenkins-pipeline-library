package de.audibene.jenkins.pipeline

interface ArtifactBuilder extends Serializable{

    /**
     * @return built artifact id
     */
    String build(Map parameters)

}
