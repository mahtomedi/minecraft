package net.minecraft.commands.synchronization.brigadier;

import com.google.gson.JsonObject;
import com.mojang.brigadier.arguments.LongArgumentType;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentUtils;
import net.minecraft.network.FriendlyByteBuf;

public class LongArgumentInfo implements ArgumentTypeInfo<LongArgumentType, LongArgumentInfo.Template> {
    public void serializeToNetwork(LongArgumentInfo.Template param0, FriendlyByteBuf param1) {
        boolean var0 = param0.min != Long.MIN_VALUE;
        boolean var1 = param0.max != Long.MAX_VALUE;
        param1.writeByte(ArgumentUtils.createNumberFlags(var0, var1));
        if (var0) {
            param1.writeLong(param0.min);
        }

        if (var1) {
            param1.writeLong(param0.max);
        }

    }

    public LongArgumentInfo.Template deserializeFromNetwork(FriendlyByteBuf param0) {
        byte var0 = param0.readByte();
        long var1 = ArgumentUtils.numberHasMin(var0) ? param0.readLong() : Long.MIN_VALUE;
        long var2 = ArgumentUtils.numberHasMax(var0) ? param0.readLong() : Long.MAX_VALUE;
        return new LongArgumentInfo.Template(var1, var2);
    }

    public void serializeToJson(LongArgumentInfo.Template param0, JsonObject param1) {
        if (param0.min != Long.MIN_VALUE) {
            param1.addProperty("min", param0.min);
        }

        if (param0.max != Long.MAX_VALUE) {
            param1.addProperty("max", param0.max);
        }

    }

    public LongArgumentInfo.Template unpack(LongArgumentType param0) {
        return new LongArgumentInfo.Template(param0.getMinimum(), param0.getMaximum());
    }

    public final class Template implements ArgumentTypeInfo.Template<LongArgumentType> {
        final long min;
        final long max;

        Template(long param1, long param2) {
            this.min = param1;
            this.max = param2;
        }

        public LongArgumentType instantiate(CommandBuildContext param0) {
            return LongArgumentType.longArg(this.min, this.max);
        }

        @Override
        public ArgumentTypeInfo<LongArgumentType, ?> type() {
            return LongArgumentInfo.this;
        }
    }
}
