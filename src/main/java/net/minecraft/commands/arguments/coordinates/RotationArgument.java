package net.minecraft.commands.arguments.coordinates;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Arrays;
import java.util.Collection;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.TranslatableComponent;

public class RotationArgument implements ArgumentType<Coordinates> {
    private static final Collection<String> EXAMPLES = Arrays.asList("0 0", "~ ~", "~-5 ~5");
    public static final SimpleCommandExceptionType ERROR_NOT_COMPLETE = new SimpleCommandExceptionType(
        new TranslatableComponent("argument.rotation.incomplete")
    );

    public static RotationArgument rotation() {
        return new RotationArgument();
    }

    public static Coordinates getRotation(CommandContext<CommandSourceStack> param0, String param1) {
        return param0.getArgument(param1, Coordinates.class);
    }

    public Coordinates parse(StringReader param0) throws CommandSyntaxException {
        int var0 = param0.getCursor();
        if (!param0.canRead()) {
            throw ERROR_NOT_COMPLETE.createWithContext(param0);
        } else {
            WorldCoordinate var1 = WorldCoordinate.parseDouble(param0, false);
            if (param0.canRead() && param0.peek() == ' ') {
                param0.skip();
                WorldCoordinate var2 = WorldCoordinate.parseDouble(param0, false);
                return new WorldCoordinates(var2, var1, new WorldCoordinate(true, 0.0));
            } else {
                param0.setCursor(var0);
                throw ERROR_NOT_COMPLETE.createWithContext(param0);
            }
        }
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}
