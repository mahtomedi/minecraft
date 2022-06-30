package com.mojang.realmsclient.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.exception.RealmsDefaultUncaughtExceptionHandler;
import com.mojang.realmsclient.gui.ErrorCallback;
import com.mojang.realmsclient.util.task.LongRunningTask;
import java.time.Duration;
import javax.annotation.Nullable;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.realms.RealmsScreen;
import net.minecraft.realms.RepeatedNarrator;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class RealmsLongRunningMcoTaskScreen extends RealmsScreen implements ErrorCallback {
    private static final RepeatedNarrator REPEATED_NARRATOR = new RepeatedNarrator(Duration.ofSeconds(5L));
    private static final Logger LOGGER = LogUtils.getLogger();
    private final Screen lastScreen;
    private volatile Component title = CommonComponents.EMPTY;
    @Nullable
    private volatile Component errorMessage;
    private volatile boolean aborted;
    private int animTicks;
    private final LongRunningTask task;
    private final int buttonLength = 212;
    private Button cancelOrBackButton;
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
        super(GameNarrator.NO_TITLE);
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
        REPEATED_NARRATOR.narrate(this.minecraft.getNarrator(), this.title);
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
        this.cancelOrBackButton = this.addRenderableWidget(
            new Button(this.width / 2 - 106, row(12), 212, 20, CommonComponents.GUI_CANCEL, param0 -> this.cancelOrBackButtonClicked())
        );
    }

    private void cancelOrBackButtonClicked() {
        this.aborted = true;
        this.task.abortTask();
        this.minecraft.setScreen(this.lastScreen);
    }

    @Override
    public void render(PoseStack param0, int param1, int param2, float param3) {
        this.renderBackground(param0);
        drawCenteredString(param0, this.font, this.title, this.width / 2, row(3), 16777215);
        Component var0 = this.errorMessage;
        if (var0 == null) {
            drawCenteredString(param0, this.font, SYMBOLS[this.animTicks % SYMBOLS.length], this.width / 2, row(8), 8421504);
        } else {
            drawCenteredString(param0, this.font, var0, this.width / 2, row(8), 16711680);
        }

        super.render(param0, param1, param2, param3);
    }

    @Override
    public void error(Component param0) {
        this.errorMessage = param0;
        this.minecraft.getNarrator().sayNow(param0);
        this.minecraft
            .execute(
                () -> {
                    this.removeWidget(this.cancelOrBackButton);
                    this.cancelOrBackButton = this.addRenderableWidget(
                        new Button(
                            this.width / 2 - 106, this.height / 4 + 120 + 12, 200, 20, CommonComponents.GUI_BACK, param0x -> this.cancelOrBackButtonClicked()
                        )
                    );
                }
            );
    }

    public void setTitle(Component param0) {
        this.title = param0;
    }

    public boolean aborted() {
        return this.aborted;
    }
}
