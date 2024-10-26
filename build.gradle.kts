import java.net.URI

plugins {
    id("java")
    id("maven-publish")
}

group = "org.dlms"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            pom {
                name = "DLMS Protos"
                description = "A proto library for dlms schemas."
                url.set("https://github.com/bxute/dlms-schemas")

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