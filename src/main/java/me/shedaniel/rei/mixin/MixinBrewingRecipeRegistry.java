/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.mixin;

import com.google.common.collect.Lists;
import me.shedaniel.rei.plugin.DefaultPlugin;
import me.shedaniel.rei.plugin.brewing.BrewingRecipe;
import me.shedaniel.rei.plugin.brewing.DefaultBrewingDisplay;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPotion;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.potion.PotionBrewing;
import net.minecraft.potion.PotionType;
import net.minecraft.potion.PotionUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;
import java.util.List;

@Mixin(PotionBrewing.class)
public class MixinBrewingRecipeRegistry {
    
    @Unique private static final List<BrewingRecipe> SELF_ITEM_RECIPES = Lists.newArrayList();
    @Unique private static final List<PotionType> REGISTERED_POTION_TYPES = Lists.newArrayList();
    @Unique private static final List<Ingredient> SELF_POTION_TYPES = Lists.newArrayList();
    
    @Inject(method = "func_196208_a", at = @At("RETURN"))
    private static void method_8080(Item item_1, CallbackInfo ci) {
        if (item_1 instanceof ItemPotion)
            SELF_POTION_TYPES.add(Ingredient.fromItems(item_1));
    }
    
    @Inject(method = "func_196207_a", at = @At("RETURN"))
    private static void method_8071(Item item_1, Item item_2, Item item_3, CallbackInfo ci) {
        if (item_1 instanceof ItemPotion && item_3 instanceof ItemPotion)
            SELF_ITEM_RECIPES.add(new BrewingRecipe(item_1, Ingredient.fromItems(item_2), item_3));
    }
    
    @Inject(method = "addMix", at = @At("RETURN"))
    private static void registerPotionRecipe(PotionType potion_1, Item item_1, PotionType potion_2, CallbackInfo ci) {
        if (!REGISTERED_POTION_TYPES.contains(potion_1))
            rei_registerPotionType(potion_1);
        if (!REGISTERED_POTION_TYPES.contains(potion_2))
            rei_registerPotionType(potion_2);
        SELF_POTION_TYPES.stream().map(Ingredient::getMatchingStacks).forEach(itemStacks -> Arrays.stream(itemStacks).forEach(stack -> {
            DefaultPlugin.registerBrewingDisplay(new DefaultBrewingDisplay(PotionUtils.addPotionToItemStack(stack.copy(), potion_1), Ingredient.fromItems(item_1), PotionUtils.addPotionToItemStack(stack.copy(), potion_2)));
        }));
    }
    
    private static void rei_registerPotionType(PotionType potion) {
        REGISTERED_POTION_TYPES.add(potion);
        SELF_ITEM_RECIPES.forEach(recipe -> {
            DefaultPlugin.registerBrewingDisplay(new DefaultBrewingDisplay(PotionUtils.addPotionToItemStack(recipe.input.getDefaultInstance(), potion), recipe.ingredient, PotionUtils.addPotionToItemStack(recipe.output.getDefaultInstance(), potion)));
        });
    }
    
}
