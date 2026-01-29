import org.slf4j.event.Level

plugins {
    eclipse
    idea
    `maven-publish`
    alias(libs.plugins.modDevGradle)
    alias(libs.plugins.lombok)
}

apply(from = "repositories.gradle.kts")

val modId      = requiredProperty("mod_id")
val modVersion = requiredProperty("mod_version")
val modGroupId = requiredProperty("mod_group_id")
val modAuthors = requiredProperty("mod_authors")

version = modVersion
group = modGroupId

base {
    archivesName = modId
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

val generateModMetadata by tasks.registering(ProcessResources::class) {
    val props = properties.mapValues { it.value.toString() }
    inputs.properties(props)
    expand(props)
    from("src/main/templates")
    into("build/generated/sources/modMetadata")
}

tasks {
    withType<JavaCompile>().configureEach {
        options.encoding = Charsets.UTF_8.name()
    }
}

val client by sourceSets.creating {
    runtimeClasspath += sourceSets.main.get().runtimeClasspath
    runtimeClasspath += sourceSets.main.get().output
}

val gameTest by sourceSets.creating {
    compileClasspath += sourceSets.main.get().compileClasspath
    compileClasspath += sourceSets.main.get().output
}

sourceSets {
    main {
        resources {
            srcDir(generateModMetadata)
            srcDir("src/generated/resources")
            exclude("**/.cache")
        }
    }
}

legacyForge {
    version = "${libs.versions.minecraft.get()}-${libs.versions.forge.get()}"

    parchment {
        minecraftVersion = libs.versions.minecraft.get()
        mappingsVersion = libs.versions.parchment.get()
    }

    mods {
        register(modId) {
            sourceSet(sourceSets.main.get())
            sourceSet(gameTest)
        }
    }

    runs {
        register("client") {
            client()
            sourceSet = client
            systemProperty("forge.enabledGameTestNamespaces", modId)
        }

        register("server") {
            server()
            programArgument("--nogui")
            systemProperty("forge.enabledGameTestNamespaces", modId)
        }

        register("gameTestServer") {
            type = "gameTestServer"
            systemProperty("forge.enabledGameTestNamespaces", modId)
        }

        register("data") {
            data()
            programArguments.addAll(
                    "--mod", modId,
                    "--all",
                    "--output", file("src/generated/resources/").absolutePath,
                    "--existing", file("src/main/resources/").absolutePath
            )
        }

        configureEach {
            systemProperty("forge.logging.markers", "REGISTRIES")
            systemProperty("forge.logging.console.level", "debug")
            logLevel = Level.DEBUG
        }
    }

    ideSyncTask(generateModMetadata)
}

mixin {
    add(sourceSets.main.get(), "${modId}.refmap.json")
    config("${modId}.mixins.json")
}

val localImplementation by configurations.creating
val localRuntime by configurations.creating
val localClientRuntime by configurations.creating

configurations {
    compileClasspath {
        extendsFrom(localImplementation)
    }

    runtimeClasspath {
        extendsFrom(localImplementation)
        extendsFrom(localRuntime)
    }

    named(client.runtimeClasspathConfigurationName) {
        extendsFrom(localClientRuntime)
    }
}

obfuscation {
    createRemappingConfiguration(localImplementation)
    createRemappingConfiguration(localRuntime)
    createRemappingConfiguration(localClientRuntime)
}

dependencies {
    // Mixin (& Extras)
    annotationProcessor(variantOf(libs.mixin) { classifier("processor") })
    annotationProcessor(libs.mixinExtras.common)?.let { api(it) }

    // Recipe Viewers
    modCompileOnly(libs.jei.api.common)
    modCompileOnly(libs.jei.api.forge)
    modCompileOnly(variantOf(libs.emi) { classifier("api") })
    modCompileOnly(libs.rei.api)

    // region For testing
    "modLocalRuntime"(libs.jei.impl)
    "modLocalRuntime"(libs.emi)
    "modLocalClientRuntime"(libs.modernui)
    "modLocalClientRuntime"(libs.jecharacters)
    "modLocalClientRuntime"(libs.jade)
    // endregion
}

idea {
    module {
        isDownloadSources = true
        isDownloadJavadoc = true
    }
}

fun requiredProperty(name: String) = providers.gradleProperty(name).orNull ?: error("Missing gradle property: $name")