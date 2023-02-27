package net.minecraft.client.gui.navigation;

import javax.annotation.Nullable;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public record ScreenRectangle(ScreenPosition position, int width, int height) {
    private static final ScreenRectangle EMPTY = new ScreenRectangle(0, 0, 0, 0);

    public ScreenRectangle(int param0, int param1, int param2, int param3) {
        this(new ScreenPosition(param0, param1), param2, param3);
    }

    public static ScreenRectangle empty() {
        return EMPTY;
    }

    public static ScreenRectangle of(ScreenAxis param0, int param1, int param2, int param3, int param4) {
        return switch(param0) {
            case HORIZONTAL -> new ScreenRectangle(param1, param2, param3, param4);
            case VERTICAL -> new ScreenRectangle(param2, param1, param4, param3);
        };
    }

    public ScreenRectangle step(ScreenDirection param0) {
        return new ScreenRectangle(this.position.step(param0), this.width, this.height);
    }

    public int getLength(ScreenAxis param0) {
        return switch(param0) {
            case HORIZONTAL -> this.width;
            case VERTICAL -> this.height;
        };
    }

    public int getBoundInDirection(ScreenDirection param0) {
        ScreenAxis var0 = param0.getAxis();
        return param0.isPositive() ? this.position.getCoordinate(var0) + this.getLength(var0) - 1 : this.position.getCoordinate(var0);
    }

    public ScreenRectangle getBorder(ScreenDirection param0) {
        int var0 = this.getBoundInDirection(param0);
        ScreenAxis var1 = param0.getAxis().orthogonal();
        int var2 = this.getBoundInDirection(var1.getNegative());
        int var3 = this.getLength(var1);
        return of(param0.getAxis(), var0, var2, 1, var3).step(param0);
    }

    public boolean overlaps(ScreenRectangle param0) {
        return this.overlapsInAxis(param0, ScreenAxis.HORIZONTAL) && this.overlapsInAxis(param0, ScreenAxis.VERTICAL);
    }

    public boolean overlapsInAxis(ScreenRectangle param0, ScreenAxis param1) {
        int var0 = this.getBoundInDirection(param1.getNegative());
        int var1 = param0.getBoundInDirection(param1.getNegative());
        int var2 = this.getBoundInDirection(param1.getPositive());
        int var3 = param0.getBoundInDirection(param1.getPositive());
        return Math.max(var0, var1) <= Math.min(var2, var3);
    }

    public int getCenterInAxis(ScreenAxis param0) {
        return (this.getBoundInDirection(param0.getPositive()) + this.getBoundInDirection(param0.getNegative())) / 2;
    }

    @Nullable
    public ScreenRectangle intersection(ScreenRectangle param0) {
        int var0 = Math.max(this.left(), param0.left());
        int var1 = Math.max(this.top(), param0.top());
        int var2 = Math.min(this.right(), param0.right());
        int var3 = Math.min(this.bottom(), param0.bottom());
        return var0 < var2 && var1 < var3 ? new ScreenRectangle(var0, var1, var2 - var0, var3 - var1) : null;
    }

    public int top() {
        return this.position.y();
    }

    public int bottom() {
        return this.position.y() + this.height;
    }

    public int left() {
        return this.position.x();
    }

    public int right() {
        return this.position.x() + this.width;
    }
}
