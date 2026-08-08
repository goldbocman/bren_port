package com.goldbocman.vgm.common;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

//? if fabric {
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModOrigin;
//?}

// This mod was renamed from "bren" (all versions through 0.7.0) to "vgm" (0.1.0+). Since the mod
// id itself changed, updating by just dropping the new jar in doesn't replace the old one - both
// end up loaded together, each registering its own copy of every gun/item. Crash loudly (and try
// to clean up the old jar) instead of letting that duplication happen silently.
public final class LegacyModGuard {

	private static final String LEGACY_MODID = "bren";

	private LegacyModGuard() {
	}

	public static void checkForLegacyMod() {
		//? if fabric {
		if (!FabricLoader.getInstance().isModLoaded(LEGACY_MODID)) {
			return;
		}

		ModContainer legacy = FabricLoader.getInstance().getModContainer(LEGACY_MODID).orElseThrow();
		ModOrigin origin = legacy.getOrigin();
		List<Path> paths = origin.getKind() == ModOrigin.Kind.PATH ? origin.getPaths() : List.of();
		throw new RuntimeException(buildMessage(paths));
		//?}
		//? if neoforge {
		/*if (!net.neoforged.fml.ModList.get().isLoaded(LEGACY_MODID)) {
			return;
		}

		Path path = net.neoforged.fml.ModList.get().getModContainerById(LEGACY_MODID)
				.map(c -> c.getModInfo().getOwningFile().getFile().getFilePath())
				.orElse(null);
		List<Path> paths = path == null ? List.of() : List.of(path);
		throw new RuntimeException(buildMessage(paths));
		*///?}
	}

	private static String buildMessage(List<Path> paths) {
		StringBuilder deleted = new StringBuilder();
		StringBuilder remaining = new StringBuilder();
		for (Path path : paths) {
			try {
				Files.delete(path);
				deleted.append("\n  - ").append(path);
			} catch (IOException e) {
				remaining.append("\n  - ").append(path);
			}
		}

		StringBuilder message = new StringBuilder();
		message.append("This mod was renamed from \"bren\" to \"vgm\" starting with version 0.1.0. ")
				.append("Both the old \"bren\" mod and this \"vgm\" mod are currently installed, which causes duplicated guns/items in-game.\n");
		if (deleted.length() > 0) {
			message.append("The old mod file was deleted automatically:").append(deleted)
					.append("\nPlease restart the game.\n");
		}
		if (remaining.length() > 0) {
			message.append("Could not automatically delete the following old mod file(s), likely because they're locked while the game is running. ")
					.append("Please close the game and delete them manually, then restart:").append(remaining).append('\n');
		}
		if (deleted.length() == 0 && remaining.length() == 0) {
			message.append("Please locate and delete the old \"bren\" mod jar from your mods folder, then restart.\n");
		}
		return message.toString();
	}
}
