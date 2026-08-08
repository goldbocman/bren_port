package com.goldbocman.vgm.common.datagen;

//? if >=1.21.11 {
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
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

public class VgmRecipeProvider extends RecipeProvider {
    public VgmRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
        super(registries, output);
    }

    @Override
    public void buildRecipes() {
        HolderGetter<Item> items = registries.lookupOrThrow(Registries.ITEM);

        ShapedRecipeBuilder.shaped(items, RecipeCategory.COMBAT, ItemReg.AUTO_LOADER_CONTRAPTION)
                .pattern("IRI")
                .pattern("RLR")
                .pattern("IRI")
                .define('I', Items.IRON_INGOT)
                .define('R', Items.REDSTONE)
                .define('L', Items.LEVER)
                .unlockedBy(getHasName(Items.IRON_INGOT), has(Items.IRON_INGOT))
                .save(output);

        ShapedRecipeBuilder.shaped(items, RecipeCategory.COMBAT, ItemReg.METAL_TUBE)
                .pattern("TTT")
                .pattern("   ")
                .pattern("TTT")
                .define('T', Items.IRON_INGOT)
                .unlockedBy(getHasName(Items.IRON_INGOT), has(Items.IRON_INGOT))
                .save(output);

        ShapedRecipeBuilder.shaped(items, RecipeCategory.COMBAT, ItemReg.MAGAZINE)
                .pattern("IMI")
                .pattern(" I ")
                .define('M', Items.BOWL)
                .define('I', Items.IRON_NUGGET)
                .unlockedBy(getHasName(Items.IRON_NUGGET), has(Items.IRON_NUGGET))
                .save(output);

        ShapedRecipeBuilder.shaped(items, RecipeCategory.COMBAT, ItemReg.SHORT_MAGAZINE)
                .pattern("IMI")
                .define('M', Items.BOWL)
                .define('I', Items.IRON_NUGGET)
                .unlockedBy(getHasName(Items.IRON_NUGGET), has(Items.IRON_NUGGET))
                .save(output);

        ShapedRecipeBuilder.shaped(items, RecipeCategory.COMBAT, ItemReg.BULLET, 16)
                .pattern(" MI")
                .pattern("MGM")
                .pattern(" M ")
                .define('M', Items.GOLD_INGOT)
                .define('I', Items.COPPER_INGOT)
                .define('G', Items.GUNPOWDER)
                .unlockedBy(getHasName(Items.GUNPOWDER), has(Items.GUNPOWDER))
                .save(output);

        ShapedRecipeBuilder.shaped(items, RecipeCategory.COMBAT, ItemReg.MAGNUM_BULLET, 8)
                .pattern(" MI")
                .pattern("MGM")
                .pattern("GM ")
                .define('M', Items.GOLD_INGOT)
                .define('I', Items.COPPER_INGOT)
                .define('G', Items.GUNPOWDER)
                .unlockedBy(getHasName(Items.GUNPOWDER), has(Items.GUNPOWDER))
                .save(output);

        ShapedRecipeBuilder.shaped(items, RecipeCategory.COMBAT, ItemReg.SHELL, 8)
                .pattern(" PN")
                .pattern("MNP")
                .pattern("GM ")
                .define('M', Items.GOLD_INGOT)
                .define('P', Items.PAPER)
                .define('G', Items.GUNPOWDER)
                .define('N', Items.IRON_NUGGET)
                .unlockedBy(getHasName(Items.GUNPOWDER), has(Items.GUNPOWDER))
                .save(output);

        ShapedRecipeBuilder.shaped(items, RecipeCategory.COMBAT, ItemReg.AUTO_GUN)
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

        ShapedRecipeBuilder.shaped(items, RecipeCategory.COMBAT, ItemReg.RIFLE)
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

        ShapedRecipeBuilder.shaped(items, RecipeCategory.COMBAT, ItemReg.SHOTGUN)
                .pattern("TFM")
                .pattern("TFP")
                .pattern("  P")
                .define('M', ItemReg.AUTO_LOADER_CONTRAPTION)
                .define('T', ItemReg.METAL_TUBE)
                .define('F', Items.IRON_INGOT)
                .define('P', ItemTags.LOGS)
                .unlockedBy(getHasName(ItemReg.AUTO_LOADER_CONTRAPTION), has(ItemReg.AUTO_LOADER_CONTRAPTION))
                .save(output);

        ShapedRecipeBuilder.shaped(items, RecipeCategory.COMBAT, ItemReg.REVOLVER)
                .pattern("T  ")
                .pattern("II ")
                .pattern("IMP")
                .define('M', ItemReg.AUTO_LOADER_CONTRAPTION)
                .define('I', Items.IRON_INGOT)
                .define('T', ItemReg.METAL_TUBE)
                .define('P', ItemTags.LOGS)
                .unlockedBy(getHasName(ItemReg.AUTO_LOADER_CONTRAPTION), has(ItemReg.AUTO_LOADER_CONTRAPTION))
                .save(output);

        netheriteUpgrade(ItemReg.AUTO_GUN, ItemReg.NETHERITE_AUTO_GUN, "netherite_auto_gun_smithing");
        netheriteUpgrade(ItemReg.RIFLE, ItemReg.NETHERITE_RIFLE, "netherite_rifle_smithing");
        netheriteUpgrade(ItemReg.SHOTGUN, ItemReg.NETHERITE_SHOTGUN, "netherite_shotgun_smithing");
        netheriteUpgrade(ItemReg.REVOLVER, ItemReg.NETHERITE_REVOLVER, "netherite_revolver_smithing");
    }

    private void netheriteUpgrade(Item base, Item result, String path) {
        SmithingTransformRecipeBuilder.smithing(
                        Ingredient.of(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE),
                        Ingredient.of(base),
                        Ingredient.of(Items.NETHERITE_INGOT),
                        RecipeCategory.COMBAT,
                        result)
                .unlocks(getHasName(Items.NETHERITE_INGOT), has(Items.NETHERITE_INGOT))
                .save(output, ResourceKey.create(Registries.RECIPE, Identifier.fromNamespaceAndPath(Bren.MODID, path)));
    }

    /** Vanilla-only {@link RecipeProvider.Runner} glue, reusable from both loaders' datagen entrypoints. */
    public static class Runner extends RecipeProvider.Runner {
        public Runner(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
            super(output, registries);
        }

        @Override
        protected RecipeProvider createRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
            return new VgmRecipeProvider(registries, output);
        }

        @Override
        public String getName() {
            return "VGM Recipes";
        }
    }
}
//?}
