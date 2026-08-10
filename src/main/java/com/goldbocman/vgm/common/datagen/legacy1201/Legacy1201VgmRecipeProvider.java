package com.goldbocman.vgm.common.datagen.legacy1201;

//? if <1.20.5 {
/*import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.SmithingTransformRecipeBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import com.goldbocman.vgm.common.Bren;
import com.goldbocman.vgm.common.registry.ItemReg;

import java.util.function.Consumer;

// A third recipe-provider shape, older than LegacyVgmRecipeProvider's (<1.21.11) one - confirmed via
// javap on the real 1.20.1 merged jar, not assumed from the 1.21.1 file's own doc comment. At 1.20.1,
// RecipeProvider's constructor takes only (PackOutput) - no CompletableFuture<HolderLookup.Provider>
// registries param at all (RecipeOutput/HolderLookup-aware datagen didn't exist yet) - and
// buildRecipes takes a plain Consumer<FinishedRecipe> instead. RecipeBuilder.save(Consumer) and
// SmithingTransformRecipeBuilder's ingredient/category/result argument order are otherwise identical
// to the <1.21.11 file, so the actual recipe bodies below are a straight copy of
// LegacyVgmRecipeProvider's.
public class Legacy1201VgmRecipeProvider extends RecipeProvider {
    public Legacy1201VgmRecipeProvider(PackOutput output) {
        super(output);
    }

    @Override
    public void buildRecipes(Consumer<FinishedRecipe> output) {
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

    private void netheriteUpgrade(Consumer<FinishedRecipe> output, Item base, Item result, String path) {
        SmithingTransformRecipeBuilder.smithing(
                        Ingredient.of(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE),
                        Ingredient.of(base),
                        Ingredient.of(Items.NETHERITE_INGOT),
                        RecipeCategory.COMBAT,
                        result)
                .unlocks(getHasName(Items.NETHERITE_INGOT), has(Items.NETHERITE_INGOT))
                .save(output, com.goldbocman.vgm.TemplateModCommon.id(Bren.MODID, path));
    }
}
*///?}
