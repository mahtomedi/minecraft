package net.minecraft.client.gui.screens.multiplayer;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import java.util.List;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.DirectJoinServerScreen;
import net.minecraft.client.gui.screens.EditServerScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerList;
import net.minecraft.client.multiplayer.ServerStatusPinger;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.server.LanServer;
import net.minecraft.client.server.LanServerDetection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@OnlyIn(Dist.CLIENT)
public class JoinMultiplayerScreen extends Screen {
    private static final Logger LOGGER = LogManager.getLogger();
    private final ServerStatusPinger pinger = new ServerStatusPinger();
    private final Screen lastScreen;
    protected ServerSelectionList serverSelectionList;
    private ServerList servers;
    private Button editButton;
    private Button selectButton;
    private Button deleteButton;
    private String toolTip;
    private ServerData editingServer;
    private LanServerDetection.LanServerList lanServerList;
    private LanServerDetection.LanServerDetector lanServerDetector;
    private boolean initedOnce;

    public JoinMultiplayerScreen(Screen param0) {
        super(new TranslatableComponent("multiplayer.title"));
        this.lastScreen = param0;
    }

    @Override
    protected void init() {
        super.init();
        this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
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
            } catch (Exception var2) {
                LOGGER.warn("Unable to start LAN server detection: {}", var2.getMessage());
            }

            this.serverSelectionList = new ServerSelectionList(this, this.minecraft, this.width, this.height, 32, this.height - 64, 36);
            this.serverSelectionList.updateOnlineServers(this.servers);
        }

        this.children.add(this.serverSelectionList);
        this.selectButton = this.addButton(
            new Button(this.width / 2 - 154, this.height - 52, 100, 20, I18n.get("selectServer.select"), param0 -> this.joinSelectedServer())
        );
        this.addButton(new Button(this.width / 2 - 50, this.height - 52, 100, 20, I18n.get("selectServer.direct"), param0 -> {
            this.editingServer = new ServerData(I18n.get("selectServer.defaultName"), "", false);
            this.minecraft.setScreen(new DirectJoinServerScreen(this::directJoinCallback, this.editingServer));
        }));
        this.addButton(new Button(this.width / 2 + 4 + 50, this.height - 52, 100, 20, I18n.get("selectServer.add"), param0 -> {
            this.editingServer = new ServerData(I18n.get("selectServer.defaultName"), "", false);
            this.minecraft.setScreen(new EditServerScreen(this::addServerCallback, this.editingServer));
        }));
        this.editButton = this.addButton(new Button(this.width / 2 - 154, this.height - 28, 70, 20, I18n.get("selectServer.edit"), param0 -> {
            ServerSelectionList.Entry var0x = this.serverSelectionList.getSelected();
            if (var0x instanceof ServerSelectionList.OnlineServerEntry) {
                ServerData var1 = ((ServerSelectionList.OnlineServerEntry)var0x).getServerData();
                this.editingServer = new ServerData(var1.name, var1.ip, false);
                this.editingServer.copyFrom(var1);
                this.minecraft.setScreen(new EditServerScreen(this::editServerCallback, this.editingServer));
            }

        }));
        this.deleteButton = this.addButton(new Button(this.width / 2 - 74, this.height - 28, 70, 20, I18n.get("selectServer.delete"), param0 -> {
            ServerSelectionList.Entry var0x = this.serverSelectionList.getSelected();
            if (var0x instanceof ServerSelectionList.OnlineServerEntry) {
                String var1 = ((ServerSelectionList.OnlineServerEntry)var0x).getServerData().name;
                if (var1 != null) {
                    Component var2x = new TranslatableComponent("selectServer.deleteQuestion");
                    Component var3 = new TranslatableComponent("selectServer.deleteWarning", var1);
                    String var4 = I18n.get("selectServer.deleteButton");
                    String var5 = I18n.get("gui.cancel");
                    this.minecraft.setScreen(new ConfirmScreen(this::deleteCallback, var2x, var3, var4, var5));
                }
            }

        }));
        this.addButton(new Button(this.width / 2 + 4, this.height - 28, 70, 20, I18n.get("selectServer.refresh"), param0 -> this.refreshServerList()));
        this.addButton(
            new Button(this.width / 2 + 4 + 76, this.height - 28, 75, 20, I18n.get("gui.cancel"), param0 -> this.minecraft.setScreen(this.lastScreen))
        );
        this.onSelectedChange();
    }

    @Override
    public void tick() {
        super.tick();
        if (this.lanServerList.isDirty()) {
            List<LanServer> var0 = this.lanServerList.getServers();
            this.lanServerList.markClean();
            this.serverSelectionList.updateNetworkServers(var0);
        }

        this.pinger.tick();
    }

    @Override
    public void removed() {
        this.minecraft.keyboardHandler.setSendRepeatsToGui(false);
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
            this.servers.add(this.editingServer);
            this.servers.save();
            this.serverSelectionList.setSelected(null);
            this.serverSelectionList.updateOnlineServers(this.servers);
        }

        this.minecraft.setScreen(this);
    }

    private void directJoinCallback(boolean param0) {
        if (param0) {
            this.join(this.editingServer);
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
            if (param0 != 257 && param0 != 335) {
                return this.serverSelectionList.keyPressed(param0, param1, param2);
            } else {
                this.joinSelectedServer();
                return true;
            }
        } else {
            return false;
        }
    }

    @Override
    public void render(int param0, int param1, float param2) {
        this.toolTip = null;
        this.renderBackground();
        this.serverSelectionList.render(param0, param1, param2);
        this.drawCenteredString(this.font, this.title.getColoredString(), this.width / 2, 20, 16777215);
        super.render(param0, param1, param2);
        if (this.toolTip != null) {
            this.renderTooltip(Lists.newArrayList(Splitter.on("\n").split(this.toolTip)), param0, param1);
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
        this.minecraft.setScreen(new ConnectScreen(this, this.minecraft, param0));
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

    public void setToolTip(String param0) {
        this.toolTip = param0;
    }

    public ServerList getServers() {
        return this.servers;
    }
}
