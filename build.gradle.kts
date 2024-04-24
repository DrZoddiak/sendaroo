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
    compileOnly(kotlin("stdlib-jdk8"))
    implementation("org.spongepowered:spongeapi:7.4.0")
    kapt("org.spongepowered:spongeapi:7.4.0")
}



tasks {
  compileKotlin {
      kotlinOptions.jvmTarget = "1.8"
  }
}
