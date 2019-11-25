package com.mojang.realmsclient.gui.screens;

import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.dto.Subscription;
import com.mojang.realmsclient.exception.RealmsServiceException;
import com.mojang.realmsclient.gui.RealmsConstants;
import com.mojang.realmsclient.util.RealmsUtil;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;
import net.minecraft.realms.Realms;
import net.minecraft.realms.RealmsButton;
import net.minecraft.realms.RealmsScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@OnlyIn(Dist.CLIENT)
public class RealmsSubscriptionInfoScreen extends RealmsScreen {
    private static final Logger LOGGER = LogManager.getLogger();
    private final RealmsScreen lastScreen;
    private final RealmsServer serverData;
    private final RealmsScreen mainScreen;
    private final int BUTTON_BACK_ID = 0;
    private final int BUTTON_DELETE_ID = 1;
    private final int BUTTON_SUBSCRIPTION_ID = 2;
    private final String subscriptionTitle;
    private final String subscriptionStartLabelText;
    private final String timeLeftLabelText;
    private final String daysLeftLabelText;
    private int daysLeft;
    private String startDate;
    private Subscription.SubscriptionType type;
    private final String PURCHASE_LINK = "https://aka.ms/ExtendJavaRealms";

    public RealmsSubscriptionInfoScreen(RealmsScreen param0, RealmsServer param1, RealmsScreen param2) {
        this.lastScreen = param0;
        this.serverData = param1;
        this.mainScreen = param2;
        this.subscriptionTitle = getLocalizedString("mco.configure.world.subscription.title");
        this.subscriptionStartLabelText = getLocalizedString("mco.configure.world.subscription.start");
        this.timeLeftLabelText = getLocalizedString("mco.configure.world.subscription.timeleft");
        this.daysLeftLabelText = getLocalizedString("mco.configure.world.subscription.recurring.daysleft");
    }

    @Override
    public void init() {
        this.getSubscription(this.serverData.id);
        Realms.narrateNow(
            this.subscriptionTitle, this.subscriptionStartLabelText, this.startDate, this.timeLeftLabelText, this.daysLeftPresentation(this.daysLeft)
        );
        this.setKeyboardHandlerSendRepeatsToGui(true);
        this.buttonsAdd(
            new RealmsButton(2, this.width() / 2 - 100, RealmsConstants.row(6), getLocalizedString("mco.configure.world.subscription.extend")) {
                @Override
                public void onPress() {
                    String var0 = "https://aka.ms/ExtendJavaRealms?subscriptionId="
                        + RealmsSubscriptionInfoScreen.this.serverData.remoteSubscriptionId
                        + "&profileId="
                        + Realms.getUUID();
                    Realms.setClipboard(var0);
                    RealmsUtil.browseTo(var0);
                }
            }
        );
        this.buttonsAdd(new RealmsButton(0, this.width() / 2 - 100, RealmsConstants.row(12), getLocalizedString("gui.back")) {
            @Override
            public void onPress() {
                Realms.setScreen(RealmsSubscriptionInfoScreen.this.lastScreen);
            }
        });
        if (this.serverData.expired) {
            this.buttonsAdd(
                new RealmsButton(1, this.width() / 2 - 100, RealmsConstants.row(10), getLocalizedString("mco.configure.world.delete.button")) {
                    @Override
                    public void onPress() {
                        String var0 = RealmsScreen.getLocalizedString("mco.configure.world.delete.question.line1");
                        String var1 = RealmsScreen.getLocalizedString("mco.configure.world.delete.question.line2");
                        Realms.setScreen(
                            new RealmsLongConfirmationScreen(RealmsSubscriptionInfoScreen.this, RealmsLongConfirmationScreen.Type.Warning, var0, var1, true, 1)
                        );
                    }
                }
            );
        }

    }

