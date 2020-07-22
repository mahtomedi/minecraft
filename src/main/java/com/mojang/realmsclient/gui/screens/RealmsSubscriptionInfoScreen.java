package com.mojang.realmsclient.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.dto.Subscription;
import com.mojang.realmsclient.exception.RealmsServiceException;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import net.minecraft.Util;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.realms.NarrationHelper;
import net.minecraft.realms.RealmsScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@OnlyIn(Dist.CLIENT)
public class RealmsSubscriptionInfoScreen extends RealmsScreen {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Component SUBSCRIPTION_TITLE = new TranslatableComponent("mco.configure.world.subscription.title");
    private static final Component SUBSCRIPTION_START_LABEL = new TranslatableComponent("mco.configure.world.subscription.start");
    private static final Component TIME_LEFT_LABEL = new TranslatableComponent("mco.configure.world.subscription.timeleft");
    private static final Component DAYS_LEFT_LABEL = new TranslatableComponent("mco.configure.world.subscription.recurring.daysleft");
    private static final Component SUBSCRIPTION_EXPIRED_TEXT = new TranslatableComponent("mco.configure.world.subscription.expired");
    private static final Component SUBSCRIPTION_LESS_THAN_A_DAY_TEXT = new TranslatableComponent("mco.configure.world.subscription.less_than_a_day");
    private static final Component MONTH_SUFFIX = new TranslatableComponent("mco.configure.world.subscription.month");
    private static final Component MONTHS_SUFFIX = new TranslatableComponent("mco.configure.world.subscription.months");
    private static final Component DAY_SUFFIX = new TranslatableComponent("mco.configure.world.subscription.day");
    private static final Component DAYS_SUFFIX = new TranslatableComponent("mco.configure.world.subscription.days");
    private final Screen lastScreen;
    private final RealmsServer serverData;
    private final Screen mainScreen;
    private Component daysLeft;
    private String startDate;
    private Subscription.SubscriptionType type;

    public RealmsSubscriptionInfoScreen(Screen param0, RealmsServer param1, Screen param2) {
        this.lastScreen = param0;
        this.serverData = param1;
        this.mainScreen = param2;
    }

    @Override
    public void init() {
        this.getSubscription(this.serverData.id);
        NarrationHelper.now(
            SUBSCRIPTION_TITLE.getString(), SUBSCRIPTION_START_LABEL.getString(), this.startDate, TIME_LEFT_LABEL.getString(), this.daysLeft.getString()
        );
        this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
        this.addButton(
            new Button(
                this.width / 2 - 100,
                row(6),
                200,
                20,
                new TranslatableComponent("mco.configure.world.subscription.extend"),
                param0 -> {
                    String var0 = "https://aka.ms/ExtendJavaRealms?subscriptionId="
                        + this.serverData.remoteSubscriptionId
                        + "&profileId="
                        + this.minecraft.getUser().getUuid();
                    this.minecraft.keyboardHandler.setClipboard(var0);
                    Util.getPlatform().openUri(var0);
                }
            )
        );
        this.addButton(new Button(this.width / 2 - 100, row(12), 200, 20, CommonComponents.GUI_BACK, param0 -> this.minecraft.setScreen(this.lastScreen)));
        if (this.serverData.expired) {
            this.addButton(new Button(this.width / 2 - 100, row(10), 200, 20, new TranslatableComponent("mco.configure.world.delete.button"), param0 -> {
                Component var0 = new TranslatableComponent("mco.configure.world.delete.question.line1");
                Component var1 = new TranslatableComponent("mco.configure.world.delete.question.line2");
                this.minecraft.setScreen(new RealmsLongConfirmationScreen(this::deleteRealm, RealmsLongConfirmationScreen.Type.Warning, var0, var1, true));
            }));
        }

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
                            RealmsSubscriptionInfoScreen.LOGGER.error("Couldn't delete world");
                            RealmsSubscriptionInfoScreen.LOGGER.error(var2);
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

    private static String localPresentation(long param0) {
        Calendar var0 = new GregorianCalendar(TimeZone.getDefault());
        var0.setTimeInMillis(param0);
        return DateFormat.getDateTimeInstance().format(var0.getTime());
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

    @Override
    public void render(PoseStack param0, int param1, int param2, float param3) {
        this.renderBackground(param0);
        int var0 = this.width / 2 - 100;
        drawCenteredString(param0, this.font, SUBSCRIPTION_TITLE, this.width / 2, 17, 16777215);
        this.font.draw(param0, SUBSCRIPTION_START_LABEL, (float)var0, (float)row(0), 10526880);
        this.font.draw(param0, this.startDate, (float)var0, (float)row(1), 16777215);
        if (this.type == Subscription.SubscriptionType.NORMAL) {
            this.font.draw(param0, TIME_LEFT_LABEL, (float)var0, (float)row(3), 10526880);
        } else if (this.type == Subscription.SubscriptionType.RECURRING) {
            this.font.draw(param0, DAYS_LEFT_LABEL, (float)var0, (float)row(3), 10526880);
        }

        this.font.draw(param0, this.daysLeft, (float)var0, (float)row(4), 16777215);
        super.render(param0, param1, param2, param3);
    }

    private Component daysLeftPresentation(int param0) {
        if (param0 == -1 && this.serverData.expired) {
            return SUBSCRIPTION_EXPIRED_TEXT;
        } else if (param0 <= 1) {
            return SUBSCRIPTION_LESS_THAN_A_DAY_TEXT;
        } else {
            int var0 = param0 / 30;
            int var1 = param0 % 30;
            MutableComponent var2 = new TextComponent("");
            if (var0 > 0) {
                var2.append(Integer.toString(var0)).append(" ");
                if (var0 == 1) {
                    var2.append(MONTH_SUFFIX);
                } else {
                    var2.append(MONTHS_SUFFIX);
                }
            }

            if (var1 > 0) {
                if (var0 > 0) {
                    var2.append(", ");
                }

                var2.append(Integer.toString(var1)).append(" ");
                if (var1 == 1) {
                    var2.append(DAY_SUFFIX);
                } else {
                    var2.append(DAYS_SUFFIX);
                }
            }

            return var2;
        }
    }
}
