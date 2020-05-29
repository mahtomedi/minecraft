package net.minecraft.commands.arguments.item;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import net.minecraft.commands.CommandFunction;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.Tag;

public class FunctionArgument implements ArgumentType<FunctionArgument.Result> {
    private static final Collection<String> EXAMPLES = Arrays.asList("foo", "foo:bar", "#foo");
    private static final DynamicCommandExceptionType ERROR_UNKNOWN_TAG = new DynamicCommandExceptionType(
        param0 -> new TranslatableComponent("arguments.function.tag.unknown", param0)
    );
    private static final DynamicCommandExceptionType ERROR_UNKNOWN_FUNCTION = new DynamicCommandExceptionType(
        param0 -> new TranslatableComponent("arguments.function.unknown", param0)
    );

    public static FunctionArgument functions() {
        return new FunctionArgument();
    }

    public FunctionArgument.Result parse(StringReader param0) throws CommandSyntaxException {
        if (param0.canRead() && param0.peek() == '#') {
            param0.skip();
            final ResourceLocation var0 = ResourceLocation.read(param0);
            return new FunctionArgument.Result() {
                @Override
                public Collection<CommandFunction> create(CommandContext<CommandSourceStack> param0) throws CommandSyntaxException {
                    Tag<CommandFunction> var0 = FunctionArgument.getFunctionTag(param0, var0);
                    return var0.getValues();
                }

                @Override
                public Pair<ResourceLocation, Either<CommandFunction, Tag<CommandFunction>>> unwrap(CommandContext<CommandSourceStack> param0) throws CommandSyntaxException {
                    return Pair.of(var0, Either.right(FunctionArgument.getFunctionTag(param0, var0)));
                }
            };
        } else {
            final ResourceLocation var1 = ResourceLocation.read(param0);
            return new FunctionArgument.Result() {
                @Override
                public Collection<CommandFunction> create(CommandContext<CommandSourceStack> param0) throws CommandSyntaxException {
                    return Collections.singleton(FunctionArgument.getFunction(param0, var1));
                }

                @Override
                public Pair<ResourceLocation, Either<CommandFunction, Tag<CommandFunction>>> unwrap(CommandContext<CommandSourceStack> param0) throws CommandSyntaxException {
                    return Pair.of(var1, Either.left(FunctionArgument.getFunction(param0, var1)));
                }
            };
        }
    }

    private static CommandFunction getFunction(CommandContext<CommandSourceStack> param0, ResourceLocation param1) throws CommandSyntaxException {
        return param0.getSource().getServer().getFunctions().get(param1).orElseThrow(() -> ERROR_UNKNOWN_FUNCTION.create(param1.toString()));
    }

    private static Tag<CommandFunction> getFunctionTag(CommandContext<CommandSourceStack> param0, ResourceLocation param1) throws CommandSyntaxException {
        Tag<CommandFunction> var0 = param0.getSource().getServer().getFunctions().getTag(param1);
        if (var0 == null) {
            throw ERROR_UNKNOWN_TAG.create(param1.toString());
        } else {
            return var0;
        }
    }

    public static Collection<CommandFunction> getFunctions(CommandContext<CommandSourceStack> param0, String param1) throws CommandSyntaxException {
        return param0.getArgument(param1, FunctionArgument.Result.class).create(param0);
    }

    public static Pair<ResourceLocation, Either<CommandFunction, Tag<CommandFunction>>> getFunctionOrTag(
        CommandContext<CommandSourceStack> param0, String param1
    ) throws CommandSyntaxException {
        return param0.getArgument(param1, FunctionArgument.Result.class).unwrap(param0);
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }

    public interface Result {
        Collection<CommandFunction> create(CommandContext<CommandSourceStack> var1) throws CommandSyntaxException;

        Pair<ResourceLocation, Either<CommandFunction, Tag<CommandFunction>>> unwrap(CommandContext<CommandSourceStack> var1) throws CommandSyntaxException;
    }
}
