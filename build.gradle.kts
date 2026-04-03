import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.jvm.toolchain.JavaLanguageVersion

plugins {
    base
}

group = providers.gradleProperty("group").get()
version = providers.gradleProperty("version").get()

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
        if (!isGateway) {
            dependencies {
                add("implementation", platform(libs.spring.boot.bom))
                add("implementation", platform(libs.spring.cloud.bom))
                // Lombok 等由 BOM 管版本；annotationProcessor 需单独挂 platform，否则会出现 lombok:.
                add("compileOnly", platform(libs.spring.boot.bom))
                add("annotationProcessor", platform(libs.spring.boot.bom))
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
