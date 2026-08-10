import org.gradle.jvm.tasks.Jar
import org.spongepowered.asm.gradle.plugins.MixinExtension

// Standalone ForgeGradle 6 project for 1.20.1-forge. See build.forge1201.gradle.kts (in the
// main multiproject) for why this can't live in the main build under Gradle 9.6.1 - this
// project has its own wrapper (Gradle 8.14.5, see gradle.properties for the Java-25-vs-old-
// Kotlin-DSL-compiler pin) and reads the shared `src/` tree from `../` directly.
//
// Workflow: switch the shared tree first from the main repo, THEN build here:
//   ./gradlew :stonecutterSwitchTo1.20.1-forge      (run from repo root)
//   cd forge-1.20.1 && ./gradlew build              (this project)
//
// Unlike 1.21.1-forge (ForgeGradle 7, no real reobfuscation ), this
// project's `jar` task IS `finalizedBy("reobfJar")` below: Forge 1.20.1's production runtime is
// genuinely SRG-obfuscated, so every `@Inject`/`@Accessor`/`@Invoker` targeting a vanilla class
// needs real refmap-based remapping (the default `remap = true`), not the `remap = false` used
// for FG7 - see the `//? if forge && >=1.20.5` guard throughout the shared mixin classes. The
// SpongePowered MixinGradle plugin below wires up refmap generation against FG6's own SRG data,
// confirmed against a real, currently-maintained FG6 gun mod's build script
// (MCModderAnchor/TACZ's 1.20.1 branch) rather than guessed from scratch.
buildscript {
    repositories {
        maven("https://repo.spongepowered.org/repository/maven-public/") { name = "SpongePowered" }
    }
    dependencies {
        classpath("org.spongepowered:mixingradle:0.7.38")
    }
}

plugins {
    id("net.minecraftforge.gradle") version "[6.0,6.2)"
}

apply(plugin = "org.spongepowered.mixin")

fun stonecutterProperty(key: String, section: String): String {
    var currentSection = ""
    var topLevelValue: String? = null
    var sectionValue: String? = null
    file("../stonecutter.properties.toml").forEachLine { raw ->
        val line = raw.trim()
        if (line.startsWith("[") && line.endsWith("]")) {
            currentSection = line.substring(1, line.length - 1)
        } else if (line.isNotEmpty() && !line.startsWith("#")) {
            val eq = line.indexOf('=')
            if (eq > 0) {
                val k = line.substring(0, eq).trim()
                if (k == key) {
                    val v = line.substring(eq + 1).trim().removeSurrounding("\"")
                    if (currentSection.isEmpty()) topLevelValue = v
                    else if (currentSection == section) sectionValue = v
                }
            }
        }
    }
    return sectionValue ?: topLevelValue
        ?: error("Property '$key' not found in section '$section' or top-level of stonecutter.properties.toml")
}

val mcVersion = "1.20.1"
val forgeSection = "forge.\"$mcVersion\""
val forgeVersion = stonecutterProperty("deps.forge_version", forgeSection)
val forgeLoaderRange = stonecutterProperty("deps.forge_loader_range", forgeSection)
val mcCompat = stonecutterProperty("mod.mc_compat", forgeSection)
val modId = stonecutterProperty("mod.id", forgeSection)
val modName = stonecutterProperty("mod.name", forgeSection)
val modVersion = stonecutterProperty("mod.version", forgeSection)

version = "$modVersion+$mcVersion"
base.archivesName = "$modId-forge"

java.toolchain.languageVersion = JavaLanguageVersion.of(17)

