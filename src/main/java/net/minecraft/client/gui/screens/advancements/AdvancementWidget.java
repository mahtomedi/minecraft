package net.minecraft.client.gui.screens.advancements;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.StringSplitter;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.GameRenderer;
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
public class AdvancementWidget extends GuiComponent {
    private static final ResourceLocation WIDGETS_LOCATION = new ResourceLocation("textures/gui/advancements/widgets.png");
    private static final int[] TEST_SPLIT_OFFSETS = new int[]{0, 10, -10, 25, -25};
    private final AdvancementTab tab;
    private final Advancement advancement;
    private final DisplayInfo display;
    private final FormattedCharSequence title;
    private final int width;
    private final List<FormattedCharSequence> description;
    private final Minecraft minecraft;
    private AdvancementWidget parent;
    private final List<AdvancementWidget> children = Lists.newArrayList();
    private AdvancementProgress progress;
    private final int x;
    private final int y;

    public AdvancementWidget(AdvancementTab param0, Minecraft param1, Advancement param2, DisplayInfo param3) {
        this.tab = param0;
        this.advancement = param2;
        this.display = param3;
        this.minecraft = param1;
        this.title = Language.getInstance().getVisualOrder(param1.font.substrByWidth(param3.getTitle(), 163));
        this.x = Mth.floor(param3.getX() * 28.0F);
        this.y = Mth.floor(param3.getY() * 27.0F);
        int var0 = param2.getMaxCriteraRequired();
        int var1 = String.valueOf(var0).length();
        int var2 = var0 > 1 ? param1.font.width("  ") + param1.font.width("0") * var1 * 2 + param1.font.width("/") : 0;
        int var3 = 29 + param1.font.width(this.title) + var2;
        this.description = Language.getInstance()
            .getVisualOrder(
                this.findOptimalLines(ComponentUtils.mergeStyles(param3.getDescription().copy(), Style.EMPTY.withColor(param3.getFrame().getChatColor())), var3)
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
    private AdvancementWidget getFirstVisibleParent(Advancement param0) {
        do {
            param0 = param0.getParent();
        } while(param0 != null && param0.getDisplay() == null);

        return param0 != null && param0.getDisplay() != null ? this.tab.getWidget(param0) : null;
    }

    public void drawConnectivity(PoseStack param0, int param1, int param2, boolean param3) {
        if (this.parent != null) {
            int var0 = param1 + this.parent.x + 13;
            int var1 = param1 + this.parent.x + 26 + 4;
            int var2 = param2 + this.parent.y + 13;
            int var3 = param1 + this.x + 13;
            int var4 = param2 + this.y + 13;
            int var5 = param3 ? -16777216 : -1;
            if (param3) {
                this.hLine(param0, var1, var0, var2 - 1, var5);
                this.hLine(param0, var1 + 1, var0, var2, var5);
                this.hLine(param0, var1, var0, var2 + 1, var5);
                this.hLine(param0, var3, var1 - 1, var4 - 1, var5);
                this.hLine(param0, var3, var1 - 1, var4, var5);
                this.hLine(param0, var3, var1 - 1, var4 + 1, var5);
                this.vLine(param0, var1 - 1, var4, var2, var5);
                this.vLine(param0, var1 + 1, var4, var2, var5);
            } else {
                this.hLine(param0, var1, var0, var2, var5);
                this.hLine(param0, var3, var1, var4, var5);
                this.vLine(param0, var1, var4, var2, var5);
            }
        }

        for(AdvancementWidget var6 : this.children) {
            var6.drawConnectivity(param0, param1, param2, param3);
        }

    }

    public void draw(PoseStack param0, int param1, int param2) {
        if (!this.display.isHidden() || this.progress != null && this.progress.isDone()) {
            float var0 = this.progress == null ? 0.0F : this.progress.getPercent();
            AdvancementWidgetType var1;
            if (var0 >= 1.0F) {
                var1 = AdvancementWidgetType.OBTAINED;
            } else {
                var1 = AdvancementWidgetType.UNOBTAINED;
            }

            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(0, WIDGETS_LOCATION);
            this.blit(param0, param1 + this.x + 3, param2 + this.y, this.display.getFrame().getTexture(), 128 + var1.getIndex() * 26, 26, 26);
            this.minecraft.getItemRenderer().renderAndDecorateFakeItem(this.display.getIcon(), param1 + this.x + 8, param2 + this.y + 5);
        }

        for(AdvancementWidget var3 : this.children) {
            var3.draw(param0, param1, param2);
        }

    }

    public void setProgress(AdvancementProgress param0) {
        this.progress = param0;
    }

    public void addChild(AdvancementWidget param0) {
        this.children.add(param0);
    }

    public void drawHover(PoseStack param0, int param1, int param2, float param3, int param4, int param5) {
        boolean var0 = param4 + param1 + this.x + this.width + 26 >= this.tab.getScreen().width;
        String var1 = this.progress == null ? null : this.progress.getProgressText();
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
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, WIDGETS_LOCATION);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
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
                this.render9Sprite(param0, var20, var19 + 26 - var22, this.width, var22, 10, 200, 26, 0, 52);
            } else {
                this.render9Sprite(param0, var20, var19, this.width, var22, 10, 200, 26, 0, 52);
            }
        }

