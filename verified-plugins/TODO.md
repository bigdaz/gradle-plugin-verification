## Plugins not yet part of verification

### Plugins owned by Gradle engineering

- [org.gradle.android.cache-fix](https://plugins.gradle.org/plugin/org.gradle.android.cache-fix)
  - Requires Android
- Gradle Enterprise plugins:
  - [com.gradle.common-custom-user-data-gradle-plugin](https://plugins.gradle.org/plugin/com.gradle.common-custom-user-data-gradle-plugin)
  - `com.gradle.build-scan`: tested as part of plugin verifier
  - `com.gradle.enterprise`: tested as part of plugin verifier
- Gradle Kotlin DSL: `org.gradle.kotlin-dsl`, `org.gradle.kotlin-dsl.*` `org.gradle.kotlin.embedded-kotlin`

### Plugins contributed-to by Gradle enterineering

- [com.bugsnag.android.gradle](https://plugins.gradle.org/plugin/com.bugsnag.android.gradle)
    - Requires Android
- [com.karumi:shot](https://search.maven.org/artifact/com.karumi/shot)
  - Not published to plugin portal
  - Requires android
- [org.jlleitschuh.gradle.ktlint](https://plugins.gradle.org/plugin/org.jlleitschuh.gradle.ktlint)
  - Also [org.jlleitschuh.gradle.ktlint-idea](https://plugins.gradle.org/plugin/org.jlleitschuh.gradle.ktlint-idea)

### Plugins with hosted Gradle Enterprise instances

- [io.ratpack.ratpack-java](https://plugins.gradle.org/plugin/io.ratpack.ratpack-java)
- [io.ratpack.ratpack-groovy](https://plugins.gradle.org/plugin/io.ratpack.ratpack-groovy)
- Many plugins provided and supported by Jetbrains: `org.jetbrains.kotlin.*` etc
- `io.spring.*`
  - [org.springframework.cloud.contract](https://plugins.gradle.org/plugin/org.springframework.cloud.contract)
  - [io.spring.dependency-management](https://plugins.gradle.org/plugin/io.spring.dependency-management)
  - [io.spring.javadoc-aggregate](https://plugins.gradle.org/plugin/io.spring.javadoc-aggregate)
  - [io.spring.release](https://plugins.gradle.org/plugin/io.spring.release)
  
### Top 50 downloaded plugins (not noted above)
- [org.sonarqube](https://plugins.gradle.org/plugin/org.sonarqube)
  - Requires sonar server to run
- [org.flywaydb.flyway](https://plugins.gradle.org/plugin/org.flywaydb.flyway)
- [com.google.cloud.tools.jib](https://plugins.gradle.org/plugin/com.google.cloud.tools.jib)
- [com.gorylenko.gradle-git-properties](https://plugins.gradle.org/plugin/com.gorylenko.gradle-git-properties)
- [io.freefair.lombok](https://plugins.gradle.org/plugin/io.freefair.lombok)
- [org.owasp.dependencycheck](https://plugins.gradle.org/plugin/org.owasp.dependencycheck)
- `com.bmuschko.docker*`
  - [com.bmuschko.docker-remote-api](https://plugins.gradle.org/plugin/com.bmuschko.docker-remote-api)
  - [com.bmuschko.docker-spring-boot-application](https://plugins.gradle.org/plugin/com.bmuschko.docker-spring-boot-application)
  - [com.bmuschko.docker-java-application](https://plugins.gradle.org/plugin/com.bmuschko.docker-java-application)
- [org.ajoberstar.grgit](https://plugins.gradle.org/plugin/org.ajoberstar.grgit)
- `net.ltgt.apt*`
  - [net.ltgt.apt](https://plugins.gradle.org/plugin/net.ltgt.apt)
  - [net.ltgt.apt-idea](https://plugins.gradle.org/plugin/net.ltgt.apt-idea)
  - [net.ltgt.apt-eclipse](https://plugins.gradle.org/plugin/net.ltgt.apt-eclipse)
- `com.github.node-gradle.*`
  - [com.github.node-gradle.node](https://plugins.gradle.org/plugin/com.github.node-gradle.node)
  - [com.github.node-gradle.gulp](https://plugins.gradle.org/plugin/com.github.node-gradle.gulp)
  - [com.github.node-gradle.grunt](https://plugins.gradle.org/plugin/com.github.node-gradle.grunt)
- [com.adarshr.test-logger](https://plugins.gradle.org/plugin/com.adarshr.test-logger)

