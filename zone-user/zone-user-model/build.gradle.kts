dependencies {
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    implementation(libs.mybatis.plus.annotation)
    api(project(":zone-common-model"))
}
