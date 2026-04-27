import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.jvm.toolchain.JavaLanguageVersion

plugins {
    base
}


group = providers.gradleProperty("group").get()
version = providers.gradleProperty("version").get()

val wvframeworkBom = "com.github.walkvoid.wvframework:wvframework-bom:4.0.0-SNAPSHOT"

subprojects {
    val isGateway = name == "zone-gateway"

    if (!isGateway) {
        apply(plugin = "java-library")
        apply(plugin = "maven-publish")
    } else {
        apply(plugin = "java")
    }

    group = rootProject.group
    version = rootProject.version

    extensions.configure<JavaPluginExtension> {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(21))
        }
        if (!isGateway) {
            withSourcesJar()
        }
    }

    tasks.withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
    }

    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
    }

    afterEvaluate {
        dependencies {
            // 使用 wvframework BOM 管理依赖版本
            add("implementation", platform(wvframeworkBom))
            if (!isGateway) {
                add("compileOnly", platform(wvframeworkBom))
                add("annotationProcessor", platform(wvframeworkBom))
            }
        }
    }

    if (!isGateway) {
        extensions.configure<PublishingExtension> {
            publications {
                create<MavenPublication>("mavenJava") {
                    from(components["java"])
                    groupId = project.group.toString()
                    artifactId = project.name
                    version = project.version.toString()
                }
            }
        }
    }
}