package net.minecraft.commands.synchronization.brigadier;

import com.google.gson.JsonObject;
import com.mojang.brigadier.arguments.FloatArgumentType;
import net.minecraft.commands.synchronization.ArgumentSerializer;
import net.minecraft.network.FriendlyByteBuf;

public class FloatArgumentSerializer implements ArgumentSerializer<FloatArgumentType> {
    public void serializeToNetwork(FloatArgumentType param0, FriendlyByteBuf param1) {
        boolean var0 = param0.getMinimum() != -Float.MAX_VALUE;
        boolean var1 = param0.getMaximum() != Float.MAX_VALUE;
        param1.writeByte(BrigadierArgumentSerializers.createNumberFlags(var0, var1));
        if (var0) {
            param1.writeFloat(param0.getMinimum());
        }

        if (var1) {
            param1.writeFloat(param0.getMaximum());
        }

    }

    public FloatArgumentType deserializeFromNetwork(FriendlyByteBuf param0) {
        byte var0 = param0.readByte();
        float var1 = BrigadierArgumentSerializers.numberHasMin(var0) ? param0.readFloat() : -Float.MAX_VALUE;
        float var2 = BrigadierArgumentSerializers.numberHasMax(var0) ? param0.readFloat() : Float.MAX_VALUE;
        return FloatArgumentType.floatArg(var1, var2);
    }

    public void serializeToJson(FloatArgumentType param0, JsonObject param1) {
        if (param0.getMinimum() != -Float.MAX_VALUE) {
            param1.addProperty("min", param0.getMinimum());
        }

        if (param0.getMaximum() != Float.MAX_VALUE) {
            param1.addProperty("max", param0.getMaximum());
        }

    }
}
