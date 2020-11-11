package net.minecraft.client.gui.components;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Map;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBossEventPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.BossEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BossHealthOverlay extends GuiComponent {
    private static final ResourceLocation GUI_BARS_LOCATION = new ResourceLocation("textures/gui/bars.png");
    private final Minecraft minecraft;
    private final Map<UUID, LerpingBossEvent> events = Maps.newLinkedHashMap();

    public BossHealthOverlay(Minecraft param0) {
        this.minecraft = param0;
    }

    public void render(PoseStack param0) {
        if (!this.events.isEmpty()) {
            int var0 = this.minecraft.getWindow().getGuiScaledWidth();
            int var1 = 12;

            for(LerpingBossEvent var2 : this.events.values()) {
                int var3 = var0 / 2 - 91;
                RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
                this.minecraft.getTextureManager().bind(GUI_BARS_LOCATION);
                this.drawBar(param0, var3, var1, var2);
                Component var5 = var2.getName();
                int var6 = this.minecraft.font.width(var5);
                int var7 = var0 / 2 - var6 / 2;
                int var8 = var1 - 9;
                this.minecraft.font.drawShadow(param0, var5, (float)var7, (float)var8, 16777215);
                var1 += 10 + 9;
                if (var1 >= this.minecraft.getWindow().getGuiScaledHeight() / 3) {
                    break;
                }
            }

        }
    }

    private void drawBar(PoseStack param0, int param1, int param2, BossEvent param3) {
        this.blit(param0, param1, param2, 0, param3.getColor().ordinal() * 5 * 2, 182, 5);
        if (param3.getOverlay() != BossEvent.BossBarOverlay.PROGRESS) {
            this.blit(param0, param1, param2, 0, 80 + (param3.getOverlay().ordinal() - 1) * 5 * 2, 182, 5);
        }

        int var0 = (int)(param3.getProgress() * 183.0F);
        if (var0 > 0) {
            this.blit(param0, param1, param2, 0, param3.getColor().ordinal() * 5 * 2 + 5, var0, 5);
            if (param3.getOverlay() != BossEvent.BossBarOverlay.PROGRESS) {
                this.blit(param0, param1, param2, 0, 80 + (param3.getOverlay().ordinal() - 1) * 5 * 2 + 5, var0, 5);
            }
        }

    }

    public void update(ClientboundBossEventPacket param0) {
        if (param0.getOperation() == ClientboundBossEventPacket.Operation.ADD) {
            this.events.put(param0.getId(), new LerpingBossEvent(param0));
        } else if (param0.getOperation() == ClientboundBossEventPacket.Operation.REMOVE) {
            this.events.remove(param0.getId());
        } else {
            this.events.get(param0.getId()).update(param0);
        }

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