        this.blit(param0, var20, var19, 0, var6.getIndex() * 26, var5, 26);
        this.blit(param0, var20 + var5, var19, 200 - var18, var7.getIndex() * 26, var18, 26);
        this.blit(param0, param1 + this.x + 3, param2 + this.y, this.display.getFrame().getTexture(), 128 + var8.getIndex() * 26, 26, 26);
        if (var0) {
            this.minecraft.font.drawShadow(param0, this.title, (float)(var20 + 5), (float)(param2 + this.y + 9), -1);
            if (var1 != null) {
                this.minecraft.font.drawShadow(param0, var1, (float)(param1 + this.x - var2), (float)(param2 + this.y + 9), -1);
            }
        } else {
            this.minecraft.font.drawShadow(param0, this.title, (float)(param1 + this.x + 32), (float)(param2 + this.y + 9), -1);
            if (var1 != null) {
                this.minecraft.font.drawShadow(param0, var1, (float)(param1 + this.x + this.width - var2 - 5), (float)(param2 + this.y + 9), -1);
            }
        }

        if (var3) {
            for(int var23 = 0; var23 < this.description.size(); ++var23) {
                this.minecraft.font.draw(param0, this.description.get(var23), (float)(var20 + 5), (float)(var19 + 26 - var22 + 7 + var23 * 9), -5592406);
            }
        } else {
            for(int var24 = 0; var24 < this.description.size(); ++var24) {
                this.minecraft.font.draw(param0, this.description.get(var24), (float)(var20 + 5), (float)(param2 + this.y + 9 + 17 + var24 * 9), -5592406);
            }
        }

        this.minecraft.getItemRenderer().renderAndDecorateFakeItem(this.display.getIcon(), param1 + this.x + 8, param2 + this.y + 5);
    }

    protected void render9Sprite(PoseStack param0, int param1, int param2, int param3, int param4, int param5, int param6, int param7, int param8, int param9) {
        this.blit(param0, param1, param2, param8, param9, param5, param5);
        this.renderRepeating(param0, param1 + param5, param2, param3 - param5 - param5, param5, param8 + param5, param9, param6 - param5 - param5, param7);
        this.blit(param0, param1 + param3 - param5, param2, param8 + param6 - param5, param9, param5, param5);
        this.blit(param0, param1, param2 + param4 - param5, param8, param9 + param7 - param5, param5, param5);
        this.renderRepeating(
            param0,
            param1 + param5,
            param2 + param4 - param5,
            param3 - param5 - param5,
            param5,
            param8 + param5,
            param9 + param7 - param5,
            param6 - param5 - param5,
            param7
        );
        this.blit(param0, param1 + param3 - param5, param2 + param4 - param5, param8 + param6 - param5, param9 + param7 - param5, param5, param5);
        this.renderRepeating(param0, param1, param2 + param5, param5, param4 - param5 - param5, param8, param9 + param5, param6, param7 - param5 - param5);
        this.renderRepeating(
            param0,
            param1 + param5,
            param2 + param5,
            param3 - param5 - param5,
            param4 - param5 - param5,
            param8 + param5,
            param9 + param5,
            param6 - param5 - param5,
            param7 - param5 - param5
        );
        this.renderRepeating(
            param0,
            param1 + param3 - param5,
            param2 + param5,
            param5,
            param4 - param5 - param5,
            param8 + param6 - param5,
            param9 + param5,
            param6,
            param7 - param5 - param5
        );
    }

    protected void renderRepeating(PoseStack param0, int param1, int param2, int param3, int param4, int param5, int param6, int param7, int param8) {
        for(int var0 = 0; var0 < param3; var0 += param7) {
            int var1 = param1 + var0;
            int var2 = Math.min(param7, param3 - var0);

            for(int var3 = 0; var3 < param4; var3 += param8) {
                int var4 = param2 + var3;
                int var5 = Math.min(param8, param4 - var3);
                this.blit(param0, var1, var4, param5, param6, var2, var5);
            }
        }

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
        if (this.parent == null && this.advancement.getParent() != null) {
            this.parent = this.getFirstVisibleParent(this.advancement);
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
