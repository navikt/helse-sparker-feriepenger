import org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL

val kafkaVersion = "2.8.0"
val junitJupiterVersion = "5.10.2"
val jacksonVersion = "2.15.2"

plugins {
    kotlin("jvm") version "1.9.10"
}


repositories {
    mavenCentral()
    maven("https://jitpack.io")
    maven("https://packages.confluent.io/maven/")
}

dependencies {
    api(kotlin("stdlib-jdk8"))
    api("org.apache.kafka:kafka-clients:$kafkaVersion")

    implementation("org.jetbrains.kotlin:kotlin-reflect:1.5.30")
    implementation("org.flywaydb:flyway-core:10.9.0")
    implementation("com.zaxxer:HikariCP:5.0.1")
    implementation("com.google.cloud.sql:postgres-socket-factory:1.7.2")
    implementation("org.postgresql:postgresql:42.7.2")
    implementation("com.github.seratch:kotliquery:1.9.0")
    implementation("ch.qos.logback:logback-classic:1.2.6")
    implementation("net.logstash.logback:logstash-logback-encoder:6.6") {
        exclude("com.fasterxml.jackson.core")
        exclude("com.fasterxml.jackson.dataformat")
    }
    implementation("com.fasterxml.jackson.core:jackson-core:$jacksonVersion")
    implementation("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$jacksonVersion")

    testImplementation("org.testcontainers:postgresql:1.19.5")
    testImplementation("io.mockk:mockk:1.13.9")

    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitJupiterVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-params:$junitJupiterVersion")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitJupiterVersion")
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "16"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "16"
    }

    named<Jar>("jar") {
        archiveFileName.set("app.jar")

        manifest {
            attributes["Main-Class"] = "no.nav.helse.sparkerferiepenger.ApplicationKt"
            attributes["Class-Path"] = configurations.runtimeClasspath.get().joinToString(separator = " ") {
                it.name
            }
        }

        doLast {
            configurations.runtimeClasspath.get().forEach {
                val file = File("$buildDir/libs/${it.name}")
                if (!file.exists())
                    it.copyTo(file)
            }
        }
    }

    withType<Test> {
        useJUnitPlatform()
        testLogging {
            events("passed", "skipped", "failed")
            showCauses = true
            showExceptions = true
            exceptionFormat = FULL
            showStackTraces = true
        }
    }

    withType<Wrapper> {
        gradleVersion = "7.2"
    }
}
