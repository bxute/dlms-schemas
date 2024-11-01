import com.google.protobuf.gradle.*
import java.net.URI
import java.util.*

plugins {
    id("java")
    id("maven-publish")
    id("com.google.protobuf") version "0.8.18"
}

group = "org.dlms"
version = "1.0.0"

repositories {
    mavenCentral()
    google()
}

dependencies {
    // proto buffer
    compileOnly("com.google.protobuf:protobuf-java:4.28.2")

    // grpc
    compileOnly("io.grpc:grpc-netty-shaded:1.68.0")
    compileOnly("io.grpc:grpc-protobuf:1.68.0")
    compileOnly("io.grpc:grpc-stub:1.68.0")
    compileOnly("io.grpc:protoc-gen-grpc-java:1.68.0")

    // annotation
    compileOnly("javax.annotation:javax.annotation-api:1.3.2")

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.register<Zip>("zipProto") {
    doFirst {
        println("Running zipping...")
    }
    from("src/main/proto")
    archiveBaseName.set("dlms-proto")
    version = project.findProperty("publishVersion").toString()
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
                val newVersion = project.findProperty("publishVersion").toString()
                version = newVersion

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

fun incrementVersion(version: String): String {
    val parts = version.split(".")
    if (parts.size < 3) {
        throw IllegalArgumentException("Version format should be MAJOR.MINOR.PATCH")
    }

    val patch = parts[2].toInt() + 1 // Increment patch version
    return "${parts[0]}.${parts[1]}.$patch" // Reconstruct the version string
}

tasks.register("updatePublishVersion") {
    doLast {
        println("updating version...")
        val gradlePropertiesFile = File(rootDir, "gradle.properties")
        val properties = Properties()

        if (gradlePropertiesFile.exists()) {
            gradlePropertiesFile.inputStream().use { properties.load(it) }
        }

        val newVersion = incrementVersion(project.findProperty("publishVersion").toString())
        // Modify properties as needed
        properties["publishVersion"] = newVersion
        println("new version: $newVersion")

        gradlePropertiesFile.outputStream().use { properties.store(it, "Updated by updateGradleProperties task") }
    }
}

// Update version before publish
tasks.publish {
    dependsOn("updatePublishVersion")
    dependsOn("testGeneratedCode")
}

val generatedProtoDirectory = layout.buildDirectory.dir("generated/source/proto/main/java")
protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:3.24.4"
    }
    generatedFilesBaseDir = generatedProtoDirectory.get().toString()
    plugins {
        id("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:1.68.0"
        }
    }
    generateProtoTasks {
        all().forEach { task ->
            task.plugins {
                id("grpc") {}
            }
            task.doFirst {
                println("Generating proto files...")
            }
        }
    }
}

// Custom task to compile generated Java code
tasks.register("testGeneratedCode") {
    dependsOn("compileJava")
    doLast {
        println("Running testGeneratedCode...")
    }
}

tasks.named("compileJava").configure {
    dependsOn("generateProto")
    doFirst {
        println("Running compileJava...")
    }
}