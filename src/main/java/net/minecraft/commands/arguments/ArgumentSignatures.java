package net.minecraft.commands.arguments;

import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContextBuilder;
import com.mojang.brigadier.context.ParsedArgument;
import com.mojang.brigadier.context.ParsedCommandNode;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.datafixers.util.Pair;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.MessageSignature;

public record ArgumentSignatures<T>(List<ArgumentSignatures.Entry> entries) {
    public static final ArgumentSignatures EMPTY = new ArgumentSignatures(List.of());
    private static final int MAX_ARGUMENT_COUNT = 8;
    private static final int MAX_ARGUMENT_NAME_LENGTH = 16;

    public ArgumentSignatures(FriendlyByteBuf param0) {
        this(param0.readCollection(FriendlyByteBuf.limitValue(ArrayList::new, 8), ArgumentSignatures.Entry::new));
    }

    public MessageSignature get(String param0) {
        for(ArgumentSignatures.Entry var0 : this.entries) {
            if (var0.name.equals(param0)) {
                return var0.signature;
            }
        }

        return MessageSignature.EMPTY;
    }

    public void write(FriendlyByteBuf param0) {
        param0.writeCollection(this.entries, (param0x, param1) -> param1.write(param0x));
    }

    public static boolean hasSignableArguments(ParseResults<?> param0) {
        CommandContextBuilder<?> var0 = param0.getContext().getLastChild();

        for(ParsedCommandNode<?> var1 : var0.getNodes()) {
            CommandNode var3 = var1.getNode();
            if (var3 instanceof ArgumentCommandNode var2 && var2.getType() instanceof SignedArgument) {
                ParsedArgument<?, ?> var3x = var0.getArguments().get(var2.getName());
                if (var3x != null) {
                    return true;
                }
            }
        }

        return false;
    }

    public static ArgumentSignatures signCommand(CommandContextBuilder<?> param0, ArgumentSignatures.Signer param1) {
        List<ArgumentSignatures.Entry> var0 = collectLastChildPlainSignableArguments(param0).stream().map(param1x -> {
            MessageSignature var0x = param1.sign(param1x.getFirst(), param1x.getSecond());
            return new ArgumentSignatures.Entry(param1x.getFirst(), var0x);
        }).toList();
        return new ArgumentSignatures(var0);
    }

    private static List<Pair<String, String>> collectLastChildPlainSignableArguments(CommandContextBuilder<?> param0) {
        CommandContextBuilder<?> var0 = param0.getLastChild();
        List<Pair<String, String>> var1 = new ArrayList<>();

        for(ParsedCommandNode<?> var2 : var0.getNodes()) {
            CommandNode var5 = var2.getNode();
            if (var5 instanceof ArgumentCommandNode var3) {
                ArgumentType var9 = var3.getType();
                if (var9 instanceof SignedArgument var4) {
                    ParsedArgument<?, ?> var5x = var0.getArguments().get(var3.getName());
                    if (var5x != null) {
                        String var6 = getSignableText(var4, var5x);
                        var1.add(Pair.of(var3.getName(), var6));
                    }
                }
            }
        }

        return var1;
    }

    private static <T> String getSignableText(SignedArgument<T> param0, ParsedArgument<?, ?> param1) {
        return param0.getSignableText((T)param1.getResult());
    }

    public static record Entry(String name, MessageSignature signature) {
        public Entry(FriendlyByteBuf param0) {
            this(param0.readUtf(16), new MessageSignature(param0));
        }

        public void write(FriendlyByteBuf param0) {
            param0.writeUtf(this.name, 16);
            this.signature.write(param0);
        }
    }

    @FunctionalInterface
    public interface Signer {
        MessageSignature sign(String var1, String var2);
    }
}
