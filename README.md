# Inception

A Gradle Plugin to Build Gradle Plugins

## Features

- Applies and configures all the necessary plugins to publish Precompiled script plugins to plugin portal
- Configure Repositories to depend on external plugins, just add `dependencies` block
- Configures Dependencies for building and testing the plugin
- Sets up Test framework setup (Spock), no configuration required


## Usage

Apply the plugin to your plugin project

```groovy
plugins {
  id 'com.medly.inception' version "<version>"
}
```


## Configuration required for publishing

The absolute minimum configuration required for before we can publish a plugin is following:

1. in `settings.gradle`

    ```groovy
    rootProject.name = "inception"
    ```

2. in `build.gradle`

    ```groovy
    // these property can be passed from command-line, environment or gradle.properties
    // artifact id is inferred from project.name in `settings.gradle`
    
    group = "com.example"
    description = "Your plugin description"
    version = "1.0.0"     
    
    pluginBundle {
        vcsUrl = 'https://github.com/foo/bar'
        tags = ['gradle', 'plugin', 'tags']
    }
    ```


> Set up `gradle.publish.key`, `gradle.publish.secret` to publish to plugin portal as documented [here](https://docs.gradle.org/current/userguide/publishing_gradle_plugins.html)
