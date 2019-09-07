
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar


val kotlinversion = "1.3.50"

buildscript {
    repositories {
        mavenCentral()
        jcenter()
    }
}

group = "com.wolle"
version = "1.0-SNAPSHOT"

plugins {
    kotlin("jvm") version "1.3.50"
    id("idea")
    id("application")
    id("com.github.ben-manes.versions") version "0.24.0"
    id("com.github.johnrengelman.shadow") version "5.1.0"
}

tasks.withType<Wrapper> {
    gradleVersion = "5.6.1"
}

application {
    mainClassName = "MainKt"
    //defaultTasks = tasks.run
}

tasks.withType<Jar> {
    manifest {
        attributes(mapOf(
                "Description" to "WebRemoteControl JAR",
                "Implementation-Title" to "WebRemoteControl",
                "Implementation-Version" to version,
                "Main-Class" to "MainKt"
        ))
    }
}

tasks.withType<ShadowJar> {
    // uses manifest from above!
    baseName = "webremotecontrol"
    classifier = ""
    version = ""
    mergeServiceFiles() // can be essential
}

repositories {
    mavenCentral()
    mavenLocal() // for jwt
    jcenter() // for kotlinx.html, aza-css
    maven(url = "https://jitpack.io") // QRgen
}

dependencies {
    compile("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinversion")
    compile("io.github.microutils:kotlin-logging:1.7.6")
    compile("org.slf4j:slf4j-simple:1.7.25") // no colors, everything stderr
    compile("io.javalin:javalin:3.4.1")
    compile("org.webjars:hammerjs:2.0.8")
    compile("com.github.kenglxn.QRGen:javase:2.6.0")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

task("dist") {
    dependsOn("shadowJar") // fat jar
}

