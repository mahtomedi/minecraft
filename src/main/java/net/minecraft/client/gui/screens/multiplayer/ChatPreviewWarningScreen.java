package net.minecraft.client.gui.screens.multiplayer;

import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerList;
import net.minecraft.client.multiplayer.chat.ChatPreviewStatus;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ChatPreviewWarningScreen extends WarningScreen {
    private static final Component TITLE = Component.translatable("chatPreview.warning.title").withStyle(ChatFormatting.BOLD);
    private static final Component CHECK = Component.translatable("chatPreview.warning.check");
    private final ServerData serverData;
    @Nullable
    private final Screen lastScreen;

    private static Component content() {
        ChatPreviewStatus var0 = Minecraft.getInstance().options.chatPreview().get();
        return Component.translatable("chatPreview.warning.content", var0.getCaption());
    }

    public ChatPreviewWarningScreen(@Nullable Screen param0, ServerData param1) {
        super(TITLE, content(), CHECK, CommonComponents.joinForNarration(TITLE, content()));
        this.serverData = param1;
        this.lastScreen = param0;
    }

    @Override
    protected void initButtons(int param0) {
        this.addRenderableWidget(new Button(this.width / 2 - 155, 100 + param0, 150, 20, Component.translatable("menu.disconnect"), param0x -> {
            this.minecraft.level.disconnect();
            this.minecraft.clearLevel();
            this.minecraft.setScreen(new JoinMultiplayerScreen(new TitleScreen()));
        }));
        this.addRenderableWidget(new Button(this.width / 2 + 5, 100 + param0, 150, 20, CommonComponents.GUI_PROCEED, param0x -> {
            this.updateOptions();
            this.onClose();
        }));
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    private void updateOptions() {
        if (this.stopShowing != null && this.stopShowing.selected()) {
            ServerData.ChatPreview var0 = this.serverData.getChatPreview();
            if (var0 != null) {
                var0.acknowledge();
                ServerList.saveSingleServer(this.serverData);
            }
        }

    }

    @Override
    protected int getLineHeight() {
        return 9 * 3 / 2;
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.lastScreen);
    }
}
