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

    public Rect2i intersect(Rect2i param0) {
        int var0 = this.xPos;
        int var1 = this.yPos;
        int var2 = this.xPos + this.width;
        int var3 = this.yPos + this.height;
        int var4 = param0.getX();
        int var5 = param0.getY();
        int var6 = var4 + param0.getWidth();
        int var7 = var5 + param0.getHeight();
        this.xPos = Math.max(var0, var4);
        this.yPos = Math.max(var1, var5);
        this.width = Math.max(0, Math.min(var2, var6) - this.xPos);
        this.height = Math.max(0, Math.min(var3, var7) - this.yPos);
        return this;
    }

    public int getX() {
        return this.xPos;
    }

    public int getY() {
        return this.yPos;
    }

    public void setX(int param0) {
        this.xPos = param0;
    }

    public void setY(int param0) {
        this.yPos = param0;
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public void setWidth(int param0) {
        this.width = param0;
    }

    public void setHeight(int param0) {
        this.height = param0;
    }

    public void setPosition(int param0, int param1) {
        this.xPos = param0;
        this.yPos = param1;
    }

    public boolean contains(int param0, int param1) {
        return param0 >= this.xPos && param0 <= this.xPos + this.width && param1 >= this.yPos && param1 <= this.yPos + this.height;
    }
}
