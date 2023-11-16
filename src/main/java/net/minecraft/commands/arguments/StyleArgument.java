package net.minecraft.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import java.util.Collection;
import java.util.List;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.ParserUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

public class StyleArgument implements ArgumentType<Style> {
    private static final Collection<String> EXAMPLES = List.of("{\"bold\": true}\n");
    public static final DynamicCommandExceptionType ERROR_INVALID_JSON = new DynamicCommandExceptionType(
        param0 -> Component.translatableEscape("argument.style.invalid", param0)
    );

    private StyleArgument() {
    }

    public static Style getStyle(CommandContext<CommandSourceStack> param0, String param1) {
        return param0.getArgument(param1, Style.class);
    }

    public static StyleArgument style() {
        return new StyleArgument();
    }

    public Style parse(StringReader param0) throws CommandSyntaxException {
        try {
            return ParserUtils.parseJson(param0, Style.Serializer.CODEC);
        } catch (Exception var4) {
            String var1 = var4.getCause() != null ? var4.getCause().getMessage() : var4.getMessage();
            throw ERROR_INVALID_JSON.createWithContext(param0, var1);
        }
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}
