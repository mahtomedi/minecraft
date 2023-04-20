package net.minecraft.client.gui.screens.multiplayer;

import com.mojang.logging.LogUtils;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.layouts.SpacerElement;
import net.minecraft.client.gui.navigation.CommonInputs;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.DirectJoinServerScreen;
import net.minecraft.client.gui.screens.EditServerScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerList;
import net.minecraft.client.multiplayer.ServerStatusPinger;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.server.LanServer;
import net.minecraft.client.server.LanServerDetection;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class JoinMultiplayerScreen extends Screen {
    public static final int BUTTON_ROW_WIDTH = 308;
    public static final int TOP_ROW_BUTTON_WIDTH = 100;
    public static final int LOWER_ROW_BUTTON_WIDTH = 74;
    public static final int FOOTER_HEIGHT = 64;
    private static final Logger LOGGER = LogUtils.getLogger();
    private final ServerStatusPinger pinger = new ServerStatusPinger();
    private final Screen lastScreen;
    protected ServerSelectionList serverSelectionList;
    private ServerList servers;
    private Button editButton;
    private Button selectButton;
    private Button deleteButton;
    @Nullable
    private List<Component> toolTip;
    private ServerData editingServer;
    private LanServerDetection.LanServerList lanServerList;
    @Nullable
    private LanServerDetection.LanServerDetector lanServerDetector;
    private boolean initedOnce;

    public JoinMultiplayerScreen(Screen param0) {
        super(Component.translatable("multiplayer.title"));
        this.lastScreen = param0;
    }

    @Override
    protected void init() {
        if (this.initedOnce) {
            this.serverSelectionList.updateSize(this.width, this.height, 32, this.height - 64);
        } else {
            this.initedOnce = true;
            this.servers = new ServerList(this.minecraft);
            this.servers.load();
            this.lanServerList = new LanServerDetection.LanServerList();

            try {
                this.lanServerDetector = new LanServerDetection.LanServerDetector(this.lanServerList);
                this.lanServerDetector.start();
            } catch (Exception var9) {
                LOGGER.warn("Unable to start LAN server detection: {}", var9.getMessage());
            }

            this.serverSelectionList = new ServerSelectionList(this, this.minecraft, this.width, this.height, 32, this.height - 64, 36);
            this.serverSelectionList.updateOnlineServers(this.servers);
        }

        this.addWidget(this.serverSelectionList);
        this.selectButton = this.addRenderableWidget(
            Button.builder(Component.translatable("selectServer.select"), param0 -> this.joinSelectedServer()).width(100).build()
        );
        Button var1 = this.addRenderableWidget(Button.builder(Component.translatable("selectServer.direct"), param0 -> {
            this.editingServer = new ServerData(I18n.get("selectServer.defaultName"), "", false);
            this.minecraft.setScreen(new DirectJoinServerScreen(this, this::directJoinCallback, this.editingServer));
        }).width(100).build());
        Button var2 = this.addRenderableWidget(Button.builder(Component.translatable("selectServer.add"), param0 -> {
            this.editingServer = new ServerData(I18n.get("selectServer.defaultName"), "", false);
            this.minecraft.setScreen(new EditServerScreen(this, this::addServerCallback, this.editingServer));
        }).width(100).build());
        this.editButton = this.addRenderableWidget(Button.builder(Component.translatable("selectServer.edit"), param0 -> {
            ServerSelectionList.Entry var0x = this.serverSelectionList.getSelected();
            if (var0x instanceof ServerSelectionList.OnlineServerEntry) {
                ServerData var1x = ((ServerSelectionList.OnlineServerEntry)var0x).getServerData();
                this.editingServer = new ServerData(var1x.name, var1x.ip, false);
                this.editingServer.copyFrom(var1x);
                this.minecraft.setScreen(new EditServerScreen(this, this::editServerCallback, this.editingServer));
            }

        }).width(74).build());
        this.deleteButton = this.addRenderableWidget(Button.builder(Component.translatable("selectServer.delete"), param0 -> {
            ServerSelectionList.Entry var0x = this.serverSelectionList.getSelected();
            if (var0x instanceof ServerSelectionList.OnlineServerEntry) {
                String var1x = ((ServerSelectionList.OnlineServerEntry)var0x).getServerData().name;
                if (var1x != null) {
                    Component var2x = Component.translatable("selectServer.deleteQuestion");
                    Component var3x = Component.translatable("selectServer.deleteWarning", var1x);
                    Component var4x = Component.translatable("selectServer.deleteButton");
                    Component var5x = CommonComponents.GUI_CANCEL;
                    this.minecraft.setScreen(new ConfirmScreen(this::deleteCallback, var2x, var3x, var4x, var5x));
                }
            }

        }).width(74).build());
        Button var3 = this.addRenderableWidget(
            Button.builder(Component.translatable("selectServer.refresh"), param0 -> this.refreshServerList()).width(74).build()
        );
        Button var4 = this.addRenderableWidget(
            Button.builder(CommonComponents.GUI_CANCEL, param0 -> this.minecraft.setScreen(this.lastScreen)).width(74).build()
        );
        GridLayout var5 = new GridLayout();
        GridLayout.RowHelper var6 = var5.createRowHelper(1);
        LinearLayout var7 = var6.addChild(new LinearLayout(308, 20, LinearLayout.Orientation.HORIZONTAL));
        var7.addChild(this.selectButton);
        var7.addChild(var1);
        var7.addChild(var2);
        var6.addChild(SpacerElement.height(4));
        LinearLayout var8 = var6.addChild(new LinearLayout(308, 20, LinearLayout.Orientation.HORIZONTAL));
        var8.addChild(this.editButton);
        var8.addChild(this.deleteButton);
        var8.addChild(var3);
        var8.addChild(var4);
        var5.arrangeElements();
        FrameLayout.centerInRectangle(var5, 0, this.height - 64, this.width, 64);
        this.onSelectedChange();
    }

    @Override
    public void tick() {
        super.tick();
        List<LanServer> var0 = this.lanServerList.takeDirtyServers();
        if (var0 != null) {
            this.serverSelectionList.updateNetworkServers(var0);
        }

        this.pinger.tick();
    }

    @Override
    public void removed() {
        if (this.lanServerDetector != null) {
            this.lanServerDetector.interrupt();
            this.lanServerDetector = null;
        }

        this.pinger.removeAll();
    }

    private void refreshServerList() {
        this.minecraft.setScreen(new JoinMultiplayerScreen(this.lastScreen));
    }

    private void deleteCallback(boolean param0) {
        ServerSelectionList.Entry var0 = this.serverSelectionList.getSelected();
        if (param0 && var0 instanceof ServerSelectionList.OnlineServerEntry) {
            this.servers.remove(((ServerSelectionList.OnlineServerEntry)var0).getServerData());
            this.servers.save();
            this.serverSelectionList.setSelected(null);
            this.serverSelectionList.updateOnlineServers(this.servers);
        }

        this.minecraft.setScreen(this);
    }

    private void editServerCallback(boolean param0) {
        ServerSelectionList.Entry var0 = this.serverSelectionList.getSelected();
        if (param0 && var0 instanceof ServerSelectionList.OnlineServerEntry) {
            ServerData var1 = ((ServerSelectionList.OnlineServerEntry)var0).getServerData();
            var1.name = this.editingServer.name;
            var1.ip = this.editingServer.ip;
            var1.copyFrom(this.editingServer);
            this.servers.save();
            this.serverSelectionList.updateOnlineServers(this.servers);
        }

        this.minecraft.setScreen(this);
    }

    private void addServerCallback(boolean param0) {
        if (param0) {
            ServerData var0 = this.servers.unhide(this.editingServer.ip);
            if (var0 != null) {
                var0.copyNameIconFrom(this.editingServer);
                this.servers.save();
            } else {
                this.servers.add(this.editingServer, false);
                this.servers.save();
            }

            this.serverSelectionList.setSelected(null);
            this.serverSelectionList.updateOnlineServers(this.servers);
        }

        this.minecraft.setScreen(this);
    }

    private void directJoinCallback(boolean param0) {
        if (param0) {
            ServerData var0 = this.servers.get(this.editingServer.ip);
            if (var0 == null) {
                this.servers.add(this.editingServer, true);
                this.servers.save();
                this.join(this.editingServer);
            } else {
                this.join(var0);
            }
        } else {
            this.minecraft.setScreen(this);
        }

    }

    @Override
    public boolean keyPressed(int param0, int param1, int param2) {
        if (super.keyPressed(param0, param1, param2)) {
            return true;
        } else if (param0 == 294) {
            this.refreshServerList();
            return true;
        } else if (this.serverSelectionList.getSelected() != null) {
            if (CommonInputs.selected(param0)) {
                this.joinSelectedServer();
                return true;
            } else {
                return this.serverSelectionList.keyPressed(param0, param1, param2);
            }
        } else {
            return false;
        }
    }

    @Override
    public void render(GuiGraphics param0, int param1, int param2, float param3) {
        this.toolTip = null;
        this.renderBackground(param0);
        this.serverSelectionList.render(param0, param1, param2, param3);
        param0.drawCenteredString(this.font, this.title, this.width / 2, 20, 16777215);
        super.render(param0, param1, param2, param3);
        if (this.toolTip != null) {
            param0.renderComponentTooltip(this.font, this.toolTip, param1, param2);
        }

    }

    public void joinSelectedServer() {
        ServerSelectionList.Entry var0 = this.serverSelectionList.getSelected();
        if (var0 instanceof ServerSelectionList.OnlineServerEntry) {
            this.join(((ServerSelectionList.OnlineServerEntry)var0).getServerData());
        } else if (var0 instanceof ServerSelectionList.NetworkServerEntry) {
            LanServer var1 = ((ServerSelectionList.NetworkServerEntry)var0).getServerData();
            this.join(new ServerData(var1.getMotd(), var1.getAddress(), true));
        }

    }

    private void join(ServerData param0) {
        ConnectScreen.startConnecting(this, this.minecraft, ServerAddress.parseString(param0.ip), param0, false);
    }

    public void setSelected(ServerSelectionList.Entry param0) {
        this.serverSelectionList.setSelected(param0);
        this.onSelectedChange();
    }

    protected void onSelectedChange() {
        this.selectButton.active = false;
        this.editButton.active = false;
        this.deleteButton.active = false;
        ServerSelectionList.Entry var0 = this.serverSelectionList.getSelected();
        if (var0 != null && !(var0 instanceof ServerSelectionList.LANHeader)) {
            this.selectButton.active = true;
            if (var0 instanceof ServerSelectionList.OnlineServerEntry) {
                this.editButton.active = true;
                this.deleteButton.active = true;
            }
        }

    }

    public ServerStatusPinger getPinger() {
        return this.pinger;
    }

    public void setToolTip(List<Component> param0) {
        this.toolTip = param0;
    }

    public ServerList getServers() {
        return this.servers;
    }
}
