package com.mojang.realmsclient.util.task;

import com.mojang.realmsclient.gui.ErrorCallback;
import com.mojang.realmsclient.gui.screens.RealmsLongRunningMcoTaskScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@OnlyIn(Dist.CLIENT)
public abstract class LongRunningTask implements ErrorCallback, Runnable {
    public static final Logger LOGGER = LogManager.getLogger();
    protected RealmsLongRunningMcoTaskScreen longRunningMcoTaskScreen;

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

    public void setScreen(RealmsLongRunningMcoTaskScreen param0) {
        this.longRunningMcoTaskScreen = param0;
    }

    @Override
    public void error(Component param0) {
        this.longRunningMcoTaskScreen.error(param0);
    }

    public void setTitle(Component param0) {
        this.longRunningMcoTaskScreen.setTitle(param0);
    }

    public boolean aborted() {
        return this.longRunningMcoTaskScreen.aborted();
    }

    public void tick() {
    }

    public void init() {
    }

    public void abortTask() {
    }
}
