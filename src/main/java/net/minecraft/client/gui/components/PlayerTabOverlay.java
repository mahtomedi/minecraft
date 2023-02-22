package net.minecraft.client.gui.components;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
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
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.MutableComponent;
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
public class PlayerTabOverlay extends GuiComponent {
    private static final Comparator<PlayerInfo> PLAYER_COMPARATOR = Comparator.<PlayerInfo>comparingInt(
            param0 -> param0.getGameMode() == GameType.SPECTATOR ? 1 : 0
        )
        .thenComparing(param0 -> Optionull.mapOrDefault(param0.getTeam(), PlayerTeam::getName, ""))
        .thenComparing(param0 -> param0.getProfile().getName(), String::compareToIgnoreCase);
    public static final int MAX_ROWS_PER_COL = 20;
    public static final int HEART_EMPTY_CONTAINER = 16;
    public static final int HEART_EMPTY_CONTAINER_BLINKING = 25;
    public static final int HEART_FULL = 52;
    public static final int HEART_HALF_FULL = 61;
    public static final int HEART_GOLDEN_FULL = 160;
    public static final int HEART_GOLDEN_HALF_FULL = 169;
    public static final int HEART_GHOST_FULL = 70;
    public static final int HEART_GHOST_HALF_FULL = 79;
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

    public void render(PoseStack param0, int param1, Scoreboard param2, @Nullable Objective param3) {
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
            fill(param0, param1 / 2 - var16 / 2 - 1, var15 - 1, param1 / 2 + var16 / 2 + 1, var15 + var17.size() * 9, Integer.MIN_VALUE);

            for(FormattedCharSequence var21 : var17) {
                int var22 = this.minecraft.font.width(var21);
                this.minecraft.font.drawShadow(param0, var21, (float)(param1 / 2 - var22 / 2), (float)var15, -1);
                var15 += 9;
            }

            ++var15;
        }

        fill(param0, param1 / 2 - var16 / 2 - 1, var15 - 1, param1 / 2 + var16 / 2 + 1, var15 + var7 * 9, Integer.MIN_VALUE);
        int var23 = this.minecraft.options.getBackgroundColor(553648127);

        for(int var24 = 0; var24 < var6; ++var24) {
            int var25 = var24 / var7;
            int var26 = var24 % var7;
            int var27 = var14 + var25 * var13 + var25 * 5;
            int var28 = var15 + var26 * 9;
            fill(param0, var27, var28, var27 + var13, var28 + 8, var23);
            RenderSystem.enableBlend();
            if (var24 < var0.size()) {
                PlayerInfo var29 = var0.get(var24);
                GameProfile var30 = var29.getProfile();
                if (var9) {
                    Player var31 = this.minecraft.level.getPlayerByUUID(var30.getId());
                    boolean var32 = var31 != null && LivingEntityRenderer.isEntityUpsideDown(var31);
                    boolean var33 = var31 != null && var31.isModelPartShown(PlayerModelPart.HAT);
                    RenderSystem.setShaderTexture(0, var29.getSkinLocation());
                    PlayerFaceRenderer.draw(param0, var27, var28, 8, var33, var32);
                    var27 += 9;
                }

                this.minecraft
                    .font
                    .drawShadow(param0, this.getNameForDisplay(var29), (float)var27, (float)var28, var29.getGameMode() == GameType.SPECTATOR ? -1862270977 : -1);
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
            fill(param0, param1 / 2 - var16 / 2 - 1, var15 - 1, param1 / 2 + var16 / 2 + 1, var15 + var19.size() * 9, Integer.MIN_VALUE);

            for(FormattedCharSequence var36 : var19) {
                int var37 = this.minecraft.font.width(var36);
                this.minecraft.font.drawShadow(param0, var36, (float)(param1 / 2 - var37 / 2), (float)var15, -1);
                var15 += 9;
            }
        }

    }

