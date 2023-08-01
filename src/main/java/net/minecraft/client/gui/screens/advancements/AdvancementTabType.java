package net.minecraft.client.gui.screens.advancements;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
enum AdvancementTabType {
    ABOVE(
        new AdvancementTabType.Sprites(
            new ResourceLocation("advancements/tab_above_left_selected"),
            new ResourceLocation("advancements/tab_above_middle_selected"),
            new ResourceLocation("advancements/tab_above_right_selected")
        ),
        new AdvancementTabType.Sprites(
            new ResourceLocation("advancements/tab_above_left"),
            new ResourceLocation("advancements/tab_above_middle"),
            new ResourceLocation("advancements/tab_above_right")
        ),
        28,
        32,
        8
    ),
    BELOW(
        new AdvancementTabType.Sprites(
            new ResourceLocation("advancements/tab_below_left_selected"),
            new ResourceLocation("advancements/tab_below_middle_selected"),
            new ResourceLocation("advancements/tab_below_right_selected")
        ),
        new AdvancementTabType.Sprites(
            new ResourceLocation("advancements/tab_below_left"),
            new ResourceLocation("advancements/tab_below_middle"),
            new ResourceLocation("advancements/tab_below_right")
        ),
        28,
        32,
        8
    ),
    LEFT(
        new AdvancementTabType.Sprites(
            new ResourceLocation("advancements/tab_left_top_selected"),
            new ResourceLocation("advancements/tab_left_middle_selected"),
            new ResourceLocation("advancements/tab_left_bottom_selected")
        ),
        new AdvancementTabType.Sprites(
            new ResourceLocation("advancements/tab_left_top"),
            new ResourceLocation("advancements/tab_left_middle"),
            new ResourceLocation("advancements/tab_left_bottom")
        ),
        32,
        28,
        5
    ),
    RIGHT(
        new AdvancementTabType.Sprites(
            new ResourceLocation("advancements/tab_right_top_selected"),
            new ResourceLocation("advancements/tab_right_middle_selected"),
            new ResourceLocation("advancements/tab_right_bottom_selected")
        ),
        new AdvancementTabType.Sprites(
            new ResourceLocation("advancements/tab_right_top"),
            new ResourceLocation("advancements/tab_right_middle"),
            new ResourceLocation("advancements/tab_right_bottom")
        ),
        32,
        28,
        5
    );

    private final AdvancementTabType.Sprites selectedSprites;
    private final AdvancementTabType.Sprites unselectedSprites;
    private final int width;
    private final int height;
    private final int max;

    private AdvancementTabType(AdvancementTabType.Sprites param0, AdvancementTabType.Sprites param1, int param2, int param3, int param4) {
        this.selectedSprites = param0;
        this.unselectedSprites = param1;
        this.width = param2;
        this.height = param3;
        this.max = param4;
    }

    public int getMax() {
        return this.max;
    }

    public void draw(GuiGraphics param0, int param1, int param2, boolean param3, int param4) {
        AdvancementTabType.Sprites var0 = param3 ? this.selectedSprites : this.unselectedSprites;
        ResourceLocation var1;
        if (param4 == 0) {
            var1 = var0.first();
        } else if (param4 == this.max - 1) {
            var1 = var0.last();
        } else {
            var1 = var0.middle();
        }

        param0.blitSprite(var1, param1 + this.getX(param4), param2 + this.getY(param4), this.width, this.height);
    }

    public void drawIcon(GuiGraphics param0, int param1, int param2, int param3, ItemStack param4) {
        int var0 = param1 + this.getX(param3);
        int var1 = param2 + this.getY(param3);
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

        param0.renderFakeItem(param4, var0, var1);
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

    @OnlyIn(Dist.CLIENT)
    static record Sprites(ResourceLocation first, ResourceLocation middle, ResourceLocation last) {
    }
}
