package net.minecraft.client.gui.screens.multiplayer;

import com.google.common.collect.Lists;
import com.google.common.hash.Hashing;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.NativeImage;
import java.net.UnknownHostException;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import net.minecraft.ChatFormatting;
import net.minecraft.DefaultUncaughtExceptionHandler;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerList;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.server.LanServer;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
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

    public void setSelected(ServerSelectionList.Entry param0) {
        super.setSelected(param0);
        if (this.getSelected() instanceof ServerSelectionList.OnlineServerEntry) {
            NarratorChatListener.INSTANCE
                .sayNow(new TranslatableComponent("narrator.select", ((ServerSelectionList.OnlineServerEntry)this.getSelected()).serverData.name).getString());
        }

    }

    @Override
    protected void moveSelection(int param0) {
        int var0 = this.children().indexOf(this.getSelected());
        int var1 = Mth.clamp(var0 + param0, 0, this.getItemCount() - 1);
        ServerSelectionList.Entry var2 = this.children().get(var1);
        super.setSelected(var2);
        if (var2 instanceof ServerSelectionList.LANHeader) {
            if (param0 <= 0 || var1 != this.getItemCount() - 1) {
                if (param0 >= 0 || var1 != 0) {
                    this.moveSelection(param0);
                }
            }
        } else {
            this.ensureVisible(var2);
            this.screen.onSelectedChange();
        }
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
        public void render(int param0, int param1, int param2, int param3, int param4, int param5, int param6, boolean param7, float param8) {
            int var0 = param1 + param4 / 2 - 9 / 2;
            this.minecraft
                .font
                .draw(
                    I18n.get("lanServer.scanning"),
                    (float)(this.minecraft.screen.width / 2 - this.minecraft.font.width(I18n.get("lanServer.scanning")) / 2),
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

            this.minecraft.font.draw(var1, (float)(this.minecraft.screen.width / 2 - this.minecraft.font.width(var1) / 2), (float)(var0 + 9), 8421504);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class NetworkServerEntry extends ServerSelectionList.Entry {
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
        public void render(int param0, int param1, int param2, int param3, int param4, int param5, int param6, boolean param7, float param8) {
            this.minecraft.font.draw(I18n.get("lanServer.title"), (float)(param2 + 32 + 3), (float)(param1 + 1), 16777215);
            this.minecraft.font.draw(this.serverData.getMotd(), (float)(param2 + 32 + 3), (float)(param1 + 12), 8421504);
            if (this.minecraft.options.hideServerAddress) {
                this.minecraft.font.draw(I18n.get("selectServer.hiddenAddress"), (float)(param2 + 32 + 3), (float)(param1 + 12 + 11), 3158064);
            } else {
                this.minecraft.font.draw(this.serverData.getAddress(), (float)(param2 + 32 + 3), (float)(param1 + 12 + 11), 3158064);
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
        public void render(int param0, int param1, int param2, int param3, int param4, int param5, int param6, boolean param7, float param8) {
            if (!this.serverData.pinged) {
                this.serverData.pinged = true;
                this.serverData.ping = -2L;
                this.serverData.motd = "";
                this.serverData.status = "";
                ServerSelectionList.THREAD_POOL.submit(() -> {
                    try {
                        this.screen.getPinger().pingServer(this.serverData);
                    } catch (UnknownHostException var2x) {
                        this.serverData.ping = -1L;
                        this.serverData.motd = ChatFormatting.DARK_RED + I18n.get("multiplayer.status.cannot_resolve");
                    } catch (Exception var3x) {
                        this.serverData.ping = -1L;
                        this.serverData.motd = ChatFormatting.DARK_RED + I18n.get("multiplayer.status.cannot_connect");
                    }

                });
            }

            boolean var0 = this.serverData.protocol > SharedConstants.getCurrentVersion().getProtocolVersion();
            boolean var1 = this.serverData.protocol < SharedConstants.getCurrentVersion().getProtocolVersion();
            boolean var2 = var0 || var1;
            this.minecraft.font.draw(this.serverData.name, (float)(param2 + 32 + 3), (float)(param1 + 1), 16777215);
            List<String> var3 = this.minecraft.font.split(this.serverData.motd, param3 - 32 - 2);

            for(int var4 = 0; var4 < Math.min(var3.size(), 2); ++var4) {
                this.minecraft.font.draw(var3.get(var4), (float)(param2 + 32 + 3), (float)(param1 + 12 + 9 * var4), 8421504);
            }

            String var5 = var2 ? ChatFormatting.DARK_RED + this.serverData.version : this.serverData.status;
            int var6 = this.minecraft.font.width(var5);
            this.minecraft.font.draw(var5, (float)(param2 + param3 - var6 - 15 - 2), (float)(param1 + 1), 8421504);
            int var7 = 0;
            String var8 = null;
            int var9;
            String var10;
            if (var2) {
                var9 = 5;
                var10 = I18n.get(var0 ? "multiplayer.status.client_out_of_date" : "multiplayer.status.server_out_of_date");
                var8 = this.serverData.playerList;
            } else if (this.serverData.pinged && this.serverData.ping != -2L) {
                if (this.serverData.ping < 0L) {
                    var9 = 5;
                } else if (this.serverData.ping < 150L) {
                    var9 = 0;
                } else if (this.serverData.ping < 300L) {
                    var9 = 1;
                } else if (this.serverData.ping < 600L) {
                    var9 = 2;
                } else if (this.serverData.ping < 1000L) {
                    var9 = 3;
                } else {
                    var9 = 4;
                }

                if (this.serverData.ping < 0L) {
                    var10 = I18n.get("multiplayer.status.no_connection");
                } else {
                    var10 = this.serverData.ping + "ms";
                    var8 = this.serverData.playerList;
                }
            } else {
                var7 = 1;
                var9 = (int)(Util.getMillis() / 100L + (long)(param0 * 2) & 7L);
                if (var9 > 4) {
                    var9 = 8 - var9;
                }

                var10 = I18n.get("multiplayer.status.pinging");
            }

            GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            this.minecraft.getTextureManager().bind(GuiComponent.GUI_ICONS_LOCATION);
            GuiComponent.blit(param2 + param3 - 15, param1, (float)(var7 * 10), (float)(176 + var9 * 8), 10, 8, 256, 256);
            if (this.serverData.getIconB64() != null && !this.serverData.getIconB64().equals(this.lastIconB64)) {
                this.lastIconB64 = this.serverData.getIconB64();
                this.loadServerIcon();
                this.screen.getServers().save();
            }

            if (this.icon != null) {
                this.drawIcon(param2, param1, this.iconLocation);
            } else {
                this.drawIcon(param2, param1, ServerSelectionList.ICON_MISSING);
            }

            int var21 = param5 - param2;
            int var22 = param6 - param1;
            if (var21 >= param3 - 15 && var21 <= param3 - 5 && var22 >= 0 && var22 <= 8) {
                this.screen.setToolTip(var10);
            } else if (var21 >= param3 - var6 - 15 - 2 && var21 <= param3 - 15 - 2 && var22 >= 0 && var22 <= 8) {
                this.screen.setToolTip(var8);
            }

            if (this.minecraft.options.touchscreen || param7) {
                this.minecraft.getTextureManager().bind(ServerSelectionList.ICON_OVERLAY_LOCATION);
                GuiComponent.fill(param2, param1, param2 + 32, param1 + 32, -1601138544);
                GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
                int var23 = param5 - param2;
                int var24 = param6 - param1;
                if (this.canJoin()) {
                    if (var23 < 32 && var23 > 16) {
                        GuiComponent.blit(param2, param1, 0.0F, 32.0F, 32, 32, 256, 256);
                    } else {
                        GuiComponent.blit(param2, param1, 0.0F, 0.0F, 32, 32, 256, 256);
                    }
                }

                if (param0 > 0) {
                    if (var23 < 16 && var24 < 16) {
                        GuiComponent.blit(param2, param1, 96.0F, 32.0F, 32, 32, 256, 256);
                    } else {
                        GuiComponent.blit(param2, param1, 96.0F, 0.0F, 32, 32, 256, 256);
                    }
                }

                if (param0 < this.screen.getServers().size() - 1) {
                    if (var23 < 16 && var24 > 16) {
                        GuiComponent.blit(param2, param1, 64.0F, 32.0F, 32, 32, 256, 256);
                    } else {
                        GuiComponent.blit(param2, param1, 64.0F, 0.0F, 32, 32, 256, 256);
                    }
                }
            }

        }

        protected void drawIcon(int param0, int param1, ResourceLocation param2) {
            this.minecraft.getTextureManager().bind(param2);
            GlStateManager.enableBlend();
            GuiComponent.blit(param0, param1, 0.0F, 0.0F, 32, 32, 32, 32);
            GlStateManager.disableBlend();
        }

        private boolean canJoin() {
            return true;
        }

        private void loadServerIcon() {
            String var0 = this.serverData.getIconB64();
            if (var0 == null) {
                this.minecraft.getTextureManager().release(this.iconLocation);
                if (this.icon != null && this.icon.getPixels() != null) {
                    this.icon.getPixels().close();
                }

                this.icon = null;
            } else {
                try {
                    NativeImage var1 = NativeImage.fromBase64(var0);
                    Validate.validState(var1.getWidth() == 64, "Must be 64 pixels wide");
                    Validate.validState(var1.getHeight() == 64, "Must be 64 pixels high");
                    if (this.icon == null) {
                        this.icon = new DynamicTexture(var1);
                    } else {
                        this.icon.setPixels(var1);
                        this.icon.upload();
                    }

                    this.minecraft.getTextureManager().register(this.iconLocation, this.icon);
                } catch (Throwable var3) {
                    ServerSelectionList.LOGGER.error("Invalid icon for server {} ({})", this.serverData.name, this.serverData.ip, var3);
                    this.serverData.setIconB64(null);
                }
            }

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
                    int var3 = Screen.hasShiftDown() ? 0 : var2 - 1;
                    this.screen.getServers().swap(var2, var3);
                    if (this.screen.serverSelectionList.getSelected() == this) {
                        this.screen.setSelected(this);
                    }

                    this.screen.serverSelectionList.updateOnlineServers(this.screen.getServers());
                    return true;
                }

                if (var0 < 16.0 && var1 > 16.0 && var2 < this.screen.getServers().size() - 1) {
                    ServerList var4 = this.screen.getServers();
                    int var5 = Screen.hasShiftDown() ? var4.size() - 1 : var2 + 1;
                    var4.swap(var2, var5);
                    if (this.screen.serverSelectionList.getSelected() == this) {
                        this.screen.setSelected(this);
                    }

                    this.screen.serverSelectionList.updateOnlineServers(var4);
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
