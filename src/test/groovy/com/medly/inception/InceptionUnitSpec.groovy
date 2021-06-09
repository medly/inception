package com.medly.inception

import com.gradle.publish.PluginBundleExtension
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

class InceptionUnitSpec extends Specification {

    Project project

    def setup(){
        project = ProjectBuilder.builder()
            .withName("MyPluginProject")
            .build()

        project.getPluginManager()
            .apply("com.medly.inception")
    }

    def "Publish plugin is applied"() {
        expect:
        project.getPluginManager().hasPlugin("com.gradle.plugin-publish")
        project.getPluginManager().hasPlugin("groovy-gradle-plugin")
        project.getTasks().getByName("pluginUnderTestMetadata") != null
        project.getTasks().getByName("publishPlugins") != null
    }

    def "Repos are configured"() {
        expect:
        project.getRepositories()*.name == ["Gradle Central Plugin Repository", "MavenRepo"]
        println project.description
    }
}