    protected void renderPingIcon(PoseStack param0, int param1, int param2, int param3, PlayerInfo param4) {
        RenderSystem.setShaderTexture(0, GUI_ICONS_LOCATION);
        int var0 = 0;
        int var1;
        if (param4.getLatency() < 0) {
            var1 = 5;
        } else if (param4.getLatency() < 150) {
            var1 = 0;
        } else if (param4.getLatency() < 300) {
            var1 = 1;
        } else if (param4.getLatency() < 600) {
            var1 = 2;
        } else if (param4.getLatency() < 1000) {
            var1 = 3;
        } else {
            var1 = 4;
        }

        param0.pushPose();
        param0.translate(0.0F, 0.0F, 100.0F);
        blit(param0, param2 + param1 - 11, param3, 0, 176 + var1 * 8, 10, 8);
        param0.popPose();
    }

    private void renderTablistScore(Objective param0, int param1, String param2, int param3, int param4, UUID param5, PoseStack param6) {
        int var0 = param0.getScoreboard().getOrCreatePlayerScore(param2, param0).getScore();
        if (param0.getRenderType() == ObjectiveCriteria.RenderType.HEARTS) {
            this.renderTablistHearts(param1, param3, param4, param5, param6, var0);
        } else {
            String var1 = "" + ChatFormatting.YELLOW + var0;
            this.minecraft.font.drawShadow(param6, var1, (float)(param4 - this.minecraft.font.width(var1)), (float)param1, 16777215);
        }
    }

    private void renderTablistHearts(int param0, int param1, int param2, UUID param3, PoseStack param4, int param5) {
        PlayerTabOverlay.HealthState var0 = this.healthStates.computeIfAbsent(param3, param1x -> new PlayerTabOverlay.HealthState(param5));
        var0.update(param5, (long)this.gui.getGuiTicks());
        int var1 = Mth.positiveCeilDiv(Math.max(param5, var0.displayedValue()), 2);
        int var2 = Math.max(param5, Math.max(var0.displayedValue(), 20)) / 2;
        boolean var3 = var0.isBlinking((long)this.gui.getGuiTicks());
        if (var1 > 0) {
            RenderSystem.setShaderTexture(0, GUI_ICONS_LOCATION);
            int var4 = Mth.floor(Math.min((float)(param2 - param1 - 4) / (float)var2, 9.0F));
            if (var4 <= 3) {
                float var5 = Mth.clamp((float)param5 / 20.0F, 0.0F, 1.0F);
                int var6 = (int)((1.0F - var5) * 255.0F) << 16 | (int)(var5 * 255.0F) << 8;
                String var7 = (float)param5 / 2.0F + "";
                if (param2 - this.minecraft.font.width(var7 + "hp") >= param1) {
                    var7 = var7 + "hp";
                }

                this.minecraft.font.drawShadow(param4, var7, (float)((param2 + param1 - this.minecraft.font.width(var7)) / 2), (float)param0, var6);
            } else {
                for(int var8 = var1; var8 < var2; ++var8) {
                    blit(param4, param1 + var8 * var4, param0, var3 ? 25 : 16, 0, 9, 9);
                }

                for(int var9 = 0; var9 < var1; ++var9) {
                    blit(param4, param1 + var9 * var4, param0, var3 ? 25 : 16, 0, 9, 9);
                    if (var3) {
                        if (var9 * 2 + 1 < var0.displayedValue()) {
                            blit(param4, param1 + var9 * var4, param0, 70, 0, 9, 9);
                        }

                        if (var9 * 2 + 1 == var0.displayedValue()) {
                            blit(param4, param1 + var9 * var4, param0, 79, 0, 9, 9);
                        }
                    }

                    if (var9 * 2 + 1 < param5) {
                        blit(param4, param1 + var9 * var4, param0, var9 >= 10 ? 160 : 52, 0, 9, 9);
                    }

                    if (var9 * 2 + 1 == param5) {
                        blit(param4, param1 + var9 * var4, param0, var9 >= 10 ? 169 : 61, 0, 9, 9);
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
