import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

repositories {
    maven(url = "https://repo.codemc.org/repository/maven-public/")
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    api(project(":api"))

    implementation("commons-io:commons-io:2.16.1")
    implementation("dev.jorel:commandapi-bukkit-shade:9.5.1")

    compileOnly("dev.folia:folia-api:1.20.6-R0.1-SNAPSHOT")
}

tasks.test {
    useJUnitPlatform()
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

tasks {
    named<ShadowJar>("shadowJar") {
        archiveBaseName.set("addere")

        dependencies {
            include(project(":api"))
            include(dependency("dev.jorel:commandapi-bukkit-shade:9.5.1"))
        }
    }
}

tasks {
    build {
        dependsOn(shadowJar)
    }
}