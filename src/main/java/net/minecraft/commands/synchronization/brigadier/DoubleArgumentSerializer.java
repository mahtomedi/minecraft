package net.minecraft.commands.synchronization.brigadier;

import com.google.gson.JsonObject;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import net.minecraft.commands.synchronization.ArgumentSerializer;
import net.minecraft.network.FriendlyByteBuf;

public class DoubleArgumentSerializer implements ArgumentSerializer<DoubleArgumentType> {
    public void serializeToNetwork(DoubleArgumentType param0, FriendlyByteBuf param1) {
        boolean var0 = param0.getMinimum() != -Double.MAX_VALUE;
        boolean var1 = param0.getMaximum() != Double.MAX_VALUE;
        param1.writeByte(BrigadierArgumentSerializers.createNumberFlags(var0, var1));
        if (var0) {
            param1.writeDouble(param0.getMinimum());
        }

        if (var1) {
            param1.writeDouble(param0.getMaximum());
        }

    }

    public DoubleArgumentType deserializeFromNetwork(FriendlyByteBuf param0) {
        byte var0 = param0.readByte();
        double var1 = BrigadierArgumentSerializers.numberHasMin(var0) ? param0.readDouble() : -Double.MAX_VALUE;
        double var2 = BrigadierArgumentSerializers.numberHasMax(var0) ? param0.readDouble() : Double.MAX_VALUE;
        return DoubleArgumentType.doubleArg(var1, var2);
    }

    public void serializeToJson(DoubleArgumentType param0, JsonObject param1) {
        if (param0.getMinimum() != -Double.MAX_VALUE) {
            param1.addProperty("min", param0.getMinimum());
        }

        if (param0.getMaximum() != Double.MAX_VALUE) {
            param1.addProperty("max", param0.getMaximum());
        }

    }
}
