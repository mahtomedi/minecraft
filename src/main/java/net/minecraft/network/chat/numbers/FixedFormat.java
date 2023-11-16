package net.minecraft.network.chat.numbers;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.MutableComponent;

public class FixedFormat implements NumberFormat {
    public static final NumberFormatType<FixedFormat> TYPE = new NumberFormatType<FixedFormat>() {
        private static final MapCodec<FixedFormat> CODEC = ComponentSerialization.CODEC.fieldOf("value").xmap(FixedFormat::new, param0 -> param0.value);

        @Override
        public MapCodec<FixedFormat> mapCodec() {
            return CODEC;
        }

        public void writeToStream(FriendlyByteBuf param0, FixedFormat param1) {
            param0.writeComponent(param1.value);
        }

        public FixedFormat readFromStream(FriendlyByteBuf param0) {
            Component var0 = param0.readComponentTrusted();
            return new FixedFormat(var0);
        }
    };
    final Component value;

    public FixedFormat(Component param0) {
        this.value = param0;
    }

    @Override
    public MutableComponent format(int param0) {
        return this.value.copy();
    }

    @Override
    public NumberFormatType<FixedFormat> type() {
        return TYPE;
    }
}
