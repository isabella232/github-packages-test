import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPom
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.bundling.Jar
import org.gradle.kotlin.dsl.*
import org.gradle.plugins.signing.SigningExtension
import java.net.URI

object Deployment {
    val githubUser = System.getenv("GITHUB_MAVEN_USERNAME")
    val githubPassword = System.getenv("GITHUB_TOKEN")
    val githubRepository = System.getenv("GITHUB_REPOSITORY")
    var deployUrl: String? = System.getenv("MAVEN_RELEASES_URL")
            ?: "https://maven.pkg.github.com/$githubRepository"

    fun initialize(project: Project) {
        initializePublishing(project)
        initializeSigning(project)
    }

    private fun initializePublishing(project: Project) {
        project.version = System.getenv()["GITHUB_TAG_NAME"]?.let {
            if (it.startsWith("v"))
                it.substring(1)
            else it
        } ?: "0.0.0"
        project.plugins.apply("maven-publish")

        val javaPlugin = project.the(JavaPluginConvention::class)

        val sourcesJar by project.tasks.creating(Jar::class) {
            classifier = "sources"
            from(javaPlugin.sourceSets["main"].allSource)
        }
        val javadocJar by project.tasks.creating(Jar::class) {
            classifier = "javadoc"
            from(javaPlugin.docsDir)
            dependsOn("javadoc")
        }

        project.configure<PublishingExtension> {
            publications {
                create("default", MavenPublication::class.java) {
                    Deployment.customizePom(project, pom)
                    from(project.components["java"])
                    artifact(sourcesJar)
                    artifact(javadocJar)
                }
            }
            repositories {
                maven {
                    name = "Local"
                    setUrl("${project.rootDir}/build/repository")
                }
                maven {
                    name = "GitHub"
                    credentials {
                        username = Deployment.githubUser
                        password = Deployment.githubPassword
                    }
                    url = URI.create(Deployment.deployUrl)
                }
            }
        }
    }

    private fun initializeSigning(project: Project) {
        val passphrase = System.getenv("GPG_PASSPHRASE")
        passphrase?.let {
            project.plugins.apply("signing")

            val publishing = project.the(PublishingExtension::class)
            project.configure<SigningExtension> {
                sign(publishing.publications.getByName("default"))
            }

            project.extra.set("signing.keyId", "XXXXXXXX")
            project.extra.set("signing.password", passphrase)
            project.extra.set("signing.secretKeyRingFile", "${project.rootProject.rootDir}/.buildsystem/secring.gpg")
        }
    }

    fun customizePom(project: Project, pom: MavenPom?) {
        pom?.apply {
            name.set(project.name)
            url.set("https://github.com/atlassian/github-packages-test")
            description.set("Integration test package for delivery pipeline")

            licenses {
                license {
                    name.set("The Apache License, Version 2.0")
                    url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                }
            }

            developers {
                developer {
                    id.set("Malinskiy")
                    name.set("Anton Malinskiy")
                    email.set("anton@malinskiy.com")
                }
            }

            scm {
                url.set("https://github.com/atlassian/github-packages-test.git")
                connection.set("scm:git:ssh://github.com/atlassian/github-packages-test")
                developerConnection.set("scm:git:ssh://github.com/atlassian/github-packages-test")
            }
        }
    }
}