minecraft {
    mappings(mapOf("channel" to "official", "version" to mcVersion))
    copyIdeResources = true

    runs {
        configureEach {
            workingDirectory(rootProject.file("../run"))
            property("forge.logging.markers", "REGISTRIES")
            property("forge.logging.console.level", "debug")
            // Running from an exploded build/resources/main/ directory (dev mode) rather than a
            // real packaged jar means Forge doesn't reliably discover the mod's MixinConfigs
            // manifest attribute (that's only ever written into the actual jar, see the `jar`
            // task's manifest block below) - mirrors build.forge.gradle.kts's identical
            // `args("--mixin.config=...")` for the same reason. Without this, Mixin never weaves
            // the client mixins at all: @Invoker stubs like LegacyItemPropertiesAccessor just
            // throw their literal AssertionError body when called - confirmed empirically.
            args.add("--mixin.config=vgm.legacy1201.mixins.json")
            mods.create(modId) {
                source(sourceSets.main.get())
            }
        }

        create("client") {
            property("forge.enabledGameTestNamespaces", modId)
        }

        create("server") {
            property("forge.enabledGameTestNamespaces", modId)
            args.add("--nogui")
        }

        create("data") {
            workingDirectory(project.file("run-data"))
            args.addAll(listOf(
                "--mod", modId,
                "--all",
                "--output", file("src/generated/resources").path,
                "--existing", rootProject.file("../src/main/resources").path
            ))
        }
    }
}

sourceSets {
    main {
        java.setSrcDirs(listOf(rootProject.file("../src/main/java")))
        resources {
            setSrcDirs(listOf(rootProject.file("../src/main/resources")))
            srcDir("src/generated/resources")
            // 1.20.1 uses plural datapack folder names and the legacy advancement/criterion
            // schema - see claude/1.20.1-fabric-backport.md. The shared tree's singular-path
            // >=1.20.5 files aren't valid here; legacy-resources-1.20.1/ supplies the real
            // plural-path replacements instead (mirrors build.fabric.gradle.kts's <1.20.5
            // handling).
            exclude("data/vgm/advancement/**")
        }
    }
}

configure<MixinExtension> {
    add(sourceSets.main.get(), "vgm-1201.refmap.json")
    config("vgm.legacy1201.mixins.json")
}

repositories {
    // ForgeGradle adds the Forge maven and Maven Central automatically.
    fun strictMaven(url: String, alias: String, vararg groups: String) = exclusiveContent {
        forRepository { maven(url) { name = alias } }
        filter { groups.forEach(::includeGroup) }
    }
    strictMaven("https://www.cursemaven.com", "CurseForge", "curse.maven")
    strictMaven("https://api.modrinth.com/maven", "Modrinth", "maven.modrinth")
}

dependencies {
    "minecraft"("net.minecraftforge:forge:$mcVersion-$forgeVersion")
    // Must match the Mixin version Forge 1.20.1 actually bundles at runtime (confirmed via a real
    // crash report's "ModLauncher services" list: "mixin-0.8.5.jar mixin PLUGINSERVICE") - not the
    // 0.8.7 used by build.forge.gradle.kts for 1.21.1 (which bundles a newer Mixin). Using a newer
    // AP than the runtime engine let compilation/config-loading succeed silently, but @Invoker
    // mixins (e.g. LegacyItemPropertiesAccessor) never actually got woven in: calling the
    // invoker method hit its literal throw-AssertionError body, crashing mod construction.
    annotationProcessor("org.spongepowered:mixin:0.8.5:processor")
}

tasks {
    named<ProcessResources>("processResources") {
        val props = mapOf(
            "id" to modId,
            "name" to modName,
            "version" to modVersion,
            "minecraft" to mcCompat,
            "forgeLoaderRange" to forgeLoaderRange
        )
        inputs.properties(props)
        filesMatching("META-INF/mods.toml") { expand(props) }
        filesMatching("*.mixins.json") { expand("java" to "JAVA_17") }

        exclude("fabric.mod.json", "META-INF/neoforge.mods.toml", "*.ct", "*.classtweaker")
        from(rootProject.file("../legacy-resources-1.20.1"))
    }

    named<Jar>("jar") {
        manifest {
            attributes["MixinConfigs"] = "vgm.legacy1201.mixins.json"
        }
        finalizedBy("reobfJar")
    }

    withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
    }

    register<Copy>("buildAndCollect") {
        group = "build"
        description = "Builds the mod jar and copies the result to `../build/libs/{mod version}/`"

        inputs.property("version", modVersion)
        from(named<Jar>("jar").flatMap { it.archiveFile })
        into(rootProject.file("../build/libs/$modVersion"))
    }
}
