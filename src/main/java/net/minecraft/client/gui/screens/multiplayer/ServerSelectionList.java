package net.minecraft.client.gui.screens.multiplayer;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.DefaultUncaughtExceptionHandler;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.FaviconTexture;
import net.minecraft.client.gui.screens.LoadingDotsText;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerList;
import net.minecraft.client.server.LanServer;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
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
    private static final ResourceLocation ICON_MISSING = new ResourceLocation("textures/misc/unknown_server.png");
    static final ResourceLocation ICON_OVERLAY_LOCATION = new ResourceLocation("textures/gui/server_selection.png");
    static final ResourceLocation GUI_ICONS_LOCATION = new ResourceLocation("textures/gui/icons.png");
    static final Component SCANNING_LABEL = Component.translatable("lanServer.scanning");
    static final Component CANT_RESOLVE_TEXT = Component.translatable("multiplayer.status.cannot_resolve").withStyle(param0 -> param0.withColor(-65536));
    static final Component CANT_CONNECT_TEXT = Component.translatable("multiplayer.status.cannot_connect").withStyle(param0 -> param0.withColor(-65536));
    static final Component INCOMPATIBLE_STATUS = Component.translatable("multiplayer.status.incompatible");
    static final Component NO_CONNECTION_STATUS = Component.translatable("multiplayer.status.no_connection");
    static final Component PINGING_STATUS = Component.translatable("multiplayer.status.pinging");
    static final Component ONLINE_STATUS = Component.translatable("multiplayer.status.online");
    private final JoinMultiplayerScreen screen;
    private final List<ServerSelectionList.OnlineServerEntry> onlineServers = Lists.newArrayList();
    private final ServerSelectionList.Entry lanHeader = new ServerSelectionList.LANHeader();
    private final List<ServerSelectionList.NetworkServerEntry> networkServers = Lists.newArrayList();

    public ServerSelectionList(JoinMultiplayerScreen param0, Minecraft param1, int param2, int param3, int param4, int param5, int param6) {
        super(param1, param2, param3, param4, param5, param6);
        this.screen = param0;
    }

    @Override
    protected void clearEntries() {
        this.children().forEach(ServerSelectionList.Entry::close);
        super.clearEntries();
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

    public void updateOnlineServers(ServerList param0) {
        this.onlineServers.clear();

        for(int var0 = 0; var0 < param0.size(); ++var0) {
            this.onlineServers.add(new ServerSelectionList.OnlineServerEntry(this.screen, param0.get(var0)));
        }

        this.refreshEntries();
    }

    public void updateNetworkServers(List<LanServer> param0) {
        int var0 = param0.size() - this.networkServers.size();
        this.networkServers.clear();

        for(LanServer var1 : param0) {
            this.networkServers.add(new ServerSelectionList.NetworkServerEntry(this.screen, var1));
        }

        this.refreshEntries();

        for(int var2 = this.networkServers.size() - var0; var2 < this.networkServers.size(); ++var2) {
            ServerSelectionList.NetworkServerEntry var3 = this.networkServers.get(var2);
            int var4 = var2 - this.networkServers.size() + this.children().size();
            int var5 = this.getRowTop(var4);
            int var6 = this.getRowBottom(var4);
            if (var6 >= this.y0 && var5 <= this.y1) {
                this.minecraft.getNarrator().say(Component.translatable("multiplayer.lan.server_found", var3.getServerNarration()));
            }
        }

    }

    @Override
    protected int getScrollbarPosition() {
        return super.getScrollbarPosition() + 30;
    }

    @Override
    public int getRowWidth() {
        return super.getRowWidth() + 85;
    }

    public void removed() {
        this.clearEntries();
    }

    @OnlyIn(Dist.CLIENT)
    public abstract static class Entry extends ObjectSelectionList.Entry<ServerSelectionList.Entry> implements AutoCloseable {
        @Override
        public void close() {
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class LANHeader extends ServerSelectionList.Entry {
        private final Minecraft minecraft = Minecraft.getInstance();

        @Override
        public void render(GuiGraphics param0, int param1, int param2, int param3, int param4, int param5, int param6, int param7, boolean param8, float param9) {
            int var0 = param2 + param5 / 2 - 9 / 2;
            param0.drawString(
                this.minecraft.font,
                ServerSelectionList.SCANNING_LABEL,
                this.minecraft.screen.width / 2 - this.minecraft.font.width(ServerSelectionList.SCANNING_LABEL) / 2,
                var0,
                16777215,
                false
            );
            String var1 = LoadingDotsText.get(Util.getMillis());
            param0.drawString(this.minecraft.font, var1, this.minecraft.screen.width / 2 - this.minecraft.font.width(var1) / 2, var0 + 9, 8421504, false);
        }

        @Override
        public Component getNarration() {
            return ServerSelectionList.SCANNING_LABEL;
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
        public void render(GuiGraphics param0, int param1, int param2, int param3, int param4, int param5, int param6, int param7, boolean param8, float param9) {
            param0.drawString(this.minecraft.font, LAN_SERVER_HEADER, param3 + 32 + 3, param2 + 1, 16777215, false);
            param0.drawString(this.minecraft.font, this.serverData.getMotd(), param3 + 32 + 3, param2 + 12, 8421504, false);
            if (this.minecraft.options.hideServerAddress) {
                param0.drawString(this.minecraft.font, HIDDEN_ADDRESS_TEXT, param3 + 32 + 3, param2 + 12 + 11, 3158064, false);
            } else {
                param0.drawString(this.minecraft.font, this.serverData.getAddress(), param3 + 32 + 3, param2 + 12 + 11, 3158064, false);
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
            return Component.translatable("narrator.select", this.getServerNarration());
        }

        public Component getServerNarration() {
            return Component.empty().append(LAN_SERVER_HEADER).append(CommonComponents.SPACE).append(this.serverData.getMotd());
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
        private final FaviconTexture icon;
        @Nullable
        private byte[] lastIconBytes;
        private long lastClickTime;

        protected OnlineServerEntry(JoinMultiplayerScreen param1, ServerData param2) {
            this.screen = param1;
            this.serverData = param2;
            this.minecraft = Minecraft.getInstance();
            this.icon = FaviconTexture.forServer(this.minecraft.getTextureManager(), param2.ip);
        }

        @Override
        public void render(GuiGraphics param0, int param1, int param2, int param3, int param4, int param5, int param6, int param7, boolean param8, float param9) {
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

            boolean var0 = !this.isCompatible();
            param0.drawString(this.minecraft.font, this.serverData.name, param3 + 32 + 3, param2 + 1, 16777215, false);
            List<FormattedCharSequence> var1 = this.minecraft.font.split(this.serverData.motd, param4 - 32 - 2);

            for(int var2 = 0; var2 < Math.min(var1.size(), 2); ++var2) {
                param0.drawString(this.minecraft.font, var1.get(var2), param3 + 32 + 3, param2 + 12 + 9 * var2, 8421504, false);
            }

            Component var3 = (Component)(var0 ? this.serverData.version.copy().withStyle(ChatFormatting.RED) : this.serverData.status);
            int var4 = this.minecraft.font.width(var3);
            param0.drawString(this.minecraft.font, var3, param3 + param4 - var4 - 15 - 2, param2 + 1, 8421504, false);
            int var5 = 0;
            int var6;
            List<Component> var8;
            Component var7;
            if (var0) {
                var6 = 5;
                var7 = ServerSelectionList.INCOMPATIBLE_STATUS;
                var8 = this.serverData.playerList;
            } else if (this.pingCompleted()) {
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
                    var7 = ServerSelectionList.NO_CONNECTION_STATUS;
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

                var7 = ServerSelectionList.PINGING_STATUS;
                var8 = Collections.emptyList();
            }

            param0.blit(ServerSelectionList.GUI_ICONS_LOCATION, param3 + param4 - 15, param2, (float)(var5 * 10), (float)(176 + var6 * 8), 10, 8, 256, 256);
            byte[] var22 = this.serverData.getIconBytes();
            if (!Arrays.equals(var22, this.lastIconBytes)) {
                if (this.uploadServerIcon(var22)) {
                    this.lastIconBytes = var22;
                } else {
                    this.serverData.setIconBytes(null);
                    this.updateServerList();
                }
            }

            this.drawIcon(param0, param3, param2, this.icon.textureLocation());
            int var23 = param6 - param3;
            int var24 = param7 - param2;
            if (var23 >= param4 - 15 && var23 <= param4 - 5 && var24 >= 0 && var24 <= 8) {
                this.screen.setToolTip(Collections.singletonList(var7));
            } else if (var23 >= param4 - var4 - 15 - 2 && var23 <= param4 - 15 - 2 && var24 >= 0 && var24 <= 8) {
                this.screen.setToolTip(var8);
            }

            if (this.minecraft.options.touchscreen().get() || param8) {
                param0.fill(param3, param2, param3 + 32, param2 + 32, -1601138544);
                int var25 = param6 - param3;
                int var26 = param7 - param2;
                if (this.canJoin()) {
                    if (var25 < 32 && var25 > 16) {
                        param0.blit(ServerSelectionList.ICON_OVERLAY_LOCATION, param3, param2, 0.0F, 32.0F, 32, 32, 256, 256);
                    } else {
                        param0.blit(ServerSelectionList.ICON_OVERLAY_LOCATION, param3, param2, 0.0F, 0.0F, 32, 32, 256, 256);
                    }
                }

                if (param1 > 0) {
                    if (var25 < 16 && var26 < 16) {
                        param0.blit(ServerSelectionList.ICON_OVERLAY_LOCATION, param3, param2, 96.0F, 32.0F, 32, 32, 256, 256);
                    } else {
                        param0.blit(ServerSelectionList.ICON_OVERLAY_LOCATION, param3, param2, 96.0F, 0.0F, 32, 32, 256, 256);
                    }
                }

                if (param1 < this.screen.getServers().size() - 1) {
                    if (var25 < 16 && var26 > 16) {
                        param0.blit(ServerSelectionList.ICON_OVERLAY_LOCATION, param3, param2, 64.0F, 32.0F, 32, 32, 256, 256);
                    } else {
                        param0.blit(ServerSelectionList.ICON_OVERLAY_LOCATION, param3, param2, 64.0F, 0.0F, 32, 32, 256, 256);
                    }
                }
            }

        }

        private boolean pingCompleted() {
            return this.serverData.pinged && this.serverData.ping != -2L;
        }

        private boolean isCompatible() {
            return this.serverData.protocol == SharedConstants.getCurrentVersion().getProtocolVersion();
        }

        public void updateServerList() {
            this.screen.getServers().save();
        }

        protected void drawIcon(GuiGraphics param0, int param1, int param2, ResourceLocation param3) {
            RenderSystem.enableBlend();
            param0.blit(param3, param1, param2, 0.0F, 0.0F, 32, 32, 32, 32);
            RenderSystem.disableBlend();
        }

        private boolean canJoin() {
            return true;
        }

        private boolean uploadServerIcon(@Nullable byte[] param0) {
            if (param0 == null) {
                this.icon.clear();
            } else {
                try {
                    this.icon.upload(NativeImage.read(param0));
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
            return true;
        }

        public ServerData getServerData() {
            return this.serverData;
        }

        @Override
        public Component getNarration() {
            MutableComponent var0 = Component.empty();
            var0.append(Component.translatable("narrator.select", this.serverData.name));
            var0.append(CommonComponents.NARRATION_SEPARATOR);
            if (!this.isCompatible()) {
                var0.append(ServerSelectionList.INCOMPATIBLE_STATUS);
                var0.append(CommonComponents.NARRATION_SEPARATOR);
                var0.append(Component.translatable("multiplayer.status.version.narration", this.serverData.version));
                var0.append(CommonComponents.NARRATION_SEPARATOR);
                var0.append(Component.translatable("multiplayer.status.motd.narration", this.serverData.motd));
            } else if (this.serverData.ping < 0L) {
                var0.append(ServerSelectionList.NO_CONNECTION_STATUS);
            } else if (!this.pingCompleted()) {
                var0.append(ServerSelectionList.PINGING_STATUS);
            } else {
                var0.append(ServerSelectionList.ONLINE_STATUS);
                var0.append(CommonComponents.NARRATION_SEPARATOR);
                var0.append(Component.translatable("multiplayer.status.ping.narration", this.serverData.ping));
                var0.append(CommonComponents.NARRATION_SEPARATOR);
                var0.append(Component.translatable("multiplayer.status.motd.narration", this.serverData.motd));
                if (this.serverData.players != null) {
                    var0.append(CommonComponents.NARRATION_SEPARATOR);
                    var0.append(
                        Component.translatable("multiplayer.status.player_count.narration", this.serverData.players.online(), this.serverData.players.max())
                    );
                    var0.append(CommonComponents.NARRATION_SEPARATOR);
                    var0.append(ComponentUtils.formatList(this.serverData.playerList, Component.literal(", ")));
                }
            }

            return var0;
        }

        @Override
        public void close() {
            this.icon.close();
        }
    }
}
