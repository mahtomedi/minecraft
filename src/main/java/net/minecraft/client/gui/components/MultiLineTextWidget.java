package net.minecraft.client.gui.components;

import java.util.OptionalInt;
import net.minecraft.Util;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.SingleKeyCache;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class MultiLineTextWidget extends AbstractStringWidget {
    private OptionalInt maxWidth = OptionalInt.empty();
    private OptionalInt maxRows = OptionalInt.empty();
    private final SingleKeyCache<MultiLineTextWidget.CacheKey, MultiLineLabel> cache;
    private boolean centered = false;

    public MultiLineTextWidget(Component param0, Font param1) {
        this(0, 0, param0, param1);
    }

    public MultiLineTextWidget(int param0, int param1, Component param2, Font param3) {
        super(param0, param1, 0, 0, param2, param3);
        this.cache = Util.singleKeyCache(
            param1x -> param1x.maxRows.isPresent()
                    ? MultiLineLabel.create(param3, param1x.message, param1x.maxWidth, param1x.maxRows.getAsInt())
                    : MultiLineLabel.create(param3, param1x.message, param1x.maxWidth)
        );
        this.active = false;
    }

    public MultiLineTextWidget setColor(int param0) {
        super.setColor(param0);
        return this;
    }

    public MultiLineTextWidget setMaxWidth(int param0) {
        this.maxWidth = OptionalInt.of(param0);
        return this;
    }

    public MultiLineTextWidget setMaxRows(int param0) {
        this.maxRows = OptionalInt.of(param0);
        return this;
    }

    public MultiLineTextWidget setCentered(boolean param0) {
        this.centered = param0;
        return this;
    }

    @Override
    public int getWidth() {
        return this.cache.getValue(this.getFreshCacheKey()).getWidth();
    }

    @Override
    public int getHeight() {
        return this.cache.getValue(this.getFreshCacheKey()).getLineCount() * 9;
    }

    @Override
    public void renderWidget(GuiGraphics param0, int param1, int param2, float param3) {
        MultiLineLabel var0 = this.cache.getValue(this.getFreshCacheKey());
        int var1 = this.getX();
        int var2 = this.getY();
        int var3 = 9;
        int var4 = this.getColor();
        if (this.centered) {
            var0.renderCentered(param0, var1 + this.getWidth() / 2, var2, var3, var4);
        } else {
            var0.renderLeftAligned(param0, var1, var2, var3, var4);
        }

    }

    private MultiLineTextWidget.CacheKey getFreshCacheKey() {
        return new MultiLineTextWidget.CacheKey(this.getMessage(), this.maxWidth.orElse(Integer.MAX_VALUE), this.maxRows);
    }

    @OnlyIn(Dist.CLIENT)
    static record CacheKey(Component message, int maxWidth, OptionalInt maxRows) {
    }
}
