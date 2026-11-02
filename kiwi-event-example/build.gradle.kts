dependencies {
    implementation(project(":kiwi-event-core"))
    implementation(project(":kiwi-event-coroutines"))

    testImplementation(kotlin("test"))
    testImplementation(libs.kotlinx.coroutines.test)
}

kotlin {
    jvmToolchain(25)
}

tasks.test {
    useJUnitPlatform()
}