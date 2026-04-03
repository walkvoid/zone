dependencies {
    api(project(":zone-finance-model"))
    api(project(":zone-finance-api"))
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-web")
}
