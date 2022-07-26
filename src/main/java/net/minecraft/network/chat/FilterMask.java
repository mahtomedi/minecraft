package net.minecraft.network.chat;

import java.util.BitSet;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.network.FriendlyByteBuf;

public class FilterMask {
    public static final FilterMask FULLY_FILTERED = new FilterMask(new BitSet(0), FilterMask.Type.FULLY_FILTERED);
    public static final FilterMask PASS_THROUGH = new FilterMask(new BitSet(0), FilterMask.Type.PASS_THROUGH);
    private static final char HASH = '#';
    private final BitSet mask;
    private final FilterMask.Type type;

    private FilterMask(BitSet param0, FilterMask.Type param1) {
        this.mask = param0;
        this.type = param1;
    }

    public FilterMask(int param0) {
        this(new BitSet(param0), FilterMask.Type.PARTIALLY_FILTERED);
    }

    public static FilterMask read(FriendlyByteBuf param0) {
        FilterMask.Type var0 = param0.readEnum(FilterMask.Type.class);

        return switch(var0) {
            case PASS_THROUGH -> PASS_THROUGH;
            case FULLY_FILTERED -> FULLY_FILTERED;
            case PARTIALLY_FILTERED -> new FilterMask(param0.readBitSet(), FilterMask.Type.PARTIALLY_FILTERED);
        };
    }

    public static void write(FriendlyByteBuf param0, FilterMask param1) {
        param0.writeEnum(param1.type);
        if (param1.type == FilterMask.Type.PARTIALLY_FILTERED) {
            param0.writeBitSet(param1.mask);
        }

    }

    public void setFiltered(int param0) {
        this.mask.set(param0);
    }

    @Nullable
    public String apply(String param0) {
        return switch(this.type) {
            case PASS_THROUGH -> param0;
            case FULLY_FILTERED -> null;
            case PARTIALLY_FILTERED -> {
                char[] var0 = param0.toCharArray();

                for(int var1 = 0; var1 < var0.length && var1 < this.mask.length(); ++var1) {
                    if (this.mask.get(var1)) {
                        var0[var1] = '#';
                    }
                }

                yield new String(var0);
            }
        };
    }

    @Nullable
    public Component apply(ChatMessageContent param0) {
        String var0 = param0.plain();
        return Util.mapNullable(this.apply(var0), Component::literal);
    }

    public boolean isEmpty() {
        return this.type == FilterMask.Type.PASS_THROUGH;
    }

    public boolean isFullyFiltered() {
        return this.type == FilterMask.Type.FULLY_FILTERED;
    }

    static enum Type {
        PASS_THROUGH,
        FULLY_FILTERED,
        PARTIALLY_FILTERED;
    }
}
