package com.mojang.realmsclient.gui.screens;

import com.mojang.realmsclient.RealmsMainScreen;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.util.task.CreateSnapshotRealmTask;
import com.mojang.realmsclient.util.task.WorldCreationTask;
import net.minecraft.Util;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.CommonLayouts;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.realms.RealmsScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RealmsCreateRealmScreen extends RealmsScreen {
    private static final Component CREATE_REALM_TEXT = Component.translatable("mco.selectServer.create");
    private static final Component NAME_LABEL = Component.translatable("mco.configure.world.name");
    private static final Component DESCRIPTION_LABEL = Component.translatable("mco.configure.world.description");
    private static final int BUTTON_SPACING = 10;
    private static final int CONTENT_WIDTH = 210;
    private final RealmsMainScreen lastScreen;
    private final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this);
    private EditBox nameBox;
    private EditBox descriptionBox;
    private final Runnable createWorldRunnable;

    public RealmsCreateRealmScreen(RealmsMainScreen param0, RealmsServer param1) {
        super(CREATE_REALM_TEXT);
        this.lastScreen = param0;
        this.createWorldRunnable = () -> this.createWorld(param1);
    }

    public RealmsCreateRealmScreen(RealmsMainScreen param0, long param1) {
        super(CREATE_REALM_TEXT);
        this.lastScreen = param0;
        this.createWorldRunnable = () -> this.createSnapshotWorld(param1);
    }

    @Override
    public void init() {
        this.layout.addToHeader(new StringWidget(this.title, this.font));
        LinearLayout var0 = this.layout.addToContents(LinearLayout.vertical()).spacing(10);
        Button var1 = Button.builder(CommonComponents.GUI_CONTINUE, param0 -> this.createWorldRunnable.run()).build();
        var1.active = false;
        this.nameBox = new EditBox(this.font, 210, 20, NAME_LABEL);
        this.nameBox.setResponder(param1 -> var1.active = !Util.isBlank(param1));
        this.descriptionBox = new EditBox(this.font, 210, 20, DESCRIPTION_LABEL);
        var0.addChild(CommonLayouts.labeledElement(this.font, this.nameBox, NAME_LABEL));
        var0.addChild(CommonLayouts.labeledElement(this.font, this.descriptionBox, DESCRIPTION_LABEL));
        LinearLayout var2 = this.layout.addToFooter(LinearLayout.horizontal().spacing(10));
        var2.addChild(var1);
        var2.addChild(Button.builder(CommonComponents.GUI_BACK, param0 -> this.onClose()).build());
        this.layout.visitWidgets(param1 -> {
        });
        this.repositionElements();
        this.setInitialFocus(this.nameBox);
    }

    @Override
    protected void repositionElements() {
        this.layout.arrangeElements();
    }

    private void createWorld(RealmsServer param0) {
        WorldCreationTask var0 = new WorldCreationTask(param0.id, this.nameBox.getValue(), this.descriptionBox.getValue());
        RealmsResetWorldScreen var1 = RealmsResetWorldScreen.forNewRealm(this, param0, var0, () -> this.minecraft.execute(() -> {
                RealmsMainScreen.refreshServerList();
                this.minecraft.setScreen(this.lastScreen);
            }));
        this.minecraft.setScreen(var1);
    }

    private void createSnapshotWorld(long param0) {
        Screen var0 = new RealmsResetNormalWorldScreen(
            param1 -> {
                if (param1 == null) {
                    this.minecraft.setScreen(this);
                } else {
                    this.minecraft
                        .setScreen(
                            new RealmsLongRunningMcoTaskScreen(
                                this, new CreateSnapshotRealmTask(this.lastScreen, param0, param1, this.nameBox.getValue(), this.descriptionBox.getValue())
                            )
                        );
                }
            },
            CREATE_REALM_TEXT
        );
        this.minecraft.setScreen(var0);
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.lastScreen);
    }
}
