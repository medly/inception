package com.medly.inception

import org.gradle.testkit.runner.GradleRunner
import spock.lang.Specification
import spock.lang.TempDir

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

class InceptionFunctionalSpec extends Specification {
    @TempDir
    File testProjectDir
    File settingsFile, buildFile
    File pluginFile, pluginTestFile

    def setup() {
        buildFile = new File(testProjectDir, 'build.gradle')
        settingsFile = new File(testProjectDir, 'settings.gradle')

        new File(testProjectDir, "src/main/groovy").mkdirs()
        new File(testProjectDir, "src/test/groovy/com/example/foo/").mkdirs()

        pluginFile = new File(testProjectDir, 'src/main/groovy/com.example.foo.gradle')
        pluginTestFile = new File(testProjectDir, 'src/test/groovy/com/example/foo/PluginSpec.groovy')
    }


    def "Dependencies are configured"() {
        given:
        settingsFile << """
         rootProject.name = "my-plugin"
         """

        buildFile << """
        plugins {
            id "com.medly.inception"
        }
        """

        when:
        def result = GradleRunner.create()
            .withPluginClasspath() // for custom plugin
            .withProjectDir(testProjectDir)
            .withArguments("dependencies")
            .build()

        then:
        result.task(":dependencies").outcome == SUCCESS
        result.output.contains('org.spockframework:spock-core:2.0-groovy-3.0')
        result.output.contains('org.junit.jupiter:junit-jupiter-api')

    }

    def "Plugin can be tested without any extra configuration"() {
        given: "a sample plugin-project with inception plugin applied"
        settingsFile << """
         rootProject.name = "my-plugin"
         """

        buildFile << """
        plugins {
            id "com.medly.inception"
        }
        """

        when: "we add logic and test to the plugin project"
        pluginFile << """
        tasks.register("helloWorld") {
            doLast {
                logger.lifecycle "Hello World!"
            }
        }
        """

        pluginTestFile << """
        package com.example.foo

        import org.gradle.testkit.runner.GradleRunner
        import spock.lang.Specification
        import spock.lang.TempDir

        import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

        class PluginSpec extends Specification {
            @TempDir
            File testProjectDir
            File settingsFile, buildFile


            def setup() {
                buildFile = new File(testProjectDir, 'build.gradle')
                settingsFile = new File(testProjectDir, 'settings.gradle')
            }

            def "helloWorld task works"() {
                given:
                settingsFile << '''
                    rootProject.name = "my-plugin-test"
                '''

                buildFile << '''
                plugins {
                    id "com.example.foo"
                }
                '''

                when:
                def result = GradleRunner.create()
                    .withPluginClasspath()
                    .withProjectDir(testProjectDir)
                    .withArguments('helloWorld')
                    .build()

                then:
                result.output.contains('Hello World!')
                result.task(":helloWorld").outcome == SUCCESS
            }
        }
        """

        and: "test task is executed"
        def result = GradleRunner.create()
            .withPluginClasspath() // for custom plugin
            .withProjectDir(testProjectDir)
            .withArguments("test")
            .build()

        then: "it should succeed"
        result.task(":test").outcome == SUCCESS
    }

}
