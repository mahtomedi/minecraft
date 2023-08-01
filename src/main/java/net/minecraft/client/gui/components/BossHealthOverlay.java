package net.minecraft.client.gui.components;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Map;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBossEventPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.BossEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BossHealthOverlay {
    private static final int BAR_WIDTH = 182;
    private static final int BAR_HEIGHT = 5;
    private static final ResourceLocation[] BAR_BACKGROUND_SPRITES = new ResourceLocation[]{
        new ResourceLocation("boss_bar/pink_background"),
        new ResourceLocation("boss_bar/blue_background"),
        new ResourceLocation("boss_bar/red_background"),
        new ResourceLocation("boss_bar/green_background"),
        new ResourceLocation("boss_bar/yellow_background"),
        new ResourceLocation("boss_bar/purple_background"),
        new ResourceLocation("boss_bar/white_background")
    };
    private static final ResourceLocation[] BAR_PROGRESS_SPRITES = new ResourceLocation[]{
        new ResourceLocation("boss_bar/pink_progress"),
        new ResourceLocation("boss_bar/blue_progress"),
        new ResourceLocation("boss_bar/red_progress"),
        new ResourceLocation("boss_bar/green_progress"),
        new ResourceLocation("boss_bar/yellow_progress"),
        new ResourceLocation("boss_bar/purple_progress"),
        new ResourceLocation("boss_bar/white_progress")
    };
    private static final ResourceLocation[] OVERLAY_BACKGROUND_SPRITES = new ResourceLocation[]{
        new ResourceLocation("boss_bar/notched_6_background"),
        new ResourceLocation("boss_bar/notched_10_background"),
        new ResourceLocation("boss_bar/notched_12_background"),
        new ResourceLocation("boss_bar/notched_20_background")
    };
    private static final ResourceLocation[] OVERLAY_PROGRESS_SPRITES = new ResourceLocation[]{
        new ResourceLocation("boss_bar/notched_6_progress"),
        new ResourceLocation("boss_bar/notched_10_progress"),
        new ResourceLocation("boss_bar/notched_12_progress"),
        new ResourceLocation("boss_bar/notched_20_progress")
    };
    private final Minecraft minecraft;
    final Map<UUID, LerpingBossEvent> events = Maps.newLinkedHashMap();

    public BossHealthOverlay(Minecraft param0) {
        this.minecraft = param0;
    }

    public void render(GuiGraphics param0) {
        if (!this.events.isEmpty()) {
            int var0 = param0.guiWidth();
            int var1 = 12;

            for(LerpingBossEvent var2 : this.events.values()) {
                int var3 = var0 / 2 - 91;
                this.drawBar(param0, var3, var1, var2);
                Component var5 = var2.getName();
                int var6 = this.minecraft.font.width(var5);
                int var7 = var0 / 2 - var6 / 2;
                int var8 = var1 - 9;
                param0.drawString(this.minecraft.font, var5, var7, var8, 16777215);
                var1 += 10 + 9;
                if (var1 >= param0.guiHeight() / 3) {
                    break;
                }
            }

        }
    }

    private void drawBar(GuiGraphics param0, int param1, int param2, BossEvent param3) {
        this.drawBar(param0, param1, param2, param3, 182, BAR_BACKGROUND_SPRITES, OVERLAY_BACKGROUND_SPRITES);
        int var0 = (int)(param3.getProgress() * 183.0F);
        if (var0 > 0) {
            this.drawBar(param0, param1, param2, param3, var0, BAR_PROGRESS_SPRITES, OVERLAY_PROGRESS_SPRITES);
        }

    }

    private void drawBar(GuiGraphics param0, int param1, int param2, BossEvent param3, int param4, ResourceLocation[] param5, ResourceLocation[] param6) {
        param0.blitSprite(param5[param3.getColor().ordinal()], 182, 5, 0, 0, param1, param2, param4, 5);
        if (param3.getOverlay() != BossEvent.BossBarOverlay.PROGRESS) {
            RenderSystem.enableBlend();
            param0.blitSprite(param6[param3.getOverlay().ordinal() - 1], param1, param2, param4, 5);
            RenderSystem.disableBlend();
        }

    }

    public void update(ClientboundBossEventPacket param0) {
        param0.dispatch(
            new ClientboundBossEventPacket.Handler() {
                @Override
                public void add(
                    UUID param0,
                    Component param1,
                    float param2,
                    BossEvent.BossBarColor param3,
                    BossEvent.BossBarOverlay param4,
                    boolean param5,
                    boolean param6,
                    boolean param7
                ) {
                    BossHealthOverlay.this.events.put(param0, new LerpingBossEvent(param0, param1, param2, param3, param4, param5, param6, param7));
                }
    
                @Override
                public void remove(UUID param0) {
                    BossHealthOverlay.this.events.remove(param0);
                }
    
                @Override
                public void updateProgress(UUID param0, float param1) {
                    BossHealthOverlay.this.events.get(param0).setProgress(param1);
                }
    
                @Override
                public void updateName(UUID param0, Component param1) {
                    BossHealthOverlay.this.events.get(param0).setName(param1);
                }
    
                @Override
                public void updateStyle(UUID param0, BossEvent.BossBarColor param1, BossEvent.BossBarOverlay param2) {
                    LerpingBossEvent var0 = BossHealthOverlay.this.events.get(param0);
                    var0.setColor(param1);
                    var0.setOverlay(param2);
                }
    
                @Override
                public void updateProperties(UUID param0, boolean param1, boolean param2, boolean param3) {
                    LerpingBossEvent var0 = BossHealthOverlay.this.events.get(param0);
                    var0.setDarkenScreen(param1);
                    var0.setPlayBossMusic(param2);
                    var0.setCreateWorldFog(param3);
                }
            }
        );
    }

    public void reset() {
        this.events.clear();
    }

    public boolean shouldPlayMusic() {
        if (!this.events.isEmpty()) {
            for(BossEvent var0 : this.events.values()) {
                if (var0.shouldPlayBossMusic()) {
                    return true;
                }
            }
        }

        return false;
    }

    public boolean shouldDarkenScreen() {
        if (!this.events.isEmpty()) {
            for(BossEvent var0 : this.events.values()) {
                if (var0.shouldDarkenScreen()) {
                    return true;
                }
            }
        }

        return false;
    }

    public boolean shouldCreateWorldFog() {
        if (!this.events.isEmpty()) {
            for(BossEvent var0 : this.events.values()) {
                if (var0.shouldCreateWorldFog()) {
                    return true;
                }
            }
        }

        return false;
    }
}
