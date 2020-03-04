package com.mojang.realmsclient.gui.screens;

import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.dto.Subscription;
import com.mojang.realmsclient.exception.RealmsServiceException;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;
import net.minecraft.Util;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.realms.NarrationHelper;
import net.minecraft.realms.RealmsScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@OnlyIn(Dist.CLIENT)
public class RealmsSubscriptionInfoScreen extends RealmsScreen {
    private static final Logger LOGGER = LogManager.getLogger();
    private final Screen lastScreen;
    private final RealmsServer serverData;
    private final Screen mainScreen;
    private final String subscriptionTitle;
    private final String subscriptionStartLabelText;
    private final String timeLeftLabelText;
    private final String daysLeftLabelText;
    private int daysLeft;
    private String startDate;
    private Subscription.SubscriptionType type;

    public RealmsSubscriptionInfoScreen(Screen param0, RealmsServer param1, Screen param2) {
        this.lastScreen = param0;
        this.serverData = param1;
        this.mainScreen = param2;
        this.subscriptionTitle = I18n.get("mco.configure.world.subscription.title");
        this.subscriptionStartLabelText = I18n.get("mco.configure.world.subscription.start");
        this.timeLeftLabelText = I18n.get("mco.configure.world.subscription.timeleft");
        this.daysLeftLabelText = I18n.get("mco.configure.world.subscription.recurring.daysleft");
    }

    @Override
    public void init() {
        this.getSubscription(this.serverData.id);
        NarrationHelper.now(
            this.subscriptionTitle, this.subscriptionStartLabelText, this.startDate, this.timeLeftLabelText, this.daysLeftPresentation(this.daysLeft)
        );
        this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
        this.addButton(
            new Button(
                this.width / 2 - 100,
                row(6),
                200,
                20,
                I18n.get("mco.configure.world.subscription.extend"),
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
        this.addButton(new Button(this.width / 2 - 100, row(12), 200, 20, I18n.get("gui.back"), param0 -> this.minecraft.setScreen(this.lastScreen)));
        if (this.serverData.expired) {
            this.addButton(new Button(this.width / 2 - 100, row(10), 200, 20, I18n.get("mco.configure.world.delete.button"), param0 -> {
                String var0 = I18n.get("mco.configure.world.delete.question.line1");
                String var1 = I18n.get("mco.configure.world.delete.question.line2");
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
            this.daysLeft = var1.daysLeft;
            this.startDate = this.localPresentation(var1.startDate);
            this.type = var1.type;
        } catch (RealmsServiceException var5) {
            LOGGER.error("Couldn't get subscription");
            this.minecraft.setScreen(new RealmsGenericErrorScreen(var5, this.lastScreen));
        }

    }

    private String localPresentation(long param0) {
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
    public void render(int param0, int param1, float param2) {
        this.renderBackground();
        int var0 = this.width / 2 - 100;
        this.drawCenteredString(this.font, this.subscriptionTitle, this.width / 2, 17, 16777215);
        this.font.draw(this.subscriptionStartLabelText, (float)var0, (float)row(0), 10526880);
        this.font.draw(this.startDate, (float)var0, (float)row(1), 16777215);
        if (this.type == Subscription.SubscriptionType.NORMAL) {
            this.font.draw(this.timeLeftLabelText, (float)var0, (float)row(3), 10526880);
        } else if (this.type == Subscription.SubscriptionType.RECURRING) {
            this.font.draw(this.daysLeftLabelText, (float)var0, (float)row(3), 10526880);
        }

        this.font.draw(this.daysLeftPresentation(this.daysLeft), (float)var0, (float)row(4), 16777215);
        super.render(param0, param1, param2);
    }

    private String daysLeftPresentation(int param0) {
        if (param0 == -1 && this.serverData.expired) {
            return I18n.get("mco.configure.world.subscription.expired");
        } else if (param0 <= 1) {
            return I18n.get("mco.configure.world.subscription.less_than_a_day");
        } else {
            int var0 = param0 / 30;
            int var1 = param0 % 30;
            StringBuilder var2 = new StringBuilder();
            if (var0 > 0) {
                var2.append(var0).append(" ");
                if (var0 == 1) {
                    var2.append(I18n.get("mco.configure.world.subscription.month").toLowerCase(Locale.ROOT));
                } else {
                    var2.append(I18n.get("mco.configure.world.subscription.months").toLowerCase(Locale.ROOT));
                }
            }

            if (var1 > 0) {
                if (var2.length() > 0) {
                    var2.append(", ");
                }

                var2.append(var1).append(" ");
                if (var1 == 1) {
                    var2.append(I18n.get("mco.configure.world.subscription.day").toLowerCase(Locale.ROOT));
                } else {
                    var2.append(I18n.get("mco.configure.world.subscription.days").toLowerCase(Locale.ROOT));
                }
            }

            return var2.toString();
        }
    }
}
