package net.minecraft.client.gui.screens.advancements;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientAdvancements;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundSeenAdvancementsPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class AdvancementsScreen extends Screen implements ClientAdvancements.Listener {
    private static final ResourceLocation WINDOW_LOCATION = new ResourceLocation("textures/gui/advancements/window.png");
    public static final ResourceLocation TABS_LOCATION = new ResourceLocation("textures/gui/advancements/tabs.png");
    public static final int WINDOW_WIDTH = 252;
    public static final int WINDOW_HEIGHT = 140;
    private static final int WINDOW_INSIDE_X = 9;
    private static final int WINDOW_INSIDE_Y = 18;
    public static final int WINDOW_INSIDE_WIDTH = 234;
    public static final int WINDOW_INSIDE_HEIGHT = 113;
    private static final int WINDOW_TITLE_X = 8;
    private static final int WINDOW_TITLE_Y = 6;
    public static final int BACKGROUND_TILE_WIDTH = 16;
    public static final int BACKGROUND_TILE_HEIGHT = 16;
    public static final int BACKGROUND_TILE_COUNT_X = 14;
    public static final int BACKGROUND_TILE_COUNT_Y = 7;
    private static final Component VERY_SAD_LABEL = Component.translatable("advancements.sad_label");
    private static final Component NO_ADVANCEMENTS_LABEL = Component.translatable("advancements.empty");
    private static final Component TITLE = Component.translatable("gui.advancements");
    private final ClientAdvancements advancements;
    private final Map<Advancement, AdvancementTab> tabs = Maps.newLinkedHashMap();
    @Nullable
    private AdvancementTab selectedTab;
    private boolean isScrolling;

    public AdvancementsScreen(ClientAdvancements param0) {
        super(GameNarrator.NO_TITLE);
        this.advancements = param0;
    }

    @Override
    protected void init() {
        this.tabs.clear();
        this.selectedTab = null;
        this.advancements.setListener(this);
        if (this.selectedTab == null && !this.tabs.isEmpty()) {
            this.advancements.setSelectedTab(this.tabs.values().iterator().next().getAdvancement(), true);
        } else {
            this.advancements.setSelectedTab(this.selectedTab == null ? null : this.selectedTab.getAdvancement(), true);
        }

    }

    @Override
    public void removed() {
        this.advancements.setListener(null);
        ClientPacketListener var0 = this.minecraft.getConnection();
        if (var0 != null) {
            var0.send(ServerboundSeenAdvancementsPacket.closedScreen());
        }

    }

    @Override
    public boolean mouseClicked(double param0, double param1, int param2) {
        if (param2 == 0) {
            int var0 = (this.width - 252) / 2;
            int var1 = (this.height - 140) / 2;

            for(AdvancementTab var2 : this.tabs.values()) {
                if (var2.isMouseOver(var0, var1, param0, param1)) {
                    this.advancements.setSelectedTab(var2.getAdvancement(), true);
                    break;
                }
            }
        }

        return super.mouseClicked(param0, param1, param2);
    }

    @Override
    public boolean keyPressed(int param0, int param1, int param2) {
        if (this.minecraft.options.keyAdvancements.matches(param0, param1)) {
            this.minecraft.setScreen(null);
            this.minecraft.mouseHandler.grabMouse();
            return true;
        } else {
            return super.keyPressed(param0, param1, param2);
        }
    }

    @Override
    public void render(GuiGraphics param0, int param1, int param2, float param3) {
        int var0 = (this.width - 252) / 2;
        int var1 = (this.height - 140) / 2;
        this.renderBackground(param0);
        this.renderInside(param0, param1, param2, var0, var1);
        this.renderWindow(param0, var0, var1);
        this.renderTooltips(param0, param1, param2, var0, var1);
    }

    @Override
    public boolean mouseDragged(double param0, double param1, int param2, double param3, double param4) {
        if (param2 != 0) {
            this.isScrolling = false;
            return false;
        } else {
            if (!this.isScrolling) {
                this.isScrolling = true;
            } else if (this.selectedTab != null) {
                this.selectedTab.scroll(param3, param4);
            }

            return true;
        }
    }

    private void renderInside(GuiGraphics param0, int param1, int param2, int param3, int param4) {
        AdvancementTab var0 = this.selectedTab;
        if (var0 == null) {
            param0.fill(param3 + 9, param4 + 18, param3 + 9 + 234, param4 + 18 + 113, -16777216);
            int var1 = param3 + 9 + 117;
            param0.drawCenteredString(this.font, NO_ADVANCEMENTS_LABEL, var1, param4 + 18 + 56 - 9 / 2, -1);
            param0.drawCenteredString(this.font, VERY_SAD_LABEL, var1, param4 + 18 + 113 - 9, -1);
        } else {
            var0.drawContents(param0, param3 + 9, param4 + 18);
        }
    }

    public void renderWindow(GuiGraphics param0, int param1, int param2) {
        RenderSystem.enableBlend();
        param0.blit(WINDOW_LOCATION, param1, param2, 0, 0, 252, 140);
        if (this.tabs.size() > 1) {
            for(AdvancementTab var0 : this.tabs.values()) {
                var0.drawTab(param0, param1, param2, var0 == this.selectedTab);
            }

            for(AdvancementTab var1 : this.tabs.values()) {
                var1.drawIcon(param0, param1, param2);
            }
        }

        param0.drawString(this.font, TITLE, param1 + 8, param2 + 6, 4210752, false);
    }

    private void renderTooltips(GuiGraphics param0, int param1, int param2, int param3, int param4) {
        if (this.selectedTab != null) {
            param0.pose().pushPose();
            param0.pose().translate((float)(param3 + 9), (float)(param4 + 18), 400.0F);
            RenderSystem.enableDepthTest();
            this.selectedTab.drawTooltips(param0, param1 - param3 - 9, param2 - param4 - 18, param3, param4);
            RenderSystem.disableDepthTest();
            param0.pose().popPose();
        }

        if (this.tabs.size() > 1) {
            for(AdvancementTab var0 : this.tabs.values()) {
                if (var0.isMouseOver(param3, param4, (double)param1, (double)param2)) {
                    param0.renderTooltip(this.font, var0.getTitle(), param1, param2);
                }
            }
        }

    }

    @Override
    public void onAddAdvancementRoot(Advancement param0) {
        AdvancementTab var0 = AdvancementTab.create(this.minecraft, this, this.tabs.size(), param0);
        if (var0 != null) {
            this.tabs.put(param0, var0);
        }
    }

    @Override
    public void onRemoveAdvancementRoot(Advancement param0) {
    }

    @Override
    public void onAddAdvancementTask(Advancement param0) {
        AdvancementTab var0 = this.getTab(param0);
        if (var0 != null) {
            var0.addAdvancement(param0);
        }

    }

    @Override
    public void onRemoveAdvancementTask(Advancement param0) {
    }

    @Override
    public void onUpdateAdvancementProgress(Advancement param0, AdvancementProgress param1) {
        AdvancementWidget var0 = this.getAdvancementWidget(param0);
        if (var0 != null) {
            var0.setProgress(param1);
        }

    }

    @Override
    public void onSelectedTabChanged(@Nullable Advancement param0) {
        this.selectedTab = this.tabs.get(param0);
    }

    @Override
    public void onAdvancementsCleared() {
        this.tabs.clear();
        this.selectedTab = null;
    }

    @Nullable
    public AdvancementWidget getAdvancementWidget(Advancement param0) {
        AdvancementTab var0 = this.getTab(param0);
        return var0 == null ? null : var0.getWidget(param0);
    }

    @Nullable
    private AdvancementTab getTab(Advancement param0) {
        while(param0.getParent() != null) {
            param0 = param0.getParent();
        }

        return this.tabs.get(param0);
    }
}
