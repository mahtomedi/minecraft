package net.minecraft.client.gui.screens.multiplayer;

import com.google.common.collect.Lists;
import com.google.common.hash.Hashing;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.logging.LogUtils;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.DefaultUncaughtExceptionHandler;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.LoadingDotsText;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerList;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.server.LanServer;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class ServerSelectionList extends ObjectSelectionList<ServerSelectionList.Entry> {
    static final Logger LOGGER = LogUtils.getLogger();
    static final ThreadPoolExecutor THREAD_POOL = new ScheduledThreadPoolExecutor(
        5,
        new ThreadFactoryBuilder()
            .setNameFormat("Server Pinger #%d")
            .setDaemon(true)
            .setUncaughtExceptionHandler(new DefaultUncaughtExceptionHandler(LOGGER))
            .build()
    );
    static final ResourceLocation ICON_MISSING = new ResourceLocation("textures/misc/unknown_server.png");
    static final ResourceLocation ICON_OVERLAY_LOCATION = new ResourceLocation("textures/gui/server_selection.png");
    static final Component SCANNING_LABEL = Component.translatable("lanServer.scanning");
    static final Component CANT_RESOLVE_TEXT = Component.translatable("multiplayer.status.cannot_resolve").withStyle(ChatFormatting.DARK_RED);
    static final Component CANT_CONNECT_TEXT = Component.translatable("multiplayer.status.cannot_connect").withStyle(ChatFormatting.DARK_RED);
    static final Component INCOMPATIBLE_TOOLTIP = Component.translatable("multiplayer.status.incompatible");
    static final Component NO_CONNECTION_TOOLTIP = Component.translatable("multiplayer.status.no_connection");
    static final Component PINGING_TOOLTIP = Component.translatable("multiplayer.status.pinging");
    private final JoinMultiplayerScreen screen;
    private final List<ServerSelectionList.OnlineServerEntry> onlineServers = Lists.newArrayList();
    private final ServerSelectionList.Entry lanHeader = new ServerSelectionList.LANHeader();
    private final List<ServerSelectionList.NetworkServerEntry> networkServers = Lists.newArrayList();

    public ServerSelectionList(JoinMultiplayerScreen param0, Minecraft param1, int param2, int param3, int param4, int param5, int param6) {
        super(param1, param2, param3, param4, param5, param6);
        this.screen = param0;
    }

    private void refreshEntries() {
        this.clearEntries();
        this.onlineServers.forEach(param1 -> this.addEntry(param1));
        this.addEntry(this.lanHeader);
        this.networkServers.forEach(param1 -> this.addEntry(param1));
    }

    public void setSelected(@Nullable ServerSelectionList.Entry param0) {
        super.setSelected(param0);
        this.screen.onSelectedChange();
    }

    @Override
    public boolean keyPressed(int param0, int param1, int param2) {
        ServerSelectionList.Entry var0 = this.getSelected();
        return var0 != null && var0.keyPressed(param0, param1, param2) || super.keyPressed(param0, param1, param2);
    }

    @Override
    protected void moveSelection(AbstractSelectionList.SelectionDirection param0) {
        this.moveSelection(param0, param0x -> !(param0x instanceof ServerSelectionList.LANHeader));
    }

    public void updateOnlineServers(ServerList param0) {
        this.onlineServers.clear();

        for(int var0 = 0; var0 < param0.size(); ++var0) {
            this.onlineServers.add(new ServerSelectionList.OnlineServerEntry(this.screen, param0.get(var0)));
        }

        this.refreshEntries();
    }

    public void updateNetworkServers(List<LanServer> param0) {
        this.networkServers.clear();

        for(LanServer var0 : param0) {
            this.networkServers.add(new ServerSelectionList.NetworkServerEntry(this.screen, var0));
        }

        this.refreshEntries();
    }

    @Override
    protected int getScrollbarPosition() {
        return super.getScrollbarPosition() + 30;
    }

    @Override
    public int getRowWidth() {
        return super.getRowWidth() + 85;
    }

    @Override
    protected boolean isFocused() {
        return this.screen.getFocused() == this;
    }

    @OnlyIn(Dist.CLIENT)
    public abstract static class Entry extends ObjectSelectionList.Entry<ServerSelectionList.Entry> {
    }

    @OnlyIn(Dist.CLIENT)
    public static class LANHeader extends ServerSelectionList.Entry {
        private final Minecraft minecraft = Minecraft.getInstance();

        @Override
        public void render(PoseStack param0, int param1, int param2, int param3, int param4, int param5, int param6, int param7, boolean param8, float param9) {
            int var0 = param2 + param5 / 2 - 9 / 2;
            this.minecraft
                .font
                .draw(
                    param0,
                    ServerSelectionList.SCANNING_LABEL,
                    (float)(this.minecraft.screen.width / 2 - this.minecraft.font.width(ServerSelectionList.SCANNING_LABEL) / 2),
                    (float)var0,
                    16777215
                );
            String var1 = LoadingDotsText.get(Util.getMillis());
            this.minecraft.font.draw(param0, var1, (float)(this.minecraft.screen.width / 2 - this.minecraft.font.width(var1) / 2), (float)(var0 + 9), 8421504);
        }

        @Override
        public Component getNarration() {
            return CommonComponents.EMPTY;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class NetworkServerEntry extends ServerSelectionList.Entry {
        private static final int ICON_WIDTH = 32;
        private static final Component LAN_SERVER_HEADER = Component.translatable("lanServer.title");
        private static final Component HIDDEN_ADDRESS_TEXT = Component.translatable("selectServer.hiddenAddress");
        private final JoinMultiplayerScreen screen;
        protected final Minecraft minecraft;
        protected final LanServer serverData;
        private long lastClickTime;

        protected NetworkServerEntry(JoinMultiplayerScreen param0, LanServer param1) {
            this.screen = param0;
            this.serverData = param1;
            this.minecraft = Minecraft.getInstance();
        }

        @Override
        public void render(PoseStack param0, int param1, int param2, int param3, int param4, int param5, int param6, int param7, boolean param8, float param9) {
            this.minecraft.font.draw(param0, LAN_SERVER_HEADER, (float)(param3 + 32 + 3), (float)(param2 + 1), 16777215);
            this.minecraft.font.draw(param0, this.serverData.getMotd(), (float)(param3 + 32 + 3), (float)(param2 + 12), 8421504);
            if (this.minecraft.options.hideServerAddress) {
                this.minecraft.font.draw(param0, HIDDEN_ADDRESS_TEXT, (float)(param3 + 32 + 3), (float)(param2 + 12 + 11), 3158064);
            } else {
                this.minecraft.font.draw(param0, this.serverData.getAddress(), (float)(param3 + 32 + 3), (float)(param2 + 12 + 11), 3158064);
            }

        }

        @Override
        public boolean mouseClicked(double param0, double param1, int param2) {
            this.screen.setSelected(this);
            if (Util.getMillis() - this.lastClickTime < 250L) {
                this.screen.joinSelectedServer();
            }

            this.lastClickTime = Util.getMillis();
            return false;
        }

        public LanServer getServerData() {
            return this.serverData;
        }

        @Override
        public Component getNarration() {
            return Component.translatable("narrator.select", Component.empty().append(LAN_SERVER_HEADER).append(" ").append(this.serverData.getMotd()));
        }
    }

    @OnlyIn(Dist.CLIENT)
    public class OnlineServerEntry extends ServerSelectionList.Entry {
        private static final int ICON_WIDTH = 32;
        private static final int ICON_HEIGHT = 32;
        private static final int ICON_OVERLAY_X_MOVE_RIGHT = 0;
        private static final int ICON_OVERLAY_X_MOVE_LEFT = 32;
        private static final int ICON_OVERLAY_X_MOVE_DOWN = 64;
        private static final int ICON_OVERLAY_X_MOVE_UP = 96;
        private static final int ICON_OVERLAY_Y_UNSELECTED = 0;
        private static final int ICON_OVERLAY_Y_SELECTED = 32;
        private final JoinMultiplayerScreen screen;
        private final Minecraft minecraft;
        private final ServerData serverData;
        private final ResourceLocation iconLocation;
        @Nullable
        private String lastIconB64;
        @Nullable
        private DynamicTexture icon;
        private long lastClickTime;

        protected OnlineServerEntry(JoinMultiplayerScreen param1, ServerData param2) {
            this.screen = param1;
            this.serverData = param2;
            this.minecraft = Minecraft.getInstance();
            this.iconLocation = new ResourceLocation("servers/" + Hashing.sha1().hashUnencodedChars(param2.ip) + "/icon");
            AbstractTexture var0 = this.minecraft.getTextureManager().getTexture(this.iconLocation, MissingTextureAtlasSprite.getTexture());
            if (var0 != MissingTextureAtlasSprite.getTexture() && var0 instanceof DynamicTexture) {
                this.icon = (DynamicTexture)var0;
            }

        }

        @Override
        public void render(PoseStack param0, int param1, int param2, int param3, int param4, int param5, int param6, int param7, boolean param8, float param9) {
            if (!this.serverData.pinged) {
                this.serverData.pinged = true;
                this.serverData.ping = -2L;
                this.serverData.motd = CommonComponents.EMPTY;
                this.serverData.status = CommonComponents.EMPTY;
                ServerSelectionList.THREAD_POOL.submit(() -> {
                    try {
                        this.screen.getPinger().pingServer(this.serverData, () -> this.minecraft.execute(this::updateServerList));
                    } catch (UnknownHostException var2x) {
                        this.serverData.ping = -1L;
                        this.serverData.motd = ServerSelectionList.CANT_RESOLVE_TEXT;
                    } catch (Exception var3x) {
                        this.serverData.ping = -1L;
                        this.serverData.motd = ServerSelectionList.CANT_CONNECT_TEXT;
                    }

                });
            }

            boolean var0 = this.serverData.protocol != SharedConstants.getCurrentVersion().getProtocolVersion();
            this.minecraft.font.draw(param0, this.serverData.name, (float)(param3 + 32 + 3), (float)(param2 + 1), 16777215);
            List<FormattedCharSequence> var1 = this.minecraft.font.split(this.serverData.motd, param4 - 32 - 2);

            for(int var2 = 0; var2 < Math.min(var1.size(), 2); ++var2) {
                this.minecraft.font.draw(param0, var1.get(var2), (float)(param3 + 32 + 3), (float)(param2 + 12 + 9 * var2), 8421504);
            }

            Component var3 = (Component)(var0 ? this.serverData.version.copy().withStyle(ChatFormatting.RED) : this.serverData.status);
            int var4 = this.minecraft.font.width(var3);
            this.minecraft.font.draw(param0, var3, (float)(param3 + param4 - var4 - 15 - 2), (float)(param2 + 1), 8421504);
            int var5 = 0;
            int var6;
            List<Component> var8;
            Component var7;
            if (var0) {
                var6 = 5;
                var7 = ServerSelectionList.INCOMPATIBLE_TOOLTIP;
                var8 = this.serverData.playerList;
            } else if (this.serverData.pinged && this.serverData.ping != -2L) {
                if (this.serverData.ping < 0L) {
                    var6 = 5;
                } else if (this.serverData.ping < 150L) {
                    var6 = 0;
                } else if (this.serverData.ping < 300L) {
                    var6 = 1;
                } else if (this.serverData.ping < 600L) {
                    var6 = 2;
                } else if (this.serverData.ping < 1000L) {
                    var6 = 3;
                } else {
                    var6 = 4;
                }

                if (this.serverData.ping < 0L) {
                    var7 = ServerSelectionList.NO_CONNECTION_TOOLTIP;
                    var8 = Collections.emptyList();
                } else {
                    var7 = Component.translatable("multiplayer.status.ping", this.serverData.ping);
                    var8 = this.serverData.playerList;
                }
            } else {
                var5 = 1;
                var6 = (int)(Util.getMillis() / 100L + (long)(param1 * 2) & 7L);
                if (var6 > 4) {
                    var6 = 8 - var6;
                }

                var7 = ServerSelectionList.PINGING_TOOLTIP;
                var8 = Collections.emptyList();
            }

            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.setShaderTexture(0, GuiComponent.GUI_ICONS_LOCATION);
            GuiComponent.blit(param0, param3 + param4 - 15, param2, (float)(var5 * 10), (float)(176 + var6 * 8), 10, 8, 256, 256);
            String var22 = this.serverData.getIconB64();
            if (!Objects.equals(var22, this.lastIconB64)) {
                if (this.uploadServerIcon(var22)) {
                    this.lastIconB64 = var22;
                } else {
                    this.serverData.setIconB64(null);
                    this.updateServerList();
                }
            }

            if (this.icon == null) {
                this.drawIcon(param0, param3, param2, ServerSelectionList.ICON_MISSING);
            } else {
                this.drawIcon(param0, param3, param2, this.iconLocation);
            }

            int var23 = param6 - param3;
            int var24 = param7 - param2;
            if (var23 >= param4 - 15 && var23 <= param4 - 5 && var24 >= 0 && var24 <= 8) {
                this.screen.setToolTip(Collections.singletonList(var7));
            } else if (var23 >= param4 - var4 - 15 - 2 && var23 <= param4 - 15 - 2 && var24 >= 0 && var24 <= 8) {
                this.screen.setToolTip(var8);
            }

            if (this.minecraft.options.touchscreen().get() || param8) {
                RenderSystem.setShaderTexture(0, ServerSelectionList.ICON_OVERLAY_LOCATION);
                GuiComponent.fill(param0, param3, param2, param3 + 32, param2 + 32, -1601138544);
                RenderSystem.setShader(GameRenderer::getPositionTexShader);
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                int var25 = param6 - param3;
                int var26 = param7 - param2;
                if (this.canJoin()) {
                    if (var25 < 32 && var25 > 16) {
                        GuiComponent.blit(param0, param3, param2, 0.0F, 32.0F, 32, 32, 256, 256);
                    } else {
                        GuiComponent.blit(param0, param3, param2, 0.0F, 0.0F, 32, 32, 256, 256);
                    }
                }

                if (param1 > 0) {
                    if (var25 < 16 && var26 < 16) {
                        GuiComponent.blit(param0, param3, param2, 96.0F, 32.0F, 32, 32, 256, 256);
                    } else {
                        GuiComponent.blit(param0, param3, param2, 96.0F, 0.0F, 32, 32, 256, 256);
                    }
                }

                if (param1 < this.screen.getServers().size() - 1) {
                    if (var25 < 16 && var26 > 16) {
                        GuiComponent.blit(param0, param3, param2, 64.0F, 32.0F, 32, 32, 256, 256);
                    } else {
                        GuiComponent.blit(param0, param3, param2, 64.0F, 0.0F, 32, 32, 256, 256);
                    }
                }
            }

        }

        public void updateServerList() {
            this.screen.getServers().save();
        }

        protected void drawIcon(PoseStack param0, int param1, int param2, ResourceLocation param3) {
            RenderSystem.setShaderTexture(0, param3);
            RenderSystem.enableBlend();
            GuiComponent.blit(param0, param1, param2, 0.0F, 0.0F, 32, 32, 32, 32);
            RenderSystem.disableBlend();
        }

        private boolean canJoin() {
            return true;
        }

        private boolean uploadServerIcon(@Nullable String param0) {
            if (param0 == null) {
                this.minecraft.getTextureManager().release(this.iconLocation);
                if (this.icon != null && this.icon.getPixels() != null) {
                    this.icon.getPixels().close();
                }

                this.icon = null;
            } else {
                try {
                    NativeImage var0 = NativeImage.fromBase64(param0);
                    Validate.validState(var0.getWidth() == 64, "Must be 64 pixels wide");
                    Validate.validState(var0.getHeight() == 64, "Must be 64 pixels high");
                    if (this.icon == null) {
                        this.icon = new DynamicTexture(var0);
                    } else {
                        this.icon.setPixels(var0);
                        this.icon.upload();
                    }

                    this.minecraft.getTextureManager().register(this.iconLocation, this.icon);
                } catch (Throwable var3) {
                    ServerSelectionList.LOGGER.error("Invalid icon for server {} ({})", this.serverData.name, this.serverData.ip, var3);
                    return false;
                }
            }

            return true;
        }

        @Override
        public boolean keyPressed(int param0, int param1, int param2) {
            if (Screen.hasShiftDown()) {
                ServerSelectionList var0 = this.screen.serverSelectionList;
                int var1 = var0.children().indexOf(this);
                if (var1 == -1) {
                    return true;
                }

                if (param0 == 264 && var1 < this.screen.getServers().size() - 1 || param0 == 265 && var1 > 0) {
                    this.swap(var1, param0 == 264 ? var1 + 1 : var1 - 1);
                    return true;
                }
            }

            return super.keyPressed(param0, param1, param2);
        }

        private void swap(int param0, int param1) {
            this.screen.getServers().swap(param0, param1);
            this.screen.serverSelectionList.updateOnlineServers(this.screen.getServers());
            ServerSelectionList.Entry var0 = this.screen.serverSelectionList.children().get(param1);
            this.screen.serverSelectionList.setSelected(var0);
            ServerSelectionList.this.ensureVisible(var0);
        }

        @Override
        public boolean mouseClicked(double param0, double param1, int param2) {
            double var0 = param0 - (double)ServerSelectionList.this.getRowLeft();
            double var1 = param1 - (double)ServerSelectionList.this.getRowTop(ServerSelectionList.this.children().indexOf(this));
            if (var0 <= 32.0) {
                if (var0 < 32.0 && var0 > 16.0 && this.canJoin()) {
                    this.screen.setSelected(this);
                    this.screen.joinSelectedServer();
                    return true;
                }

                int var2 = this.screen.serverSelectionList.children().indexOf(this);
                if (var0 < 16.0 && var1 < 16.0 && var2 > 0) {
                    this.swap(var2, var2 - 1);
                    return true;
                }

                if (var0 < 16.0 && var1 > 16.0 && var2 < this.screen.getServers().size() - 1) {
                    this.swap(var2, var2 + 1);
                    return true;
                }
            }

            this.screen.setSelected(this);
            if (Util.getMillis() - this.lastClickTime < 250L) {
                this.screen.joinSelectedServer();
            }

            this.lastClickTime = Util.getMillis();
            return false;
        }

        public ServerData getServerData() {
            return this.serverData;
        }

        @Override
        public Component getNarration() {
            return Component.translatable("narrator.select", this.serverData.name);
        }
    }
}
