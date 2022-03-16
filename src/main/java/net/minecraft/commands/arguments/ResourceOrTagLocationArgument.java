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
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.structure.Structure;

public class ResourceOrTagLocationArgument<T> implements ArgumentType<ResourceOrTagLocationArgument.Result<T>> {
    private static final Collection<String> EXAMPLES = Arrays.asList("foo", "foo:bar", "012", "#skeletons", "#minecraft:skeletons");
    private static final DynamicCommandExceptionType ERROR_INVALID_BIOME = new DynamicCommandExceptionType(
        param0 -> new TranslatableComponent("commands.locatebiome.invalid", param0)
    );
    private static final DynamicCommandExceptionType ERROR_INVALID_STRUCTURE = new DynamicCommandExceptionType(
        param0 -> new TranslatableComponent("commands.locate.invalid", param0)
    );
    final ResourceKey<? extends Registry<T>> registryKey;

    public ResourceOrTagLocationArgument(ResourceKey<? extends Registry<T>> param0) {
        this.registryKey = param0;
    }

    public static <T> ResourceOrTagLocationArgument<T> resourceOrTag(ResourceKey<? extends Registry<T>> param0) {
        return new ResourceOrTagLocationArgument<>(param0);
    }

    private static <T> ResourceOrTagLocationArgument.Result<T> getRegistryType(
        CommandContext<CommandSourceStack> param0, String param1, ResourceKey<Registry<T>> param2, DynamicCommandExceptionType param3
    ) throws CommandSyntaxException {
        ResourceOrTagLocationArgument.Result<?> var0 = param0.getArgument(param1, ResourceOrTagLocationArgument.Result.class);
        Optional<ResourceOrTagLocationArgument.Result<T>> var1 = var0.cast(param2);
        return var1.orElseThrow(() -> param3.create(var0));
    }

    public static ResourceOrTagLocationArgument.Result<Biome> getBiome(CommandContext<CommandSourceStack> param0, String param1) throws CommandSyntaxException {
        return getRegistryType(param0, param1, Registry.BIOME_REGISTRY, ERROR_INVALID_BIOME);
    }

    public static ResourceOrTagLocationArgument.Result<Structure> getStructure(CommandContext<CommandSourceStack> param0, String param1) throws CommandSyntaxException {
        return getRegistryType(param0, param1, Registry.STRUCTURE_REGISTRY, ERROR_INVALID_STRUCTURE);
    }

    public ResourceOrTagLocationArgument.Result<T> parse(StringReader param0) throws CommandSyntaxException {
        if (param0.canRead() && param0.peek() == '#') {
            int var0 = param0.getCursor();

            try {
                param0.skip();
                ResourceLocation var1 = ResourceLocation.read(param0);
                return new ResourceOrTagLocationArgument.TagResult<>(TagKey.create(this.registryKey, var1));
            } catch (CommandSyntaxException var4) {
                param0.setCursor(var0);
                throw var4;
            }
        } else {
            ResourceLocation var3 = ResourceLocation.read(param0);
            return new ResourceOrTagLocationArgument.ResourceResult<>(ResourceKey.create(this.registryKey, var3));
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

    public static class Info<T> implements ArgumentTypeInfo<ResourceOrTagLocationArgument<T>, ResourceOrTagLocationArgument.Info<T>.Template> {
        public void serializeToNetwork(ResourceOrTagLocationArgument.Info<T>.Template param0, FriendlyByteBuf param1) {
            param1.writeResourceLocation(param0.registryKey.location());
        }

        public ResourceOrTagLocationArgument.Info<T>.Template deserializeFromNetwork(FriendlyByteBuf param0) {
            ResourceLocation var0 = param0.readResourceLocation();
            return new ResourceOrTagLocationArgument.Info.Template(ResourceKey.createRegistryKey(var0));
        }

        public void serializeToJson(ResourceOrTagLocationArgument.Info<T>.Template param0, JsonObject param1) {
            param1.addProperty("registry", param0.registryKey.location().toString());
        }

        public ResourceOrTagLocationArgument.Info<T>.Template unpack(ResourceOrTagLocationArgument<T> param0) {
            return new ResourceOrTagLocationArgument.Info.Template(param0.registryKey);
        }

        public final class Template implements ArgumentTypeInfo.Template<ResourceOrTagLocationArgument<T>> {
            final ResourceKey<? extends Registry<T>> registryKey;

            Template(ResourceKey<? extends Registry<T>> param1) {
                this.registryKey = param1;
            }

            public ResourceOrTagLocationArgument<T> instantiate(CommandBuildContext param0) {
                return new ResourceOrTagLocationArgument<>(this.registryKey);
            }

            @Override
            public ArgumentTypeInfo<ResourceOrTagLocationArgument<T>, ?> type() {
                return Info.this;
            }
        }
    }

    static record ResourceResult<T>(ResourceKey<T> key) implements ResourceOrTagLocationArgument.Result<T> {
        @Override
        public Either<ResourceKey<T>, TagKey<T>> unwrap() {
            return Either.left(this.key);
        }

        @Override
        public <E> Optional<ResourceOrTagLocationArgument.Result<E>> cast(ResourceKey<? extends Registry<E>> param0) {
            return this.key.cast(param0).map(ResourceOrTagLocationArgument.ResourceResult::new);
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

        <E> Optional<ResourceOrTagLocationArgument.Result<E>> cast(ResourceKey<? extends Registry<E>> var1);

        String asPrintable();
    }

    static record TagResult<T>(TagKey<T> key) implements ResourceOrTagLocationArgument.Result<T> {
        @Override
        public Either<ResourceKey<T>, TagKey<T>> unwrap() {
            return Either.right(this.key);
        }

        @Override
        public <E> Optional<ResourceOrTagLocationArgument.Result<E>> cast(ResourceKey<? extends Registry<E>> param0) {
            return this.key.cast(param0).map(ResourceOrTagLocationArgument.TagResult::new);
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