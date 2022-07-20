package net.minecraft.commands.arguments;

import com.mojang.brigadier.context.ParsedArgument;
import com.mojang.datafixers.util.Pair;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.chat.PreviewableCommand;

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

    public static boolean hasSignableArguments(PreviewableCommand<?> param0) {
        return param0.arguments().stream().anyMatch(param0x -> param0x.previewType() instanceof SignedArgument);
    }

    public static ArgumentSignatures signCommand(PreviewableCommand<?> param0, ArgumentSignatures.Signer param1) {
        List<ArgumentSignatures.Entry> var0 = collectPlainSignableArguments(param0).stream().map(param1x -> {
            MessageSignature var0x = param1.sign(param1x.getFirst(), param1x.getSecond());
            return new ArgumentSignatures.Entry(param1x.getFirst(), var0x);
        }).toList();
        return new ArgumentSignatures(var0);
    }

    public static List<Pair<String, String>> collectPlainSignableArguments(PreviewableCommand<?> param0) {
        List<Pair<String, String>> var0 = new ArrayList<>();

        for(PreviewableCommand.Argument<?> var1 : param0.arguments()) {
            PreviewedArgument var3 = var1.previewType();
            if (var3 instanceof SignedArgument var2) {
                String var3x = getSignableText(var2, var1.parsedValue());
                var0.add(Pair.of(var1.name(), var3x));
            }
        }

        return var0;
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
