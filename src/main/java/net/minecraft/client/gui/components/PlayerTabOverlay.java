package net.minecraft.client.gui.components;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.Optionull;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.level.GameType;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PlayerTabOverlay {
    private static final ResourceLocation PING_UNKNOWN_SPRITE = new ResourceLocation("icon/ping_unknown");
    private static final ResourceLocation PING_1_SPRITE = new ResourceLocation("icon/ping_1");
    private static final ResourceLocation PING_2_SPRITE = new ResourceLocation("icon/ping_2");
    private static final ResourceLocation PING_3_SPRITE = new ResourceLocation("icon/ping_3");
    private static final ResourceLocation PING_4_SPRITE = new ResourceLocation("icon/ping_4");
    private static final ResourceLocation PING_5_SPRITE = new ResourceLocation("icon/ping_5");
    private static final ResourceLocation HEART_CONTAINER_BLINKING_SPRITE = new ResourceLocation("hud/heart/container_blinking");
    private static final ResourceLocation HEART_CONTAINER_SPRITE = new ResourceLocation("hud/heart/container");
    private static final ResourceLocation HEART_FULL_BLINKING_SPRITE = new ResourceLocation("hud/heart/full_blinking");
    private static final ResourceLocation HEART_HALF_BLINKING_SPRITE = new ResourceLocation("hud/heart/half_blinking");
    private static final ResourceLocation HEART_ABSORBING_FULL_BLINKING_SPRITE = new ResourceLocation("hud/heart/absorbing_full_blinking");
    private static final ResourceLocation HEART_FULL_SPRITE = new ResourceLocation("hud/heart/full");
    private static final ResourceLocation HEART_ABSORBING_HALF_BLINKING_SPRITE = new ResourceLocation("hud/heart/absorbing_half_blinking");
    private static final ResourceLocation HEART_HALF_SPRITE = new ResourceLocation("hud/heart/half");
    private static final Comparator<PlayerInfo> PLAYER_COMPARATOR = Comparator.<PlayerInfo>comparingInt(
            param0 -> param0.getGameMode() == GameType.SPECTATOR ? 1 : 0
        )
        .thenComparing(param0 -> Optionull.mapOrDefault(param0.getTeam(), PlayerTeam::getName, ""))
        .thenComparing(param0 -> param0.getProfile().getName(), String::compareToIgnoreCase);
    public static final int MAX_ROWS_PER_COL = 20;
    private final Minecraft minecraft;
    private final Gui gui;
    @Nullable
    private Component footer;
    @Nullable
    private Component header;
    private boolean visible;
    private final Map<UUID, PlayerTabOverlay.HealthState> healthStates = new Object2ObjectOpenHashMap<>();

    public PlayerTabOverlay(Minecraft param0, Gui param1) {
        this.minecraft = param0;
        this.gui = param1;
    }

    public Component getNameForDisplay(PlayerInfo param0) {
        return param0.getTabListDisplayName() != null
            ? this.decorateName(param0, param0.getTabListDisplayName().copy())
            : this.decorateName(param0, PlayerTeam.formatNameForTeam(param0.getTeam(), Component.literal(param0.getProfile().getName())));
    }

    private Component decorateName(PlayerInfo param0, MutableComponent param1) {
        return param0.getGameMode() == GameType.SPECTATOR ? param1.withStyle(ChatFormatting.ITALIC) : param1;
    }

    public void setVisible(boolean param0) {
        if (this.visible != param0) {
            this.healthStates.clear();
            this.visible = param0;
            if (param0) {
                Component var0 = ComponentUtils.formatList(this.getPlayerInfos(), Component.literal(", "), this::getNameForDisplay);
                this.minecraft.getNarrator().sayNow(Component.translatable("multiplayer.player.list.narration", var0));
            }
        }

    }

    private List<PlayerInfo> getPlayerInfos() {
        return this.minecraft.player.connection.getListedOnlinePlayers().stream().sorted(PLAYER_COMPARATOR).limit(80L).toList();
    }

    public void render(GuiGraphics param0, int param1, Scoreboard param2, @Nullable Objective param3) {
        List<PlayerInfo> var0 = this.getPlayerInfos();
        int var1 = 0;
        int var2 = 0;

        for(PlayerInfo var3 : var0) {
            int var4 = this.minecraft.font.width(this.getNameForDisplay(var3));
            var1 = Math.max(var1, var4);
            if (param3 != null && param3.getRenderType() != ObjectiveCriteria.RenderType.HEARTS) {
                var4 = this.minecraft.font.width(" " + param2.getOrCreatePlayerScore(var3.getProfile().getName(), param3).getScore());
                var2 = Math.max(var2, var4);
            }
        }

        if (!this.healthStates.isEmpty()) {
            Set<UUID> var5 = var0.stream().map(param0x -> param0x.getProfile().getId()).collect(Collectors.toSet());
            this.healthStates.keySet().removeIf(param1x -> !var5.contains(param1x));
        }

        int var6 = var0.size();
        int var7 = var6;

        int var8;
        for(var8 = 1; var7 > 20; var7 = (var6 + var8 - 1) / var8) {
            ++var8;
        }

        boolean var9 = this.minecraft.isLocalServer() || this.minecraft.getConnection().getConnection().isEncrypted();
        int var10;
        if (param3 != null) {
            if (param3.getRenderType() == ObjectiveCriteria.RenderType.HEARTS) {
                var10 = 90;
            } else {
                var10 = var2;
            }
        } else {
            var10 = 0;
        }

        int var13 = Math.min(var8 * ((var9 ? 9 : 0) + var1 + var10 + 13), param1 - 50) / var8;
        int var14 = param1 / 2 - (var13 * var8 + (var8 - 1) * 5) / 2;
        int var15 = 10;
        int var16 = var13 * var8 + (var8 - 1) * 5;
        List<FormattedCharSequence> var17 = null;
        if (this.header != null) {
            var17 = this.minecraft.font.split(this.header, param1 - 50);

            for(FormattedCharSequence var18 : var17) {
                var16 = Math.max(var16, this.minecraft.font.width(var18));
            }
        }

        List<FormattedCharSequence> var19 = null;
        if (this.footer != null) {
            var19 = this.minecraft.font.split(this.footer, param1 - 50);

            for(FormattedCharSequence var20 : var19) {
                var16 = Math.max(var16, this.minecraft.font.width(var20));
            }
        }

        if (var17 != null) {
            param0.fill(param1 / 2 - var16 / 2 - 1, var15 - 1, param1 / 2 + var16 / 2 + 1, var15 + var17.size() * 9, Integer.MIN_VALUE);

            for(FormattedCharSequence var21 : var17) {
                int var22 = this.minecraft.font.width(var21);
                param0.drawString(this.minecraft.font, var21, param1 / 2 - var22 / 2, var15, -1);
                var15 += 9;
            }

            ++var15;
        }

        param0.fill(param1 / 2 - var16 / 2 - 1, var15 - 1, param1 / 2 + var16 / 2 + 1, var15 + var7 * 9, Integer.MIN_VALUE);
        int var23 = this.minecraft.options.getBackgroundColor(553648127);

        for(int var24 = 0; var24 < var6; ++var24) {
            int var25 = var24 / var7;
            int var26 = var24 % var7;
            int var27 = var14 + var25 * var13 + var25 * 5;
            int var28 = var15 + var26 * 9;
            param0.fill(var27, var28, var27 + var13, var28 + 8, var23);
            RenderSystem.enableBlend();
            if (var24 < var0.size()) {
                PlayerInfo var29 = var0.get(var24);
                GameProfile var30 = var29.getProfile();
                if (var9) {
                    Player var31 = this.minecraft.level.getPlayerByUUID(var30.getId());
                    boolean var32 = var31 != null && LivingEntityRenderer.isEntityUpsideDown(var31);
                    boolean var33 = var31 != null && var31.isModelPartShown(PlayerModelPart.HAT);
                    PlayerFaceRenderer.draw(param0, var29.getSkin().texture(), var27, var28, 8, var33, var32);
                    var27 += 9;
                }

                param0.drawString(
                    this.minecraft.font, this.getNameForDisplay(var29), var27, var28, var29.getGameMode() == GameType.SPECTATOR ? -1862270977 : -1
                );
                if (param3 != null && var29.getGameMode() != GameType.SPECTATOR) {
                    int var34 = var27 + var1 + 1;
                    int var35 = var34 + var10;
                    if (var35 - var34 > 5) {
                        this.renderTablistScore(param3, var28, var30.getName(), var34, var35, var30.getId(), param0);
                    }
                }

                this.renderPingIcon(param0, var13, var27 - (var9 ? 9 : 0), var28, var29);
            }
        }

        if (var19 != null) {
            var15 += var7 * 9 + 1;
            param0.fill(param1 / 2 - var16 / 2 - 1, var15 - 1, param1 / 2 + var16 / 2 + 1, var15 + var19.size() * 9, Integer.MIN_VALUE);

            for(FormattedCharSequence var36 : var19) {
                int var37 = this.minecraft.font.width(var36);
                param0.drawString(this.minecraft.font, var36, param1 / 2 - var37 / 2, var15, -1);
                var15 += 9;
            }
        }

    }

    protected void renderPingIcon(GuiGraphics param0, int param1, int param2, int param3, PlayerInfo param4) {
        ResourceLocation var0;
        if (param4.getLatency() < 0) {
            var0 = PING_UNKNOWN_SPRITE;
        } else if (param4.getLatency() < 150) {
            var0 = PING_5_SPRITE;
        } else if (param4.getLatency() < 300) {
            var0 = PING_4_SPRITE;
        } else if (param4.getLatency() < 600) {
            var0 = PING_3_SPRITE;
        } else if (param4.getLatency() < 1000) {
            var0 = PING_2_SPRITE;
        } else {
            var0 = PING_1_SPRITE;
        }

        param0.pose().pushPose();
        param0.pose().translate(0.0F, 0.0F, 100.0F);
        param0.blitSprite(var0, param2 + param1 - 11, param3, 10, 8);
        param0.pose().popPose();
    }

    private void renderTablistScore(Objective param0, int param1, String param2, int param3, int param4, UUID param5, GuiGraphics param6) {
        int var0 = param0.getScoreboard().getOrCreatePlayerScore(param2, param0).getScore();
        if (param0.getRenderType() == ObjectiveCriteria.RenderType.HEARTS) {
            this.renderTablistHearts(param1, param3, param4, param5, param6, var0);
        } else {
            String var1 = "" + ChatFormatting.YELLOW + var0;
            param6.drawString(this.minecraft.font, var1, param4 - this.minecraft.font.width(var1), param1, 16777215);
        }
    }

    private void renderTablistHearts(int param0, int param1, int param2, UUID param3, GuiGraphics param4, int param5) {
        PlayerTabOverlay.HealthState var0 = this.healthStates.computeIfAbsent(param3, param1x -> new PlayerTabOverlay.HealthState(param5));
        var0.update(param5, (long)this.gui.getGuiTicks());
        int var1 = Mth.positiveCeilDiv(Math.max(param5, var0.displayedValue()), 2);
        int var2 = Math.max(param5, Math.max(var0.displayedValue(), 20)) / 2;
        boolean var3 = var0.isBlinking((long)this.gui.getGuiTicks());
        if (var1 > 0) {
            int var4 = Mth.floor(Math.min((float)(param2 - param1 - 4) / (float)var2, 9.0F));
            if (var4 <= 3) {
                float var5 = Mth.clamp((float)param5 / 20.0F, 0.0F, 1.0F);
                int var6 = (int)((1.0F - var5) * 255.0F) << 16 | (int)(var5 * 255.0F) << 8;
                float var7 = (float)param5 / 2.0F;
                Component var8 = Component.translatable("multiplayer.player.list.hp", var7);
                Component var9;
                if (param2 - this.minecraft.font.width(var8) >= param1) {
                    var9 = var8;
                } else {
                    var9 = Component.literal(var7 + "");
                }

                param4.drawString(this.minecraft.font, var9, (param2 + param1 - this.minecraft.font.width(var9)) / 2, param0, var6);
            } else {
                ResourceLocation var11 = var3 ? HEART_CONTAINER_BLINKING_SPRITE : HEART_CONTAINER_SPRITE;

                for(int var12 = var1; var12 < var2; ++var12) {
                    param4.blitSprite(var11, param1 + var12 * var4, param0, 9, 9);
                }

                for(int var13 = 0; var13 < var1; ++var13) {
                    param4.blitSprite(var11, param1 + var13 * var4, param0, 9, 9);
                    if (var3) {
                        if (var13 * 2 + 1 < var0.displayedValue()) {
                            param4.blitSprite(HEART_FULL_BLINKING_SPRITE, param1 + var13 * var4, param0, 9, 9);
                        }

                        if (var13 * 2 + 1 == var0.displayedValue()) {
                            param4.blitSprite(HEART_HALF_BLINKING_SPRITE, param1 + var13 * var4, param0, 9, 9);
                        }
                    }

                    if (var13 * 2 + 1 < param5) {
                        param4.blitSprite(var13 >= 10 ? HEART_ABSORBING_FULL_BLINKING_SPRITE : HEART_FULL_SPRITE, param1 + var13 * var4, param0, 9, 9);
                    }

                    if (var13 * 2 + 1 == param5) {
                        param4.blitSprite(var13 >= 10 ? HEART_ABSORBING_HALF_BLINKING_SPRITE : HEART_HALF_SPRITE, param1 + var13 * var4, param0, 9, 9);
                    }
                }

            }
        }
    }

    public void setFooter(@Nullable Component param0) {
        this.footer = param0;
    }

    public void setHeader(@Nullable Component param0) {
        this.header = param0;
    }

    public void reset() {
        this.header = null;
        this.footer = null;
    }

    @OnlyIn(Dist.CLIENT)
    static class HealthState {
        private static final long DISPLAY_UPDATE_DELAY = 20L;
        private static final long DECREASE_BLINK_DURATION = 20L;
        private static final long INCREASE_BLINK_DURATION = 10L;
        private int lastValue;
        private int displayedValue;
        private long lastUpdateTick;
        private long blinkUntilTick;

        public HealthState(int param0) {
            this.displayedValue = param0;
            this.lastValue = param0;
        }

        public void update(int param0, long param1) {
            if (param0 != this.lastValue) {
                long var0 = param0 < this.lastValue ? 20L : 10L;
                this.blinkUntilTick = param1 + var0;
                this.lastValue = param0;
                this.lastUpdateTick = param1;
            }

            if (param1 - this.lastUpdateTick > 20L) {
                this.displayedValue = param0;
            }

        }

        public int displayedValue() {
            return this.displayedValue;
        }

        public boolean isBlinking(long param0) {
            return this.blinkUntilTick > param0 && (this.blinkUntilTick - param0) % 6L >= 3L;
        }
    }
}