    private void getSubscription(long param0) {
        RealmsClient var0 = RealmsClient.createRealmsClient();

        try {
            Subscription var1 = var0.subscriptionFor(param0);
            this.daysLeft = var1.daysLeft;
            this.startDate = this.localPresentation(var1.startDate);
            this.type = var1.type;
        } catch (RealmsServiceException var5) {
            LOGGER.error("Couldn't get subscription");
            Realms.setScreen(new RealmsGenericErrorScreen(var5, this.lastScreen));
        } catch (IOException var6) {
            LOGGER.error("Couldn't parse response subscribing");
        }

    }

    @Override
    public void confirmResult(boolean param0, int param1) {
        if (param1 == 1 && param0) {
            (new Thread("Realms-delete-realm") {
                @Override
                public void run() {
                    try {
                        RealmsClient var0 = RealmsClient.createRealmsClient();
                        var0.deleteWorld(RealmsSubscriptionInfoScreen.this.serverData.id);
                    } catch (RealmsServiceException var21) {
                        RealmsSubscriptionInfoScreen.LOGGER.error("Couldn't delete world");
                        RealmsSubscriptionInfoScreen.LOGGER.error(var21);
                    } catch (IOException var3) {
                        RealmsSubscriptionInfoScreen.LOGGER.error("Couldn't delete world");
                        var3.printStackTrace();
                    }

                    Realms.setScreen(RealmsSubscriptionInfoScreen.this.mainScreen);
                }
            }).start();
        }

        Realms.setScreen(this);
    }

    private String localPresentation(long param0) {
        Calendar var0 = new GregorianCalendar(TimeZone.getDefault());
        var0.setTimeInMillis(param0);
        return DateFormat.getDateTimeInstance().format(var0.getTime());
    }

    @Override
    public void removed() {
        this.setKeyboardHandlerSendRepeatsToGui(false);
    }

    @Override
    public boolean keyPressed(int param0, int param1, int param2) {
        if (param0 == 256) {
            Realms.setScreen(this.lastScreen);
            return true;
        } else {
            return super.keyPressed(param0, param1, param2);
        }
    }

    @Override
    public void render(int param0, int param1, float param2) {
        this.renderBackground();
        int var0 = this.width() / 2 - 100;
        this.drawCenteredString(this.subscriptionTitle, this.width() / 2, 17, 16777215);
        this.drawString(this.subscriptionStartLabelText, var0, RealmsConstants.row(0), 10526880);
        this.drawString(this.startDate, var0, RealmsConstants.row(1), 16777215);
        if (this.type == Subscription.SubscriptionType.NORMAL) {
            this.drawString(this.timeLeftLabelText, var0, RealmsConstants.row(3), 10526880);
        } else if (this.type == Subscription.SubscriptionType.RECURRING) {
            this.drawString(this.daysLeftLabelText, var0, RealmsConstants.row(3), 10526880);
        }

        this.drawString(this.daysLeftPresentation(this.daysLeft), var0, RealmsConstants.row(4), 16777215);
        super.render(param0, param1, param2);
    }

    private String daysLeftPresentation(int param0) {
        if (param0 == -1 && this.serverData.expired) {
            return getLocalizedString("mco.configure.world.subscription.expired");
        } else if (param0 <= 1) {
            return getLocalizedString("mco.configure.world.subscription.less_than_a_day");
        } else {
            int var0 = param0 / 30;
            int var1 = param0 % 30;
            StringBuilder var2 = new StringBuilder();
            if (var0 > 0) {
                var2.append(var0).append(" ");
                if (var0 == 1) {
                    var2.append(getLocalizedString("mco.configure.world.subscription.month").toLowerCase(Locale.ROOT));
                } else {
                    var2.append(getLocalizedString("mco.configure.world.subscription.months").toLowerCase(Locale.ROOT));
                }
            }

            if (var1 > 0) {
                if (var2.length() > 0) {
                    var2.append(", ");
                }

                var2.append(var1).append(" ");
                if (var1 == 1) {
                    var2.append(getLocalizedString("mco.configure.world.subscription.day").toLowerCase(Locale.ROOT));
                } else {
                    var2.append(getLocalizedString("mco.configure.world.subscription.days").toLowerCase(Locale.ROOT));
                }
            }

            return var2.toString();
        }
    }
}
