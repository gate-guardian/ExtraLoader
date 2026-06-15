import org.slf4j.event.Level

plugins {
    eclipse
    idea
    `maven-publish`
    alias(libs.plugins.modDevGradle)
    alias(libs.plugins.lombok)
}

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
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
    // JEI
    maven {
        name = "Jared's Maven"
        url = uri("https://maven.blamejared.com/")
    }
    // JEI mirror, AE2, Mekanism
    maven {
        name = "ModMaven"
        url = uri("https://modmaven.dev")
    }
    // shedaniel - REI, architectury, cloth-config
    maven {
        url = uri("https://maven.shedaniel.me/")
        content {
            includeGroupAndSubgroups("me.shedaniel")
            includeGroup("dev.architectury")
        }
    }
    // terraformers - EMI
    maven {
        url = uri("https://maven.terraformersmc.com/releases/")
        content {
            includeGroup("dev.emi")
        }
    }

    exclusiveContent {
        forRepository {
            maven {
                name = "Modrinth"
                url = uri("https://api.modrinth.com/maven")
            }
        }
        filter {
            includeGroup("maven.modrinth")
        }
    }

    exclusiveContent {
        forRepository {
            maven {
                name = "CurseForge"
                url = uri("https://cursemaven.com")
            }
        }
        filter {
            includeGroup("curse.maven")
        }
    }

    exclusiveContent {
        forRepository {
            maven {
                name = "FTB Mods"
                url = uri("https://maven.ftb.dev/releases")
            }
        }
        filter {
            includeGroup("dev.ftb.mods")
        }
    }

    mavenLocal()
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

neoForge {
    version = libs.versions.neo.get()

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
            systemProperty("neoforge.enabledGameTestNamespaces", modId)
        }

        register("server") {
            server()
            programArgument("--nogui")
            systemProperty("neoforge.enabledGameTestNamespaces", modId)
        }

        register("gameTestServer") {
            type = "gameTestServer"
            systemProperty("neoforge.enabledGameTestNamespaces", modId)
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

dependencies {
    // Mixin (& Extras)
    annotationProcessor(libs.mixinExtras.common)
    implementation(libs.mixinExtras.common)
    implementation(libs.mixinExtras.neoforge)
    jarJar(libs.mixinExtras.neoforge)

    // Recipe Viewers - compile only
    compileOnly(libs.jei.api.common)
    compileOnly(libs.jei.api.neoforge)
    compileOnly(variantOf(libs.emi) { classifier("api") })
    // REI - 1.21.1 NeoForge 暂不可用，已注释

    // region For testing
    "localRuntime"(libs.jei.impl)
    "localRuntime"(libs.emi)
    "localClientRuntime"(libs.modernui)
    "localClientRuntime"(libs.jecharacters)
    "localClientRuntime"(libs.jade)
    // endregion
}

idea {
    module {
        isDownloadSources = true
        isDownloadJavadoc = true
    }
}

fun requiredProperty(name: String) = providers.gradleProperty(name).orNull ?: error("Missing gradle property: $name")
