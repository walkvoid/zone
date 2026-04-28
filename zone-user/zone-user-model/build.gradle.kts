dependencies {
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    implementation(libs.mybatis.plus.annotation)
    implementation("com.github.walkvoid.wvframework:wvframework-models")
    api(project(":zone-common-model"))
}
