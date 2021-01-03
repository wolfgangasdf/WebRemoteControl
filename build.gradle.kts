
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

version = "1.0-SNAPSHOT"
val kotlinversion = "1.4.21"

println("Current Java version: ${JavaVersion.current()}")
if (JavaVersion.current().majorVersion.toInt() < 15) throw GradleException("Use Java >= 15")

buildscript {
    repositories {
        mavenCentral()
        jcenter()
    }
}

plugins {
    kotlin("jvm") version "1.4.21"
    id("idea")
    application
    id("com.github.ben-manes.versions") version "0.36.0"
    id("org.beryx.runtime") version "1.12.1"
}

application {
    mainClass.set("MainKt")
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
    maven {
        url = uri("https://jitpack.io")
        content { includeGroup("com.github.kenglxn.QRGen") }
    }
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinversion")
    implementation("io.github.microutils:kotlin-logging:2.0.4")
    implementation("org.slf4j:slf4j-simple:1.8.0-beta4") // no colors, everything stderr
    implementation("io.javalin:javalin:3.12.0") { exclude("org.jetbrains.kotlin", "kotlin-stdlib-jdk8") }
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

