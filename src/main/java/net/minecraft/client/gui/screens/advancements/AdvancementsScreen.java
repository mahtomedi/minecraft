package net.minecraft.client.gui.screens.advancements;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientAdvancements;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.protocol.game.ServerboundSeenAdvancementsPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class AdvancementsScreen extends Screen implements ClientAdvancements.Listener {
    private static final ResourceLocation WINDOW_LOCATION = new ResourceLocation("textures/gui/advancements/window.png");
    private static final ResourceLocation TABS_LOCATION = new ResourceLocation("textures/gui/advancements/tabs.png");
    private final ClientAdvancements advancements;
    private final Map<Advancement, AdvancementTab> tabs = Maps.newLinkedHashMap();
    private AdvancementTab selectedTab;
    private boolean isScrolling;

    public AdvancementsScreen(ClientAdvancements param0) {
        super(NarratorChatListener.NO_TITLE);
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
    public void render(int param0, int param1, float param2) {
        int var0 = (this.width - 252) / 2;
        int var1 = (this.height - 140) / 2;
        this.renderBackground();
        this.renderInside(param0, param1, var0, var1);
        this.renderWindow(var0, var1);
        this.renderTooltips(param0, param1, var0, var1);
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

    private void renderInside(int param0, int param1, int param2, int param3) {
        AdvancementTab var0 = this.selectedTab;
        if (var0 == null) {
            fill(param2 + 9, param3 + 18, param2 + 9 + 234, param3 + 18 + 113, -16777216);
            String var1 = I18n.get("advancements.empty");
            int var2 = this.font.width(var1);
            this.font.draw(var1, (float)(param2 + 9 + 117 - var2 / 2), (float)(param3 + 18 + 56 - 9 / 2), -1);
            this.font.draw(":(", (float)(param2 + 9 + 117 - this.font.width(":(") / 2), (float)(param3 + 18 + 113 - 9), -1);
        } else {
            RenderSystem.pushMatrix();
            RenderSystem.translatef((float)(param2 + 9), (float)(param3 + 18), -400.0F);
            RenderSystem.enableDepthTest();
            var0.drawContents();
            RenderSystem.popMatrix();
            RenderSystem.depthFunc(515);
            RenderSystem.disableDepthTest();
        }
    }

    public void renderWindow(int param0, int param1) {
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.enableBlend();
        Lighting.turnOff();
        this.minecraft.getTextureManager().bind(WINDOW_LOCATION);
        this.blit(param0, param1, 0, 0, 252, 140);
        if (this.tabs.size() > 1) {
            this.minecraft.getTextureManager().bind(TABS_LOCATION);

            for(AdvancementTab var0 : this.tabs.values()) {
                var0.drawTab(param0, param1, var0 == this.selectedTab);
            }

            RenderSystem.enableRescaleNormal();
            RenderSystem.defaultBlendFunc();
            Lighting.turnOnGui();

            for(AdvancementTab var1 : this.tabs.values()) {
                var1.drawIcon(param0, param1, this.itemRenderer);
            }

            RenderSystem.disableBlend();
        }

        this.font.draw(I18n.get("gui.advancements"), (float)(param0 + 8), (float)(param1 + 6), 4210752);
    }

    private void renderTooltips(int param0, int param1, int param2, int param3) {
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        if (this.selectedTab != null) {
            RenderSystem.pushMatrix();
            RenderSystem.enableDepthTest();
            RenderSystem.translatef((float)(param2 + 9), (float)(param3 + 18), 400.0F);
            this.selectedTab.drawTooltips(param0 - param2 - 9, param1 - param3 - 18, param2, param3);
            RenderSystem.disableDepthTest();
            RenderSystem.popMatrix();
        }

        if (this.tabs.size() > 1) {
            for(AdvancementTab var0 : this.tabs.values()) {
                if (var0.isMouseOver(param2, param3, (double)param0, (double)param1)) {
                    this.renderTooltip(var0.getTitle(), param0, param1);
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
