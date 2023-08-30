package com.mojang.realmsclient.gui.screens;

import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.exception.RealmsDefaultUncaughtExceptionHandler;
import com.mojang.realmsclient.util.task.LongRunningTask;
import java.time.Duration;
import javax.annotation.Nullable;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.LoadingDotsWidget;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.realms.RealmsScreen;
import net.minecraft.realms.RepeatedNarrator;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class RealmsLongRunningMcoTaskScreen extends RealmsScreen {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final RepeatedNarrator REPEATED_NARRATOR = new RepeatedNarrator(Duration.ofSeconds(5L));
    private LongRunningTask task;
    private final Screen lastScreen;
    private volatile Component title = CommonComponents.EMPTY;
    private final LinearLayout layout = LinearLayout.vertical();
    @Nullable
    private LoadingDotsWidget loadingDotsWidget;

    public RealmsLongRunningMcoTaskScreen(Screen param0, LongRunningTask param1) {
        super(GameNarrator.NO_TITLE);
        this.lastScreen = param0;
        this.task = param1;
        this.setTitle(param1.getTitle());
        Thread var0 = new Thread(param1, "Realms-long-running-task");
        var0.setUncaughtExceptionHandler(new RealmsDefaultUncaughtExceptionHandler(LOGGER));
        var0.start();
    }

    @Override
    public void tick() {
        super.tick();
        REPEATED_NARRATOR.narrate(this.minecraft.getNarrator(), this.loadingDotsWidget.getMessage());
    }

    @Override
    public boolean keyPressed(int param0, int param1, int param2) {
        if (param0 == 256) {
            this.cancel();
            return true;
        } else {
            return super.keyPressed(param0, param1, param2);
        }
    }

    @Override
    public void init() {
        this.layout.defaultCellSetting().alignHorizontallyCenter();
        this.loadingDotsWidget = new LoadingDotsWidget(this.font, this.title);
        this.layout.addChild(this.loadingDotsWidget, param0 -> param0.paddingBottom(30));
        this.layout.addChild(Button.builder(CommonComponents.GUI_CANCEL, param0 -> this.cancel()).build());
        this.layout.visitWidgets(param1 -> {
        });
        this.repositionElements();
    }

    @Override
    protected void repositionElements() {
        this.layout.arrangeElements();
        FrameLayout.centerInRectangle(this.layout, this.getRectangle());
    }

    protected void cancel() {
        this.task.abortTask();
        this.minecraft.setScreen(this.lastScreen);
    }

    public void setTitle(Component param0) {
        if (this.loadingDotsWidget != null) {
            this.loadingDotsWidget.setMessage(param0);
        }

        this.title = param0;
    }
}
