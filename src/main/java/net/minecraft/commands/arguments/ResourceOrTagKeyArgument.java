package net.minecraft.commands.arguments;

import com.google.gson.JsonObject;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.datafixers.util.Either;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;

public class ResourceOrTagKeyArgument<T> implements ArgumentType<ResourceOrTagKeyArgument.Result<T>> {
    private static final Collection<String> EXAMPLES = Arrays.asList("foo", "foo:bar", "012", "#skeletons", "#minecraft:skeletons");
    final ResourceKey<? extends Registry<T>> registryKey;

    public ResourceOrTagKeyArgument(ResourceKey<? extends Registry<T>> param0) {
        this.registryKey = param0;
    }

    public static <T> ResourceOrTagKeyArgument<T> resourceOrTagKey(ResourceKey<? extends Registry<T>> param0) {
        return new ResourceOrTagKeyArgument<>(param0);
    }

    public static <T> ResourceOrTagKeyArgument.Result<T> getResourceOrTagKey(
        CommandContext<CommandSourceStack> param0, String param1, ResourceKey<Registry<T>> param2, DynamicCommandExceptionType param3
    ) throws CommandSyntaxException {
        ResourceOrTagKeyArgument.Result<?> var0 = param0.getArgument(param1, ResourceOrTagKeyArgument.Result.class);
        Optional<ResourceOrTagKeyArgument.Result<T>> var1 = var0.cast(param2);
        return var1.orElseThrow(() -> param3.create(var0));
    }

    public ResourceOrTagKeyArgument.Result<T> parse(StringReader param0) throws CommandSyntaxException {
        if (param0.canRead() && param0.peek() == '#') {
            int var0 = param0.getCursor();

            try {
                param0.skip();
                ResourceLocation var1 = ResourceLocation.read(param0);
                return new ResourceOrTagKeyArgument.TagResult<>(TagKey.create(this.registryKey, var1));
            } catch (CommandSyntaxException var4) {
                param0.setCursor(var0);
                throw var4;
            }
        } else {
            ResourceLocation var3 = ResourceLocation.read(param0);
            return new ResourceOrTagKeyArgument.ResourceResult<>(ResourceKey.create(this.registryKey, var3));
        }
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> param0, SuggestionsBuilder param1) {
        Object var4 = param0.getSource();
        return var4 instanceof SharedSuggestionProvider var0
            ? var0.suggestRegistryElements(this.registryKey, SharedSuggestionProvider.ElementSuggestionType.ALL, param1, param0)
            : param1.buildFuture();
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }

    public static class Info<T> implements ArgumentTypeInfo<ResourceOrTagKeyArgument<T>, ResourceOrTagKeyArgument.Info<T>.Template> {
        public void serializeToNetwork(ResourceOrTagKeyArgument.Info<T>.Template param0, FriendlyByteBuf param1) {
            param1.writeResourceKey(param0.registryKey);
        }

        public ResourceOrTagKeyArgument.Info<T>.Template deserializeFromNetwork(FriendlyByteBuf param0) {
            return new ResourceOrTagKeyArgument.Info.Template(param0.readRegistryKey());
        }

        public void serializeToJson(ResourceOrTagKeyArgument.Info<T>.Template param0, JsonObject param1) {
            param1.addProperty("registry", param0.registryKey.location().toString());
        }

        public ResourceOrTagKeyArgument.Info<T>.Template unpack(ResourceOrTagKeyArgument<T> param0) {
            return new ResourceOrTagKeyArgument.Info.Template(param0.registryKey);
        }

        public final class Template implements ArgumentTypeInfo.Template<ResourceOrTagKeyArgument<T>> {
            final ResourceKey<? extends Registry<T>> registryKey;

            Template(ResourceKey<? extends Registry<T>> param1) {
                this.registryKey = param1;
            }

            public ResourceOrTagKeyArgument<T> instantiate(CommandBuildContext param0) {
                return new ResourceOrTagKeyArgument<>(this.registryKey);
            }

            @Override
            public ArgumentTypeInfo<ResourceOrTagKeyArgument<T>, ?> type() {
                return Info.this;
            }
        }
    }

    static record ResourceResult<T>(ResourceKey<T> key) implements ResourceOrTagKeyArgument.Result<T> {
        @Override
        public Either<ResourceKey<T>, TagKey<T>> unwrap() {
            return Either.left(this.key);
        }

        @Override
        public <E> Optional<ResourceOrTagKeyArgument.Result<E>> cast(ResourceKey<? extends Registry<E>> param0) {
            return this.key.cast(param0).map(ResourceOrTagKeyArgument.ResourceResult::new);
        }

        public boolean test(Holder<T> param0) {
            return param0.is(this.key);
        }

        @Override
        public String asPrintable() {
            return this.key.location().toString();
        }
    }

    public interface Result<T> extends Predicate<Holder<T>> {
        Either<ResourceKey<T>, TagKey<T>> unwrap();

        <E> Optional<ResourceOrTagKeyArgument.Result<E>> cast(ResourceKey<? extends Registry<E>> var1);

        String asPrintable();
    }

    static record TagResult<T>(TagKey<T> key) implements ResourceOrTagKeyArgument.Result<T> {
        @Override
        public Either<ResourceKey<T>, TagKey<T>> unwrap() {
            return Either.right(this.key);
        }

        @Override
        public <E> Optional<ResourceOrTagKeyArgument.Result<E>> cast(ResourceKey<? extends Registry<E>> param0) {
            return this.key.cast(param0).map(ResourceOrTagKeyArgument.TagResult::new);
        }

        public boolean test(Holder<T> param0) {
            return param0.is(this.key);
        }

        @Override
        public String asPrintable() {
            return "#" + this.key.location();
        }
    }
}
