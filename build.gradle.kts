import com.google.protobuf.gradle.protobuf
import com.google.protobuf.gradle.protoc
import java.net.URI

plugins {
    id("java")
    id("maven-publish")
    id("com.google.protobuf") version "0.8.18"
}

group = "org.dlms"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.google.protobuf:protobuf-java:3.21.12")
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:3.21.12"
    }
}

tasks.register<Zip>("zipProto") {
    from("src/main/proto")
    archiveBaseName.set("dlms-proto")
    archiveClassifier.set("proto")
    destinationDirectory.set(layout.buildDirectory.dir("dist"))
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            // Add the proto files as an artifact with a classifier
            artifact(tasks["zipProto"])

            pom {
                name = "DLMS Protos"
                description = "A proto library for dlms schemas."
                url.set("https://github.com/bxute/dlms-schemas")
                groupId = "org.dlms.protos"
                version = project.version.toString()

                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }

                developers {
                    developer {
                        id.set("org.dlms")
                        name.set("Ankit Kumar")
                        email.set("ankit.kumar071460@gmail.com")
                    }
                }

                scm {
                    url.set("https://github.com/bxute/dlms-schemas")
                }
            }
        }
    }
    repositories {
        maven {
            name = "GitHubPackages"
            url = URI.create("https://maven.pkg.github.com/bxute/dlms-schemas") // For release
            credentials {
                username = (project.findProperty("gpr.user") ?: "").toString()
                password = (project.findProperty("gpr.token") ?: "").toString()
            }
        }
    }
}