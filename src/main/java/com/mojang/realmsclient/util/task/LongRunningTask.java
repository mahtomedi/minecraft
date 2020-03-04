package com.mojang.realmsclient.util.task;

import com.mojang.realmsclient.gui.screens.RealmsLongRunningMcoTaskScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@OnlyIn(Dist.CLIENT)
public abstract class LongRunningTask implements Runnable {
    public static final Logger LOGGER = LogManager.getLogger();
    protected RealmsLongRunningMcoTaskScreen longRunningMcoTaskScreen;

    protected static void pause(int param0) {
        try {
            Thread.sleep((long)(param0 * 1000));
        } catch (InterruptedException var2) {
            LOGGER.error("", (Throwable)var2);
        }

    }

    public static void setScreen(Screen param0) {
        Minecraft var0 = Minecraft.getInstance();
        var0.execute(() -> var0.setScreen(param0));
    }

    public void setScreen(RealmsLongRunningMcoTaskScreen param0) {
        this.longRunningMcoTaskScreen = param0;
    }

    public void error(String param0) {
        this.longRunningMcoTaskScreen.error(param0);
    }

    public void setTitle(String param0) {
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
