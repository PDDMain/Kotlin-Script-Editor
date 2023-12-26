plugins {
    kotlin("jvm") version "1.9.21"
    application
}

group = "org.editor"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-scripting-common:1.9.21")
    implementation("org.jetbrains.kotlin:kotlin-scripting-jvm:1.9.21")
    implementation("org.jetbrains.kotlin:kotlin-scripting-jvm-host:1.9.21")
    implementation("org.jetbrains.kotlin:kotlin-main-kts:1.9.21")
    testImplementation("org.jetbrains.kotlin:kotlin-test")
}

application {
    mainClass.set("org.editor.MainKt")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21)
}

