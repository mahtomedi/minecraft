package com.mojang.realmsclient.gui.screens;

import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.exception.RealmsServiceException;
import com.mojang.realmsclient.util.task.GetServerDetailsTask;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.realms.RealmsScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class RealmsTermsScreen extends RealmsScreen {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Component TITLE = Component.translatable("mco.terms.title");
    private static final Component TERMS_STATIC_TEXT = Component.translatable("mco.terms.sentence.1");
    private static final Component TERMS_LINK_TEXT = CommonComponents.space()
        .append(Component.translatable("mco.terms.sentence.2").withStyle(Style.EMPTY.withUnderlined(true)));
    private final Screen lastScreen;
    private final RealmsServer realmsServer;
    private boolean onLink;

    public RealmsTermsScreen(Screen param0, RealmsServer param1) {
        super(TITLE);
        this.lastScreen = param0;
        this.realmsServer = param1;
    }

    @Override
    public void init() {
        int var0 = this.width / 4 - 2;
        this.addRenderableWidget(
            Button.builder(Component.translatable("mco.terms.buttons.agree"), param0 -> this.agreedToTos()).bounds(this.width / 4, row(12), var0, 20).build()
        );
        this.addRenderableWidget(
            Button.builder(Component.translatable("mco.terms.buttons.disagree"), param0 -> this.minecraft.setScreen(this.lastScreen))
                .bounds(this.width / 2 + 4, row(12), var0, 20)
                .build()
        );
    }

    @Override
    public boolean keyPressed(int param0, int param1, int param2) {
        if (param0 == 256) {
            this.minecraft.setScreen(this.lastScreen);
            return true;
        } else {
            return super.keyPressed(param0, param1, param2);
        }
    }

    private void agreedToTos() {
        RealmsClient var0 = RealmsClient.create();

        try {
            var0.agreeToTos();
            this.minecraft.setScreen(new RealmsLongRunningMcoTaskScreen(this.lastScreen, new GetServerDetailsTask(this.lastScreen, this.realmsServer)));
        } catch (RealmsServiceException var3) {
            LOGGER.error("Couldn't agree to TOS", (Throwable)var3);
        }

    }

    @Override
    public boolean mouseClicked(double param0, double param1, int param2) {
        if (this.onLink) {
            this.minecraft.keyboardHandler.setClipboard("https://aka.ms/MinecraftRealmsTerms");
            Util.getPlatform().openUri("https://aka.ms/MinecraftRealmsTerms");
            return true;
        } else {
            return super.mouseClicked(param0, param1, param2);
        }
    }

    @Override
    public Component getNarrationMessage() {
        return CommonComponents.joinForNarration(super.getNarrationMessage(), TERMS_STATIC_TEXT).append(CommonComponents.SPACE).append(TERMS_LINK_TEXT);
    }

    @Override
    public void render(GuiGraphics param0, int param1, int param2, float param3) {
        super.render(param0, param1, param2, param3);
        param0.drawCenteredString(this.font, this.title, this.width / 2, 17, -1);
        param0.drawString(this.font, TERMS_STATIC_TEXT, this.width / 2 - 120, row(5), -1, false);
        int var0 = this.font.width(TERMS_STATIC_TEXT);
        int var1 = this.width / 2 - 121 + var0;
        int var2 = row(5);
        int var3 = var1 + this.font.width(TERMS_LINK_TEXT) + 1;
        int var4 = var2 + 1 + 9;
        this.onLink = var1 <= param1 && param1 <= var3 && var2 <= param2 && param2 <= var4;
        param0.drawString(this.font, TERMS_LINK_TEXT, this.width / 2 - 120 + var0, row(5), this.onLink ? 7107012 : 3368635, false);
    }
}
