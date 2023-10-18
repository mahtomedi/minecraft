package net.minecraft.client.gui.screens;

import net.minecraft.ChatFormatting;
import net.minecraft.Util;
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
public class NoticeWithLinkScreen extends Screen {
    private static final Component SYMLINK_WORLD_TITLE = Component.translatable("symlink_warning.title.world").withStyle(ChatFormatting.BOLD);
    private static final Component SYMLINK_WORLD_MESSAGE_TEXT = Component.translatable("symlink_warning.message.world", "https://aka.ms/MinecraftSymLinks");
    private static final Component SYMLINK_PACK_TITLE = Component.translatable("symlink_warning.title.pack").withStyle(ChatFormatting.BOLD);
    private static final Component SYMLINK_PACK_MESSAGE_TEXT = Component.translatable("symlink_warning.message.pack", "https://aka.ms/MinecraftSymLinks");
    private final Component message;
    private final String url;
    private final Runnable onClose;
    private final GridLayout layout = new GridLayout().rowSpacing(10);

    public NoticeWithLinkScreen(Component param0, Component param1, String param2, Runnable param3) {
        super(param0);
        this.message = param1;
        this.url = param2;
        this.onClose = param3;
    }

    public static Screen createWorldSymlinkWarningScreen(Runnable param0) {
        return new NoticeWithLinkScreen(SYMLINK_WORLD_TITLE, SYMLINK_WORLD_MESSAGE_TEXT, "https://aka.ms/MinecraftSymLinks", param0);
    }

    public static Screen createPackSymlinkWarningScreen(Runnable param0) {
        return new NoticeWithLinkScreen(SYMLINK_PACK_TITLE, SYMLINK_PACK_MESSAGE_TEXT, "https://aka.ms/MinecraftSymLinks", param0);
    }

    @Override
    protected void init() {
        super.init();
        this.layout.defaultCellSetting().alignHorizontallyCenter();
        GridLayout.RowHelper var0 = this.layout.createRowHelper(1);
        var0.addChild(new StringWidget(this.title, this.font));
        var0.addChild(new MultiLineTextWidget(this.message, this.font).setMaxWidth(this.width - 50).setCentered(true));
        int var1 = 120;
        GridLayout var2 = new GridLayout().columnSpacing(5);
        GridLayout.RowHelper var3 = var2.createRowHelper(3);
        var3.addChild(Button.builder(CommonComponents.GUI_OPEN_IN_BROWSER, param0 -> Util.getPlatform().openUri(this.url)).size(120, 20).build());
        var3.addChild(
            Button.builder(CommonComponents.GUI_COPY_LINK_TO_CLIPBOARD, param0 -> this.minecraft.keyboardHandler.setClipboard(this.url)).size(120, 20).build()
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
    public Component getNarrationMessage() {
        return CommonComponents.joinForNarration(super.getNarrationMessage(), this.message);
    }

    @Override
    public void onClose() {
        this.onClose.run();
    }
}
