dependencies {
    api(project(":zone-tools-model"))
    api(project(":zone-tools-api"))
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}
