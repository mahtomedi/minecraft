package net.minecraft.commands.synchronization.brigadier;

import com.google.gson.JsonObject;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.synchronization.ArgumentSerializer;
import net.minecraft.network.FriendlyByteBuf;

public class IntegerArgumentSerializer implements ArgumentSerializer<IntegerArgumentType> {
    public void serializeToNetwork(IntegerArgumentType param0, FriendlyByteBuf param1) {
        boolean var0 = param0.getMinimum() != Integer.MIN_VALUE;
        boolean var1 = param0.getMaximum() != Integer.MAX_VALUE;
        param1.writeByte(BrigadierArgumentSerializers.createNumberFlags(var0, var1));
        if (var0) {
            param1.writeInt(param0.getMinimum());
        }

        if (var1) {
            param1.writeInt(param0.getMaximum());
        }

    }

    public IntegerArgumentType deserializeFromNetwork(FriendlyByteBuf param0) {
        byte var0 = param0.readByte();
        int var1 = BrigadierArgumentSerializers.numberHasMin(var0) ? param0.readInt() : Integer.MIN_VALUE;
        int var2 = BrigadierArgumentSerializers.numberHasMax(var0) ? param0.readInt() : Integer.MAX_VALUE;
        return IntegerArgumentType.integer(var1, var2);
    }

    public void serializeToJson(IntegerArgumentType param0, JsonObject param1) {
        if (param0.getMinimum() != Integer.MIN_VALUE) {
            param1.addProperty("min", param0.getMinimum());
        }

        if (param0.getMaximum() != Integer.MAX_VALUE) {
            param1.addProperty("max", param0.getMaximum());
        }

    }
}
