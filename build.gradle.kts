plugins {
    kotlin("jvm") version "1.3.40"
    kotlin("kapt") version "1.3.40"
}

group = "me.zodd"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven {
        url = uri("https://repo.spongepowered.org/maven")
    }
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))

    val sponge = create("org.spongepowered:spongeapi:7.4.0")
    implementation(sponge)
    kapt(sponge)
}

tasks {
  compileKotlin {
      kotlinOptions.jvmTarget = "1.8"
  }
}
