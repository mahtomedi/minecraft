package net.minecraft.commands.arguments;

import com.google.gson.JsonObject;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;

public class ResourceKeyArgument<T> implements ArgumentType<ResourceKey<T>> {
    private static final Collection<String> EXAMPLES = Arrays.asList("foo", "foo:bar", "012");
    private static final DynamicCommandExceptionType ERROR_UNKNOWN_ATTRIBUTE = new DynamicCommandExceptionType(
        param0 -> Component.translatable("attribute.unknown", param0)
    );
    private static final DynamicCommandExceptionType ERROR_INVALID_FEATURE = new DynamicCommandExceptionType(
        param0 -> Component.translatable("commands.place.feature.invalid", param0)
    );
    private static final DynamicCommandExceptionType ERROR_INVALID_STRUCTURE = new DynamicCommandExceptionType(
        param0 -> Component.translatable("commands.place.structure.invalid", param0)
    );
    private static final DynamicCommandExceptionType ERROR_INVALID_TEMPLATE_POOL = new DynamicCommandExceptionType(
        param0 -> Component.translatable("commands.place.jigsaw.invalid", param0)
    );
    final ResourceKey<? extends Registry<T>> registryKey;

    public ResourceKeyArgument(ResourceKey<? extends Registry<T>> param0) {
        this.registryKey = param0;
    }

    public static <T> ResourceKeyArgument<T> key(ResourceKey<? extends Registry<T>> param0) {
        return new ResourceKeyArgument<>(param0);
    }

    private static <T> ResourceKey<T> getRegistryType(
        CommandContext<CommandSourceStack> param0, String param1, ResourceKey<Registry<T>> param2, DynamicCommandExceptionType param3
    ) throws CommandSyntaxException {
        ResourceKey<?> var0 = param0.getArgument(param1, ResourceKey.class);
        Optional<ResourceKey<T>> var1 = var0.cast(param2);
        return var1.orElseThrow(() -> param3.create(var0));
    }

    private static <T> Registry<T> getRegistry(CommandContext<CommandSourceStack> param0, ResourceKey<? extends Registry<T>> param1) {
        return param0.getSource().getServer().registryAccess().registryOrThrow(param1);
    }

    private static <T> Holder<T> getRegistryKeyType(
        CommandContext<CommandSourceStack> param0, String param1, ResourceKey<Registry<T>> param2, DynamicCommandExceptionType param3
    ) throws CommandSyntaxException {
        ResourceKey<T> var0 = getRegistryType(param0, param1, param2, param3);
        return getRegistry(param0, param2).getHolder(var0).orElseThrow(() -> param3.create(var0.location()));
    }

    public static Attribute getAttribute(CommandContext<CommandSourceStack> param0, String param1) throws CommandSyntaxException {
        ResourceKey<Attribute> var0 = getRegistryType(param0, param1, Registry.ATTRIBUTE_REGISTRY, ERROR_UNKNOWN_ATTRIBUTE);
        return getRegistry(param0, Registry.ATTRIBUTE_REGISTRY).getOptional(var0).orElseThrow(() -> ERROR_UNKNOWN_ATTRIBUTE.create(var0.location()));
    }

    public static Holder<ConfiguredFeature<?, ?>> getConfiguredFeature(CommandContext<CommandSourceStack> param0, String param1) throws CommandSyntaxException {
        return getRegistryKeyType(param0, param1, Registry.CONFIGURED_FEATURE_REGISTRY, ERROR_INVALID_FEATURE);
    }

    public static Holder<Structure> getStructure(CommandContext<CommandSourceStack> param0, String param1) throws CommandSyntaxException {
        return getRegistryKeyType(param0, param1, Registry.STRUCTURE_REGISTRY, ERROR_INVALID_STRUCTURE);
    }

    public static Holder<StructureTemplatePool> getStructureTemplatePool(CommandContext<CommandSourceStack> param0, String param1) throws CommandSyntaxException {
        return getRegistryKeyType(param0, param1, Registry.TEMPLATE_POOL_REGISTRY, ERROR_INVALID_TEMPLATE_POOL);
    }

    public ResourceKey<T> parse(StringReader param0) throws CommandSyntaxException {
        ResourceLocation var0 = ResourceLocation.read(param0);
        return ResourceKey.create(this.registryKey, var0);
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> param0, SuggestionsBuilder param1) {
        Object var4 = param0.getSource();
        return var4 instanceof SharedSuggestionProvider var0
            ? var0.suggestRegistryElements(this.registryKey, SharedSuggestionProvider.ElementSuggestionType.ELEMENTS, param1, param0)
            : param1.buildFuture();
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }

    public static class Info<T> implements ArgumentTypeInfo<ResourceKeyArgument<T>, ResourceKeyArgument.Info<T>.Template> {
        public void serializeToNetwork(ResourceKeyArgument.Info.Template param0, FriendlyByteBuf param1) {
            param1.writeResourceLocation(param0.registryKey.location());
        }

        public ResourceKeyArgument.Info<T>.Template deserializeFromNetwork(FriendlyByteBuf param0) {
            ResourceLocation var0 = param0.readResourceLocation();
            return new ResourceKeyArgument.Info.Template(ResourceKey.createRegistryKey(var0));
        }

        public void serializeToJson(ResourceKeyArgument.Info.Template param0, JsonObject param1) {
            param1.addProperty("registry", param0.registryKey.location().toString());
        }

        public ResourceKeyArgument.Info<T>.Template unpack(ResourceKeyArgument<T> param0) {
            return new ResourceKeyArgument.Info.Template(param0.registryKey);
        }

        public final class Template implements ArgumentTypeInfo.Template<ResourceKeyArgument<T>> {
            final ResourceKey<? extends Registry<T>> registryKey;

            Template(ResourceKey<? extends Registry<T>> param1) {
                this.registryKey = param1;
            }

            public ResourceKeyArgument<T> instantiate(CommandBuildContext param0) {
                return new ResourceKeyArgument<>(this.registryKey);
            }

            @Override
            public ArgumentTypeInfo<ResourceKeyArgument<T>, ?> type() {
                return Info.this;
            }
        }
    }
}
