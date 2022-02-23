package net.minecraft.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import java.util.Arrays;
import java.util.Collection;
import net.minecraft.advancements.Advancement;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.storage.loot.ItemModifierManager;
import net.minecraft.world.level.storage.loot.PredicateManager;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class ResourceLocationArgument implements ArgumentType<ResourceLocation> {
    private static final Collection<String> EXAMPLES = Arrays.asList("foo", "foo:bar", "012");
    private static final DynamicCommandExceptionType ERROR_UNKNOWN_ADVANCEMENT = new DynamicCommandExceptionType(
        param0 -> new TranslatableComponent("advancement.advancementNotFound", param0)
    );
    private static final DynamicCommandExceptionType ERROR_UNKNOWN_RECIPE = new DynamicCommandExceptionType(
        param0 -> new TranslatableComponent("recipe.notFound", param0)
    );
    private static final DynamicCommandExceptionType ERROR_UNKNOWN_PREDICATE = new DynamicCommandExceptionType(
        param0 -> new TranslatableComponent("predicate.unknown", param0)
    );
    private static final DynamicCommandExceptionType ERROR_UNKNOWN_ITEM_MODIFIER = new DynamicCommandExceptionType(
        param0 -> new TranslatableComponent("item_modifier.unknown", param0)
    );

    public static ResourceLocationArgument id() {
        return new ResourceLocationArgument();
    }

    public static Advancement getAdvancement(CommandContext<CommandSourceStack> param0, String param1) throws CommandSyntaxException {
        ResourceLocation var0 = getId(param0, param1);
        Advancement var1 = param0.getSource().getServer().getAdvancements().getAdvancement(var0);
        if (var1 == null) {
            throw ERROR_UNKNOWN_ADVANCEMENT.create(var0);
        } else {
            return var1;
        }
    }

    public static Recipe<?> getRecipe(CommandContext<CommandSourceStack> param0, String param1) throws CommandSyntaxException {
        RecipeManager var0 = param0.getSource().getServer().getRecipeManager();
        ResourceLocation var1 = getId(param0, param1);
        return var0.byKey(var1).orElseThrow(() -> ERROR_UNKNOWN_RECIPE.create(var1));
    }

    public static LootItemCondition getPredicate(CommandContext<CommandSourceStack> param0, String param1) throws CommandSyntaxException {
        ResourceLocation var0 = getId(param0, param1);
        PredicateManager var1 = param0.getSource().getServer().getPredicateManager();
        LootItemCondition var2 = var1.get(var0);
        if (var2 == null) {
            throw ERROR_UNKNOWN_PREDICATE.create(var0);
        } else {
            return var2;
        }
    }

    public static LootItemFunction getItemModifier(CommandContext<CommandSourceStack> param0, String param1) throws CommandSyntaxException {
        ResourceLocation var0 = getId(param0, param1);
        ItemModifierManager var1 = param0.getSource().getServer().getItemModifierManager();
        LootItemFunction var2 = var1.get(var0);
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
