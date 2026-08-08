package com.goldbocman.vgm.client.legacy;

//? if <1.21.11 {
/*import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import com.goldbocman.vgm.common.Bren;
import com.goldbocman.vgm.common.registry.ItemReg;
import com.goldbocman.vgm.common.registry.custom.types.GunWithMagItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// 1.21.1 predates the Item Model Definition rework (see claude/gun-rendering.md's "Item model
// resolution" section) - the item's own registered model can't vary texture by display context, only
// by (globally-shared) display transform. LegacyItemInHandRendererMixin substitutes these already-
// correct <gun>_held.json / <gun>_with_magazine.json models (32x32 texture, hand-tuned transforms -
// the same files 26.2/1.21.11 already use for their `fallback`/held case) in for hand display
// contexts specifically, leaving the item's normally-resolved 16x16 GUI model untouched everywhere
// else (inventory, ground, item frame).
public class LegacyGunHeldModels {
    private record HeldModelSet(Identifier base, Identifier withMagazine) {
    }

    private static final Map<Item, HeldModelSet> MODELS = new HashMap<>();

    static {
        register(ItemReg.RIFLE, "rifle_held", "rifle_with_magazine");
        register(ItemReg.NETHERITE_RIFLE, "netherite_rifle_held", "netherite_rifle_with_magazine");
        register(ItemReg.AUTO_GUN, "auto_gun_held", "auto_gun_with_magazine");
        register(ItemReg.NETHERITE_AUTO_GUN, "netherite_auto_gun_held", "netherite_auto_gun_with_magazine");
        register(ItemReg.SHOTGUN, "shotgun_held", null);
        register(ItemReg.NETHERITE_SHOTGUN, "netherite_shotgun_held", null);
    }

    private static void register(Item item, String basePath, String magazinePath) {
        MODELS.put(item, new HeldModelSet(
                Identifier.fromNamespaceAndPath(Bren.MODID, "item/" + basePath),
                magazinePath == null ? null : Identifier.fromNamespaceAndPath(Bren.MODID, "item/" + magazinePath)));
    }

    // Every extra model id that needs to be baked/registered at startup (Fabric) or resource-reload
    // (NeoForge) time - see ClientBren's registerLegacyGunHeldModels()/onRegisterAdditionalModels().
    public static List<Identifier> allModelIds() {
        List<Identifier> ids = new ArrayList<>();
        for (HeldModelSet set : MODELS.values()) {
            ids.add(set.base());
            if (set.withMagazine() != null) ids.add(set.withMagazine());
        }
        return ids;
    }

    // Returns null for any item that isn't one of the six dual-texture guns above.
    public static BakedModel resolve(ItemStack stack) {
        HeldModelSet set = MODELS.get(stack.getItem());
        if (set == null) return null;

        boolean magazine = set.withMagazine() != null
                && stack.getItem() instanceof GunWithMagItem
                && GunWithMagItem.hasMagazine(stack);
        return getBakedModel(magazine ? set.withMagazine() : set.base());
    }

    //? if fabric {
    private static BakedModel getBakedModel(Identifier id) {
        return ((net.fabricmc.fabric.api.client.model.loading.v1.FabricBakedModelManager)
                Minecraft.getInstance().getModelManager()).getModel(id);
    }
    //?} elif neoforge {
    /^private static BakedModel getBakedModel(Identifier id) {
        // ModelIdentifier has no standalone() factory (only inventory()/vanilla()) - confirmed
        // via javap on the real 1.21.1 jars. NeoForge's ModelEvent.RegisterAdditional.register(...)
        // hard-requires the literal "standalone" variant string for side-loaded models (see
        // registerLegacyGunHeldModels's sibling registration in ClientBren - same id+variant pairing
        // is required here to look the baked model back up from the same ModelManager key).
        return Minecraft.getInstance().getModelManager()
                .getModel(new net.minecraft.client.resources.model.ModelIdentifier(id, "standalone"));
    }
    ^///?}
}
*///?}
