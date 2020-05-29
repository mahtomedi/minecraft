package net.minecraft.client.gui.components;

import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Ordering;
import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Comparator;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
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
    private static final Ordering<PlayerInfo> PLAYER_ORDERING = Ordering.from(new PlayerTabOverlay.PlayerInfoComparator());
    private final Minecraft minecraft;
    private final Gui gui;
    private Component footer;
    private Component header;
    private long visibilityId;
    private boolean visible;

    public PlayerTabOverlay(Minecraft param0, Gui param1) {
        this.minecraft = param0;
        this.gui = param1;
    }

    public Component getNameForDisplay(PlayerInfo param0) {
        return param0.getTabListDisplayName() != null
            ? this.decorateName(param0, param0.getTabListDisplayName().mutableCopy())
            : this.decorateName(param0, PlayerTeam.formatNameForTeam(param0.getTeam(), new TextComponent(param0.getProfile().getName())));
    }

    private Component decorateName(PlayerInfo param0, MutableComponent param1) {
        return param0.getGameMode() == GameType.SPECTATOR ? param1.withStyle(ChatFormatting.ITALIC) : param1;
    }

    public void setVisible(boolean param0) {
        if (param0 && !this.visible) {
            this.visibilityId = Util.getMillis();
        }

        this.visible = param0;
    }

    public void render(PoseStack param0, int param1, Scoreboard param2, @Nullable Objective param3) {
        ClientPacketListener var0 = this.minecraft.player.connection;
        List<PlayerInfo> var1 = PLAYER_ORDERING.sortedCopy(var0.getOnlinePlayers());
        int var2 = 0;
        int var3 = 0;

        for(PlayerInfo var4 : var1) {
            int var5 = this.minecraft.font.width(this.getNameForDisplay(var4));
            var2 = Math.max(var2, var5);
            if (param3 != null && param3.getRenderType() != ObjectiveCriteria.RenderType.HEARTS) {
                var5 = this.minecraft.font.width(" " + param2.getOrCreatePlayerScore(var4.getProfile().getName(), param3).getScore());
                var3 = Math.max(var3, var5);
            }
        }

        var1 = var1.subList(0, Math.min(var1.size(), 80));
        int var6 = var1.size();
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
                var10 = var3;
            }
        } else {
            var10 = 0;
        }

        int var13 = Math.min(var8 * ((var9 ? 9 : 0) + var2 + var10 + 13), param1 - 50) / var8;
        int var14 = param1 / 2 - (var13 * var8 + (var8 - 1) * 5) / 2;
        int var15 = 10;
        int var16 = var13 * var8 + (var8 - 1) * 5;
        List<FormattedText> var17 = null;
        if (this.header != null) {
            var17 = this.minecraft.font.split(this.header, param1 - 50);

            for(FormattedText var18 : var17) {
                var16 = Math.max(var16, this.minecraft.font.width(var18));
            }
        }

        List<FormattedText> var19 = null;
        if (this.footer != null) {
            var19 = this.minecraft.font.split(this.footer, param1 - 50);

            for(FormattedText var20 : var19) {
                var16 = Math.max(var16, this.minecraft.font.width(var20));
            }
        }

        if (var17 != null) {
            fill(param0, param1 / 2 - var16 / 2 - 1, var15 - 1, param1 / 2 + var16 / 2 + 1, var15 + var17.size() * 9, Integer.MIN_VALUE);

            for(FormattedText var21 : var17) {
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
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.enableAlphaTest();
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            if (var24 < var1.size()) {
                PlayerInfo var29 = var1.get(var24);
                GameProfile var30 = var29.getProfile();
                if (var9) {
                    Player var31 = this.minecraft.level.getPlayerByUUID(var30.getId());
                    boolean var32 = var31 != null
                        && var31.isModelPartShown(PlayerModelPart.CAPE)
                        && ("Dinnerbone".equals(var30.getName()) || "Grumm".equals(var30.getName()));
                    this.minecraft.getTextureManager().bind(var29.getSkinLocation());
                    int var33 = 8 + (var32 ? 8 : 0);
                    int var34 = 8 * (var32 ? -1 : 1);
                    GuiComponent.blit(param0, var27, var28, 8, 8, 8.0F, (float)var33, 8, var34, 64, 64);
                    if (var31 != null && var31.isModelPartShown(PlayerModelPart.HAT)) {
                        int var35 = 8 + (var32 ? 8 : 0);
                        int var36 = 8 * (var32 ? -1 : 1);
                        GuiComponent.blit(param0, var27, var28, 8, 8, 40.0F, (float)var35, 8, var36, 64, 64);
                    }

                    var27 += 9;
                }

                this.minecraft
                    .font
                    .drawShadow(param0, this.getNameForDisplay(var29), (float)var27, (float)var28, var29.getGameMode() == GameType.SPECTATOR ? -1862270977 : -1);
                if (param3 != null && var29.getGameMode() != GameType.SPECTATOR) {
                    int var37 = var27 + var2 + 1;
                    int var38 = var37 + var10;
                    if (var38 - var37 > 5) {
                        this.renderTablistScore(param3, var28, var30.getName(), var37, var38, var29, param0);
                    }
                }

                this.renderPingIcon(param0, var13, var27 - (var9 ? 9 : 0), var28, var29);
            }
        }

        if (var19 != null) {
            var15 += var7 * 9 + 1;
            fill(param0, param1 / 2 - var16 / 2 - 1, var15 - 1, param1 / 2 + var16 / 2 + 1, var15 + var19.size() * 9, Integer.MIN_VALUE);

            for(FormattedText var39 : var19) {
                int var40 = this.minecraft.font.width(var39);
                this.minecraft.font.drawShadow(param0, var39, (float)(param1 / 2 - var40 / 2), (float)var15, -1);
                var15 += 9;
            }
        }

    }

    protected void renderPingIcon(PoseStack param0, int param1, int param2, int param3, PlayerInfo param4) {
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.minecraft.getTextureManager().bind(GUI_ICONS_LOCATION);
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

        this.setBlitOffset(this.getBlitOffset() + 100);
        this.blit(param0, param2 + param1 - 11, param3, 0, 176 + var1 * 8, 10, 8);
        this.setBlitOffset(this.getBlitOffset() - 100);
    }

    private void renderTablistScore(Objective param0, int param1, String param2, int param3, int param4, PlayerInfo param5, PoseStack param6) {
        int var0 = param0.getScoreboard().getOrCreatePlayerScore(param2, param0).getScore();
        if (param0.getRenderType() == ObjectiveCriteria.RenderType.HEARTS) {
            this.minecraft.getTextureManager().bind(GUI_ICONS_LOCATION);
            long var1 = Util.getMillis();
            if (this.visibilityId == param5.getRenderVisibilityId()) {
                if (var0 < param5.getLastHealth()) {
                    param5.setLastHealthTime(var1);
                    param5.setHealthBlinkTime((long)(this.gui.getGuiTicks() + 20));
                } else if (var0 > param5.getLastHealth()) {
                    param5.setLastHealthTime(var1);
                    param5.setHealthBlinkTime((long)(this.gui.getGuiTicks() + 10));
                }
            }

            if (var1 - param5.getLastHealthTime() > 1000L || this.visibilityId != param5.getRenderVisibilityId()) {
                param5.setLastHealth(var0);
                param5.setDisplayHealth(var0);
                param5.setLastHealthTime(var1);
            }

            param5.setRenderVisibilityId(this.visibilityId);
            param5.setLastHealth(var0);
            int var2 = Mth.ceil((float)Math.max(var0, param5.getDisplayHealth()) / 2.0F);
            int var3 = Math.max(Mth.ceil((float)(var0 / 2)), Math.max(Mth.ceil((float)(param5.getDisplayHealth() / 2)), 10));
            boolean var4 = param5.getHealthBlinkTime() > (long)this.gui.getGuiTicks()
                && (param5.getHealthBlinkTime() - (long)this.gui.getGuiTicks()) / 3L % 2L == 1L;
            if (var2 > 0) {
                int var5 = Mth.floor(Math.min((float)(param4 - param3 - 4) / (float)var3, 9.0F));
                if (var5 > 3) {
                    for(int var6 = var2; var6 < var3; ++var6) {
                        this.blit(param6, param3 + var6 * var5, param1, var4 ? 25 : 16, 0, 9, 9);
                    }

                    for(int var7 = 0; var7 < var2; ++var7) {
                        this.blit(param6, param3 + var7 * var5, param1, var4 ? 25 : 16, 0, 9, 9);
                        if (var4) {
                            if (var7 * 2 + 1 < param5.getDisplayHealth()) {
                                this.blit(param6, param3 + var7 * var5, param1, 70, 0, 9, 9);
                            }

                            if (var7 * 2 + 1 == param5.getDisplayHealth()) {
                                this.blit(param6, param3 + var7 * var5, param1, 79, 0, 9, 9);
                            }
                        }

                        if (var7 * 2 + 1 < var0) {
                            this.blit(param6, param3 + var7 * var5, param1, var7 >= 10 ? 160 : 52, 0, 9, 9);
                        }

                        if (var7 * 2 + 1 == var0) {
                            this.blit(param6, param3 + var7 * var5, param1, var7 >= 10 ? 169 : 61, 0, 9, 9);
                        }
                    }
                } else {
                    float var8 = Mth.clamp((float)var0 / 20.0F, 0.0F, 1.0F);
                    int var9 = (int)((1.0F - var8) * 255.0F) << 16 | (int)(var8 * 255.0F) << 8;
                    String var10 = "" + (float)var0 / 2.0F;
                    if (param4 - this.minecraft.font.width(var10 + "hp") >= param3) {
                        var10 = var10 + "hp";
                    }

                    this.minecraft.font.drawShadow(param6, var10, (float)((param4 + param3) / 2 - this.minecraft.font.width(var10) / 2), (float)param1, var9);
                }
            }
        } else {
            String var11 = ChatFormatting.YELLOW + "" + var0;
            this.minecraft.font.drawShadow(param6, var11, (float)(param4 - this.minecraft.font.width(var11)), (float)param1, 16777215);
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
    static class PlayerInfoComparator implements Comparator<PlayerInfo> {
        private PlayerInfoComparator() {
        }

        public int compare(PlayerInfo param0, PlayerInfo param1) {
            PlayerTeam var0 = param0.getTeam();
            PlayerTeam var1 = param1.getTeam();
            return ComparisonChain.start()
                .compareTrueFirst(param0.getGameMode() != GameType.SPECTATOR, param1.getGameMode() != GameType.SPECTATOR)
                .compare(var0 != null ? var0.getName() : "", var1 != null ? var1.getName() : "")
                .compare(param0.getProfile().getName(), param1.getProfile().getName(), String::compareToIgnoreCase)
                .result();
        }
    }
}
