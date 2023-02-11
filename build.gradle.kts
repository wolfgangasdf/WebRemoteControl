
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

version = "1.0.0"
val kotlinversion = "1.8.10"
val javaversion = 18
println("Current Java version: ${JavaVersion.current()}")
if (JavaVersion.current().majorVersion.toInt() != javaversion) throw GradleException("Use Java $javaversion")

buildscript {
    repositories {
        mavenCentral()
    }
}

plugins {
    kotlin("jvm") version "1.8.10"
    id("idea")
    application
    id("com.github.ben-manes.versions") version "0.45.0"
    id("org.beryx.runtime") version "1.13.0"
}

application {
    mainClass.set("MainKt")
}

runtime {
    imageZip.set(project.file("${project.buildDir}/image-zip/webremotecontrol"))
    options.set(listOf("--strip-debug", "--compress", "2", "--no-header-files", "--no-man-pages"))
    modules.set(listOf("java.desktop", "java.logging", "java.prefs", "java.xml"))
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
    implementation("io.github.microutils:kotlin-logging:3.0.5")
    implementation("org.slf4j:slf4j-simple:2.0.6") // no colors, everything stderr
    implementation("io.javalin:javalin:5.3.2") { exclude("org.jetbrains.kotlin", "kotlin-stdlib-jdk8") }
    implementation("org.webjars:hammerjs:2.0.8")
    implementation("com.github.kenglxn.QRGen:javase:3.0.1")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "$javaversion"
}

task("dist") {
    dependsOn("runtimeZip")
    doLast {
        println("Deleting build/[jre,install]")
        project.delete(project.runtime.jreDir.get(), "${project.buildDir.path}/install")
        println("Created zips in build/image-zip")
    }
}

