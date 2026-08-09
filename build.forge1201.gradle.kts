// 1.20.1-forge is registered here ONLY so stonecutter can toggle the shared `src/` tree for
// this variant (`:stonecutterSwitchTo1.20.1-forge`) and so `[forge."1.20.1"]` property tags
// resolve. It is deliberately NOT built through this multiproject / Gradle 9.6.1 wrapper:
// ForgeGradle 6 (required for classic Forge on 1.20.1 - FG7 only supports >=1.20.2) hard-fails
// under Gradle 9+ (`EnvironmentChecks$EnvironmentCheckFailedException: Found Gradle version
// Gradle 9.6.1. Versions 9.0 and newer are not supported yet.`, confirmed via javap against
// ForgeGradle-6.0.54.jar - it requires Gradle [8.1, 9.0)). This repo has one shared Gradle
// wrapper for every other subproject, so 1.20.1-forge's real build lives in the standalone
// `forge-1.20.1/` Gradle project instead, which has its own wrapper pinned to Gradle 8.14.5.
// See claude/forge-port.md.
//
// Workflow: `./gradlew :stonecutterSwitchTo1.20.1-forge` (here, toggles the shared tree), then
// `cd forge-1.20.1 && ./gradlew build` (separate wrapper/process, applies FG6).
//
// No plugins are applied here on purpose - keeps this subproject's configuration free (no FG6
// resolution attempt at all) even if something ever does configure it. The guard below turns an
// accidental `:1.20.1-forge:build` under this wrapper into a clear error instead of relying on
// the default "task not found" message.
//
// dev.kikugie.loom-back-compat (applied at the settings level, see settings.gradle.kts) installs
// a `Gradle.beforeProject` hook (dev/kikugie/loomx/LoomCompatPlugin.kt's deprecated
// LoomCompatSettingsExtension) that runs for every project in the build, before that project's
// own buildscript evaluates. If `project.extra.has("loomx.loom_version")` is true, it
// unconditionally injects a `net.fabricmc.fabric-loom`/`net.fabricmc.fabric-loom-remap`
// dependency into that project's old-style `buildscript{}` classpath configuration - confirmed
// via its decompiled sources (loom-back-compat-0.4.2-sources.jar) and empirically reproducing
// this exact failure via `./gradlew tasks --all --stacktrace`. Exactly why this project ends up
// with that extra property set (from stonecutter.properties.toml's loader-unscoped top-level
// `loomx.loom_version = "1.17-SNAPSHOT"` default) while e.g. `:1.21.1-forge`/`:1.21.1-neoforge`
// don't hit the same injection isn't fully understood - both were confirmed NOT to trigger it via
// `./gradlew :1.21.1-forge:dependencies --stacktrace`, so it isn't simply "every non-fabric loader
// is affected". What IS confirmed: the hook fires before this file's own content ever executes,
// so it can't be intercepted or skipped from here - only worked around by giving the injected
// classpath dependency somewhere to actually resolve from. It's never applied as a plugin either
// way (that only happens if a project applies `dev.kikugie.loom-back-compat` itself, which this
// one doesn't) - this buildscript{} block exists purely so that resolution attempt succeeds
// instead of failing with "no repositories are defined".
buildscript {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.fabricmc.net/")
    }
}

// Something upstream (stonecutter's own per-subproject base setup, going by this task already
// existing here despite no plugin of ours registering it) already provides a "build" lifecycle
// task for every version node - reconfigure it rather than tasks.register("build"), which throws
// DuplicateTaskException.
tasks.named("build") {
    doFirst {
        throw GradleException(
            "1.20.1-forge is not built through this wrapper (Gradle 9.6.1 - ForgeGradle 6 needs " +
                "Gradle 8.1-8.x). Run `./gradlew :stonecutterSwitchTo1.20.1-forge` here first, then " +
                "build from the standalone project: `cd forge-1.20.1 && ./gradlew build`."
        )
    }
}
