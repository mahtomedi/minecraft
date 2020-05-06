package net.minecraft.client.gui.screens.advancements;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
enum AdvancementTabType {
    ABOVE(0, 0, 28, 32, 8),
    BELOW(84, 0, 28, 32, 8),
    LEFT(0, 64, 32, 28, 5),
    RIGHT(96, 64, 32, 28, 5);

    private final int textureX;
    private final int textureY;
    private final int width;
    private final int height;
    private final int max;

    private AdvancementTabType(int param0, int param1, int param2, int param3, int param4) {
        this.textureX = param0;
        this.textureY = param1;
        this.width = param2;
        this.height = param3;
        this.max = param4;
    }

    public int getMax() {
        return this.max;
    }

    public void draw(PoseStack param0, GuiComponent param1, int param2, int param3, boolean param4, int param5) {
        int var0 = this.textureX;
        if (param5 > 0) {
            var0 += this.width;
        }

        if (param5 == this.max - 1) {
            var0 += this.width;
        }

        int var1 = param4 ? this.textureY + this.height : this.textureY;
        param1.blit(param0, param2 + this.getX(param5), param3 + this.getY(param5), var0, var1, this.width, this.height);
    }

    public void drawIcon(int param0, int param1, int param2, ItemRenderer param3, ItemStack param4) {
        int var0 = param0 + this.getX(param2);
        int var1 = param1 + this.getY(param2);
        switch(this) {
            case ABOVE:
                var0 += 6;
                var1 += 9;
                break;
            case BELOW:
                var0 += 6;
                var1 += 6;
                break;
            case LEFT:
                var0 += 10;
                var1 += 5;
                break;
            case RIGHT:
                var0 += 6;
                var1 += 5;
        }

        param3.renderAndDecorateFakeItem(param4, var0, var1);
    }

    public int getX(int param0) {
        switch(this) {
            case ABOVE:
                return (this.width + 4) * param0;
            case BELOW:
                return (this.width + 4) * param0;
            case LEFT:
                return -this.width + 4;
            case RIGHT:
                return 248;
            default:
                throw new UnsupportedOperationException("Don't know what this tab type is!" + this);
        }
    }

    public int getY(int param0) {
        switch(this) {
            case ABOVE:
                return -this.height + 4;
            case BELOW:
                return 136;
            case LEFT:
                return this.height * param0;
            case RIGHT:
                return this.height * param0;
            default:
                throw new UnsupportedOperationException("Don't know what this tab type is!" + this);
        }
    }

    public boolean isMouseOver(int param0, int param1, int param2, double param3, double param4) {
        int var0 = param0 + this.getX(param2);
        int var1 = param1 + this.getY(param2);
        return param3 > (double)var0 && param3 < (double)(var0 + this.width) && param4 > (double)var1 && param4 < (double)(var1 + this.height);
    }
}
