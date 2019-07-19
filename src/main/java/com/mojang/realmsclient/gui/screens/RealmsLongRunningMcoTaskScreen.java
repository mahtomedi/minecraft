package com.mojang.realmsclient.gui.screens;

import com.mojang.realmsclient.exception.RealmsDefaultUncaughtExceptionHandler;
import com.mojang.realmsclient.gui.LongRunningTask;
import com.mojang.realmsclient.gui.RealmsConstants;
import net.minecraft.realms.Realms;
import net.minecraft.realms.RealmsButton;
import net.minecraft.realms.RealmsScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@OnlyIn(Dist.CLIENT)
public class RealmsLongRunningMcoTaskScreen extends RealmsScreen {
    private static final Logger LOGGER = LogManager.getLogger();
    private final int BUTTON_CANCEL_ID = 666;
    private final int BUTTON_BACK_ID = 667;
    private final RealmsScreen lastScreen;
    private final LongRunningTask taskThread;
    private volatile String title = "";
    private volatile boolean error;
    private volatile String errorMessage;
    private volatile boolean aborted;
    private int animTicks;
    private final LongRunningTask task;
    private final int buttonLength = 212;
    public static final String[] symbols = new String[]{
        "\u2583 \u2584 \u2585 \u2586 \u2587 \u2588 \u2587 \u2586 \u2585 \u2584 \u2583",
        "_ \u2583 \u2584 \u2585 \u2586 \u2587 \u2588 \u2587 \u2586 \u2585 \u2584",
        "_ _ \u2583 \u2584 \u2585 \u2586 \u2587 \u2588 \u2587 \u2586 \u2585",
        "_ _ _ \u2583 \u2584 \u2585 \u2586 \u2587 \u2588 \u2587 \u2586",
        "_ _ _ _ \u2583 \u2584 \u2585 \u2586 \u2587 \u2588 \u2587",
        "_ _ _ _ _ \u2583 \u2584 \u2585 \u2586 \u2587 \u2588",
        "_ _ _ _ \u2583 \u2584 \u2585 \u2586 \u2587 \u2588 \u2587",
        "_ _ _ \u2583 \u2584 \u2585 \u2586 \u2587 \u2588 \u2587 \u2586",
        "_ _ \u2583 \u2584 \u2585 \u2586 \u2587 \u2588 \u2587 \u2586 \u2585",
        "_ \u2583 \u2584 \u2585 \u2586 \u2587 \u2588 \u2587 \u2586 \u2585 \u2584",
        "\u2583 \u2584 \u2585 \u2586 \u2587 \u2588 \u2587 \u2586 \u2585 \u2584 \u2583",
        "\u2584 \u2585 \u2586 \u2587 \u2588 \u2587 \u2586 \u2585 \u2584 \u2583 _",
        "\u2585 \u2586 \u2587 \u2588 \u2587 \u2586 \u2585 \u2584 \u2583 _ _",
        "\u2586 \u2587 \u2588 \u2587 \u2586 \u2585 \u2584 \u2583 _ _ _",
        "\u2587 \u2588 \u2587 \u2586 \u2585 \u2584 \u2583 _ _ _ _",
        "\u2588 \u2587 \u2586 \u2585 \u2584 \u2583 _ _ _ _ _",
        "\u2587 \u2588 \u2587 \u2586 \u2585 \u2584 \u2583 _ _ _ _",
        "\u2586 \u2587 \u2588 \u2587 \u2586 \u2585 \u2584 \u2583 _ _ _",
        "\u2585 \u2586 \u2587 \u2588 \u2587 \u2586 \u2585 \u2584 \u2583 _ _",
        "\u2584 \u2585 \u2586 \u2587 \u2588 \u2587 \u2586 \u2585 \u2584 \u2583 _"
    };

    public RealmsLongRunningMcoTaskScreen(RealmsScreen param0, LongRunningTask param1) {
        this.lastScreen = param0;
        this.task = param1;
        param1.setScreen(this);
        this.taskThread = param1;
    }

    public void start() {
        Thread var0 = new Thread(this.taskThread, "Realms-long-running-task");
        var0.setUncaughtExceptionHandler(new RealmsDefaultUncaughtExceptionHandler(LOGGER));
        var0.start();
    }

    @Override
    public void tick() {
        super.tick();
        Realms.narrateRepeatedly(this.title);
        ++this.animTicks;
        this.task.tick();
    }

    @Override
    public boolean keyPressed(int param0, int param1, int param2) {
        if (param0 == 256) {
            this.cancelOrBackButtonClicked();
            return true;
        } else {
            return super.keyPressed(param0, param1, param2);
        }
    }

    @Override
    public void init() {
        this.task.init();
        this.buttonsAdd(new RealmsButton(666, this.width() / 2 - 106, RealmsConstants.row(12), 212, 20, getLocalizedString("gui.cancel")) {
            @Override
            public void onPress() {
                RealmsLongRunningMcoTaskScreen.this.cancelOrBackButtonClicked();
            }
        });
    }

    private void cancelOrBackButtonClicked() {
        this.aborted = true;
        this.task.abortTask();
        Realms.setScreen(this.lastScreen);
    }

    @Override
    public void render(int param0, int param1, float param2) {
        this.renderBackground();
        this.drawCenteredString(this.title, this.width() / 2, RealmsConstants.row(3), 16777215);
        if (!this.error) {
            this.drawCenteredString(symbols[this.animTicks % symbols.length], this.width() / 2, RealmsConstants.row(8), 8421504);
        }

        if (this.error) {
            this.drawCenteredString(this.errorMessage, this.width() / 2, RealmsConstants.row(8), 16711680);
        }

        super.render(param0, param1, param2);
    }

    public void error(String param0) {
        this.error = true;
        this.errorMessage = param0;
        Realms.narrateNow(param0);
        this.buttonsClear();
        this.buttonsAdd(new RealmsButton(667, this.width() / 2 - 106, this.height() / 4 + 120 + 12, getLocalizedString("gui.back")) {
            @Override
            public void onPress() {
                RealmsLongRunningMcoTaskScreen.this.cancelOrBackButtonClicked();
            }
        });
    }

    public void setTitle(String param0) {
        this.title = param0;
    }

    public boolean aborted() {
        return this.aborted;
    }
}
