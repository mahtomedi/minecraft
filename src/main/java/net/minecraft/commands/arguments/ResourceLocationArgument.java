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
import net.minecraft.core.Registry;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
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
    private static final DynamicCommandExceptionType ERROR_UNKNOWN_ATTRIBUTE = new DynamicCommandExceptionType(
        param0 -> new TranslatableComponent("attribute.unknown", param0)
    );
    private static final DynamicCommandExceptionType ERROR_UNKNOWN_ITEM_MODIFIER = new DynamicCommandExceptionType(
        param0 -> new TranslatableComponent("item_modifier.unknown", param0)
    );
    private static final DynamicCommandExceptionType ERROR_INVALID_BIOME = new DynamicCommandExceptionType(
        param0 -> new TranslatableComponent("commands.locatebiome.invalid", param0)
    );
    private static final DynamicCommandExceptionType ERROR_INVALID_FEATURE = new DynamicCommandExceptionType(
        param0 -> new TranslatableComponent("commands.placefeature.invalid", param0)
    );
    private static final DynamicCommandExceptionType ERROR_INVALID_STRUCTURE = new DynamicCommandExceptionType(
        param0 -> new TranslatableComponent("commands.locate.invalid", param0)
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

    public static Attribute getAttribute(CommandContext<CommandSourceStack> param0, String param1) throws CommandSyntaxException {
        ResourceLocation var0 = getId(param0, param1);
        return Registry.ATTRIBUTE.getOptional(var0).orElseThrow(() -> ERROR_UNKNOWN_ATTRIBUTE.create(var0));
    }

    private static <T> ResourceLocationArgument.LocatedResource<T> getRegistryType(
        CommandContext<CommandSourceStack> param0, String param1, ResourceKey<Registry<T>> param2, DynamicCommandExceptionType param3
    ) throws CommandSyntaxException {
        ResourceLocation var0 = getId(param0, param1);
        T var1 = param0.getSource().getServer().registryAccess().<T>registryOrThrow(param2).getOptional(var0).orElseThrow(() -> param3.create(var0));
        return new ResourceLocationArgument.LocatedResource<>(var0, var1);
    }

    public static ResourceLocationArgument.LocatedResource<Biome> getBiome(CommandContext<CommandSourceStack> param0, String param1) throws CommandSyntaxException {
        return getRegistryType(param0, param1, Registry.BIOME_REGISTRY, ERROR_INVALID_BIOME);
    }

    public static ResourceLocationArgument.LocatedResource<ConfiguredFeature<?, ?>> getConfiguredFeature(
        CommandContext<CommandSourceStack> param0, String param1
    ) throws CommandSyntaxException {
        return getRegistryType(param0, param1, Registry.CONFIGURED_FEATURE_REGISTRY, ERROR_INVALID_FEATURE);
    }

    public static ResourceLocationArgument.LocatedResource<StructureFeature<?>> getStructureFeature(CommandContext<CommandSourceStack> param0, String param1) throws CommandSyntaxException {
        return getRegistryType(param0, param1, Registry.STRUCTURE_FEATURE_REGISTRY, ERROR_INVALID_STRUCTURE);
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

    public static record LocatedResource<T>(ResourceLocation id, T resource) {
    }
}
