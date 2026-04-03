plugins {
    java
    alias(libs.plugins.spring.boot)
}

dependencies {
    implementation(project(":zone-finance-business"))
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-web")
}

tasks.jar {
    enabled = false
}
