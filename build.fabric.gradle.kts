plugins {
    // This plugin applies the correct loom variant based on the Minecraft version
    id("dev.kikugie.loom-back-compat")
}

// DO NOT set group = ...!
version = "${property("mod.version")}+${sc.current.version}"
base.archivesName = "${property("mod.id") as String}-fabric"

val requiredJava: JavaVersion = when {
    sc.current.parsed >= "26.1" -> JavaVersion.VERSION_25
    sc.current.parsed >= "1.20.5" -> JavaVersion.VERSION_21
    sc.current.parsed >= "1.18" -> JavaVersion.VERSION_17
    sc.current.parsed >= "1.17" -> JavaVersion.VERSION_16
    else -> JavaVersion.VERSION_1_8
}

// This can be used for publishing on Modrinth and Curseforge
val compatibleVersions: List<String> = sc.properties.rawOrNull("mod", "mc_releases")
    ?.asList().orEmpty().map { it.toString() }

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
    /**
     * Fetches only the required Fabric API modules to not waste time downloading all of them for each version.
     * @see <a href="https://github.com/FabricMC/fabric">List of Fabric API modules</a>
     */
    fun fapi(vararg modules: String) {
        for (it in modules) modImplementation(fabricApi.module(it, sc.properties["deps.fabric_api"]))
    }

    minecraft("com.mojang:minecraft:${sc.current.version}")
    // Applies Mojang Mappings on obfuscated versions
    loomx.applyMojangMappings()

    // Use `mod{dependency type}` even on 26.1+ - loom-back-compat converts them
    modImplementation("net.fabricmc:fabric-loader:${property("deps.fabric_loader")}")
    fapi(
        "fabric-lifecycle-events-v1",
        "fabric-resource-loader-v0",
        "fabric-content-registries-v0",
        "fabric-registry-sync-v0",
        "fabric-networking-api-v1",
        "fabric-particles-v1",
        "fabric-rendering-v1",
        "fabric-model-loading-api-v1",
        if (sc.current.parsed >= "26.1") "fabric-key-mapping-api-v1" else "fabric-key-binding-api-v1",
        if (sc.current.parsed >= "26.1") "fabric-creative-tab-api-v1" else "fabric-item-group-api-v1",
        "fabric-convention-tags-v2",
        "fabric-data-generation-api-v1"
    )
}

loom {
    fabricModJsonPath = rootProject.file("src/main/resources/fabric.mod.json") // Useful for interface injection
    accessWidenerPath = sc.process(
        rootProject.file("src/main/resources/vgm.ct"),
        "build/processed.ct"
    )

    decompilerOptions.named("vineflower") {
        options.put("mark-corresponding-synthetics", "1") // Adds names to lambdas - useful for mixins
    }

    runConfigs.all {
        preferGradleTask = true
        generateRunConfig = true
        runDirectory = rootProject.file("run") // Shares the run directory between versions
        jvmArguments.add("-Dmixin.debug.export=true") // Exports transformed classes for debugging
    }
}

fabricApi {
    configureDataGeneration()
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

java {
    withSourcesJar()
    targetCompatibility = requiredJava
    sourceCompatibility = requiredJava

    toolchain {
        vendor = JvmVendorSpec.ADOPTIUM
        languageVersion = JavaLanguageVersion.of(requiredJava.majorVersion)
    }
}

tasks {
    processResources {
        // `fabricApi.configureDataGeneration()` wires `src/main/generated` as a live resources
        // source dir, which duplicates whatever's already been diff-copied into the shared
        // `src/main/resources` tree (this repo treats datagen output as a manual diff/copy
        // artifact, never a live-compiled source set) - keep the first-seen (real) copy.
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE

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

        filesMatching("fabric.mod.json") { expand(props) }

        val mixinJava = "JAVA_${requiredJava.majorVersion}"
        filesMatching("*.mixins.json") { expand("java" to mixinJava) }

        exclude("META-INF/neoforge.mods.toml")

        if (sc.current.parsed < "1.21.11") {
            from(rootProject.file("legacy-resources"))
        }

        if (sc.current.parsed < "26.2") {
            from(rootProject.file("legacy-resources-pre-26.2"))
        }
    }

    register<Copy>("buildAndCollect") {
        group = "build"
        description = "Builds mod jars and copies results to `build/libs/{mod version}/`"

        inputs.property("version", project.property("mod.version"))
        // loomx.mod(Sources)Jar returns the jar task for the applied loom variant
        from(loomx.modJar.flatMap { it.archiveFile }, loomx.modSourcesJar.flatMap { it.archiveFile })
        into(rootProject.layout.buildDirectory.file("libs/${project.property("mod.version")}"))
    }
}
