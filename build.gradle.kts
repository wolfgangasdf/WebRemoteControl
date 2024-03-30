
import org.gradle.kotlin.dsl.support.zipTo
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.util.*

version = "1.0-SNAPSHOT"
val cPlatforms = listOf("mac", "linux", "win") // compile for these platforms. "mac", "mac-aarch64", "linux", "win"
val kotlinVersion = "1.9.22"
val javaVersion = 21
println("Current Java version: ${JavaVersion.current()}")
if (JavaVersion.current().majorVersion.toInt() != javaVersion) throw GradleException("Use Java $javaVersion")

buildscript {
    repositories {
        mavenCentral()
    }
}

tasks.wrapper {
    distributionType = Wrapper.DistributionType.BIN
    gradleVersion = "8.7"
}

plugins {
    kotlin("jvm") version "1.9.22"
    id("idea")
    application
    id("com.github.ben-manes.versions") version "0.45.0"
    id("org.beryx.runtime") version "1.13.0"
}

idea {
    module {
        isDownloadJavadoc = true
        isDownloadSources = true
    }
}

kotlin {
    jvmToolchain(javaVersion)
}

application {
    mainClass.set("MainKt")
}

repositories {
    mavenCentral()
    maven {
        url = uri("https://jitpack.io")
        content { includeGroup("com.github.kenglxn.QRGen") }
    }
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
    implementation("io.github.microutils:kotlin-logging:3.0.5")
    implementation("org.slf4j:slf4j-simple:2.0.11") // no colors, everything stderr
    implementation("io.javalin:javalin:6.0.0") { exclude("org.jetbrains.kotlin", "kotlin-stdlib-jdk8") }
    implementation("org.webjars:hammerjs:2.0.8")
    implementation("io.github.g0dkar:qrcode-kotlin:4.1.1")
}

runtime {
    imageZip.set(project.file("${project.buildDir}/image-zip/webremotecontrol"))
    options.set(listOf("--strip-debug", "--compress", "2", "--no-header-files", "--no-man-pages"))
    modules.set(listOf("java.desktop", "java.logging", "java.prefs", "java.xml"))

    // sets targetPlatform JDK for host os from toolchain, for others (cross-package) from adoptium / jdkDownload
    // https://github.com/beryx/badass-runtime-plugin/issues/99
    // if https://github.com/gradle/gradle/issues/18817 is solved: use toolchain
    fun setTargetPlatform(jfxplatformname: String) {
        val platf = if (jfxplatformname == "win") "windows" else jfxplatformname // jfx expects "win" but adoptium needs "windows"
        val os = org.gradle.internal.os.OperatingSystem.current()
        var oss = if (os.isLinux) "linux" else if (os.isWindows) "windows" else if (os.isMacOsX) "mac" else ""
        if (oss == "") throw GradleException("unsupported os")
        if (System.getProperty("os.arch") == "aarch64") oss += "-aarch64"// https://github.com/openjfx/javafx-gradle-plugin#4-cross-platform-projects-and-libraries
        if (oss == platf) {
            targetPlatform(jfxplatformname, javaToolchains.launcherFor(java.toolchain).get().executablePath.asFile.parentFile.parentFile.absolutePath)
        } else { // https://api.adoptium.net/q/swagger-ui/#/Binary/getBinary
            targetPlatform(jfxplatformname) {
                val ddir = "${if (os.isWindows) "c:/" else "/"}tmp/jdk$javaVersion-$platf"
                println("downloading jdks to or using jdk from $ddir, delete folder to update jdk!")
                @Suppress("INACCESSIBLE_TYPE")
                setJdkHome(
                    jdkDownload("https://api.adoptium.net/v3/binary/latest/$javaVersion/ga/$platf/x64/jdk/hotspot/normal/eclipse?project=jdk",
                        closureOf<org.beryx.runtime.util.JdkUtil.JdkDownloadOptions> {
                            downloadDir = ddir // put jdks here so different projects can use them!
                            archiveExtension = if (platf == "windows") "zip" else "tar.gz"
                        }
                    )
                )
            }
        }
    }
    cPlatforms.forEach { setTargetPlatform(it) }
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "$javaVersion"
}

task("dist") {
    dependsOn("runtimeZip")
    doLast {
        println("Deleting build/[jre,install]")
        project.delete(project.runtime.jreDir.get(), "${project.buildDir.path}/install")
        println("Created zips in build/image-zip")
    }
}

fun isNonStable(version: String): Boolean {
    val stableKeyword = listOf("RELEASE", "FINAL", "GA").any { version.uppercase(Locale.getDefault()).contains(it) }
    val regex = "^[0-9,.v-]+(-r)?$".toRegex()
    val isStable = stableKeyword || regex.matches(version)
    return isStable.not()
}
tasks.withType<com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask> {
    rejectVersionIf {
        isNonStable(candidate.version)
    }
    gradleReleaseChannel = "current"
}
