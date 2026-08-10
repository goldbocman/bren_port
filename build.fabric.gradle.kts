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
        "fabric-renderer-indigo",
        "fabric-model-loading-api-v1",
        if (sc.current.parsed >= "26.1") "fabric-key-mapping-api-v1" else "fabric-key-binding-api-v1",
        if (sc.current.parsed >= "26.1") "fabric-creative-tab-api-v1" else "fabric-item-group-api-v1",
        // fabric-convention-tags-v2 doesn't exist yet at 1.20.1 (fabric-api 0.92.11+1.20.1's own
        // settings.gradle only lists a v1) - confirmed by checking that version's actual module list
        // on GitHub, not assumed from a version-number pattern.
        if (sc.current.parsed >= "1.20.5") "fabric-convention-tags-v2" else "fabric-convention-tags-v1",
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

if (sc.current.parsed < "1.20.5") {
    // 1.20.1 uses plural data pack folder names (data/vgm/recipes/, data/vgm/advancements/)
    // instead of >=1.20.5's singular data/vgm/recipe/ and data/vgm/advancement/ - confirmed against
    // vanilla's own 1.20.1 jar (data/minecraft/recipes/, data/minecraft/advancements/), not assumed
    // from the >=1.21.11 schema difference above. data/vgm/recipe/*.json is already excluded by the
    // <1.21.11 block above (1.20.1 < 1.21.11 too); this additionally drops the rest of
    // data/vgm/advancement/ (only root.json is excluded above) since every advancement file needs
    // the plural path here, not just the ones with schema differences - the plural-path replacements
    // are datagen'd via the 1.20.1 VgmAdvancementProvider/Legacy1201VgmRecipeProvider branches and
    // diff-copied into legacy-resources-1.20.1/.
    sourceSets.main {
        resources {
            exclude("data/vgm/advancement/**")
        }
    }
}


fun registerPlaceDatagenOutput(taskDescription: String, vararg mirrors: Pair<String, String>) {
    val syncTasks = mirrors.mapIndexed { i, (fromDir, intoDir) ->
        tasks.register<Sync>("placeDatagenOutputSync$i") {
            dependsOn("runDatagen")
            from(fromDir)
            into(rootProject.file(intoDir))
        }
    }
    val cleanup = tasks.register<Delete>("placeDatagenOutputCleanup") {
        dependsOn(syncTasks)
        delete("src/main/generated")
    }
    tasks.register("placeDatagenOutput") {
        group = "vgm"
        description = taskDescription
        dependsOn(cleanup)
    }
}

if (sc.current.parsed < "1.20.5") {
    registerPlaceDatagenOutput(
        "Runs datagen and diff-copies its output into legacy-resources-1.20.1/, then deletes the " +
            "src/main/generated scratch copy so it can never linger stale.",
        "src/main/generated/data/vgm/advancements" to "legacy-resources-1.20.1/data/vgm/advancements",
        "src/main/generated/data/vgm/recipes" to "legacy-resources-1.20.1/data/vgm/recipes",
    )
} else if (sc.current.parsed >= "1.21.11") {
    registerPlaceDatagenOutput(
        "Runs datagen and diff-copies its output into the canonical src/main/resources/ tree, then " +
            "deletes the src/main/generated scratch copy so it can never linger stale.",
        "src/main/generated/data/vgm/advancement" to "src/main/resources/data/vgm/advancement",
        "src/main/generated/data/vgm/recipe" to "src/main/resources/data/vgm/recipe",
    )
} else {
    // >=1.20.5 <1.21.11 (1.21.1): recipe/ is the complete mirror, advancement/ is NOT (see above) -
    // only sync the recipe half automatically. root.json/warcrimes.json still need a manual
    // diff/copy from src/main/generated/data/vgm/advancement/ on the rare occasion either of those
    // two specific schema differences needs to change.
    registerPlaceDatagenOutput(
        "Runs datagen and diff-copies its recipe/ output into legacy-resources/, then deletes the " +
            "src/main/generated scratch copy so it can never linger stale. Does NOT touch " +
            "advancement/ - root.json/warcrimes.json are deliberate sparse overrides, not a mirror, " +
            "and need a manual diff/copy if they ever have to change.",
        "src/main/generated/data/vgm/recipe" to "legacy-resources/data/vgm/recipe",
    )
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
            // 1.20.1 predates DeltaTracker too (see vgm.legacy1201.mixins.json), a narrower cutoff
            // nested inside the same "legacy" tier.
            put("mixinsConfig", when {
                sc.current.parsed >= "1.21.11" -> "vgm.mixins.json"
                sc.current.parsed >= "1.20.5" -> "vgm.legacy.mixins.json"
                else -> "vgm.legacy1201.mixins.json"
            })
        }

        filesMatching("fabric.mod.json") { expand(props) }

        val mixinJava = "JAVA_${requiredJava.majorVersion}"
        filesMatching("*.mixins.json") { expand("java" to mixinJava) }

        exclude("META-INF/neoforge.mods.toml", "META-INF/mods.toml")

        // legacy-resources/ and legacy-resources-pre-26.2/ are 1.21.1-schema (singular data/vgm/recipe,
        // data/vgm/advancement paths) - 1.20.1 needs its own plural-path schema instead (see the
        // <1.20.5 exclude block above), so both are scoped to >=1.20.5 here. Below 1.20.5, everything
        // needed already comes from legacy-resources-1.20.1/ (self-sufficient, including the warcrimes
        // bare-"type"-key fix baked into the freshly datagen'd JSON) - pulling in the singular-path
        // directories too would just add dead files 1.20.1's data pack loader never scans.
        if (sc.current.parsed < "1.21.11" && sc.current.parsed >= "1.20.5") {
            from(rootProject.file("legacy-resources"))
        }

        if (sc.current.parsed < "26.2" && sc.current.parsed >= "1.20.5") {
            from(rootProject.file("legacy-resources-pre-26.2"))
        }

        if (sc.current.parsed < "1.20.5") {
            from(rootProject.file("legacy-resources-1.20.1"))
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
