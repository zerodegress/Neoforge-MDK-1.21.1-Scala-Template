import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.wrapper.Wrapper
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.language.jvm.tasks.ProcessResources
import org.gradle.plugins.ide.idea.model.IdeaModel
import org.gradle.api.plugins.scala.ScalaPluginExtension

plugins {
    scala
    `java-library`
    eclipse
    idea
    `maven-publish`
    id("net.neoforged.gradle.userdev") version "7.1.25"
}

val minecraft_version: String by project
val minecraft_version_range: String by project
val neo_version: String by project
val loader_version_range: String by project
val mod_id: String by project
val mod_name: String by project
val mod_license: String by project
val mod_version: String by project
val mod_authors: String by project
val mod_description: String by project
val mod_group_id: String by project

tasks.named<Wrapper>("wrapper").configure {
    distributionType = Wrapper.DistributionType.BIN
}

version = mod_version
group = mod_group_id

the<SourceSetContainer>().named("main") {
    resources.srcDir("src/generated/resources")
    resources.exclude("**/*.bbmodel")
    resources.exclude("src/generated/**/.cache")
}

repositories {
    mavenLocal()

    maven("https://maven.kotori316.com") {
        name = "kotori316"
        content {
            includeModule("com.kotori316", "scalablecatsforce-neoforge")
            includeModule("org.typelevel", "cats-core_3")
            includeModule("org.typelevel", "cats-kernel_3")
            includeModule("org.typelevel", "cats-free_3")
        }
    }
}

base {
    archivesName.set(mod_id)
}

java.toolchain.languageVersion.set(JavaLanguageVersion.of(21))

runs {
    configureEach {
        systemProperty("forge.logging.markers", "REGISTRIES")
        systemProperty("forge.logging.console.level", "debug")
        modSource(project.sourceSets.main.get())
    }

    create("client") {
        systemProperty("neoforge.enabledGameTestNamespaces", mod_id)
    }

    create("server") {
        systemProperty("neoforge.enabledGameTestNamespaces", mod_id)
        argument("--nogui")
    }

    create("gameTestServer") {
        systemProperty("neoforge.enabledGameTestNamespaces", mod_id)
    }

    create("data") {
        arguments.addAll(
            "--mod", mod_id,
            "--all",
            "--output", file("src/generated/resources/").absolutePath,
            "--existing", file("src/main/resources/").absolutePath,
        )
    }
}

configurations {
    runtimeClasspath.get().extendsFrom(localRuntime.get())
}

dependencies {
    implementation("net.neoforged:neoforge:$neo_version")

    compileOnly(group = "org.scala-lang", name = "scala3-library_3", version = "3.3.4")
    compileOnly(group = "org.typelevel", name = "cats-core_3", version = "2.12.1-kotori")
    runtimeOnly(group = "com.kotori316", name = "scalablecatsforce-neoforge", version = "3.6.2-build-0", classifier = "with-library") {
        isTransitive = false
    }
}

tasks.withType<ProcessResources>().configureEach {
    val replaceProperties = mapOf(
        "minecraft_version" to minecraft_version,
        "minecraft_version_range" to minecraft_version_range,
        "neo_version" to neo_version,
        "loader_version_range" to loader_version_range,
        "mod_id" to mod_id,
        "mod_name" to mod_name,
        "mod_license" to mod_license,
        "mod_version" to mod_version,
        "mod_authors" to mod_authors,
        "mod_description" to mod_description,
    )

    inputs.properties(replaceProperties)

    filesMatching("META-INF/neoforge.mods.toml") {
        expand(replaceProperties)
    }
}

publishing {
    publications {
        register<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }
    repositories {
        maven(url = uri("file://${project.projectDir}/repo"))
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
}

configure<IdeaModel> {
    module {
        isDownloadSources = true
        isDownloadJavadoc = true
    }
}

configure<ScalaPluginExtension> {
    scalaVersion.set("3.3.4")
}
