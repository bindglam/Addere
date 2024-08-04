plugins {
    id("java")
}

group = "io.github.bindglam"
version = "unspecified"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    implementation(project(":api"))

    compileOnly("dev.folia:folia-api:1.20.6-R0.1-SNAPSHOT")
}

tasks.test {
    useJUnitPlatform()
}