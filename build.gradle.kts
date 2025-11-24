import org.gradle.api.JavaVersion.VERSION_17
import org.gradle.jvm.tasks.Jar

plugins {
    application
}

group = "org.voiddev"
version = ""

description = "A Kick VOD downloading tool"

java {
    sourceCompatibility = VERSION_17
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    implementation("org.slf4j:slf4j-api:1.7.25")
    implementation("com.github.kokorin.jaffree:jaffree:2024.08.29")
    implementation("com.microsoft.playwright:playwright:1.55.0")}

application {
    mainClass.set("org.voiddev.KickDownloader")
}
val fatJar = task("fatJar", type = Jar::class) {
    baseName = "${project.name}"
    manifest {
        attributes["Implementation-Title"] = "Gradle Jar File"
        attributes["Implementation-Version"] = version
        attributes["Main-Class"] = "org.voiddev.KickDownloader"
    }
    from(configurations.runtimeClasspath.get().map({ if (it.isDirectory) it else zipTree(it) }))
    with(tasks.jar.get() as CopySpec)
}

tasks.withType<org.gradle.jvm.tasks.Jar>() {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    exclude("META-INF/BC1024KE.RSA", "META-INF/BC1024KE.SF", "META-INF/BC1024KE.DSA")
    exclude("META-INF/BC2048KE.RSA", "META-INF/BC2048KE.SF", "META-INF/BC2048KE.DSA")
}

tasks {
    "build" {
        dependsOn(fatJar)
    }
}