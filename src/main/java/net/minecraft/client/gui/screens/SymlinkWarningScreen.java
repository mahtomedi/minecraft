package net.minecraft.client.gui.screens;

import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SymlinkWarningScreen extends Screen {
    private static final Component TITLE = Component.translatable("symlink_warning.title").withStyle(ChatFormatting.BOLD);
    private static final Component MESSAGE_TEXT = Component.translatable("symlink_warning.message", "https://aka.ms/MinecraftSymLinks");
    @Nullable
    private final Screen callbackScreen;
    private final GridLayout layout = new GridLayout().rowSpacing(10);

    public SymlinkWarningScreen(@Nullable Screen param0) {
        super(TITLE);
        this.callbackScreen = param0;
    }

    @Override
    protected void init() {
        super.init();
        this.layout.defaultCellSetting().alignHorizontallyCenter();
        GridLayout.RowHelper var0 = this.layout.createRowHelper(1);
        var0.addChild(new StringWidget(this.title, this.font));
        var0.addChild(new MultiLineTextWidget(MESSAGE_TEXT, this.font).setMaxWidth(this.width - 50).setCentered(true));
        int var1 = 120;
        GridLayout var2 = new GridLayout().columnSpacing(5);
        GridLayout.RowHelper var3 = var2.createRowHelper(3);
        var3.addChild(
            Button.builder(CommonComponents.GUI_OPEN_IN_BROWSER, param0 -> Util.getPlatform().openUri("https://aka.ms/MinecraftSymLinks"))
                .size(120, 20)
                .build()
        );
        var3.addChild(
            Button.builder(
                    CommonComponents.GUI_COPY_LINK_TO_CLIPBOARD, param0 -> this.minecraft.keyboardHandler.setClipboard("https://aka.ms/MinecraftSymLinks")
                )
                .size(120, 20)
                .build()
        );
        var3.addChild(Button.builder(CommonComponents.GUI_BACK, param0 -> this.onClose()).size(120, 20).build());
        var0.addChild(var2);
        this.repositionElements();
        this.layout.visitWidgets(this::addRenderableWidget);
    }

    @Override
    protected void repositionElements() {
        this.layout.arrangeElements();
        FrameLayout.centerInRectangle(this.layout, this.getRectangle());
    }

    @Override
    public void render(GuiGraphics param0, int param1, int param2, float param3) {
        this.renderBackground(param0);
        super.render(param0, param1, param2, param3);
    }

    @Override
    public Component getNarrationMessage() {
        return CommonComponents.joinForNarration(super.getNarrationMessage(), MESSAGE_TEXT);
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.callbackScreen);
    }
}
