package net.minecraft.world.scores;

import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.numbers.NumberFormat;
import net.minecraft.network.chat.numbers.NumberFormatTypes;

public class Score implements ReadOnlyScoreInfo {
    private static final String TAG_SCORE = "Score";
    private static final String TAG_LOCKED = "Locked";
    private static final String TAG_DISPLAY = "display";
    private static final String TAG_FORMAT = "format";
    private int value;
    private boolean locked = true;
    @Nullable
    private Component display;
    @Nullable
    private NumberFormat numberFormat;

    @Override
    public int value() {
        return this.value;
    }

    public void value(int param0) {
        this.value = param0;
    }

    @Override
    public boolean isLocked() {
        return this.locked;
    }

    public void setLocked(boolean param0) {
        this.locked = param0;
    }

    @Nullable
    public Component display() {
        return this.display;
    }

    public void display(@Nullable Component param0) {
        this.display = param0;
    }

    @Nullable
    @Override
    public NumberFormat numberFormat() {
        return this.numberFormat;
    }

    public void numberFormat(@Nullable NumberFormat param0) {
        this.numberFormat = param0;
    }

    public CompoundTag write() {
        CompoundTag var0 = new CompoundTag();
        var0.putInt("Score", this.value);
        var0.putBoolean("Locked", this.locked);
        if (this.display != null) {
            var0.putString("display", Component.Serializer.toJson(this.display));
        }

        if (this.numberFormat != null) {
            NumberFormatTypes.CODEC.encodeStart(NbtOps.INSTANCE, this.numberFormat).result().ifPresent(param1 -> var0.put("format", param1));
        }

        return var0;
    }

    public static Score read(CompoundTag param0) {
        Score var0 = new Score();
        var0.value = param0.getInt("Score");
        var0.locked = param0.getBoolean("Locked");
        if (param0.contains("display", 8)) {
            var0.display = Component.Serializer.fromJson(param0.getString("display"));
        }

        if (param0.contains("format", 10)) {
            NumberFormatTypes.CODEC.parse(NbtOps.INSTANCE, param0.get("format")).result().ifPresent(param1 -> var0.numberFormat = param1);
        }

        return var0;
    }
}
