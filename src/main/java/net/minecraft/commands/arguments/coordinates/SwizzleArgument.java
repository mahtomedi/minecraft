package net.minecraft.commands.arguments.coordinates;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;

public class SwizzleArgument implements ArgumentType<EnumSet<Direction.Axis>> {
    private static final Collection<String> EXAMPLES = Arrays.asList("xyz", "x");
    private static final SimpleCommandExceptionType ERROR_INVALID = new SimpleCommandExceptionType(Component.translatable("arguments.swizzle.invalid"));

    public static SwizzleArgument swizzle() {
        return new SwizzleArgument();
    }

    public static EnumSet<Direction.Axis> getSwizzle(CommandContext<CommandSourceStack> param0, String param1) {
        return param0.getArgument(param1, EnumSet.class);
    }

    public EnumSet<Direction.Axis> parse(StringReader param0) throws CommandSyntaxException {
        EnumSet<Direction.Axis> var0 = EnumSet.noneOf(Direction.Axis.class);

        while(param0.canRead() && param0.peek() != ' ') {
            char var1 = param0.read();

            Direction.Axis var5 = switch(var1) {
                case 'x' -> Direction.Axis.X;
                case 'y' -> Direction.Axis.Y;
                case 'z' -> Direction.Axis.Z;
                default -> throw ERROR_INVALID.create();
            };
            if (var0.contains(var5)) {
                throw ERROR_INVALID.create();
            }

            var0.add(var5);
        }

        return var0;
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}
