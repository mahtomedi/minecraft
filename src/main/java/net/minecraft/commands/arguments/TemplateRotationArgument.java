package net.minecraft.commands.arguments;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.world.level.block.Rotation;

public class TemplateRotationArgument extends StringRepresentableArgument<Rotation> {
    private TemplateRotationArgument() {
        super(Rotation.CODEC, Rotation::values);
    }

    public static TemplateRotationArgument templateRotation() {
        return new TemplateRotationArgument();
    }

    public static Rotation getRotation(CommandContext<CommandSourceStack> param0, String param1) {
        return param0.getArgument(param1, Rotation.class);
    }
}
