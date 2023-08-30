package com.mojang.realmsclient.gui.screens;

import com.mojang.realmsclient.RealmsMainScreen;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.util.task.WorldCreationTask;
import net.minecraft.Util;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.CommonLayouts;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.realms.RealmsScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RealmsCreateRealmScreen extends RealmsScreen {
    private static final Component NAME_LABEL = Component.translatable("mco.configure.world.name");
    private static final Component DESCRIPTION_LABEL = Component.translatable("mco.configure.world.description");
    private static final int BUTTON_SPACING = 10;
    private static final int CONTENT_WIDTH = 210;
    private final RealmsServer server;
    private final RealmsMainScreen lastScreen;
    private final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this);
    private EditBox nameBox;
    private EditBox descriptionBox;

    public RealmsCreateRealmScreen(RealmsServer param0, RealmsMainScreen param1) {
        super(Component.translatable("mco.selectServer.create"));
        this.server = param0;
        this.lastScreen = param1;
    }

    @Override
    public void init() {
        this.layout.addToHeader(new StringWidget(this.title, this.font));
        LinearLayout var0 = this.layout.addToContents(LinearLayout.vertical()).spacing(10);
        Button var1 = Button.builder(Component.translatable("mco.create.world"), param0 -> this.createWorld()).build();
        var1.active = false;
        this.nameBox = new EditBox(this.font, 210, 20, Component.translatable("mco.configure.world.name"));
        this.nameBox.setResponder(param1 -> var1.active = !Util.isBlank(param1));
        this.descriptionBox = new EditBox(this.font, 210, 20, Component.translatable("mco.configure.world.description"));
        var0.addChild(CommonLayouts.labeledElement(this.font, this.nameBox, NAME_LABEL));
        var0.addChild(CommonLayouts.labeledElement(this.font, this.descriptionBox, DESCRIPTION_LABEL));
        LinearLayout var2 = this.layout.addToFooter(LinearLayout.horizontal().spacing(10));
        var2.addChild(var1);
        var2.addChild(Button.builder(CommonComponents.GUI_CANCEL, param0 -> this.onClose()).build());
        this.layout.visitWidgets(param1 -> {
        });
        this.repositionElements();
        this.setInitialFocus(this.nameBox);
    }

    @Override
    protected void repositionElements() {
        this.layout.arrangeElements();
    }

    private void createWorld() {
        RealmsResetWorldScreen var0 = RealmsResetWorldScreen.forNewRealm(this.lastScreen, this.server, () -> this.minecraft.execute(() -> {
                this.lastScreen.refreshServerList();
                this.minecraft.setScreen(this.lastScreen.newScreen());
            }));
        this.minecraft
            .setScreen(
                new RealmsLongRunningMcoTaskScreen(
                    this.lastScreen, new WorldCreationTask(this.server.id, this.nameBox.getValue(), this.descriptionBox.getValue(), var0)
                )
            );
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.lastScreen);
    }
}
