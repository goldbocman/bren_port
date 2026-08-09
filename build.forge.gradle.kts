plugins {
    id("net.minecraftforge.gradle") version "[7.0,8.0)"
}

version = "${property("mod.version")}+${sc.current.version}"
base.archivesName = "${property("mod.id") as String}-forge"

val requiredJava = when {
    sc.current.parsed >= "26.1" -> JavaVersion.VERSION_25
    sc.current.parsed >= "1.20.5" -> JavaVersion.VERSION_21
    sc.current.parsed >= "1.18" -> JavaVersion.VERSION_17
    sc.current.parsed >= "1.17" -> JavaVersion.VERSION_16
    else -> JavaVersion.VERSION_1_8
}

// 1.21.1 predates the render-state rendering rewrite, so its client mixins target entirely
// different vanilla classes than 1.21.11+/26.2.x - see vgm.legacy.mixins.json.
val mixinsConfig = if (sc.current.parsed >= "1.21.11") "vgm.mixins.json" else "vgm.legacy.mixins.json"

java.toolchain.languageVersion = JavaLanguageVersion.of(requiredJava.majorVersion)

minecraft {
    mappings(mapOf("channel" to "official", "version" to sc.current.version))

    runs {
        configureEach {
            workingDir.set(rootProject.file("run"))
            systemProperty("forge.logging.console.level", "debug")
            systemProperty("eventbus.api.strictRuntimeChecks", "true")
            args("--mixin.config=$mixinsConfig")
        }

        register("client") {
            systemProperty("forge.enabledGameTestNamespaces", property("mod.id") as String)
        }

        register("server") {
            systemProperty("forge.enabledGameTestNamespaces", property("mod.id") as String)
            args("--nogui")
        }

        register("data") {
            workingDir.set(rootProject.file("run-data"))
            args(
                "--mod", property("mod.id") as String,
                "--all",
                "--output", file("src/generated/resources").path,
                "--existing", rootProject.file("src/main/resources").path
            )
        }
    }
}

repositories {
    minecraft.mavenizer(this)
    maven(fg.forgeMaven)
    maven(fg.minecraftLibsMaven)

    /**
     * Restricts dependency search of the given [groups] to the [maven URL][url],
     * improving the setup speed.
     */
    fun strictMaven(url: String, alias: String, vararg groups: String) = exclusiveContent {
        forRepository { maven(url) { name = alias } }
        filter { groups.forEach(::includeGroup) }
    }
    strictMaven("https://www.cursemaven.com", "CurseForge", "curse.maven")
    strictMaven("https://api.modrinth.com/maven", "Modrinth", "maven.modrinth")
}

dependencies {
    implementation(minecraft.dependency("net.minecraftforge:forge:${sc.current.version}-${property("deps.forge_version")}"))
    annotationProcessor("org.spongepowered:mixin:0.8.7:processor")
    // Forge 1.21.6+ moved most EventBusSubscriber runtime validation to compile-time via this
    // annotation processor - see https://docs.minecraftforge.net for details.
    if (sc.current.parsed >= "1.21.6") {
        annotationProcessor("net.minecraftforge:eventbus-validator:7.0.5")
    }
}

// Forge 1.21.1+ ships against Mojang's official (non-obfuscated) mappings in both dev and
// production, so there's no real SRG remapping step for Mixin to perform - but Mixin 0.8.7's
// annotation processor still bundles a legacy MCP obfuscation-mapping validator that otherwise
// hard-fails every @Inject with "Unable to locate obfuscation mapping" (ForgeGradle 7 doesn't
// auto-wire this - confirmed by inspecting the plugin jar itself, no mixin-related classes at
// all). Fixed the same way Forge's own official mixins-only MDK example does it: mark injections
// remap = false (see the //? if forge additions across this mod's mixin classes). A real SRG file
// exists in the Mavenizer cache (mcp_config/.../joined.tsrg) but it's TSRG-format, not the classic
// .srg format Mixin's ObfuscationServiceMCP expects ("No valid input files for searge could be
// read" if pointed at it) - not worth converting since remap=false makes it unnecessary anyway.

if (sc.current.parsed < "1.21.11") {
    // See build.neoforge.gradle.kts for why - identical reasoning, mirrored here.
    sourceSets.main {
        resources {
            exclude("data/vgm/recipe/*.json", "data/vgm/advancement/adventure/root.json")
        }
    }
}

if (sc.current.parsed < "26.2") {
    // See build.neoforge.gradle.kts for why - identical reasoning, mirrored here.
    sourceSets.main {
        resources {
            exclude("data/vgm/advancement/adventure/warcrimes.json")
        }
    }
}

tasks {
    processResources {
        fun MutableMap<String, String>.register(key: String, property: String) {
            val value: String = sc.properties[property]
            inputs.property(key, value)
            set(key, value)
        }

        val props = buildMap {
            register("id", "mod.id")
            register("name", "mod.name")
            register("version", "mod.version")
            register("minecraft", "mod.mc_compat")
            register("forgeLoaderRange", "deps.forge_loader_range")
        }

        filesMatching("META-INF/mods.toml") { expand(props) }

        val mixinJava = "JAVA_${requiredJava.majorVersion}"
        filesMatching("*.mixins.json") { expand("java" to mixinJava) }

        exclude("fabric.mod.json", "META-INF/neoforge.mods.toml", "*.ct", "*.classtweaker")

        if (sc.current.parsed < "1.21.11") {
            from(rootProject.file("legacy-resources"))
        }

        if (sc.current.parsed < "26.2") {
            from(rootProject.file("legacy-resources-pre-26.2"))
        }
    }

    named("jar", Jar::class) {
        manifest {
            // Mixin reads this attribute to find its config(s) at runtime from the built jar;
            // the --mixin.config run arg above only covers the Gradle-run dev environment.
            attributes["MixinConfigs"] = mixinsConfig
        }
    }

    register<Copy>("buildAndCollect") {
        group = "build"
        description = "Builds mod jars and copies results to `build/libs/{mod version}/`"

        inputs.property("version", project.property("mod.version"))
        from(jar.flatMap { it.archiveFile })
        into(rootProject.layout.buildDirectory.file("libs/${project.property("mod.version")}"))
    }
}
