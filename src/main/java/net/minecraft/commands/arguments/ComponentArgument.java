package net.minecraft.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import java.util.Arrays;
import java.util.Collection;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.ParserUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;

public class ComponentArgument implements ArgumentType<Component> {
    private static final Collection<String> EXAMPLES = Arrays.asList("\"hello world\"", "\"\"", "\"{\"text\":\"hello world\"}", "[\"\"]");
    public static final DynamicCommandExceptionType ERROR_INVALID_JSON = new DynamicCommandExceptionType(
        param0 -> Component.translatableEscape("argument.component.invalid", param0)
    );

    private ComponentArgument() {
    }

    public static Component getComponent(CommandContext<CommandSourceStack> param0, String param1) {
        return param0.getArgument(param1, Component.class);
    }

    public static ComponentArgument textComponent() {
        return new ComponentArgument();
    }

    public Component parse(StringReader param0) throws CommandSyntaxException {
        try {
            return ParserUtils.parseJson(param0, ComponentSerialization.CODEC);
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
