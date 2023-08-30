package com.mojang.realmsclient.util.task;

import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.RealmsMainScreen;
import com.mojang.realmsclient.exception.RealmsServiceException;
import com.mojang.realmsclient.gui.screens.RealmsGenericErrorScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public abstract class LongRunningTask implements Runnable {
    protected static final int NUMBER_OF_RETRIES = 25;
    private static final Logger LOGGER = LogUtils.getLogger();
    private boolean aborted = false;

    protected static void pause(long param0) {
        try {
            Thread.sleep(param0 * 1000L);
        } catch (InterruptedException var3) {
            Thread.currentThread().interrupt();
            LOGGER.error("", (Throwable)var3);
        }

    }

    public static void setScreen(Screen param0) {
        Minecraft var0 = Minecraft.getInstance();
        var0.execute(() -> var0.setScreen(param0));
    }

    protected void error(Component param0) {
        this.abortTask();
        Minecraft var0 = Minecraft.getInstance();
        var0.execute(() -> var0.setScreen(new RealmsGenericErrorScreen(param0, new RealmsMainScreen(new TitleScreen()))));
    }

    protected void error(Exception param0) {
        if (param0 instanceof RealmsServiceException var0) {
            this.error(var0.realmsError.errorMessage());
        } else {
            this.error(Component.literal(param0.getMessage()));
        }

    }

    protected void error(RealmsServiceException param0) {
        this.error(param0.realmsError.errorMessage());
    }

    public abstract Component getTitle();

    public boolean aborted() {
        return this.aborted;
    }

    public void tick() {
    }

    public void init() {
    }

    public void abortTask() {
        this.aborted = true;
    }
}
