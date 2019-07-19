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
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;

public class MobEffectArgument implements ArgumentType<MobEffect> {
    private static final Collection<String> EXAMPLES = Arrays.asList("spooky", "effect");
    public static final DynamicCommandExceptionType ERROR_UNKNOWN_EFFECT = new DynamicCommandExceptionType(
        param0 -> new TranslatableComponent("effect.effectNotFound", param0)
    );

    public static MobEffectArgument effect() {
        return new MobEffectArgument();
    }

    public static MobEffect getEffect(CommandContext<CommandSourceStack> param0, String param1) throws CommandSyntaxException {
        return param0.getArgument(param1, MobEffect.class);
    }

    public MobEffect parse(StringReader param0) throws CommandSyntaxException {
        ResourceLocation var0 = ResourceLocation.read(param0);
        return Registry.MOB_EFFECT.getOptional(var0).orElseThrow(() -> ERROR_UNKNOWN_EFFECT.create(var0));
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> param0, SuggestionsBuilder param1) {
        return SharedSuggestionProvider.suggestResource(Registry.MOB_EFFECT.keySet(), param1);
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}
