package net.minecraft.commands.arguments;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContextBuilder;
import com.mojang.brigadier.context.ParsedArgument;
import com.mojang.brigadier.context.ParsedCommandNode;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Crypt;

public record ArgumentSignatures<T>(long salt, Map<String, byte[]> signatures) {
    private static final int MAX_ARGUMENT_NAME_LENGTH = 16;

    public ArgumentSignatures(FriendlyByteBuf param0) {
        this(param0.readLong(), param0.readMap(param0x -> param0x.readUtf(16), FriendlyByteBuf::readByteArray));
    }

    public static ArgumentSignatures empty() {
        return new ArgumentSignatures(0L, Map.of());
    }

    @Nullable
    public Crypt.SaltSignaturePair get(String param0) {
        byte[] var0 = this.signatures.get(param0);
        return var0 != null ? new Crypt.SaltSignaturePair(this.salt, var0) : null;
    }

    public void write(FriendlyByteBuf param0) {
        param0.writeLong(this.salt);
        param0.writeMap(this.signatures, (param0x, param1) -> param0x.writeUtf(param1, 16), FriendlyByteBuf::writeByteArray);
    }

    public static Map<String, Component> collectLastChildPlainSignableComponents(CommandContextBuilder<?> param0) {
        CommandContextBuilder<?> var0 = param0.getLastChild();
        Map<String, Component> var1 = new Object2ObjectArrayMap<>();

        for(ParsedCommandNode<?> var2 : var0.getNodes()) {
            CommandNode var5 = var2.getNode();
            if (var5 instanceof ArgumentCommandNode var3) {
                ArgumentType var8 = var3.getType();
                if (var8 instanceof SignedArgument var4) {
                    ParsedArgument<?, ?> var5x = var0.getArguments().get(var3.getName());
                    if (var5x != null) {
                        var1.put(var3.getName(), getPlainComponentUnchecked(var4, var5x));
                    }
                }
            }
        }

        return var1;
    }

    private static <T> Component getPlainComponentUnchecked(SignedArgument<T> param0, ParsedArgument<?, ?> param1) {
        return param0.getPlainSignableComponent((T)param1.getResult());
    }
}
