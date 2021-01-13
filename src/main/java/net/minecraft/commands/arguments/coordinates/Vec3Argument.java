package net.minecraft.commands.arguments.coordinates;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.phys.Vec3;

public class Vec3Argument implements ArgumentType<Coordinates> {
    private static final Collection<String> EXAMPLES = Arrays.asList("0 0 0", "~ ~ ~", "^ ^ ^", "^1 ^ ^-5", "0.1 -0.5 .9", "~0.5 ~1 ~-5");
    public static final SimpleCommandExceptionType ERROR_NOT_COMPLETE = new SimpleCommandExceptionType(new TranslatableComponent("argument.pos3d.incomplete"));
    public static final SimpleCommandExceptionType ERROR_MIXED_TYPE = new SimpleCommandExceptionType(new TranslatableComponent("argument.pos.mixed"));
    private final boolean centerCorrect;

    public Vec3Argument(boolean param0) {
        this.centerCorrect = param0;
    }

    public static Vec3Argument vec3() {
        return new Vec3Argument(true);
    }

    public static Vec3Argument vec3(boolean param0) {
        return new Vec3Argument(param0);
    }

    public static Vec3 getVec3(CommandContext<CommandSourceStack> param0, String param1) throws CommandSyntaxException {
        return param0.getArgument(param1, Coordinates.class).getPosition(param0.getSource());
    }

    public static Coordinates getCoordinates(CommandContext<CommandSourceStack> param0, String param1) {
        return param0.getArgument(param1, Coordinates.class);
    }

    public Coordinates parse(StringReader param0) throws CommandSyntaxException {
        return (Coordinates)(param0.canRead() && param0.peek() == '^'
            ? LocalCoordinates.parse(param0)
            : WorldCoordinates.parseDouble(param0, this.centerCorrect));
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> param0, SuggestionsBuilder param1) {
        if (!(param0.getSource() instanceof SharedSuggestionProvider)) {
            return Suggestions.empty();
        } else {
            String var0 = param1.getRemaining();
            Collection<SharedSuggestionProvider.TextCoordinates> var1;
            if (!var0.isEmpty() && var0.charAt(0) == '^') {
                var1 = Collections.singleton(SharedSuggestionProvider.TextCoordinates.DEFAULT_LOCAL);
            } else {
                var1 = ((SharedSuggestionProvider)param0.getSource()).getAbsoluteCoordinates();
            }

            return SharedSuggestionProvider.suggestCoordinates(var0, var1, param1, Commands.createValidator(this::parse));
        }
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}
