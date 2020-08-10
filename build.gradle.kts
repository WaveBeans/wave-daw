import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    val kotlinVersion: String by System.getProperties()

    kotlin("jvm") version kotlinVersion
    id("com.jfrog.bintray") version "1.8.4"

    `java-library`
    `maven-publish`
}

group = "io.wavebeans.daw"
version = properties["version"].toString().let {
    if (it.endsWith("-SNAPSHOT"))
        it.removeSuffix("-SNAPSHOT") + "." + System.currentTimeMillis().toString()
    else
        it
}

val spekVersion: String by System.getProperties()
val wavebeansVersion: String by System.getProperties()

apply {
    plugin("kotlin")
}

repositories {
    jcenter()
    mavenCentral()
    mavenLocal()
    maven {
        name = "Bintray WaveBeans"
        url = uri("https://dl.bintray.com/wavebeans/wavebeans")
    }
}

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = "1.8"
}

val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
    jvmTarget = "1.8"
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("reflect"))
    implementation("io.wavebeans:lib:$wavebeansVersion")

    testImplementation("org.spekframework.spek2:spek-dsl-jvm:$spekVersion")
    testRuntimeOnly("org.spekframework.spek2:spek-runner-junit5:$spekVersion")
    testImplementation("com.willowtreeapps.assertk:assertk-jvm:0.13")
    testImplementation("com.nhaarman.mockitokotlin2:mockito-kotlin:2.2.0")
    testImplementation("ch.qos.logback:logback-classic:1.2.3")

}

java {
    withJavadocJar()
    withSourcesJar()
}

tasks.test {
    systemProperty("SPEK_TIMEOUT", 0)
    useJUnitPlatform {
        includeEngines("spek2")
    }
    maxHeapSize = "2g"
}

publishing {
    publications {
        create<MavenPublication>("wave-daw") {
            groupId = "io.wavebeans"
            artifactId = "daw"
            version = project.version.toString()

            from(components["java"])
        }
    }
}

bintray {
    user = findProperty("bintray.user")?.toString() ?: ""
    key = findProperty("bintray.key")?.toString() ?: ""
    setPublications("wave-daw")
    pkg(delegateClosureOf<com.jfrog.bintray.gradle.BintrayExtension.PackageConfig> {
        repo = "wavebeans"
        name = "wavebeans"
        userOrg = "wavebeans"
        vcsUrl = "https://github.com/WaveBeans/wavebeans"
        setLicenses("Apache-2.0")
        publish = true
        version(delegateClosureOf<com.jfrog.bintray.gradle.BintrayExtension.VersionConfig> {
            name = project.version.toString()
        })
    })
}