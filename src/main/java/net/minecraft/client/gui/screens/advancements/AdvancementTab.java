package net.minecraft.client.gui.screens.advancements;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class AdvancementTab extends GuiComponent {
    private final Minecraft minecraft;
    private final AdvancementsScreen screen;
    private final AdvancementTabType type;
    private final int index;
    private final Advancement advancement;
    private final DisplayInfo display;
    private final ItemStack icon;
    private final String title;
    private final AdvancementWidget root;
    private final Map<Advancement, AdvancementWidget> widgets = Maps.newLinkedHashMap();
    private double scrollX;
    private double scrollY;
    private int minX = Integer.MAX_VALUE;
    private int minY = Integer.MAX_VALUE;
    private int maxX = Integer.MIN_VALUE;
    private int maxY = Integer.MIN_VALUE;
    private float fade;
    private boolean centered;

    public AdvancementTab(Minecraft param0, AdvancementsScreen param1, AdvancementTabType param2, int param3, Advancement param4, DisplayInfo param5) {
        this.minecraft = param0;
        this.screen = param1;
        this.type = param2;
        this.index = param3;
        this.advancement = param4;
        this.display = param5;
        this.icon = param5.getIcon();
        this.title = param5.getTitle().getColoredString();
        this.root = new AdvancementWidget(this, param0, param4, param5);
        this.addWidget(this.root, param4);
    }

    public Advancement getAdvancement() {
        return this.advancement;
    }

    public String getTitle() {
        return this.title;
    }

    public void drawTab(int param0, int param1, boolean param2) {
        this.type.draw(this, param0, param1, param2, this.index);
    }

    public void drawIcon(int param0, int param1, ItemRenderer param2) {
        this.type.drawIcon(param0, param1, this.index, param2, this.icon);
    }

    public void drawContents() {
        if (!this.centered) {
            this.scrollX = (double)(117 - (this.maxX + this.minX) / 2);
            this.scrollY = (double)(56 - (this.maxY + this.minY) / 2);
            this.centered = true;
        }

        RenderSystem.pushMatrix();
        RenderSystem.enableDepthTest();
        RenderSystem.translatef(0.0F, 0.0F, 950.0F);
        RenderSystem.colorMask(false, false, false, false);
        fill(468, 226, -234, -113, -16777216);
        RenderSystem.colorMask(true, true, true, true);
        RenderSystem.translatef(0.0F, 0.0F, -950.0F);
        RenderSystem.depthFunc(518);
        fill(234, 113, 0, 0, -16777216);
        RenderSystem.depthFunc(515);
        ResourceLocation var0 = this.display.getBackground();
        if (var0 != null) {
            this.minecraft.getTextureManager().bind(var0);
        } else {
            this.minecraft.getTextureManager().bind(TextureManager.INTENTIONAL_MISSING_TEXTURE);
        }

        int var1 = Mth.floor(this.scrollX);
        int var2 = Mth.floor(this.scrollY);
        int var3 = var1 % 16;
        int var4 = var2 % 16;

        for(int var5 = -1; var5 <= 15; ++var5) {
            for(int var6 = -1; var6 <= 8; ++var6) {
                blit(var3 + 16 * var5, var4 + 16 * var6, 0.0F, 0.0F, 16, 16, 16, 16);
            }
        }

        this.root.drawConnectivity(var1, var2, true);
        this.root.drawConnectivity(var1, var2, false);
        this.root.draw(var1, var2);
        RenderSystem.depthFunc(518);
        RenderSystem.translatef(0.0F, 0.0F, -950.0F);
        RenderSystem.colorMask(false, false, false, false);
        fill(468, 226, -234, -113, -16777216);
        RenderSystem.colorMask(true, true, true, true);
        RenderSystem.translatef(0.0F, 0.0F, 950.0F);
        RenderSystem.depthFunc(515);
        RenderSystem.popMatrix();
    }

    public void drawTooltips(int param0, int param1, int param2, int param3) {
        RenderSystem.pushMatrix();
        RenderSystem.translatef(0.0F, 0.0F, 200.0F);
        fill(0, 0, 234, 113, Mth.floor(this.fade * 255.0F) << 24);
        boolean var0 = false;
        int var1 = Mth.floor(this.scrollX);
        int var2 = Mth.floor(this.scrollY);
        if (param0 > 0 && param0 < 234 && param1 > 0 && param1 < 113) {
            for(AdvancementWidget var3 : this.widgets.values()) {
                if (var3.isMouseOver(var1, var2, param0, param1)) {
                    var0 = true;
                    var3.drawHover(var1, var2, this.fade, param2, param3);
                    break;
                }
            }
        }

        RenderSystem.popMatrix();
        if (var0) {
            this.fade = Mth.clamp(this.fade + 0.02F, 0.0F, 0.3F);
        } else {
            this.fade = Mth.clamp(this.fade - 0.04F, 0.0F, 1.0F);
        }

    }

    public boolean isMouseOver(int param0, int param1, double param2, double param3) {
        return this.type.isMouseOver(param0, param1, this.index, param2, param3);
    }

    @Nullable
    public static AdvancementTab create(Minecraft param0, AdvancementsScreen param1, int param2, Advancement param3) {
        if (param3.getDisplay() == null) {
            return null;
        } else {
            for(AdvancementTabType var0 : AdvancementTabType.values()) {
                if (param2 < var0.getMax()) {
                    return new AdvancementTab(param0, param1, var0, param2, param3, param3.getDisplay());
                }

                param2 -= var0.getMax();
            }

            return null;
        }
    }

    public void scroll(double param0, double param1) {
        if (this.maxX - this.minX > 234) {
            this.scrollX = Mth.clamp(this.scrollX + param0, (double)(-(this.maxX - 234)), 0.0);
        }

        if (this.maxY - this.minY > 113) {
            this.scrollY = Mth.clamp(this.scrollY + param1, (double)(-(this.maxY - 113)), 0.0);
        }

    }

    public void addAdvancement(Advancement param0) {
        if (param0.getDisplay() != null) {
            AdvancementWidget var0 = new AdvancementWidget(this, this.minecraft, param0, param0.getDisplay());
            this.addWidget(var0, param0);
        }
    }

    private void addWidget(AdvancementWidget param0, Advancement param1) {
        this.widgets.put(param1, param0);
        int var0 = param0.getX();
        int var1 = var0 + 28;
        int var2 = param0.getY();
        int var3 = var2 + 27;
        this.minX = Math.min(this.minX, var0);
        this.maxX = Math.max(this.maxX, var1);
        this.minY = Math.min(this.minY, var2);
        this.maxY = Math.max(this.maxY, var3);

        for(AdvancementWidget var4 : this.widgets.values()) {
            var4.attachToParent();
        }

    }

    @Nullable
    public AdvancementWidget getWidget(Advancement param0) {
        return this.widgets.get(param0);
    }

    public AdvancementsScreen getScreen() {
        return this.screen;
    }
}
