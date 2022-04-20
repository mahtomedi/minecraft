package net.minecraft.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Arrays;
import java.util.Collection;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

public class UuidArgument implements ArgumentType<UUID> {
    public static final SimpleCommandExceptionType ERROR_INVALID_UUID = new SimpleCommandExceptionType(Component.translatable("argument.uuid.invalid"));
    private static final Collection<String> EXAMPLES = Arrays.asList("dd12be42-52a9-4a91-a8a1-11c01849e498");
    private static final Pattern ALLOWED_CHARACTERS = Pattern.compile("^([-A-Fa-f0-9]+)");

    public static UUID getUuid(CommandContext<CommandSourceStack> param0, String param1) {
        return param0.getArgument(param1, UUID.class);
    }

    public static UuidArgument uuid() {
        return new UuidArgument();
    }

    public UUID parse(StringReader param0) throws CommandSyntaxException {
        String var0 = param0.getRemaining();
        Matcher var1 = ALLOWED_CHARACTERS.matcher(var0);
        if (var1.find()) {
            String var2 = var1.group(1);

            try {
                UUID var3 = UUID.fromString(var2);
                param0.setCursor(param0.getCursor() + var2.length());
                return var3;
            } catch (IllegalArgumentException var6) {
            }
        }

        throw ERROR_INVALID_UUID.create();
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}
