package net.minecraft.client.gui.navigation;

import it.unimi.dsi.fastutil.ints.IntComparator;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public enum ScreenDirection {
    UP,
    DOWN,
    LEFT,
    RIGHT;

    private final IntComparator coordinateValueComparator = (param0, param1) -> param0 == param1 ? 0 : (this.isBefore(param0, param1) ? -1 : 1);

    public ScreenAxis getAxis() {
        return switch(this) {
            case UP, DOWN -> ScreenAxis.VERTICAL;
            case LEFT, RIGHT -> ScreenAxis.HORIZONTAL;
        };
    }

    public ScreenDirection getOpposite() {
        return switch(this) {
            case UP -> DOWN;
            case DOWN -> UP;
            case LEFT -> RIGHT;
            case RIGHT -> LEFT;
        };
    }

    public boolean isPositive() {
        return switch(this) {
            case UP, LEFT -> false;
            case DOWN, RIGHT -> true;
        };
    }

    public boolean isAfter(int param0, int param1) {
        if (this.isPositive()) {
            return param0 > param1;
        } else {
            return param1 > param0;
        }
    }

    public boolean isBefore(int param0, int param1) {
        if (this.isPositive()) {
            return param0 < param1;
        } else {
            return param1 < param0;
        }
    }

    public IntComparator coordinateValueComparator() {
        return this.coordinateValueComparator;
    }
}
