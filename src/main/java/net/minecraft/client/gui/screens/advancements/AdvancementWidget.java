package net.minecraft.client.gui.screens.advancements;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.advancements.AdvancementNode;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.StringSplitter;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class AdvancementWidget {
    private static final ResourceLocation TITLE_BOX_SPRITE = new ResourceLocation("advancements/title_box");
    private static final int HEIGHT = 26;
    private static final int BOX_X = 0;
    private static final int BOX_WIDTH = 200;
    private static final int FRAME_WIDTH = 26;
    private static final int ICON_X = 8;
    private static final int ICON_Y = 5;
    private static final int ICON_WIDTH = 26;
    private static final int TITLE_PADDING_LEFT = 3;
    private static final int TITLE_PADDING_RIGHT = 5;
    private static final int TITLE_X = 32;
    private static final int TITLE_Y = 9;
    private static final int TITLE_MAX_WIDTH = 163;
    private static final int[] TEST_SPLIT_OFFSETS = new int[]{0, 10, -10, 25, -25};
    private final AdvancementTab tab;
    private final AdvancementNode advancementNode;
    private final DisplayInfo display;
    private final FormattedCharSequence title;
    private final int width;
    private final List<FormattedCharSequence> description;
    private final Minecraft minecraft;
    @Nullable
    private AdvancementWidget parent;
    private final List<AdvancementWidget> children = Lists.newArrayList();
    @Nullable
    private AdvancementProgress progress;
    private final int x;
    private final int y;

    public AdvancementWidget(AdvancementTab param0, Minecraft param1, AdvancementNode param2, DisplayInfo param3) {
        this.tab = param0;
        this.advancementNode = param2;
        this.display = param3;
        this.minecraft = param1;
        this.title = Language.getInstance().getVisualOrder(param1.font.substrByWidth(param3.getTitle(), 163));
        this.x = Mth.floor(param3.getX() * 28.0F);
        this.y = Mth.floor(param3.getY() * 27.0F);
        int var0 = param2.advancement().requirements().size();
        int var1 = String.valueOf(var0).length();
        int var2 = var0 > 1 ? param1.font.width("  ") + param1.font.width("0") * var1 * 2 + param1.font.width("/") : 0;
        int var3 = 29 + param1.font.width(this.title) + var2;
        this.description = Language.getInstance()
            .getVisualOrder(
                this.findOptimalLines(ComponentUtils.mergeStyles(param3.getDescription().copy(), Style.EMPTY.withColor(param3.getType().getChatColor())), var3)
            );

        for(FormattedCharSequence var4 : this.description) {
            var3 = Math.max(var3, param1.font.width(var4));
        }

        this.width = var3 + 3 + 5;
    }

    private static float getMaxWidth(StringSplitter param0, List<FormattedText> param1) {
        return (float)param1.stream().mapToDouble(param0::stringWidth).max().orElse(0.0);
    }

    private List<FormattedText> findOptimalLines(Component param0, int param1) {
        StringSplitter var0 = this.minecraft.font.getSplitter();
        List<FormattedText> var1 = null;
        float var2 = Float.MAX_VALUE;

        for(int var3 : TEST_SPLIT_OFFSETS) {
            List<FormattedText> var4 = var0.splitLines(param0, param1 - var3, Style.EMPTY);
            float var5 = Math.abs(getMaxWidth(var0, var4) - (float)param1);
            if (var5 <= 10.0F) {
                return var4;
            }

            if (var5 < var2) {
                var2 = var5;
                var1 = var4;
            }
        }

        return var1;
    }

    @Nullable
    private AdvancementWidget getFirstVisibleParent(AdvancementNode param0) {
        do {
            param0 = param0.parent();
        } while(param0 != null && param0.advancement().display().isEmpty());

        return param0 != null && !param0.advancement().display().isEmpty() ? this.tab.getWidget(param0.holder()) : null;
    }

    public void drawConnectivity(GuiGraphics param0, int param1, int param2, boolean param3) {
        if (this.parent != null) {
            int var0 = param1 + this.parent.x + 13;
            int var1 = param1 + this.parent.x + 26 + 4;
            int var2 = param2 + this.parent.y + 13;
            int var3 = param1 + this.x + 13;
            int var4 = param2 + this.y + 13;
            int var5 = param3 ? -16777216 : -1;
            if (param3) {
                param0.hLine(var1, var0, var2 - 1, var5);
                param0.hLine(var1 + 1, var0, var2, var5);
                param0.hLine(var1, var0, var2 + 1, var5);
                param0.hLine(var3, var1 - 1, var4 - 1, var5);
                param0.hLine(var3, var1 - 1, var4, var5);
                param0.hLine(var3, var1 - 1, var4 + 1, var5);
                param0.vLine(var1 - 1, var4, var2, var5);
                param0.vLine(var1 + 1, var4, var2, var5);
            } else {
                param0.hLine(var1, var0, var2, var5);
                param0.hLine(var3, var1, var4, var5);
                param0.vLine(var1, var4, var2, var5);
            }
        }

        for(AdvancementWidget var6 : this.children) {
            var6.drawConnectivity(param0, param1, param2, param3);
        }

    }

    public void draw(GuiGraphics param0, int param1, int param2) {
        if (!this.display.isHidden() || this.progress != null && this.progress.isDone()) {
            float var0 = this.progress == null ? 0.0F : this.progress.getPercent();
            AdvancementWidgetType var1;
            if (var0 >= 1.0F) {
                var1 = AdvancementWidgetType.OBTAINED;
            } else {
                var1 = AdvancementWidgetType.UNOBTAINED;
            }

            param0.blitSprite(var1.frameSprite(this.display.getType()), param1 + this.x + 3, param2 + this.y, 26, 26);
            param0.renderFakeItem(this.display.getIcon(), param1 + this.x + 8, param2 + this.y + 5);
        }

        for(AdvancementWidget var3 : this.children) {
            var3.draw(param0, param1, param2);
        }

    }

    public int getWidth() {
        return this.width;
    }

    public void setProgress(AdvancementProgress param0) {
        this.progress = param0;
    }

    public void addChild(AdvancementWidget param0) {
        this.children.add(param0);
    }

    public void drawHover(GuiGraphics param0, int param1, int param2, float param3, int param4, int param5) {
        boolean var0 = param4 + param1 + this.x + this.width + 26 >= this.tab.getScreen().width;
        Component var1 = this.progress == null ? null : this.progress.getProgressText();
        int var2 = var1 == null ? 0 : this.minecraft.font.width(var1);
        boolean var3 = 113 - param2 - this.y - 26 <= 6 + this.description.size() * 9;
        float var4 = this.progress == null ? 0.0F : this.progress.getPercent();
        int var5 = Mth.floor(var4 * (float)this.width);
        AdvancementWidgetType var6;
        AdvancementWidgetType var7;
        AdvancementWidgetType var8;
        if (var4 >= 1.0F) {
            var5 = this.width / 2;
            var6 = AdvancementWidgetType.OBTAINED;
            var7 = AdvancementWidgetType.OBTAINED;
            var8 = AdvancementWidgetType.OBTAINED;
        } else if (var5 < 2) {
            var5 = this.width / 2;
            var6 = AdvancementWidgetType.UNOBTAINED;
            var7 = AdvancementWidgetType.UNOBTAINED;
            var8 = AdvancementWidgetType.UNOBTAINED;
        } else if (var5 > this.width - 2) {
            var5 = this.width / 2;
            var6 = AdvancementWidgetType.OBTAINED;
            var7 = AdvancementWidgetType.OBTAINED;
            var8 = AdvancementWidgetType.UNOBTAINED;
        } else {
            var6 = AdvancementWidgetType.OBTAINED;
            var7 = AdvancementWidgetType.UNOBTAINED;
            var8 = AdvancementWidgetType.UNOBTAINED;
        }

        int var18 = this.width - var5;
        RenderSystem.enableBlend();
        int var19 = param2 + this.y;
        int var20;
        if (var0) {
            var20 = param1 + this.x - this.width + 26 + 6;
        } else {
            var20 = param1 + this.x;
        }

        int var22 = 32 + this.description.size() * 9;
        if (!this.description.isEmpty()) {
            if (var3) {
                param0.blitSprite(TITLE_BOX_SPRITE, var20, var19 + 26 - var22, this.width, var22);
            } else {
                param0.blitSprite(TITLE_BOX_SPRITE, var20, var19, this.width, var22);
            }
        }

        param0.blitSprite(var6.boxSprite(), 200, 26, 0, 0, var20, var19, var5, 26);
        param0.blitSprite(var7.boxSprite(), 200, 26, 200 - var18, 0, var20 + var5, var19, var18, 26);
        param0.blitSprite(var8.frameSprite(this.display.getType()), param1 + this.x + 3, param2 + this.y, 26, 26);
        if (var0) {
            param0.drawString(this.minecraft.font, this.title, var20 + 5, param2 + this.y + 9, -1);
            if (var1 != null) {
                param0.drawString(this.minecraft.font, var1, param1 + this.x - var2, param2 + this.y + 9, -1);
            }
        } else {
            param0.drawString(this.minecraft.font, this.title, param1 + this.x + 32, param2 + this.y + 9, -1);
            if (var1 != null) {
                param0.drawString(this.minecraft.font, var1, param1 + this.x + this.width - var2 - 5, param2 + this.y + 9, -1);
            }
        }

        if (var3) {
            for(int var23 = 0; var23 < this.description.size(); ++var23) {
                param0.drawString(this.minecraft.font, this.description.get(var23), var20 + 5, var19 + 26 - var22 + 7 + var23 * 9, -5592406, false);
            }
        } else {
            for(int var24 = 0; var24 < this.description.size(); ++var24) {
                param0.drawString(this.minecraft.font, this.description.get(var24), var20 + 5, param2 + this.y + 9 + 17 + var24 * 9, -5592406, false);
            }
        }

        param0.renderFakeItem(this.display.getIcon(), param1 + this.x + 8, param2 + this.y + 5);
    }

    public boolean isMouseOver(int param0, int param1, int param2, int param3) {
        if (!this.display.isHidden() || this.progress != null && this.progress.isDone()) {
            int var0 = param0 + this.x;
            int var1 = var0 + 26;
            int var2 = param1 + this.y;
            int var3 = var2 + 26;
            return param2 >= var0 && param2 <= var1 && param3 >= var2 && param3 <= var3;
        } else {
            return false;
        }
    }

    public void attachToParent() {
        if (this.parent == null && this.advancementNode.parent() != null) {
            this.parent = this.getFirstVisibleParent(this.advancementNode);
            if (this.parent != null) {
                this.parent.addChild(this);
            }
        }

    }

    public int getY() {
        return this.y;
    }

    public int getX() {
        return this.x;
    }
}
