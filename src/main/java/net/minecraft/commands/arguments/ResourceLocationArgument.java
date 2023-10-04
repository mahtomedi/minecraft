package net.minecraft.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import java.util.Arrays;
import java.util.Collection;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.storage.loot.LootDataManager;
import net.minecraft.world.level.storage.loot.LootDataType;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class ResourceLocationArgument implements ArgumentType<ResourceLocation> {
    private static final Collection<String> EXAMPLES = Arrays.asList("foo", "foo:bar", "012");
    private static final DynamicCommandExceptionType ERROR_UNKNOWN_ADVANCEMENT = new DynamicCommandExceptionType(
        param0 -> Component.translatableEscape("advancement.advancementNotFound", param0)
    );
    private static final DynamicCommandExceptionType ERROR_UNKNOWN_RECIPE = new DynamicCommandExceptionType(
        param0 -> Component.translatableEscape("recipe.notFound", param0)
    );
    private static final DynamicCommandExceptionType ERROR_UNKNOWN_PREDICATE = new DynamicCommandExceptionType(
        param0 -> Component.translatableEscape("predicate.unknown", param0)
    );
    private static final DynamicCommandExceptionType ERROR_UNKNOWN_ITEM_MODIFIER = new DynamicCommandExceptionType(
        param0 -> Component.translatableEscape("item_modifier.unknown", param0)
    );

    public static ResourceLocationArgument id() {
        return new ResourceLocationArgument();
    }

    public static AdvancementHolder getAdvancement(CommandContext<CommandSourceStack> param0, String param1) throws CommandSyntaxException {
        ResourceLocation var0 = getId(param0, param1);
        AdvancementHolder var1 = param0.getSource().getServer().getAdvancements().get(var0);
        if (var1 == null) {
            throw ERROR_UNKNOWN_ADVANCEMENT.create(var0);
        } else {
            return var1;
        }
    }

    public static RecipeHolder<?> getRecipe(CommandContext<CommandSourceStack> param0, String param1) throws CommandSyntaxException {
        RecipeManager var0 = param0.getSource().getServer().getRecipeManager();
        ResourceLocation var1 = getId(param0, param1);
        return var0.byKey(var1).orElseThrow(() -> ERROR_UNKNOWN_RECIPE.create(var1));
    }

    public static LootItemCondition getPredicate(CommandContext<CommandSourceStack> param0, String param1) throws CommandSyntaxException {
        ResourceLocation var0 = getId(param0, param1);
        LootDataManager var1 = param0.getSource().getServer().getLootData();
        LootItemCondition var2 = var1.getElement(LootDataType.PREDICATE, var0);
        if (var2 == null) {
            throw ERROR_UNKNOWN_PREDICATE.create(var0);
        } else {
            return var2;
        }
    }

    public static LootItemFunction getItemModifier(CommandContext<CommandSourceStack> param0, String param1) throws CommandSyntaxException {
        ResourceLocation var0 = getId(param0, param1);
        LootDataManager var1 = param0.getSource().getServer().getLootData();
        LootItemFunction var2 = var1.getElement(LootDataType.MODIFIER, var0);
        if (var2 == null) {
            throw ERROR_UNKNOWN_ITEM_MODIFIER.create(var0);
        } else {
            return var2;
        }
    }

    public static ResourceLocation getId(CommandContext<CommandSourceStack> param0, String param1) {
        return param0.getArgument(param1, ResourceLocation.class);
    }

    public ResourceLocation parse(StringReader param0) throws CommandSyntaxException {
        return ResourceLocation.read(param0);
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}
