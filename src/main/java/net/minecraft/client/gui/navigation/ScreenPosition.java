package net.minecraft.client.gui.navigation;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public record ScreenPosition(int x, int y) {
    public static ScreenPosition of(ScreenAxis param0, int param1, int param2) {
        return switch(param0) {
            case HORIZONTAL -> new ScreenPosition(param1, param2);
            case VERTICAL -> new ScreenPosition(param2, param1);
        };
    }

    public ScreenPosition step(ScreenDirection param0) {
        return switch(param0) {
            case DOWN -> new ScreenPosition(this.x, this.y + 1);
            case UP -> new ScreenPosition(this.x, this.y - 1);
            case LEFT -> new ScreenPosition(this.x - 1, this.y);
            case RIGHT -> new ScreenPosition(this.x + 1, this.y);
        };
    }

    public int getCoordinate(ScreenAxis param0) {
        return switch(param0) {
            case HORIZONTAL -> this.x;
            case VERTICAL -> this.y;
        };
    }
}
