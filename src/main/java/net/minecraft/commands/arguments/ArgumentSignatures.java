package net.minecraft.commands.arguments;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.chat.SignableCommand;

public record ArgumentSignatures(List<ArgumentSignatures.Entry> entries) {
    public static final ArgumentSignatures EMPTY = new ArgumentSignatures(List.of());
    private static final int MAX_ARGUMENT_COUNT = 8;
    private static final int MAX_ARGUMENT_NAME_LENGTH = 16;

    public ArgumentSignatures(FriendlyByteBuf param0) {
        this(param0.readCollection(FriendlyByteBuf.limitValue(ArrayList::new, 8), ArgumentSignatures.Entry::new));
    }

    @Nullable
    public MessageSignature get(String param0) {
        for(ArgumentSignatures.Entry var0 : this.entries) {
            if (var0.name.equals(param0)) {
                return var0.signature;
            }
        }

        return null;
    }

    public void write(FriendlyByteBuf param0) {
        param0.writeCollection(this.entries, (param0x, param1) -> param1.write(param0x));
    }

    public static ArgumentSignatures signCommand(SignableCommand<?> param0, ArgumentSignatures.Signer param1) {
        List<ArgumentSignatures.Entry> var0 = param0.arguments().stream().map(param1x -> {
            MessageSignature var0x = param1.sign(param1x.value());
            return var0x != null ? new ArgumentSignatures.Entry(param1x.name(), var0x) : null;
        }).filter(Objects::nonNull).toList();
        return new ArgumentSignatures(var0);
    }

    public static record Entry(String name, MessageSignature signature) {
        public Entry(FriendlyByteBuf param0) {
            this(param0.readUtf(16), MessageSignature.read(param0));
        }

        public void write(FriendlyByteBuf param0) {
            param0.writeUtf(this.name, 16);
            MessageSignature.write(param0, this.signature);
        }
    }

    @FunctionalInterface
    public interface Signer {
        @Nullable
        MessageSignature sign(String var1);
    }
}
