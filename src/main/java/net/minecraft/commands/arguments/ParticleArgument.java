package net.minecraft.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public class ParticleArgument implements ArgumentType<ParticleOptions> {
    private static final Collection<String> EXAMPLES = Arrays.asList("foo", "foo:bar", "particle with options");
    public static final DynamicCommandExceptionType ERROR_UNKNOWN_PARTICLE = new DynamicCommandExceptionType(
        param0 -> Component.translatableEscape("particle.notFound", param0)
    );
    private final HolderLookup<ParticleType<?>> particles;

    public ParticleArgument(CommandBuildContext param0) {
        this.particles = param0.holderLookup(Registries.PARTICLE_TYPE);
    }

    public static ParticleArgument particle(CommandBuildContext param0) {
        return new ParticleArgument(param0);
    }

    public static ParticleOptions getParticle(CommandContext<CommandSourceStack> param0, String param1) {
        return param0.getArgument(param1, ParticleOptions.class);
    }

    public ParticleOptions parse(StringReader param0) throws CommandSyntaxException {
        return readParticle(param0, this.particles);
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }

    public static ParticleOptions readParticle(StringReader param0, HolderLookup<ParticleType<?>> param1) throws CommandSyntaxException {
        ParticleType<?> var0 = readParticleType(param0, param1);
        return readParticle(param0, var0);
    }

    private static ParticleType<?> readParticleType(StringReader param0, HolderLookup<ParticleType<?>> param1) throws CommandSyntaxException {
        ResourceLocation var0 = ResourceLocation.read(param0);
        ResourceKey<ParticleType<?>> var1 = ResourceKey.create(Registries.PARTICLE_TYPE, var0);
        return param1.get(var1).orElseThrow(() -> ERROR_UNKNOWN_PARTICLE.create(var0)).value();
    }

    private static <T extends ParticleOptions> T readParticle(StringReader param0, ParticleType<T> param1) throws CommandSyntaxException {
        return param1.getDeserializer().fromCommand(param1, param0);
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> param0, SuggestionsBuilder param1) {
        return SharedSuggestionProvider.suggestResource(this.particles.listElementIds().map(ResourceKey::location), param1);
    }
}
