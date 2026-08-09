pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.fabricmc.net/") { name = "FabricMC" }
        maven("https://maven.neoforged.net/releases/") { name = "NeoForged" }
        maven("https://maven.minecraftforge.net/") { name = "MinecraftForge" }
        maven("https://maven.kikugie.dev/releases") { name = "KikuGie Releases" }
        maven("https://maven.kikugie.dev/snapshots") { name = "KikuGie Snapshots" }
    }
}

plugins {
    // Check the latest version on https://stonecutter.kikugie.dev/blog/changes/0.9
    id("dev.kikugie.stonecutter") version "0.9.7"

    // Used for cross-compat for 26.1+ and older versions (https://codeberg.org/KikuGie/loom-back-compat)
    id("dev.kikugie.loom-back-compat") version "0.4.2"

    // Sometimes it is needed to make Gradle run at all, so it doesn't hurt to have
    // (https://github.com/gradle/foojay-toolchains)
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

stonecutter {
    create(rootProject) {
        /**
         * Creates version nodes for multiple loaders.
         *
         * This function will create subprojects named `versions/{project}-{loader}`.
         * Each project has a logical [version], which should match the Minecraft version,
         * whereas [project] is the arbitrary name part of the folder.
         *
         * Each project will also have a separate build script assigned depending on the loader,
         * named `build.{loader}.gradle.kts`.
         */
        fun match(project: String, vararg loaders: String, version: String = project) {
            for (loader in loaders) version("$project-$loader", version).buildscript("build.$loader.gradle.kts")
        }

        // See https://stonecutter.kikugie.dev/wiki/start/#choosing-minecraft-versions
        // NeoForge has no 1.20.1 builds at all (its earliest published artifact is 20.2.3-beta, MC
        // 1.20.2 - confirmed via maven.neoforged.net's maven-metadata.xml) - it forked from Forge's
        // 1.20.1 codebase but only started publishing from 1.20.2 onward, so 1.20.1 is fabric-only
        // for now. See claude/1.20.1-port.md.
        match("1.20.1", "fabric")
        match("1.21.1", "fabric", "neoforge", "forge")
//        match("1.21.11", "fabric", "neoforge")
        // 26.2-forge is intentionally NOT wired up yet: Forge rewrote its event system (EventBus 7,
        // static per-event BUS fields instead of @SubscribeEvent/IEventBus) starting ~1.21.6, so the
        // classic-API forge platform code written for 1.21.1 below does not compile against 26.2's
        // Forge jar - confirmed via javap against the real 26.2-65.1.0 forge jar. [forge."26.2.x"] in
        // stonecutter.properties.toml is kept for when that second code path gets written.
        match("26.2.x", "fabric", "neoforge", version = "26.2")
        vcsVersion = "26.2.x-fabric"
    }
}

rootProject.name = "Vanilla Gun Mod Multiloader"
