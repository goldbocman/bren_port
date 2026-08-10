# Stonecutter Fabric & NeoForge template

## Useful links
- [Stonecutter beginner's guide](https://stonecutter.kikugie.dev/wiki/start/): *spoiler: you* ***need*** *to understand how it works!*
- [Fabric Discord server](https://discord.gg/v6v4pMv): for mod development help.
- [Stonecutter Discord server](https://discord.kikugie.dev/): for Stonecutter and Gradle help.
- [How To Ask Questions - the guide](http://www.catb.org/esr/faqs/smart-questions.html): also in [video form](https://www.youtube.com/results?search_query=How+To+Ask+Questions+The+Smart+Way).


## Building 1.20.1 Forge

Since it requires FG6, here is a separate gradle folder for it.
In root:
`./gradlew :stonecutterSwitchTo1.20.1-forge`

Then
`cd forge-1.20.1`

and 

`./gradlew runClient`