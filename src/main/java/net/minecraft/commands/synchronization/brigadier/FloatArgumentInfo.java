package net.minecraft.commands.synchronization.brigadier;

import com.google.gson.JsonObject;
import com.mojang.brigadier.arguments.FloatArgumentType;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentUtils;
import net.minecraft.network.FriendlyByteBuf;

public class FloatArgumentInfo implements ArgumentTypeInfo<FloatArgumentType, FloatArgumentInfo.Template> {
    public void serializeToNetwork(FloatArgumentInfo.Template param0, FriendlyByteBuf param1) {
        boolean var0 = param0.min != -Float.MAX_VALUE;
        boolean var1 = param0.max != Float.MAX_VALUE;
        param1.writeByte(ArgumentUtils.createNumberFlags(var0, var1));
        if (var0) {
            param1.writeFloat(param0.min);
        }

        if (var1) {
            param1.writeFloat(param0.max);
        }

    }

    public FloatArgumentInfo.Template deserializeFromNetwork(FriendlyByteBuf param0) {
        byte var0 = param0.readByte();
        float var1 = ArgumentUtils.numberHasMin(var0) ? param0.readFloat() : -Float.MAX_VALUE;
        float var2 = ArgumentUtils.numberHasMax(var0) ? param0.readFloat() : Float.MAX_VALUE;
        return new FloatArgumentInfo.Template(var1, var2);
    }

    public void serializeToJson(FloatArgumentInfo.Template param0, JsonObject param1) {
        if (param0.min != -Float.MAX_VALUE) {
            param1.addProperty("min", param0.min);
        }

        if (param0.max != Float.MAX_VALUE) {
            param1.addProperty("max", param0.max);
        }

    }

    public FloatArgumentInfo.Template unpack(FloatArgumentType param0) {
        return new FloatArgumentInfo.Template(param0.getMinimum(), param0.getMaximum());
    }

    public final class Template implements ArgumentTypeInfo.Template<FloatArgumentType> {
        final float min;
        final float max;

        Template(float param1, float param2) {
            this.min = param1;
            this.max = param2;
        }

        public FloatArgumentType instantiate(CommandBuildContext param0) {
            return FloatArgumentType.floatArg(this.min, this.max);
        }

        @Override
        public ArgumentTypeInfo<FloatArgumentType, ?> type() {
            return FloatArgumentInfo.this;
        }
    }
}
