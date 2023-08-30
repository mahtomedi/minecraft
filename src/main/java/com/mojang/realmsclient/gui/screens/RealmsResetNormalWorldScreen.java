package com.mojang.realmsclient.gui.screens;

import com.mojang.realmsclient.util.LevelType;
import com.mojang.realmsclient.util.WorldGenerationInfo;
import java.util.function.Consumer;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
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
public class RealmsResetNormalWorldScreen extends RealmsScreen {
    private static final Component SEED_LABEL = Component.translatable("mco.reset.world.seed");
    public static final Component TITLE = Component.translatable("mco.reset.world.generate");
    private static final int BUTTON_SPACING = 10;
    private static final int CONTENT_WIDTH = 210;
    private final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this);
    private final Consumer<WorldGenerationInfo> callback;
    private EditBox seedEdit;
    private LevelType levelType = LevelType.DEFAULT;
    private boolean generateStructures = true;
    private final Component buttonTitle;

    public RealmsResetNormalWorldScreen(Consumer<WorldGenerationInfo> param0, Component param1) {
        super(TITLE);
        this.callback = param0;
        this.buttonTitle = param1;
    }

    @Override
    public void init() {
        this.seedEdit = new EditBox(this.font, 210, 20, Component.translatable("mco.reset.world.seed"));
        this.seedEdit.setMaxLength(32);
        this.setInitialFocus(this.seedEdit);
        this.layout.addToHeader(new StringWidget(this.title, this.font));
        LinearLayout var0 = this.layout.addToContents(LinearLayout.vertical()).spacing(10);
        var0.addChild(CommonLayouts.labeledElement(this.font, this.seedEdit, SEED_LABEL));
        var0.addChild(
            CycleButton.builder(LevelType::getName)
                .withValues(LevelType.values())
                .withInitialValue(this.levelType)
                .create(0, 0, 210, 20, Component.translatable("selectWorld.mapType"), (param0, param1) -> this.levelType = param1)
        );
        var0.addChild(
            CycleButton.onOffBuilder(this.generateStructures)
                .create(0, 0, 210, 20, Component.translatable("selectWorld.mapFeatures"), (param0, param1) -> this.generateStructures = param1)
        );
        LinearLayout var1 = this.layout.addToFooter(LinearLayout.horizontal().spacing(10));
        var1.addChild(Button.builder(this.buttonTitle, param0 -> this.callback.accept(this.createWorldGenerationInfo())).build());
        var1.addChild(Button.builder(CommonComponents.GUI_BACK, param0 -> this.onClose()).build());
        this.layout.visitWidgets(param1 -> {
        });
        this.repositionElements();
    }

    private WorldGenerationInfo createWorldGenerationInfo() {
        return new WorldGenerationInfo(this.seedEdit.getValue(), this.levelType, this.generateStructures);
    }

    @Override
    protected void repositionElements() {
        this.layout.arrangeElements();
    }

    @Override
    public void onClose() {
        this.callback.accept(null);
    }
}
