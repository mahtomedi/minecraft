package com.mojang.realmsclient.gui.screens;

import com.mojang.realmsclient.RealmsMainScreen;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.util.task.WorldCreationTask;
import net.minecraft.Util;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LayoutSettings;
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
        var0.defaultCellSetting().alignHorizontallyCenter();
        Button var1 = Button.builder(Component.translatable("mco.create.world"), param0 -> this.createWorld()).build();
        var1.active = false;
        this.nameBox = new EditBox(this.font, 208, 20, Component.translatable("mco.configure.world.name"));
        this.nameBox.setResponder(param1 -> var1.active = !Util.isBlank(param1));
        this.descriptionBox = new EditBox(this.font, 208, 20, Component.translatable("mco.configure.world.description"));
        LinearLayout var2 = var0.addChild(LinearLayout.vertical().spacing(4));
        var2.addChild(new StringWidget(NAME_LABEL, this.font), LayoutSettings::alignHorizontallyLeft);
        var2.addChild(this.nameBox, param0 -> param0.padding(1));
        LinearLayout var3 = var0.addChild(LinearLayout.vertical().spacing(4));
        var3.addChild(new StringWidget(DESCRIPTION_LABEL, this.font), LayoutSettings::alignHorizontallyLeft);
        var3.addChild(this.descriptionBox, param0 -> param0.padding(1));
        LinearLayout var4 = this.layout.addToFooter(LinearLayout.horizontal().spacing(10));
        var4.addChild(var1);
        var4.addChild(Button.builder(CommonComponents.GUI_CANCEL, param0 -> this.onClose()).build());
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
        RealmsResetWorldScreen var0 = new RealmsResetWorldScreen(
            this.lastScreen,
            this.server,
            Component.translatable("mco.selectServer.create"),
            Component.translatable("mco.create.world.subtitle"),
            -6250336,
            Component.translatable("mco.create.world.skip"),
            () -> this.minecraft.execute(() -> this.minecraft.setScreen(this.lastScreen.newScreen())),
            () -> this.minecraft.setScreen(this.lastScreen.newScreen())
        );
        var0.setResetTitle(Component.translatable("mco.create.world.reset.title"));
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
