package de.audibene.jenkins.pipeline.builder

interface ArtifactBuilder extends Serializable{

    /**
     * @return built artifact id
     */
    String build(Map parameters)

}
