package net.minecraft.client.gui.screens.advancements;

import com.google.common.collect.Maps;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementNode;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class AdvancementTab {
    private final Minecraft minecraft;
    private final AdvancementsScreen screen;
    private final AdvancementTabType type;
    private final int index;
    private final AdvancementNode rootNode;
    private final DisplayInfo display;
    private final ItemStack icon;
    private final Component title;
    private final AdvancementWidget root;
    private final Map<AdvancementHolder, AdvancementWidget> widgets = Maps.newLinkedHashMap();
    private double scrollX;
    private double scrollY;
    private int minX = Integer.MAX_VALUE;
    private int minY = Integer.MAX_VALUE;
    private int maxX = Integer.MIN_VALUE;
    private int maxY = Integer.MIN_VALUE;
    private float fade;
    private boolean centered;

    public AdvancementTab(Minecraft param0, AdvancementsScreen param1, AdvancementTabType param2, int param3, AdvancementNode param4, DisplayInfo param5) {
        this.minecraft = param0;
        this.screen = param1;
        this.type = param2;
        this.index = param3;
        this.rootNode = param4;
        this.display = param5;
        this.icon = param5.getIcon();
        this.title = param5.getTitle();
        this.root = new AdvancementWidget(this, param0, param4, param5);
        this.addWidget(this.root, param4.holder());
    }

    public AdvancementTabType getType() {
        return this.type;
    }

    public int getIndex() {
        return this.index;
    }

    public AdvancementNode getRootNode() {
        return this.rootNode;
    }

    public Component getTitle() {
        return this.title;
    }

    public DisplayInfo getDisplay() {
        return this.display;
    }

    public void drawTab(GuiGraphics param0, int param1, int param2, boolean param3) {
        this.type.draw(param0, param1, param2, param3, this.index);
    }

    public void drawIcon(GuiGraphics param0, int param1, int param2) {
        this.type.drawIcon(param0, param1, param2, this.index, this.icon);
    }

    public void drawContents(GuiGraphics param0, int param1, int param2) {
        if (!this.centered) {
            this.scrollX = (double)(117 - (this.maxX + this.minX) / 2);
            this.scrollY = (double)(56 - (this.maxY + this.minY) / 2);
            this.centered = true;
        }

        param0.enableScissor(param1, param2, param1 + 234, param2 + 113);
        param0.pose().pushPose();
        param0.pose().translate((float)param1, (float)param2, 0.0F);
        ResourceLocation var0 = Objects.requireNonNullElse(this.display.getBackground(), TextureManager.INTENTIONAL_MISSING_TEXTURE);
        int var1 = Mth.floor(this.scrollX);
        int var2 = Mth.floor(this.scrollY);
        int var3 = var1 % 16;
        int var4 = var2 % 16;

        for(int var5 = -1; var5 <= 15; ++var5) {
            for(int var6 = -1; var6 <= 8; ++var6) {
                param0.blit(var0, var3 + 16 * var5, var4 + 16 * var6, 0.0F, 0.0F, 16, 16, 16, 16);
            }
        }

        this.root.drawConnectivity(param0, var1, var2, true);
        this.root.drawConnectivity(param0, var1, var2, false);
        this.root.draw(param0, var1, var2);
        param0.pose().popPose();
        param0.disableScissor();
    }

    public void drawTooltips(GuiGraphics param0, int param1, int param2, int param3, int param4) {
        param0.pose().pushPose();
        param0.pose().translate(0.0F, 0.0F, -200.0F);
        param0.fill(0, 0, 234, 113, Mth.floor(this.fade * 255.0F) << 24);
        boolean var0 = false;
        int var1 = Mth.floor(this.scrollX);
        int var2 = Mth.floor(this.scrollY);
        if (param1 > 0 && param1 < 234 && param2 > 0 && param2 < 113) {
            for(AdvancementWidget var3 : this.widgets.values()) {
                if (var3.isMouseOver(var1, var2, param1, param2)) {
                    var0 = true;
                    var3.drawHover(param0, var1, var2, this.fade, param3, param4);
                    break;
                }
            }
        }

        param0.pose().popPose();
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
    public static AdvancementTab create(Minecraft param0, AdvancementsScreen param1, int param2, AdvancementNode param3) {
        Optional<DisplayInfo> var0 = param3.advancement().display();
        if (var0.isEmpty()) {
            return null;
        } else {
            for(AdvancementTabType var1 : AdvancementTabType.values()) {
                if (param2 < var1.getMax()) {
                    return new AdvancementTab(param0, param1, var1, param2, param3, var0.get());
                }

                param2 -= var1.getMax();
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

    public void addAdvancement(AdvancementNode param0) {
        Optional<DisplayInfo> var0 = param0.advancement().display();
        if (!var0.isEmpty()) {
            AdvancementWidget var1 = new AdvancementWidget(this, this.minecraft, param0, var0.get());
            this.addWidget(var1, param0.holder());
        }
    }

    private void addWidget(AdvancementWidget param0, AdvancementHolder param1) {
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
    public AdvancementWidget getWidget(AdvancementHolder param0) {
        return this.widgets.get(param0);
    }

    public AdvancementsScreen getScreen() {
        return this.screen;
    }
}
