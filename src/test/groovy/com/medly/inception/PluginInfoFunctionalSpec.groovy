package com.medly.inception

import org.gradle.testkit.runner.GradleRunner
import spock.lang.Specification
import spock.lang.TempDir

import static org.gradle.testkit.runner.TaskOutcome.FAILED
import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

class PluginInfoFunctionalSpec extends Specification {
    @TempDir
    File testProjectDir
    File pluginSrcDir
    File settingsFile, buildFile

    def setup() {
        buildFile = new File(testProjectDir, 'build.gradle')
        settingsFile = new File(testProjectDir, 'settings.gradle')
        pluginSrcDir = new File(testProjectDir, "src/main/groovy")
        pluginSrcDir.mkdirs()
    }


    def "uses sensible defaults when required properties are provided"() {
        given: "minimal default properties"
        settingsFile << """
        rootProject.name = "my-plugin"
        """

        buildFile << """
        plugins {
            id "com.medly.inception"
        }

        version = '1.0'
        description = "my sample plugin's project description"
        group = "com.example"

        pluginBundle {
            vcsUrl = 'https://github.com/medly/inception'
            tags = ['gradle', 'plugin']
        }
        """

        def pluginFoo = new File(pluginSrcDir, 'com.example.foo.gradle')
        pluginFoo << "tasks.register('foo')"

        when: "asked to print info"
        def result = GradleRunner.create()
            .withPluginClasspath() // for custom plugin
            .withProjectDir(testProjectDir)
            .withArguments('printPluginInfo')
            .build()

        then:
        result.task(":printPluginInfo").outcome == SUCCESS
        result.output.contains("""\
            PluginBundle (required for publishing)
            --------------------------------------
            description      : my sample plugin's project description
            website          : https://github.com/medly/inception
            vcsUrl           : https://github.com/medly/inception
            tags             : [gradle, plugin]
            mavenCoordinates : com.example:my-plugin:1.0
            """.stripIndent())

        result.output.contains("""\
            PluginDeclaration [com.example.foo]
            -----------------------------------
            name                : com.example.foo
            id                  : com.example.foo
            implementationClass : ComExampleFooPlugin
            displayName         : com.example.foo
            description         : my sample plugin's project description
            """.stripIndent().trim())
    }


    def "does not overwrite properties if set by user"() {
        given:
        settingsFile << """
         rootProject.name = "my-plugin"
         """

        buildFile << """
        plugins {
            id "com.medly.inception"
        }

        version = '1.0'
        description = "my sample plugin"
        group = "com.example"

        pluginBundle {
            website = "https://example.org"
            vcsUrl = 'https://github.com/medly/inception'
            tags = ['gradle', 'plugin']
            description = "description in plugin bundle"
        }
        """

        def pluginFoo = new File(pluginSrcDir, 'com.example.foo.gradle')
        pluginFoo << "tasks.register('foo')"


        when:
        def result = GradleRunner.create()
            .withPluginClasspath() // for custom plugin
            .withProjectDir(testProjectDir)
            .withArguments('printPluginInfo')
            .build()

        then:
        result.task(":printPluginInfo").outcome == SUCCESS
        result.output.contains("""\
            PluginBundle (required for publishing)
            --------------------------------------
            description      : description in plugin bundle
            website          : https://example.org
            vcsUrl           : https://github.com/medly/inception
            tags             : [gradle, plugin]
            mavenCoordinates : com.example:my-plugin:1.0
            """.stripIndent())

        result.output.contains("""\
            PluginDeclaration [com.example.foo]
            -----------------------------------
            name                : com.example.foo
            id                  : com.example.foo
            implementationClass : ComExampleFooPlugin
            displayName         : com.example.foo
            description         : description in plugin bundle
            """.stripIndent())
    }

    def "multiple plugins within same jar"() {
        given:
        settingsFile << """
         rootProject.name = "my-plugin"
         """

        buildFile << """
        plugins {
            id "com.medly.inception"
        }

        version = '1.0'
        description = "my sample plugin"
        group = "com.example"

        pluginBundle {
            vcsUrl = 'https://github.com/medly/inception'
            tags = ['gradle', 'plugin']
        }
        """

        def pluginFoo = new File(pluginSrcDir, 'com.example.foo.gradle')
        def pluginBar = new File(pluginSrcDir, 'com.example.bar.gradle')

        pluginFoo << "tasks.register('foo')"
        pluginBar << "tasks.register('bar')"

        when:

        def result = GradleRunner.create()
            .withPluginClasspath() // for custom plugin
            .withProjectDir(testProjectDir)
            .withArguments('printPluginInfo')
            .build()

        then:
        result.task(":printPluginInfo").outcome == SUCCESS

        result.output.contains("""\
            PluginBundle (required for publishing)
            --------------------------------------
            description      : my sample plugin
            website          : https://github.com/medly/inception
            vcsUrl           : https://github.com/medly/inception
            tags             : [gradle, plugin]
            mavenCoordinates : com.example:my-plugin:1.0
            """.stripIndent())


        result.output.contains("""\
            PluginDeclaration [com.example.foo]
            -----------------------------------
            name                : com.example.foo
            id                  : com.example.foo
            implementationClass : ComExampleFooPlugin
            displayName         : com.example.foo
            description         : my sample plugin
            """.stripIndent())

        result.output.contains("""\
            PluginDeclaration [com.example.bar]
            -----------------------------------
            name                : com.example.bar
            id                  : com.example.bar
            implementationClass : ComExampleBarPlugin
            displayName         : com.example.bar
            description         : my sample plugin
            """.stripIndent())

    }

}
