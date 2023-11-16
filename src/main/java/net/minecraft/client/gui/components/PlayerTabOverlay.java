package net.minecraft.client.gui.components;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.ArrayList;
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
import net.minecraft.network.chat.numbers.NumberFormat;
import net.minecraft.network.chat.numbers.StyledFormat;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.level.GameType;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.ReadOnlyScoreInfo;
import net.minecraft.world.scores.ScoreHolder;
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
        List<PlayerTabOverlay.ScoreDisplayEntry> var1 = new ArrayList<>(var0.size());
        int var2 = this.minecraft.font.width(" ");
        int var3 = 0;
        int var4 = 0;

        for(PlayerInfo var5 : var0) {
            Component var6 = this.getNameForDisplay(var5);
            var3 = Math.max(var3, this.minecraft.font.width(var6));
            int var7 = 0;
            Component var8 = null;
            int var9 = 0;
            if (param3 != null) {
                ScoreHolder var10 = ScoreHolder.fromGameProfile(var5.getProfile());
                ReadOnlyScoreInfo var11 = param2.getPlayerScoreInfo(var10, param3);
                if (var11 != null) {
                    var7 = var11.value();
                }

                if (param3.getRenderType() != ObjectiveCriteria.RenderType.HEARTS) {
                    NumberFormat var12 = param3.numberFormatOrDefault(StyledFormat.PLAYER_LIST_DEFAULT);
                    var8 = ReadOnlyScoreInfo.safeFormatValue(var11, var12);
                    var9 = this.minecraft.font.width(var8);
                    var4 = Math.max(var4, var9 > 0 ? var2 + var9 : 0);
                }
            }

            var1.add(new PlayerTabOverlay.ScoreDisplayEntry(var6, var7, var8, var9));
        }

        if (!this.healthStates.isEmpty()) {
            Set<UUID> var13 = var0.stream().map(param0x -> param0x.getProfile().getId()).collect(Collectors.toSet());
            this.healthStates.keySet().removeIf(param1x -> !var13.contains(param1x));
        }

        int var14 = var0.size();
        int var15 = var14;

        int var16;
        for(var16 = 1; var15 > 20; var15 = (var14 + var16 - 1) / var16) {
            ++var16;
        }

        boolean var17 = this.minecraft.isLocalServer() || this.minecraft.getConnection().getConnection().isEncrypted();
        int var18;
        if (param3 != null) {
            if (param3.getRenderType() == ObjectiveCriteria.RenderType.HEARTS) {
                var18 = 90;
            } else {
                var18 = var4;
            }
        } else {
            var18 = 0;
        }

        int var21 = Math.min(var16 * ((var17 ? 9 : 0) + var3 + var18 + 13), param1 - 50) / var16;
        int var22 = param1 / 2 - (var21 * var16 + (var16 - 1) * 5) / 2;
        int var23 = 10;
        int var24 = var21 * var16 + (var16 - 1) * 5;
        List<FormattedCharSequence> var25 = null;
        if (this.header != null) {
            var25 = this.minecraft.font.split(this.header, param1 - 50);

            for(FormattedCharSequence var26 : var25) {
                var24 = Math.max(var24, this.minecraft.font.width(var26));
            }
        }

        List<FormattedCharSequence> var27 = null;
        if (this.footer != null) {
            var27 = this.minecraft.font.split(this.footer, param1 - 50);

            for(FormattedCharSequence var28 : var27) {
                var24 = Math.max(var24, this.minecraft.font.width(var28));
            }
        }

        if (var25 != null) {
            param0.fill(param1 / 2 - var24 / 2 - 1, var23 - 1, param1 / 2 + var24 / 2 + 1, var23 + var25.size() * 9, Integer.MIN_VALUE);

            for(FormattedCharSequence var29 : var25) {
                int var30 = this.minecraft.font.width(var29);
                param0.drawString(this.minecraft.font, var29, param1 / 2 - var30 / 2, var23, -1);
                var23 += 9;
            }

            ++var23;
        }

        param0.fill(param1 / 2 - var24 / 2 - 1, var23 - 1, param1 / 2 + var24 / 2 + 1, var23 + var15 * 9, Integer.MIN_VALUE);
        int var31 = this.minecraft.options.getBackgroundColor(553648127);

        for(int var32 = 0; var32 < var14; ++var32) {
            int var33 = var32 / var15;
            int var34 = var32 % var15;
            int var35 = var22 + var33 * var21 + var33 * 5;
            int var36 = var23 + var34 * 9;
            param0.fill(var35, var36, var35 + var21, var36 + 8, var31);
            RenderSystem.enableBlend();
            if (var32 < var0.size()) {
                PlayerInfo var37 = var0.get(var32);
                PlayerTabOverlay.ScoreDisplayEntry var38 = var1.get(var32);
                GameProfile var39 = var37.getProfile();
                if (var17) {
                    Player var40 = this.minecraft.level.getPlayerByUUID(var39.getId());
                    boolean var41 = var40 != null && LivingEntityRenderer.isEntityUpsideDown(var40);
                    boolean var42 = var40 != null && var40.isModelPartShown(PlayerModelPart.HAT);
                    PlayerFaceRenderer.draw(param0, var37.getSkin().texture(), var35, var36, 8, var42, var41);
                    var35 += 9;
                }

                param0.drawString(this.minecraft.font, var38.name, var35, var36, var37.getGameMode() == GameType.SPECTATOR ? -1862270977 : -1);
                if (param3 != null && var37.getGameMode() != GameType.SPECTATOR) {
                    int var43 = var35 + var3 + 1;
                    int var44 = var43 + var18;
                    if (var44 - var43 > 5) {
                        this.renderTablistScore(param3, var36, var38, var43, var44, var39.getId(), param0);
                    }
                }

                this.renderPingIcon(param0, var21, var35 - (var17 ? 9 : 0), var36, var37);
            }
        }

        if (var27 != null) {
            var23 += var15 * 9 + 1;
            param0.fill(param1 / 2 - var24 / 2 - 1, var23 - 1, param1 / 2 + var24 / 2 + 1, var23 + var27.size() * 9, Integer.MIN_VALUE);

            for(FormattedCharSequence var45 : var27) {
                int var46 = this.minecraft.font.width(var45);
                param0.drawString(this.minecraft.font, var45, param1 / 2 - var46 / 2, var23, -1);
                var23 += 9;
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

    private void renderTablistScore(
        Objective param0, int param1, PlayerTabOverlay.ScoreDisplayEntry param2, int param3, int param4, UUID param5, GuiGraphics param6
    ) {
        if (param0.getRenderType() == ObjectiveCriteria.RenderType.HEARTS) {
            this.renderTablistHearts(param1, param3, param4, param5, param6, param2.score);
        } else if (param2.formattedScore != null) {
            param6.drawString(this.minecraft.font, param2.formattedScore, param4 - param2.scoreWidth, param1, 16777215);
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
                    var9 = Component.literal(Float.toString(var7));
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

    @OnlyIn(Dist.CLIENT)
    static record ScoreDisplayEntry(Component name, int score, @Nullable Component formattedScore, int scoreWidth) {
    }
}
