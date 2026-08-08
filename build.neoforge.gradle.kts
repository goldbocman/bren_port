plugins {
    id("net.neoforged.moddev") version "2.0.140"
    id("neoforge-mutex")
}

version = "${property("mod.version")}+${sc.current.version}"
base.archivesName = "${property("mod.id") as String}-neoforge"

val requiredJava = when {
    sc.current.parsed >= "26.1" -> JavaVersion.VERSION_25
    sc.current.parsed >= "1.20.5" -> JavaVersion.VERSION_21
    sc.current.parsed >= "1.18" -> JavaVersion.VERSION_17
    sc.current.parsed >= "1.17" -> JavaVersion.VERSION_16
    else -> JavaVersion.VERSION_1_8
}

repositories {
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

}

neoForge {
    version = property("deps.neo_loader") as String

    mods {
        register("vgm") {
            sourceSet(sourceSets.main.get())
        }
    }

    runs {
        register("client") {
            gameDirectory = file("../../run/")
            client()
        }

        register("server") {
            gameDirectory = file("../../run/")
            server()
        }

        register("data") {
            gameDirectory = file("../../run/")
            // Pre-1.21.11 NeoForge/NeoForm only exposes a single unified "data" run type;
            // 1.21.11+/26.2.x split it into clientData/serverData. Calling the wrong one
            // compiles fine (both are real RunModel DSL methods) but fails at task-configuration
            // time with "Trying to prepare unknown run: <type>. Available run types: [...]"
            // since the valid set comes from that MC version's own NeoForm run templates.
            if (sc.current.parsed < "1.21.11") data() else clientData()
        }
    }
}

java {
    withSourcesJar()
    targetCompatibility = requiredJava
    sourceCompatibility = requiredJava

    toolchain {
        vendor = JvmVendorSpec.ADOPTIUM
        languageVersion = JavaLanguageVersion.of(requiredJava.majorVersion)
    }
}

if (sc.current.parsed < "1.21.11") {
    // 1.21.1's recipe JSON schema (ingredient objects, e.g. {"item": "..."}) differs from
    // 26.2.x/1.21.11+'s (bare namespaced-ID strings) - the shared data/vgm/recipe/*.json files
    // are 26.2/1.21.11-schema, so exclude them here and substitute the legacy-schema copies
    // datagen'd via LegacyVgmRecipeProvider and diff-copied into legacy-resources/.
    //
    // Same story for the root advancement's display.background: 1.21.1 needs the full
    // "textures/...png" path while 1.21.11+/26.2.x resolve a bare sprite-style id - see
    // claude/datagen-migration.md.
    sourceSets.main {
        resources {
            exclude("data/vgm/recipe/*.json", "data/vgm/advancement/adventure/root.json")
        }
    }
}

if (sc.current.parsed < "26.2") {
    // The vgm:warcrimes advancement's entity-type predicate key is "minecraft:entity_type" only
    // on 26.2; 1.21.1 and 1.21.11 both need the bare "type" key instead, or the predicate is
    // silently ignored and the criterion fires for any killed entity - see
    // claude/datagen-migration.md. This cutoff is narrower than (and independent of) the
    // 1.21.11 one above, so it needs its own directory/gate.
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
            // 1.21.1 predates the render-state rendering rewrite, so its client mixins target
            // entirely different vanilla classes than 1.21.11+/26.2.x - see vgm.legacy.mixins.json.
            put("mixinsConfig", if (sc.current.parsed >= "1.21.11") "vgm.mixins.json" else "vgm.legacy.mixins.json")
        }

        filesMatching("META-INF/neoforge.mods.toml") { expand(props) }

        val mixinJava = "JAVA_${requiredJava.majorVersion}"
        filesMatching("*.mixins.json") { expand("java" to mixinJava) }

        exclude("fabric.mod.json", "*.ct", "*.classtweaker")

        if (sc.current.parsed < "1.21.11") {
            from(rootProject.file("legacy-resources"))
        }

        if (sc.current.parsed < "26.2") {
            from(rootProject.file("legacy-resources-pre-26.2"))
        }
    }

    named("createMinecraftArtifacts") {
        dependsOn("stonecutterGenerate")
    }

    register<Copy>("buildAndCollect") {
        group = "build"
        description = "Builds mod jars and copies results to `build/libs/{mod version}/`"

        inputs.property("version", project.property("mod.version"))
        from(jar.flatMap { it.archiveFile }, named<Jar>("sourcesJar").flatMap { it.archiveFile })
        into(rootProject.layout.buildDirectory.file("libs/${project.property("mod.version")}"))
    }
}
