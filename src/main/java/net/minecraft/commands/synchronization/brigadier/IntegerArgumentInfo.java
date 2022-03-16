package net.minecraft.commands.synchronization.brigadier;

import com.google.gson.JsonObject;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentUtils;
import net.minecraft.network.FriendlyByteBuf;

public class IntegerArgumentInfo implements ArgumentTypeInfo<IntegerArgumentType, IntegerArgumentInfo.Template> {
    public void serializeToNetwork(IntegerArgumentInfo.Template param0, FriendlyByteBuf param1) {
        boolean var0 = param0.min != Integer.MIN_VALUE;
        boolean var1 = param0.max != Integer.MAX_VALUE;
        param1.writeByte(ArgumentUtils.createNumberFlags(var0, var1));
        if (var0) {
            param1.writeInt(param0.min);
        }

        if (var1) {
            param1.writeInt(param0.max);
        }

    }

    public IntegerArgumentInfo.Template deserializeFromNetwork(FriendlyByteBuf param0) {
        byte var0 = param0.readByte();
        int var1 = ArgumentUtils.numberHasMin(var0) ? param0.readInt() : Integer.MIN_VALUE;
        int var2 = ArgumentUtils.numberHasMax(var0) ? param0.readInt() : Integer.MAX_VALUE;
        return new IntegerArgumentInfo.Template(var1, var2);
    }

    public void serializeToJson(IntegerArgumentInfo.Template param0, JsonObject param1) {
        if (param0.min != Integer.MIN_VALUE) {
            param1.addProperty("min", param0.min);
        }

        if (param0.max != Integer.MAX_VALUE) {
            param1.addProperty("max", param0.max);
        }

    }

    public IntegerArgumentInfo.Template unpack(IntegerArgumentType param0) {
        return new IntegerArgumentInfo.Template(param0.getMinimum(), param0.getMaximum());
    }

    public final class Template implements ArgumentTypeInfo.Template<IntegerArgumentType> {
        final int min;
        final int max;

        Template(int param1, int param2) {
            this.min = param1;
            this.max = param2;
        }

        public IntegerArgumentType instantiate(CommandBuildContext param0) {
            return IntegerArgumentType.integer(this.min, this.max);
        }

        @Override
        public ArgumentTypeInfo<IntegerArgumentType, ?> type() {
            return IntegerArgumentInfo.this;
        }
    }
}
