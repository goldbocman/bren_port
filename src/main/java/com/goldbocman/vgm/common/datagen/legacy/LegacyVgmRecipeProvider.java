package com.goldbocman.vgm.common.datagen.legacy;

//? if <1.21.11 {
/*import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.SmithingTransformRecipeBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import com.goldbocman.vgm.common.Bren;
import com.goldbocman.vgm.common.registry.ItemReg;

import java.util.concurrent.CompletableFuture;

// Legacy counterpart of VgmRecipeProvider. 1.21.1's RecipeProvider predates the constructor/
// buildRecipes() simplification the modern file relies on - its constructor takes
// (PackOutput, CompletableFuture<HolderLookup.Provider>) directly (matching Fabric/NeoForge's data
// provider factory shape with no separate RecipeProvider.Runner glue class needed, since that class
// doesn't exist yet either) and buildRecipes(RecipeOutput) takes the output as a parameter instead of
// an inherited field. ShapedRecipeBuilder.shaped(...) also has no HolderGetter<Item> first parameter
// here - the modern file's `items` argument is simply dropped, since nothing else in these recipes
// used it. Every individual recipe below is otherwise identical to VgmRecipeProvider.
public class LegacyVgmRecipeProvider extends RecipeProvider {
    public LegacyVgmRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    public void buildRecipes(RecipeOutput output) {
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ItemReg.AUTO_LOADER_CONTRAPTION)
                .pattern("IRI")
                .pattern("RLR")
                .pattern("IRI")
                .define('I', Items.IRON_INGOT)
                .define('R', Items.REDSTONE)
                .define('L', Items.LEVER)
                .unlockedBy(getHasName(Items.IRON_INGOT), has(Items.IRON_INGOT))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ItemReg.METAL_TUBE)
                .pattern("TTT")
                .pattern("   ")
                .pattern("TTT")
                .define('T', Items.IRON_INGOT)
                .unlockedBy(getHasName(Items.IRON_INGOT), has(Items.IRON_INGOT))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ItemReg.MAGAZINE)
                .pattern("IMI")
                .pattern(" I ")
                .define('M', Items.BOWL)
                .define('I', Items.IRON_NUGGET)
                .unlockedBy(getHasName(Items.IRON_NUGGET), has(Items.IRON_NUGGET))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ItemReg.SHORT_MAGAZINE)
                .pattern("IMI")
                .define('M', Items.BOWL)
                .define('I', Items.IRON_NUGGET)
                .unlockedBy(getHasName(Items.IRON_NUGGET), has(Items.IRON_NUGGET))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ItemReg.BULLET, 16)
                .pattern(" MI")
                .pattern("MGM")
                .pattern(" M ")
                .define('M', Items.GOLD_INGOT)
                .define('I', Items.COPPER_INGOT)
                .define('G', Items.GUNPOWDER)
                .unlockedBy(getHasName(Items.GUNPOWDER), has(Items.GUNPOWDER))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ItemReg.MAGNUM_BULLET, 8)
                .pattern(" MI")
                .pattern("MGM")
                .pattern("GM ")
                .define('M', Items.GOLD_INGOT)
                .define('I', Items.COPPER_INGOT)
                .define('G', Items.GUNPOWDER)
                .unlockedBy(getHasName(Items.GUNPOWDER), has(Items.GUNPOWDER))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ItemReg.SHELL, 8)
                .pattern(" PN")
                .pattern("MNP")
                .pattern("GM ")
                .define('M', Items.GOLD_INGOT)
                .define('P', Items.PAPER)
                .define('G', Items.GUNPOWDER)
                .define('N', Items.IRON_NUGGET)
                .unlockedBy(getHasName(Items.GUNPOWDER), has(Items.GUNPOWDER))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ItemReg.AUTO_GUN)
                .pattern("TP ")
                .pattern(" FM")
                .pattern(" IP")
                .define('M', ItemReg.AUTO_LOADER_CONTRAPTION)
                .define('I', Items.IRON_INGOT)
                .define('T', ItemReg.METAL_TUBE)
                .define('F', Items.IRON_BLOCK)
                .define('P', ItemTags.LOGS)
                .unlockedBy(getHasName(ItemReg.AUTO_LOADER_CONTRAPTION), has(ItemReg.AUTO_LOADER_CONTRAPTION))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ItemReg.RIFLE)
                .pattern("TI ")
                .pattern("PFM")
                .pattern(" LP")
                .define('M', ItemReg.AUTO_LOADER_CONTRAPTION)
                .define('I', Items.IRON_INGOT)
                .define('T', ItemReg.METAL_TUBE)
                .define('F', Items.IRON_BLOCK)
                .define('L', Items.LEVER)
                .define('P', ItemTags.LOGS)
                .unlockedBy(getHasName(ItemReg.AUTO_LOADER_CONTRAPTION), has(ItemReg.AUTO_LOADER_CONTRAPTION))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ItemReg.SHOTGUN)
                .pattern("TFM")
                .pattern("TFP")
                .pattern("  P")
                .define('M', ItemReg.AUTO_LOADER_CONTRAPTION)
                .define('T', ItemReg.METAL_TUBE)
                .define('F', Items.IRON_INGOT)
                .define('P', ItemTags.LOGS)
                .unlockedBy(getHasName(ItemReg.AUTO_LOADER_CONTRAPTION), has(ItemReg.AUTO_LOADER_CONTRAPTION))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ItemReg.REVOLVER)
                .pattern("T  ")
                .pattern("II ")
                .pattern("IMP")
                .define('M', ItemReg.AUTO_LOADER_CONTRAPTION)
                .define('I', Items.IRON_INGOT)
                .define('T', ItemReg.METAL_TUBE)
                .define('P', ItemTags.LOGS)
                .unlockedBy(getHasName(ItemReg.AUTO_LOADER_CONTRAPTION), has(ItemReg.AUTO_LOADER_CONTRAPTION))
                .save(output);

        netheriteUpgrade(output, ItemReg.AUTO_GUN, ItemReg.NETHERITE_AUTO_GUN, "netherite_auto_gun_smithing");
        netheriteUpgrade(output, ItemReg.RIFLE, ItemReg.NETHERITE_RIFLE, "netherite_rifle_smithing");
        netheriteUpgrade(output, ItemReg.SHOTGUN, ItemReg.NETHERITE_SHOTGUN, "netherite_shotgun_smithing");
        netheriteUpgrade(output, ItemReg.REVOLVER, ItemReg.NETHERITE_REVOLVER, "netherite_revolver_smithing");
    }

    private void netheriteUpgrade(RecipeOutput output, Item base, Item result, String path) {
        SmithingTransformRecipeBuilder.smithing(
                        Ingredient.of(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE),
                        Ingredient.of(base),
                        Ingredient.of(Items.NETHERITE_INGOT),
                        RecipeCategory.COMBAT,
                        result)
                .unlocks(getHasName(Items.NETHERITE_INGOT), has(Items.NETHERITE_INGOT))
                .save(output, Identifier.fromNamespaceAndPath(Bren.MODID, path));
    }
}
*///?}
