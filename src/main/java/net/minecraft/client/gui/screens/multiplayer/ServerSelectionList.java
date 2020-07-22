package net.minecraft.client.gui.screens.multiplayer;

import com.google.common.collect.Lists;
import com.google.common.hash.Hashing;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
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
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerList;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.server.LanServer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@OnlyIn(Dist.CLIENT)
public class ServerSelectionList extends ObjectSelectionList<ServerSelectionList.Entry> {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final ThreadPoolExecutor THREAD_POOL = new ScheduledThreadPoolExecutor(
        5,
        new ThreadFactoryBuilder()
            .setNameFormat("Server Pinger #%d")
            .setDaemon(true)
            .setUncaughtExceptionHandler(new DefaultUncaughtExceptionHandler(LOGGER))
            .build()
    );
    private static final ResourceLocation ICON_MISSING = new ResourceLocation("textures/misc/unknown_server.png");
    private static final ResourceLocation ICON_OVERLAY_LOCATION = new ResourceLocation("textures/gui/server_selection.png");
    private static final Component SCANNING_LABEL = new TranslatableComponent("lanServer.scanning");
    private static final Component CANT_RESOLVE_TEXT = new TranslatableComponent("multiplayer.status.cannot_resolve").withStyle(ChatFormatting.DARK_RED);
    private static final Component CANT_CONNECT_TEXT = new TranslatableComponent("multiplayer.status.cannot_connect").withStyle(ChatFormatting.DARK_RED);
    private static final Component CLIENT_OUT_OF_DATE_TOOLTIP = new TranslatableComponent("multiplayer.status.client_out_of_date");
    private static final Component SERVER_OUT_OF_DATE_TOOLTIP = new TranslatableComponent("multiplayer.status.server_out_of_date");
    private static final Component NO_CONNECTION_TOOLTIP = new TranslatableComponent("multiplayer.status.no_connection");
    private static final Component PINGING_TOOLTIP = new TranslatableComponent("multiplayer.status.pinging");
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
        this.onlineServers.forEach(this::addEntry);
        this.addEntry(this.lanHeader);
        this.networkServers.forEach(this::addEntry);
    }

    public void setSelected(@Nullable ServerSelectionList.Entry param0) {
        super.setSelected(param0);
        if (this.getSelected() instanceof ServerSelectionList.OnlineServerEntry) {
            NarratorChatListener.INSTANCE
                .sayNow(new TranslatableComponent("narrator.select", ((ServerSelectionList.OnlineServerEntry)this.getSelected()).serverData.name).getString());
        }

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
            String var1;
            switch((int)(Util.getMillis() / 300L % 4L)) {
                case 0:
                default:
                    var1 = "O o o";
                    break;
                case 1:
                case 3:
                    var1 = "o O o";
                    break;
                case 2:
                    var1 = "o o O";
            }

            this.minecraft.font.draw(param0, var1, (float)(this.minecraft.screen.width / 2 - this.minecraft.font.width(var1) / 2), (float)(var0 + 9), 8421504);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class NetworkServerEntry extends ServerSelectionList.Entry {
        private static final Component LAN_SERVER_HEADER = new TranslatableComponent("lanServer.title");
        private static final Component HIDDEN_ADDRESS_TEXT = new TranslatableComponent("selectServer.hiddenAddress");
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
    }

    @OnlyIn(Dist.CLIENT)
    public class OnlineServerEntry extends ServerSelectionList.Entry {
        private final JoinMultiplayerScreen screen;
        private final Minecraft minecraft;
        private final ServerData serverData;
        private final ResourceLocation iconLocation;
        private String lastIconB64;
        private DynamicTexture icon;
        private long lastClickTime;

        protected OnlineServerEntry(JoinMultiplayerScreen param1, ServerData param2) {
            this.screen = param1;
            this.serverData = param2;
            this.minecraft = Minecraft.getInstance();
            this.iconLocation = new ResourceLocation("servers/" + Hashing.sha1().hashUnencodedChars(param2.ip) + "/icon");
            this.icon = (DynamicTexture)this.minecraft.getTextureManager().getTexture(this.iconLocation);
        }

        @Override
        public void render(PoseStack param0, int param1, int param2, int param3, int param4, int param5, int param6, int param7, boolean param8, float param9) {
            if (!this.serverData.pinged) {
                this.serverData.pinged = true;
                this.serverData.ping = -2L;
                this.serverData.motd = TextComponent.EMPTY;
                this.serverData.status = TextComponent.EMPTY;
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

            boolean var0 = this.serverData.protocol > SharedConstants.getCurrentVersion().getProtocolVersion();
            boolean var1 = this.serverData.protocol < SharedConstants.getCurrentVersion().getProtocolVersion();
            boolean var2 = var0 || var1;
            this.minecraft.font.draw(param0, this.serverData.name, (float)(param3 + 32 + 3), (float)(param2 + 1), 16777215);
            List<FormattedCharSequence> var3 = this.minecraft.font.split(this.serverData.motd, param4 - 32 - 2);

            for(int var4 = 0; var4 < Math.min(var3.size(), 2); ++var4) {
                this.minecraft.font.draw(param0, var3.get(var4), (float)(param3 + 32 + 3), (float)(param2 + 12 + 9 * var4), 8421504);
            }

            Component var5 = (Component)(var2 ? this.serverData.version.copy().withStyle(ChatFormatting.DARK_RED) : this.serverData.status);
            int var6 = this.minecraft.font.width(var5);
            this.minecraft.font.draw(param0, var5, (float)(param3 + param4 - var6 - 15 - 2), (float)(param2 + 1), 8421504);
            int var7 = 0;
            int var8;
            List<Component> var10;
            Component var9;
            if (var2) {
                var8 = 5;
                var9 = var0 ? ServerSelectionList.CLIENT_OUT_OF_DATE_TOOLTIP : ServerSelectionList.SERVER_OUT_OF_DATE_TOOLTIP;
                var10 = this.serverData.playerList;
            } else if (this.serverData.pinged && this.serverData.ping != -2L) {
                if (this.serverData.ping < 0L) {
                    var8 = 5;
                } else if (this.serverData.ping < 150L) {
                    var8 = 0;
                } else if (this.serverData.ping < 300L) {
                    var8 = 1;
                } else if (this.serverData.ping < 600L) {
                    var8 = 2;
                } else if (this.serverData.ping < 1000L) {
                    var8 = 3;
                } else {
                    var8 = 4;
                }

                if (this.serverData.ping < 0L) {
                    var9 = ServerSelectionList.NO_CONNECTION_TOOLTIP;
                    var10 = Collections.emptyList();
                } else {
                    var9 = new TranslatableComponent("multiplayer.status.ping", this.serverData.ping);
                    var10 = this.serverData.playerList;
                }
            } else {
                var7 = 1;
                var8 = (int)(Util.getMillis() / 100L + (long)(param1 * 2) & 7L);
                if (var8 > 4) {
                    var8 = 8 - var8;
                }

                var9 = ServerSelectionList.PINGING_TOOLTIP;
                var10 = Collections.emptyList();
            }

            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            this.minecraft.getTextureManager().bind(GuiComponent.GUI_ICONS_LOCATION);
            GuiComponent.blit(param0, param3 + param4 - 15, param2, (float)(var7 * 10), (float)(176 + var8 * 8), 10, 8, 256, 256);
            String var24 = this.serverData.getIconB64();
            if (!Objects.equals(var24, this.lastIconB64)) {
                if (this.uploadServerIcon(var24)) {
                    this.lastIconB64 = var24;
                } else {
                    this.serverData.setIconB64(null);
                    this.updateServerList();
                }
            }

            if (this.icon != null) {
                this.drawIcon(param0, param3, param2, this.iconLocation);
            } else {
                this.drawIcon(param0, param3, param2, ServerSelectionList.ICON_MISSING);
            }

            int var25 = param6 - param3;
            int var26 = param7 - param2;
            if (var25 >= param4 - 15 && var25 <= param4 - 5 && var26 >= 0 && var26 <= 8) {
                this.screen.setToolTip(Collections.singletonList(var9));
            } else if (var25 >= param4 - var6 - 15 - 2 && var25 <= param4 - 15 - 2 && var26 >= 0 && var26 <= 8) {
                this.screen.setToolTip(var10);
            }

            if (this.minecraft.options.touchscreen || param8) {
                this.minecraft.getTextureManager().bind(ServerSelectionList.ICON_OVERLAY_LOCATION);
                GuiComponent.fill(param0, param3, param2, param3 + 32, param2 + 32, -1601138544);
                RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
                int var27 = param6 - param3;
                int var28 = param7 - param2;
                if (this.canJoin()) {
                    if (var27 < 32 && var27 > 16) {
                        GuiComponent.blit(param0, param3, param2, 0.0F, 32.0F, 32, 32, 256, 256);
                    } else {
                        GuiComponent.blit(param0, param3, param2, 0.0F, 0.0F, 32, 32, 256, 256);
                    }
                }

                if (param1 > 0) {
                    if (var27 < 16 && var28 < 16) {
                        GuiComponent.blit(param0, param3, param2, 96.0F, 32.0F, 32, 32, 256, 256);
                    } else {
                        GuiComponent.blit(param0, param3, param2, 96.0F, 0.0F, 32, 32, 256, 256);
                    }
                }

                if (param1 < this.screen.getServers().size() - 1) {
                    if (var27 < 16 && var28 > 16) {
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
            this.minecraft.getTextureManager().bind(param3);
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
    }
}
