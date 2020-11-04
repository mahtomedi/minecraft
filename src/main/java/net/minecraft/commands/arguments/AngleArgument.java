package net.minecraft.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Arrays;
import java.util.Collection;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.coordinates.WorldCoordinate;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.Mth;

public class AngleArgument implements ArgumentType<AngleArgument.SingleAngle> {
    private static final Collection<String> EXAMPLES = Arrays.asList("0", "~", "~-5");
    public static final SimpleCommandExceptionType ERROR_NOT_COMPLETE = new SimpleCommandExceptionType(new TranslatableComponent("argument.angle.incomplete"));
    public static final SimpleCommandExceptionType ERROR_INVALID_ANGLE = new SimpleCommandExceptionType(new TranslatableComponent("argument.angle.invalid"));

    public static AngleArgument angle() {
        return new AngleArgument();
    }

    public static float getAngle(CommandContext<CommandSourceStack> param0, String param1) {
        return param0.getArgument(param1, AngleArgument.SingleAngle.class).getAngle(param0.getSource());
    }

    public AngleArgument.SingleAngle parse(StringReader param0) throws CommandSyntaxException {
        if (!param0.canRead()) {
            throw ERROR_NOT_COMPLETE.createWithContext(param0);
        } else {
            boolean var0 = WorldCoordinate.isRelative(param0);
            float var1 = param0.canRead() && param0.peek() != ' ' ? param0.readFloat() : 0.0F;
            if (!Float.isNaN(var1) && !Float.isInfinite(var1)) {
                return new AngleArgument.SingleAngle(var1, var0);
            } else {
                throw ERROR_INVALID_ANGLE.createWithContext(param0);
            }
        }
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }

    public static final class SingleAngle {
        private final float angle;
        private final boolean isRelative;

        private SingleAngle(float param0, boolean param1) {
            this.angle = param0;
            this.isRelative = param1;
        }

        public float getAngle(CommandSourceStack param0) {
            return Mth.wrapDegrees(this.isRelative ? this.angle + param0.getRotation().y : this.angle);
        }
    }
}
