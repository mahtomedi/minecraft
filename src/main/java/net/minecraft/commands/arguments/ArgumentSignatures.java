package net.minecraft.commands.arguments;

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
import net.minecraft.network.chat.Component;
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

    public static ArgumentSignatures signCommand(CommandContextBuilder<?> param0, ArgumentSignatures.Signer param1) {
        List<ArgumentSignatures.Entry> var0 = collectLastChildPlainSignableComponents(param0).stream().map(param1x -> {
            MessageSignature var0x = param1.sign(param1x.getFirst(), param1x.getSecond());
            return new ArgumentSignatures.Entry(param1x.getFirst(), var0x);
        }).toList();
        return new ArgumentSignatures(var0);
    }

    private static List<Pair<String, Component>> collectLastChildPlainSignableComponents(CommandContextBuilder<?> param0) {
        CommandContextBuilder<?> var0 = param0.getLastChild();
        List<Pair<String, Component>> var1 = new ArrayList<>();

        for(ParsedCommandNode<?> var2 : var0.getNodes()) {
            CommandNode var5 = var2.getNode();
            if (var5 instanceof ArgumentCommandNode var3) {
                ArgumentType var9 = var3.getType();
                if (var9 instanceof SignedArgument var4) {
                    ParsedArgument<?, ?> var5x = var0.getArguments().get(var3.getName());
                    if (var5x != null) {
                        Component var6 = getPlainComponentUnchecked(var4, var5x);
                        var1.add(Pair.of(var3.getName(), var6));
                    }
                }
            }
        }

        return var1;
    }

    private static <T> Component getPlainComponentUnchecked(SignedArgument<T> param0, ParsedArgument<?, ?> param1) {
        return param0.getPlainSignableComponent((T)param1.getResult());
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
        MessageSignature sign(String var1, Component var2);
    }
}
