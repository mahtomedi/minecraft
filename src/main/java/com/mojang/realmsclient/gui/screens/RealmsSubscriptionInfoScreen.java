package com.mojang.realmsclient.gui.screens;

import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.dto.Subscription;
import com.mojang.realmsclient.exception.RealmsServiceException;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.FittingMultiLineTextWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.realms.RealmsScreen;
import net.minecraft.util.CommonLinks;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class RealmsSubscriptionInfoScreen extends RealmsScreen {
    static final Logger LOGGER = LogUtils.getLogger();
    private static final Component SUBSCRIPTION_TITLE = Component.translatable("mco.configure.world.subscription.title");
    private static final Component SUBSCRIPTION_START_LABEL = Component.translatable("mco.configure.world.subscription.start");
    private static final Component TIME_LEFT_LABEL = Component.translatable("mco.configure.world.subscription.timeleft");
    private static final Component DAYS_LEFT_LABEL = Component.translatable("mco.configure.world.subscription.recurring.daysleft");
    private static final Component SUBSCRIPTION_EXPIRED_TEXT = Component.translatable("mco.configure.world.subscription.expired");
    private static final Component SUBSCRIPTION_LESS_THAN_A_DAY_TEXT = Component.translatable("mco.configure.world.subscription.less_than_a_day");
    private static final Component UNKNOWN = Component.translatable("mco.configure.world.subscription.unknown");
    private static final Component RECURRING_INFO = Component.translatable("mco.configure.world.subscription.recurring.info");
    private final Screen lastScreen;
    final RealmsServer serverData;
    final Screen mainScreen;
    private Component daysLeft = UNKNOWN;
    private Component startDate = UNKNOWN;
    @Nullable
    private Subscription.SubscriptionType type;

    public RealmsSubscriptionInfoScreen(Screen param0, RealmsServer param1, Screen param2) {
        super(GameNarrator.NO_TITLE);
        this.lastScreen = param0;
        this.serverData = param1;
        this.mainScreen = param2;
    }

    @Override
    public void init() {
        this.getSubscription(this.serverData.id);
        this.addRenderableWidget(Button.builder(Component.translatable("mco.configure.world.subscription.extend"), param0 -> {
            String var0 = CommonLinks.extendRealms(this.serverData.remoteSubscriptionId, this.minecraft.getUser().getUuid());
            this.minecraft.keyboardHandler.setClipboard(var0);
            Util.getPlatform().openUri(var0);
        }).bounds(this.width / 2 - 100, row(6), 200, 20).build());
        if (this.serverData.expired) {
            this.addRenderableWidget(Button.builder(Component.translatable("mco.configure.world.delete.button"), param0 -> {
                Component var0 = Component.translatable("mco.configure.world.delete.question.line1");
                Component var1 = Component.translatable("mco.configure.world.delete.question.line2");
                this.minecraft.setScreen(new RealmsLongConfirmationScreen(this::deleteRealm, RealmsLongConfirmationScreen.Type.WARNING, var0, var1, true));
            }).bounds(this.width / 2 - 100, row(10), 200, 20).build());
        } else {
            this.addRenderableWidget(new FittingMultiLineTextWidget(this.width / 2 - 100, row(8), 200, 46, RECURRING_INFO, this.font).setColor(10526880));
        }

        this.addRenderableWidget(
            Button.builder(CommonComponents.GUI_BACK, param0 -> this.minecraft.setScreen(this.lastScreen))
                .bounds(this.width / 2 - 100, row(12), 200, 20)
                .build()
        );
    }

    @Override
    public Component getNarrationMessage() {
        return CommonComponents.joinLines(SUBSCRIPTION_TITLE, SUBSCRIPTION_START_LABEL, this.startDate, TIME_LEFT_LABEL, this.daysLeft);
    }

    private void deleteRealm(boolean param0) {
        if (param0) {
            (new Thread("Realms-delete-realm") {
                    @Override
                    public void run() {
                        try {
                            RealmsClient var0 = RealmsClient.create();
                            var0.deleteWorld(RealmsSubscriptionInfoScreen.this.serverData.id);
                        } catch (RealmsServiceException var2) {
                            RealmsSubscriptionInfoScreen.LOGGER.error("Couldn't delete world", (Throwable)var2);
                        }
    
                        RealmsSubscriptionInfoScreen.this.minecraft
                            .execute(() -> RealmsSubscriptionInfoScreen.this.minecraft.setScreen(RealmsSubscriptionInfoScreen.this.mainScreen));
                    }
                })
                .start();
        }

        this.minecraft.setScreen(this);
    }

    private void getSubscription(long param0) {
        RealmsClient var0 = RealmsClient.create();

        try {
            Subscription var1 = var0.subscriptionFor(param0);
            this.daysLeft = this.daysLeftPresentation(var1.daysLeft);
            this.startDate = localPresentation(var1.startDate);
            this.type = var1.type;
        } catch (RealmsServiceException var5) {
            LOGGER.error("Couldn't get subscription");
            this.minecraft.setScreen(new RealmsGenericErrorScreen(var5, this.lastScreen));
        }

    }

    private static Component localPresentation(long param0) {
        Calendar var0 = new GregorianCalendar(TimeZone.getDefault());
        var0.setTimeInMillis(param0);
        return Component.literal(DateFormat.getDateTimeInstance().format(var0.getTime()));
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

    @Override
    public void render(GuiGraphics param0, int param1, int param2, float param3) {
        this.renderBackground(param0);
        int var0 = this.width / 2 - 100;
        param0.drawCenteredString(this.font, SUBSCRIPTION_TITLE, this.width / 2, 17, 16777215);
        param0.drawString(this.font, SUBSCRIPTION_START_LABEL, var0, row(0), 10526880, false);
        param0.drawString(this.font, this.startDate, var0, row(1), 16777215, false);
        if (this.type == Subscription.SubscriptionType.NORMAL) {
            param0.drawString(this.font, TIME_LEFT_LABEL, var0, row(3), 10526880, false);
        } else if (this.type == Subscription.SubscriptionType.RECURRING) {
            param0.drawString(this.font, DAYS_LEFT_LABEL, var0, row(3), 10526880, false);
        }

        param0.drawString(this.font, this.daysLeft, var0, row(4), 16777215, false);
        super.render(param0, param1, param2, param3);
    }

    private Component daysLeftPresentation(int param0) {
        if (param0 < 0 && this.serverData.expired) {
            return SUBSCRIPTION_EXPIRED_TEXT;
        } else if (param0 <= 1) {
            return SUBSCRIPTION_LESS_THAN_A_DAY_TEXT;
        } else {
            int var0 = param0 / 30;
            int var1 = param0 % 30;
            boolean var2 = var0 > 0;
            boolean var3 = var1 > 0;
            if (var2 && var3) {
                return Component.translatable("mco.configure.world.subscription.remaining.months.days", var0, var1);
            } else if (var2) {
                return Component.translatable("mco.configure.world.subscription.remaining.months", var0);
            } else {
                return var3 ? Component.translatable("mco.configure.world.subscription.remaining.days", var1) : Component.empty();
            }
        }
    }
}
