package net.minecraft.client.gui.screens.multiplayer;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ServerReconfigScreen extends Screen {
    private static final int DISCONNECT_TIME = 600;
    private final Connection connection;
    private Button disconnectButton;
    private int delayTicker;
    private final LinearLayout layout = LinearLayout.vertical();

    public ServerReconfigScreen(Component param0, Connection param1) {
        super(param0);
        this.connection = param1;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    protected void init() {
        this.layout.defaultCellSetting().alignHorizontallyCenter().padding(10);
        this.layout.addChild(new StringWidget(this.title, this.font));
        this.disconnectButton = this.layout
            .addChild(Button.builder(CommonComponents.GUI_DISCONNECT, param0 -> this.connection.disconnect(ConnectScreen.ABORT_CONNECTION)).build());
        this.disconnectButton.active = false;
        this.layout.arrangeElements();
        this.layout.visitWidgets(param1 -> {
        });
        this.repositionElements();
    }

    @Override
    protected void repositionElements() {
        FrameLayout.centerInRectangle(this.layout, this.getRectangle());
    }

    @Override
    public void tick() {
        super.tick();
        ++this.delayTicker;
        if (this.delayTicker == 600) {
            this.disconnectButton.active = true;
        }

        if (this.connection.isConnected()) {
            this.connection.tick();
        } else {
            this.connection.handleDisconnection();
        }

    }
}
