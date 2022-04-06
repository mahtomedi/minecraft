package net.minecraft.commands.synchronization.brigadier;

import com.google.gson.JsonObject;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentUtils;
import net.minecraft.network.FriendlyByteBuf;

public class DoubleArgumentInfo implements ArgumentTypeInfo<DoubleArgumentType, DoubleArgumentInfo.Template> {
    public void serializeToNetwork(DoubleArgumentInfo.Template param0, FriendlyByteBuf param1) {
        boolean var0 = param0.min != -Double.MAX_VALUE;
        boolean var1 = param0.max != Double.MAX_VALUE;
        param1.writeByte(ArgumentUtils.createNumberFlags(var0, var1));
        if (var0) {
            param1.writeDouble(param0.min);
        }

        if (var1) {
            param1.writeDouble(param0.max);
        }

    }

    public DoubleArgumentInfo.Template deserializeFromNetwork(FriendlyByteBuf param0) {
        byte var0 = param0.readByte();
        double var1 = ArgumentUtils.numberHasMin(var0) ? param0.readDouble() : -Double.MAX_VALUE;
        double var2 = ArgumentUtils.numberHasMax(var0) ? param0.readDouble() : Double.MAX_VALUE;
        return new DoubleArgumentInfo.Template(var1, var2);
    }

    public void serializeToJson(DoubleArgumentInfo.Template param0, JsonObject param1) {
        if (param0.min != -Double.MAX_VALUE) {
            param1.addProperty("min", param0.min);
        }

        if (param0.max != Double.MAX_VALUE) {
            param1.addProperty("max", param0.max);
        }

    }

    public DoubleArgumentInfo.Template unpack(DoubleArgumentType param0) {
        return new DoubleArgumentInfo.Template(param0.getMinimum(), param0.getMaximum());
    }

    public final class Template implements ArgumentTypeInfo.Template<DoubleArgumentType> {
        final double min;
        final double max;

        Template(double param1, double param2) {
            this.min = param1;
            this.max = param2;
        }

        public DoubleArgumentType instantiate(CommandBuildContext param0) {
            return DoubleArgumentType.doubleArg(this.min, this.max);
        }

        @Override
        public ArgumentTypeInfo<DoubleArgumentType, ?> type() {
            return DoubleArgumentInfo.this;
        }
    }
}
