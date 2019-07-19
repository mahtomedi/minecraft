package net.minecraft.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Arrays;
import java.util.Collection;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;

public class NbtTagArgument implements ArgumentType<Tag> {
    private static final Collection<String> EXAMPLES = Arrays.asList("0", "0b", "0l", "0.0", "\"foo\"", "{foo=bar}", "[0]");

    private NbtTagArgument() {
    }

    public static NbtTagArgument nbtTag() {
        return new NbtTagArgument();
    }

    public static <S> Tag getNbtTag(CommandContext<S> param0, String param1) {
        return param0.getArgument(param1, Tag.class);
    }

    public Tag parse(StringReader param0) throws CommandSyntaxException {
        return new TagParser(param0).readValue();
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}
