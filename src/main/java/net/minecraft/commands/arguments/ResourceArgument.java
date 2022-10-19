package net.minecraft.commands.arguments;

import com.google.gson.JsonObject;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.Dynamic3CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.structure.Structure;

public class ResourceArgument<T> implements ArgumentType<Holder.Reference<T>> {
    private static final Collection<String> EXAMPLES = Arrays.asList("foo", "foo:bar", "012");
    private static final DynamicCommandExceptionType ERROR_NOT_SUMMONABLE_ENTITY = new DynamicCommandExceptionType(
        param0 -> Component.translatable("entity.not_summonable", param0)
    );
    public static final Dynamic2CommandExceptionType ERROR_UNKNOWN_RESOURCE = new Dynamic2CommandExceptionType(
        (param0, param1) -> Component.translatable("argument.resource.not_found", param0, param1)
    );
    public static final Dynamic3CommandExceptionType ERROR_INVALID_RESOURCE_TYPE = new Dynamic3CommandExceptionType(
        (param0, param1, param2) -> Component.translatable("argument.resource.invalid_type", param0, param1, param2)
    );
    final ResourceKey<? extends Registry<T>> registryKey;
    private final HolderLookup<T> registryLookup;

    public ResourceArgument(CommandBuildContext param0, ResourceKey<? extends Registry<T>> param1) {
        this.registryKey = param1;
        this.registryLookup = param0.holderLookup(param1);
    }

    public static <T> ResourceArgument<T> resource(CommandBuildContext param0, ResourceKey<? extends Registry<T>> param1) {
        return new ResourceArgument<>(param0, param1);
    }

    public static <T> Holder.Reference<T> getResource(CommandContext<CommandSourceStack> param0, String param1, ResourceKey<Registry<T>> param2) throws CommandSyntaxException {
        Holder.Reference<T> var0 = param0.getArgument(param1, Holder.Reference.class);
        ResourceKey<?> var1 = var0.key();
        if (var1.isFor(param2)) {
            return var0;
        } else {
            throw ERROR_INVALID_RESOURCE_TYPE.create(var1.location(), var1.registry(), param2.location());
        }
    }

    public static Holder.Reference<Attribute> getAttribute(CommandContext<CommandSourceStack> param0, String param1) throws CommandSyntaxException {
        return getResource(param0, param1, Registry.ATTRIBUTE_REGISTRY);
    }

    public static Holder.Reference<ConfiguredFeature<?, ?>> getConfiguredFeature(CommandContext<CommandSourceStack> param0, String param1) throws CommandSyntaxException {
        return getResource(param0, param1, Registry.CONFIGURED_FEATURE_REGISTRY);
    }

    public static Holder.Reference<Structure> getStructure(CommandContext<CommandSourceStack> param0, String param1) throws CommandSyntaxException {
        return getResource(param0, param1, Registry.STRUCTURE_REGISTRY);
    }

    public static Holder.Reference<EntityType<?>> getEntityType(CommandContext<CommandSourceStack> param0, String param1) throws CommandSyntaxException {
        return getResource(param0, param1, Registry.ENTITY_TYPE_REGISTRY);
    }

    public static Holder.Reference<EntityType<?>> getSummonableEntityType(CommandContext<CommandSourceStack> param0, String param1) throws CommandSyntaxException {
        Holder.Reference<EntityType<?>> var0 = getResource(param0, param1, Registry.ENTITY_TYPE_REGISTRY);
        if (!var0.value().canSummon()) {
            throw ERROR_NOT_SUMMONABLE_ENTITY.create(var0.key().location().toString());
        } else {
            return var0;
        }
    }

    public static Holder.Reference<MobEffect> getMobEffect(CommandContext<CommandSourceStack> param0, String param1) throws CommandSyntaxException {
        return getResource(param0, param1, Registry.MOB_EFFECT_REGISTRY);
    }

    public static Holder.Reference<Enchantment> getEnchantment(CommandContext<CommandSourceStack> param0, String param1) throws CommandSyntaxException {
        return getResource(param0, param1, Registry.ENCHANTMENT_REGISTRY);
    }

    public Holder.Reference<T> parse(StringReader param0) throws CommandSyntaxException {
        ResourceLocation var0 = ResourceLocation.read(param0);
        ResourceKey<T> var1 = ResourceKey.create(this.registryKey, var0);
        return this.registryLookup.get(var1).orElseThrow(() -> ERROR_UNKNOWN_RESOURCE.create(var0, this.registryKey.location()));
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> param0, SuggestionsBuilder param1) {
        return SharedSuggestionProvider.suggestResource(this.registryLookup.listElements().map(ResourceKey::location), param1);
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }

    public static class Info<T> implements ArgumentTypeInfo<ResourceArgument<T>, ResourceArgument.Info<T>.Template> {
        public void serializeToNetwork(ResourceArgument.Info<T>.Template param0, FriendlyByteBuf param1) {
            param1.writeResourceLocation(param0.registryKey.location());
        }

        public ResourceArgument.Info<T>.Template deserializeFromNetwork(FriendlyByteBuf param0) {
            ResourceLocation var0 = param0.readResourceLocation();
            return new ResourceArgument.Info.Template(ResourceKey.createRegistryKey(var0));
        }

        public void serializeToJson(ResourceArgument.Info<T>.Template param0, JsonObject param1) {
            param1.addProperty("registry", param0.registryKey.location().toString());
        }

        public ResourceArgument.Info<T>.Template unpack(ResourceArgument<T> param0) {
            return new ResourceArgument.Info.Template(param0.registryKey);
        }

        public final class Template implements ArgumentTypeInfo.Template<ResourceArgument<T>> {
            final ResourceKey<? extends Registry<T>> registryKey;

            Template(ResourceKey<? extends Registry<T>> param1) {
                this.registryKey = param1;
            }

            public ResourceArgument<T> instantiate(CommandBuildContext param0) {
                return new ResourceArgument<>(param0, this.registryKey);
            }

            @Override
            public ArgumentTypeInfo<ResourceArgument<T>, ?> type() {
                return Info.this;
            }
        }
    }
}
