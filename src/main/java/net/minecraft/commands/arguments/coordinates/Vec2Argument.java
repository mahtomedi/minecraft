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
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public class Vec2Argument implements ArgumentType<Coordinates> {
    private static final Collection<String> EXAMPLES = Arrays.asList("0 0", "~ ~", "0.1 -0.5", "~1 ~-2");
    public static final SimpleCommandExceptionType ERROR_NOT_COMPLETE = new SimpleCommandExceptionType(new TranslatableComponent("argument.pos2d.incomplete"));
    private final boolean centerCorrect;

    public Vec2Argument(boolean param0) {
        this.centerCorrect = param0;
    }

    public static Vec2Argument vec2() {
        return new Vec2Argument(true);
    }

    public static Vec2Argument vec2(boolean param0) {
        return new Vec2Argument(param0);
    }

    public static Vec2 getVec2(CommandContext<CommandSourceStack> param0, String param1) {
        Vec3 var0 = param0.getArgument(param1, Coordinates.class).getPosition(param0.getSource());
        return new Vec2((float)var0.x, (float)var0.z);
    }

    public Coordinates parse(StringReader param0) throws CommandSyntaxException {
        int var0 = param0.getCursor();
        if (!param0.canRead()) {
            throw ERROR_NOT_COMPLETE.createWithContext(param0);
        } else {
            WorldCoordinate var1 = WorldCoordinate.parseDouble(param0, this.centerCorrect);
            if (param0.canRead() && param0.peek() == ' ') {
                param0.skip();
                WorldCoordinate var2 = WorldCoordinate.parseDouble(param0, this.centerCorrect);
                return new WorldCoordinates(var1, new WorldCoordinate(true, 0.0), var2);
            } else {
                param0.setCursor(var0);
                throw ERROR_NOT_COMPLETE.createWithContext(param0);
            }
        }
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

            return SharedSuggestionProvider.suggest2DCoordinates(var0, var1, param1, Commands.createValidator(this::parse));
        }
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}
