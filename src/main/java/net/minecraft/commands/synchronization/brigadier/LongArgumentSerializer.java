package net.minecraft.commands.synchronization.brigadier;

import com.google.gson.JsonObject;
import com.mojang.brigadier.arguments.LongArgumentType;
import net.minecraft.commands.synchronization.ArgumentSerializer;
import net.minecraft.network.FriendlyByteBuf;

public class LongArgumentSerializer implements ArgumentSerializer<LongArgumentType> {
    public void serializeToNetwork(LongArgumentType param0, FriendlyByteBuf param1) {
        boolean var0 = param0.getMinimum() != Long.MIN_VALUE;
        boolean var1 = param0.getMaximum() != Long.MAX_VALUE;
        param1.writeByte(BrigadierArgumentSerializers.createNumberFlags(var0, var1));
        if (var0) {
            param1.writeLong(param0.getMinimum());
        }

        if (var1) {
            param1.writeLong(param0.getMaximum());
        }

    }

    public LongArgumentType deserializeFromNetwork(FriendlyByteBuf param0) {
        byte var0 = param0.readByte();
        long var1 = BrigadierArgumentSerializers.numberHasMin(var0) ? param0.readLong() : Long.MIN_VALUE;
        long var2 = BrigadierArgumentSerializers.numberHasMax(var0) ? param0.readLong() : Long.MAX_VALUE;
        return LongArgumentType.longArg(var1, var2);
    }

    public void serializeToJson(LongArgumentType param0, JsonObject param1) {
        if (param0.getMinimum() != Long.MIN_VALUE) {
            param1.addProperty("min", param0.getMinimum());
        }

        if (param0.getMaximum() != Long.MAX_VALUE) {
            param1.addProperty("max", param0.getMaximum());
        }

    }
}
