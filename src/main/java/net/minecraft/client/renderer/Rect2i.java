package net.minecraft.client.renderer;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class Rect2i {
    private int xPos;
    private int yPos;
    private int width;
    private int height;

    public Rect2i(int param0, int param1, int param2, int param3) {
        this.xPos = param0;
        this.yPos = param1;
        this.width = param2;
        this.height = param3;
    }

    public int getX() {
        return this.xPos;
    }

    public int getY() {
        return this.yPos;
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public boolean contains(int param0, int param1) {
        return param0 >= this.xPos && param0 <= this.xPos + this.width && param1 >= this.yPos && param1 <= this.yPos + this.height;
    }
}
