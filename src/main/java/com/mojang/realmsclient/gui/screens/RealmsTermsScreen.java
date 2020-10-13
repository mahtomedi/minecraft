package com.mojang.realmsclient.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.realmsclient.RealmsMainScreen;
import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.exception.RealmsServiceException;
import com.mojang.realmsclient.util.task.GetServerDetailsTask;
import java.util.concurrent.locks.ReentrantLock;
import net.minecraft.Util;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.realms.RealmsScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@OnlyIn(Dist.CLIENT)
public class RealmsTermsScreen extends RealmsScreen {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Component TITLE = new TranslatableComponent("mco.terms.title");
    private static final Component TERMS_STATIC_TEXT = new TranslatableComponent("mco.terms.sentence.1");
    private static final Component TERMS_LINK_TEXT = new TextComponent(" ")
        .append(new TranslatableComponent("mco.terms.sentence.2").withStyle(Style.EMPTY.withUnderlined(true)));
    private final Screen lastScreen;
    private final RealmsMainScreen mainScreen;
    private final RealmsServer realmsServer;
    private boolean onLink;
    private final String realmsToSUrl = "https://aka.ms/MinecraftRealmsTerms";

    public RealmsTermsScreen(Screen param0, RealmsMainScreen param1, RealmsServer param2) {
        this.lastScreen = param0;
        this.mainScreen = param1;
        this.realmsServer = param2;
    }

    @Override
    public void init() {
        this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
        int var0 = this.width / 4 - 2;
        this.addButton(new Button(this.width / 4, row(12), var0, 20, new TranslatableComponent("mco.terms.buttons.agree"), param0 -> this.agreedToTos()));
        this.addButton(
            new Button(
                this.width / 2 + 4,
                row(12),
                var0,
                20,
                new TranslatableComponent("mco.terms.buttons.disagree"),
                param0 -> this.minecraft.setScreen(this.lastScreen)
            )
        );
    }

    @Override
    public void removed() {
        this.minecraft.keyboardHandler.setSendRepeatsToGui(false);
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
            this.minecraft
                .setScreen(
                    new RealmsLongRunningMcoTaskScreen(
                        this.lastScreen, new GetServerDetailsTask(this.mainScreen, this.lastScreen, this.realmsServer, new ReentrantLock())
                    )
                );
        } catch (RealmsServiceException var3) {
            LOGGER.error("Couldn't agree to TOS");
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
    public void render(PoseStack param0, int param1, int param2, float param3) {
        this.renderBackground(param0);
        drawCenteredString(param0, this.font, TITLE, this.width / 2, 17, 16777215);
        this.font.draw(param0, TERMS_STATIC_TEXT, (float)(this.width / 2 - 120), (float)row(5), 16777215);
        int var0 = this.font.width(TERMS_STATIC_TEXT);
        int var1 = this.width / 2 - 121 + var0;
        int var2 = row(5);
        int var3 = var1 + this.font.width(TERMS_LINK_TEXT) + 1;
        int var4 = var2 + 1 + 9;
        this.onLink = var1 <= param1 && param1 <= var3 && var2 <= param2 && param2 <= var4;
        this.font.draw(param0, TERMS_LINK_TEXT, (float)(this.width / 2 - 120 + var0), (float)row(5), this.onLink ? 7107012 : 3368635);
        super.render(param0, param1, param2, param3);
    }
}
