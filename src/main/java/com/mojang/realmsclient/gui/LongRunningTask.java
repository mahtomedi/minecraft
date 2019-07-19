package com.mojang.realmsclient.gui;

import com.mojang.realmsclient.gui.screens.RealmsLongRunningMcoTaskScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class LongRunningTask implements Runnable {
    protected RealmsLongRunningMcoTaskScreen longRunningMcoTaskScreen;

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
