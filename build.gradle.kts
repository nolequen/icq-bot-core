import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  kotlin("jvm") version "1.3.21"
  jacoco
  signing
  maven
}

group = "su.nlq"
version = "1.2.1"

repositories {
  maven("https://kotlin.bintray.com/kotlinx")
  mavenCentral()
}

dependencies {
  val ktorVersion = "1.1.1"

  compile(kotlin("stdlib-jdk8"))
  compile("io.ktor", "ktor-client-core", ktorVersion)
  compile("io.ktor", "ktor-client-cio", ktorVersion)
  compile("io.ktor", "ktor-client-json", ktorVersion)
  compile("io.ktor", "ktor-client-jackson", ktorVersion)

  testCompile("junit", "junit", "4.12")
  testCompile("io.ktor", "ktor-client-mock", ktorVersion)
  testCompile("io.ktor", "ktor-client-mock-jvm", ktorVersion)
}

tasks.withType<KotlinCompile> {
  kotlinOptions.jvmTarget = "1.8"
  kotlinOptions.freeCompilerArgs = listOf("-Xallow-result-return-type")
}

tasks.jacocoTestReport {
  reports {
    xml.isEnabled = true
    html.isEnabled = false
  }
}

tasks.test { finalizedBy(tasks.jacocoTestReport) }

tasks {
  getByName<Upload>("uploadArchives") {
    repositories {
      withConvention(MavenRepositoryHandlerConvention::class) {
        mavenDeployer {
          beforeDeployment {
            signing.signPom(this)
          }
          val user = if (project.hasProperty("ossrhUsername")) project.properties["ossrhUsername"] else ""
          val password = if (project.hasProperty("ossrhPassword")) project.properties["ossrhPassword"] else ""

          withGroovyBuilder {
            "repository"("url" to "https://oss.sonatype.org/service/local/staging/deploy/maven2/") {
              "authentication"("userName" to user, "password" to password)
            }
            "snapshotRepository"("url" to "https://oss.sonatype.org/content/repositories/snapshots/") {
              "authentication"("userName" to user, "password" to password)
            }
          }

          pom.project {
            withGroovyBuilder {
              "name"("ICQ Bot Core")
              "description"("ICQ Bot Core API library")
              "url"("https://github.com/nolequen/icq-bot-core")
              "licenses" {
                "license" {
                  "name"("The MIT License")
                  "url"("https://opensource.org/licenses/MIT")
                  "distribution"("repo")
                }
              }
              "scm" {
                "url"("https://github.com/nolequen/icq-bot-core")
                "connection"("scm:git:git://github.com/nolequen/icq-bot-core.git")
                "developerConnection"("scm:git:ssh://github.com:nolequen/icq-bot-core.git")
              }
              "developers" {
                "developer" {
                  "name"("Nolequen")
                  "email"("nolequen@gmail.com")
                  "url"("http://www.nlq.su/")
                }
              }
            }
          }
        }
      }
    }
  }
}

val sourcesJar by tasks.creating(Jar::class) {
  dependsOn("classes")
  archiveClassifier.set("sources")
  from(sourceSets["main"].allSource)
}

val javadocJar by tasks.creating(Jar::class) {
  dependsOn("javadoc")
  archiveClassifier.set("javadoc")
  from(tasks["javadoc"])
}

artifacts {
  withGroovyBuilder {
    "archives"(sourcesJar, javadocJar)
  }
}

signing {
  sign(configurations.archives.get())
}
