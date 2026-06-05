plugins {
    `maven-publish`
    id("hytale-mod") version "0.+"
    id("com.gradleup.shadow") version "9.4.2"
}


group = "com.riprod"
version = "0.8.6"
val javaVersion = 25

repositories {
    mavenCentral()
    maven("https://www.cursemaven.com/")
    maven("https://maven.hytale-modding.info/releases") {
        name = "HytaleModdingReleases"
    }
}

val shadowBundle by configurations.creating

dependencies {
    compileOnly(libs.jetbrains.annotations)
    compileOnly(libs.jspecify)

    compileOnly(fileTree("lib/") { include("*.jar") })

    // patcher is bundled into hexcode's output jar via the Shadow plugin
    shadowBundle("curse.maven:patchly-1550889:8188362")
    implementation("curse.maven:patchly-1550889:8188362")

}

tasks.shadowJar {
    archiveClassifier.set("")
    mergeServiceFiles()
    configurations = listOf(shadowBundle)
    relocate("com.riprod.patchly", "com.riprod.hexcode.shaded.patchly")
}

tasks.jar {
    enabled = false
}

tasks.build {
    dependsOn(tasks.shadowJar)
}

hytale {
    // uncomment if you want to add the Assets.zip file to your external libraries;
    // ⚠️ CAUTION, this file is very big and might make your IDE unresponsive for some time!
    //
    // addAssetsDependency = true

    // uncomment if you want to develop your mod against the pre-release version of the game.
    //
    // updateChannel = "pre-release"
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(javaVersion)
    }

    withSourcesJar()
}

tasks.named<ProcessResources>("processResources") {
    var replaceProperties = mapOf(
        "plugin_group" to findProperty("plugin_group"),
        "plugin_maven_group" to project.group,
        "plugin_name" to project.name,
        "plugin_version" to project.version,
        "server_version" to findProperty("server_version"),

        "plugin_description" to findProperty("plugin_description"),
        "plugin_website" to findProperty("plugin_website"),

        "plugin_main_entrypoint" to findProperty("plugin_main_entrypoint"),
        "plugin_author" to findProperty("plugin_author")
    )

    filesMatching("manifest.json") {
        expand(replaceProperties)
    }

    inputs.properties(replaceProperties)
}

tasks.withType<Jar> {
    manifest {
        attributes["Specification-Title"] = rootProject.name
        attributes["Specification-Version"] = version
        attributes["Implementation-Title"] = project.name
        attributes["Implementation-Version"] =
            providers.environmentVariable("COMMIT_SHA_SHORT")
                .map { "${version}-${it}" }
                .getOrElse(version.toString())
    }
}

publishing {
    repositories {
        // This is where you put repositories that you want to publish to.
        // Do NOT put repositories for your dependencies here.
    }

    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}

// IDEA no longer automatically downloads sources/javadoc jars for dependencies, so we need to explicitly enable the behavior.
idea {
    module {
        isDownloadSources = true
        isDownloadJavadoc = true
    }
}

val linkedDirs = listOf("Server")
val syncBackDirs = listOf("Common")

val unlinkAssets = tasks.register("unlinkAssets") {
    doFirst {
        val buildRes = layout.buildDirectory.dir("resources/main").get().asFile
        linkedDirs.forEach { dir ->
            val buildDir = File(buildRes, dir)
            if (buildDir.exists()) {
                val isSymlink = ProcessBuilder("test", "-L", buildDir.absolutePath)
                    .start().waitFor() == 0
                if (isSymlink) {
                    ProcessBuilder("rm", buildDir.absolutePath)
                        .inheritIO().start().waitFor()
                    logger.lifecycle("Unlinked ${buildDir.toPath()}")
                }
            }
        }
    }
}

tasks.named("processResources") {
    dependsOn(unlinkAssets)
}

tasks.register("linkAssets") {
    group = "hytale"
    description = "Replaces built asset dirs with symlinks to source for live editing."
    dependsOn("jar")

    doLast {
        val buildRes = layout.buildDirectory.dir("resources/main").get().asFile
        val srcRes = file("src/main/resources")

        linkedDirs.forEach { dir ->
            val srcDir = File(srcRes, dir)
            val buildDir = File(buildRes, dir)
            if (srcDir.exists()) {
                if (buildDir.exists()) {
                    ProcessBuilder("rm", "-rf", buildDir.absolutePath)
                        .inheritIO().start().waitFor()
                }
                ProcessBuilder("ln", "-s", srcDir.absolutePath, buildDir.absolutePath)
                    .inheritIO().start().waitFor()
                logger.lifecycle("Linked ${buildDir.toPath()} -> ${srcDir.toPath()}")
            }
        }
    }
}

tasks.register("syncBackAssets") {
    group = "hytale"
    description = "Copies non-symlinked build asset dirs back to source."

    doLast {
        val buildRes = layout.buildDirectory.dir("resources/main").get().asFile
        val srcRes = file("src/main/resources")

        syncBackDirs.forEach { dir ->
            val buildDir = File(buildRes, dir)
            val srcDir = File(srcRes, dir)
            if (buildDir.exists() && buildDir.isDirectory) {
                val isSymlink = ProcessBuilder("test", "-L", buildDir.absolutePath)
                    .start().waitFor() == 0
                if (!isSymlink) {
                    buildDir.copyRecursively(srcDir, overwrite = true)
                    logger.lifecycle("Synced back ${buildDir.toPath()} -> ${srcDir.toPath()}")
                }
            }
        }
    }
}

afterEvaluate {
    val targetTask = tasks.findByName("runServer") ?: tasks.findByName("server")
    if (targetTask != null) {
        targetTask.dependsOn("linkAssets")
        targetTask.finalizedBy("syncBackAssets")
        if (targetTask is JavaExec) {
            targetTask.classpath += fileTree("lib/") { include("*.jar") }
            // hytale-mod 0.7.2-alpha injects an empty string into jvmArgs when
            // HytaleServer.aot is missing; java reads that as the main class. strip it.
            targetTask.doFirst {
                val exec = this as JavaExec
                exec.jvmArgs = exec.jvmArgs?.filter { it.isNotEmpty() } ?: emptyList()
            }
        }
    }
}
