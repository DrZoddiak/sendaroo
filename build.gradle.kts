plugins {
    kotlin("jvm") version "1.3.40"
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

group = "me.zodd"
version = "1.1-SNAPSHOT"

repositories {
    mavenCentral()
    maven {
        url = uri("https://repo.spongepowered.org/maven")
    }
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.spongepowered:spongeapi:7.4.0")
}

tasks {
    shadowJar {
        configurations = listOf(project.configurations.shadow.get())
    }
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
        targetCompatibility = "1.8"
        sourceCompatibility = "1.8"
    }
    compileJava {
        targetCompatibility = "1.8"
        sourceCompatibility = "1.8"
    }
}