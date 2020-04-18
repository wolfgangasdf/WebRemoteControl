
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile


val kotlinversion = "1.3.61"
group = "com.wolle"
version = "1.0-SNAPSHOT"

println("Current Java version: ${JavaVersion.current()}")
if (JavaVersion.current().majorVersion.toInt() < 14) throw GradleException("Use Java >= 14")

buildscript {
    repositories {
        mavenCentral()
        jcenter()
    }
}

plugins {
    kotlin("jvm") version "1.3.61"
    id("idea")
    application
    id("com.github.ben-manes.versions") version "0.27.0"
    id("org.beryx.runtime") version "1.8.0"
}

application {
    mainClassName = "MainKt"
    //defaultTasks = tasks.run
}

runtime {
    imageZip.set(project.file("${project.buildDir}/image-zip/webremotecontrol"))
    options.set(listOf("--strip-debug", "--compress", "2", "--no-header-files", "--no-man-pages"))
    targetPlatform("linux", System.getenv("JDK_LINUX_HOME"))
    targetPlatform("mac", System.getenv("JDK_MAC_HOME"))
    targetPlatform("win", System.getenv("JDK_WIN_HOME"))
}

repositories {
    mavenCentral()
    mavenLocal() // for jwt
    jcenter() // for kotlinx.html, aza-css
    maven(url = "https://jitpack.io") // QRgen
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinversion")
    implementation("io.github.microutils:kotlin-logging:1.7.8")
    implementation("org.slf4j:slf4j-simple:1.8.0-beta4") // no colors, everything stderr
    implementation("io.javalin:javalin:3.7.0")
    implementation("org.webjars:hammerjs:2.0.8")
    implementation("com.github.kenglxn.QRGen:javase:2.6.0")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

task("dist") {
    dependsOn("runtimeZip")
    doLast {
        println("Deleting build/[jre,install]")
        project.delete(project.runtime.jreDir.get(), "${project.buildDir.path}/install")
        println("Created zips in build/image-zip")
    }
}

