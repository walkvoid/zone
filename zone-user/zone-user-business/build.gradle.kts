dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation(libs.mybatis.plus.boot3)
    api(project(":zone-user-model"))
    api(project(":zone-user-api"))
}
