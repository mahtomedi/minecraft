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
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.synchronization.ArgumentSerializer;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

public class ResourceKeyArgument<T> implements ArgumentType<ResourceKey<T>> {
    private static final Collection<String> EXAMPLES = Arrays.asList("foo", "foo:bar", "012");
    private static final DynamicCommandExceptionType ERROR_UNKNOWN_ATTRIBUTE = new DynamicCommandExceptionType(
        param0 -> new TranslatableComponent("attribute.unknown", param0)
    );
    private static final DynamicCommandExceptionType ERROR_INVALID_FEATURE = new DynamicCommandExceptionType(
        param0 -> new TranslatableComponent("commands.placefeature.invalid", param0)
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

    public static Attribute getAttribute(CommandContext<CommandSourceStack> param0, String param1) throws CommandSyntaxException {
        ResourceKey<Attribute> var0 = getRegistryType(param0, param1, Registry.ATTRIBUTE_REGISTRY, ERROR_UNKNOWN_ATTRIBUTE);
        return getRegistry(param0, Registry.ATTRIBUTE_REGISTRY).getOptional(var0).orElseThrow(() -> ERROR_UNKNOWN_ATTRIBUTE.create(var0.location()));
    }

    public static Holder<ConfiguredFeature<?, ?>> getConfiguredFeature(CommandContext<CommandSourceStack> param0, String param1) throws CommandSyntaxException {
        ResourceKey<ConfiguredFeature<?, ?>> var0 = getRegistryType(param0, param1, Registry.CONFIGURED_FEATURE_REGISTRY, ERROR_INVALID_FEATURE);
        return getRegistry(param0, Registry.CONFIGURED_FEATURE_REGISTRY).getHolder(var0).orElseThrow(() -> ERROR_INVALID_FEATURE.create(var0.location()));
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

    public static class Serializer implements ArgumentSerializer<ResourceKeyArgument<?>> {
        public void serializeToNetwork(ResourceKeyArgument<?> param0, FriendlyByteBuf param1) {
            param1.writeResourceLocation(param0.registryKey.location());
        }

        public ResourceKeyArgument<?> deserializeFromNetwork(FriendlyByteBuf param0) {
            ResourceLocation var0 = param0.readResourceLocation();
            return new ResourceKeyArgument(ResourceKey.createRegistryKey(var0));
        }

        public void serializeToJson(ResourceKeyArgument<?> param0, JsonObject param1) {
            param1.addProperty("registry", param0.registryKey.location().toString());
        }
    }
}
