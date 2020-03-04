package com.mojang.realmsclient.gui.screens;

import com.google.common.collect.Sets;
import com.mojang.realmsclient.exception.RealmsDefaultUncaughtExceptionHandler;
import com.mojang.realmsclient.util.task.LongRunningTask;
import java.util.Set;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.realms.NarrationHelper;
import net.minecraft.realms.RealmsScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@OnlyIn(Dist.CLIENT)
public class RealmsLongRunningMcoTaskScreen extends RealmsScreen {
    private static final Logger LOGGER = LogManager.getLogger();
    private final Screen lastScreen;
    private volatile String title = "";
    private volatile boolean error;
    private volatile String errorMessage;
    private volatile boolean aborted;
    private int animTicks;
    private final LongRunningTask task;
    private final int buttonLength = 212;
    public static final String[] SYMBOLS = new String[]{
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

    public RealmsLongRunningMcoTaskScreen(Screen param0, LongRunningTask param1) {
        this.lastScreen = param0;
        this.task = param1;
        param1.setScreen(this);
        Thread var0 = new Thread(param1, "Realms-long-running-task");
        var0.setUncaughtExceptionHandler(new RealmsDefaultUncaughtExceptionHandler(LOGGER));
        var0.start();
    }

    @Override
    public void tick() {
        super.tick();
        NarrationHelper.repeatedly(this.title);
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
        this.addButton(new Button(this.width / 2 - 106, row(12), 212, 20, I18n.get("gui.cancel"), param0 -> this.cancelOrBackButtonClicked()));
    }

    private void cancelOrBackButtonClicked() {
        this.aborted = true;
        this.task.abortTask();
        this.minecraft.setScreen(this.lastScreen);
    }

    @Override
    public void render(int param0, int param1, float param2) {
        this.renderBackground();
        this.drawCenteredString(this.font, this.title, this.width / 2, row(3), 16777215);
        if (!this.error) {
            this.drawCenteredString(this.font, SYMBOLS[this.animTicks % SYMBOLS.length], this.width / 2, row(8), 8421504);
        }

        if (this.error) {
            this.drawCenteredString(this.font, this.errorMessage, this.width / 2, row(8), 16711680);
        }

        super.render(param0, param1, param2);
    }

    public void error(String param0) {
        this.error = true;
        this.errorMessage = param0;
        NarrationHelper.now(param0);
        this.buttonsClear();
        this.addButton(new Button(this.width / 2 - 106, this.height / 4 + 120 + 12, 200, 20, I18n.get("gui.back"), param0x -> this.cancelOrBackButtonClicked()));
    }

    public void buttonsClear() {
        Set<GuiEventListener> var0 = Sets.newHashSet(this.buttons);
        this.children.removeIf(var0::contains);
        this.buttons.clear();
    }

    public void setTitle(String param0) {
        this.title = param0;
    }

    public boolean aborted() {
        return this.aborted;
    }
}
