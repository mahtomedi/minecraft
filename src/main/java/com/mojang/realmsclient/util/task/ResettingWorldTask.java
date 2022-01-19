package com.mojang.realmsclient.util.task;

import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.exception.RealmsServiceException;
import com.mojang.realmsclient.exception.RetryCallException;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public abstract class ResettingWorldTask extends LongRunningTask {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final long serverId;
    private final Component title;
    private final Runnable callback;

    public ResettingWorldTask(long param0, Component param1, Runnable param2) {
        this.serverId = param0;
        this.title = param1;
        this.callback = param2;
    }

    protected abstract void sendResetRequest(RealmsClient var1, long var2) throws RealmsServiceException;

    @Override
    public void run() {
        RealmsClient var0 = RealmsClient.create();
        this.setTitle(this.title);
        int var1 = 0;

        while(var1 < 25) {
            try {
                if (this.aborted()) {
                    return;
                }

                this.sendResetRequest(var0, this.serverId);
                if (this.aborted()) {
                    return;
                }

                this.callback.run();
                return;
            } catch (RetryCallException var4) {
                if (this.aborted()) {
                    return;
                }

                pause((long)var4.delaySeconds);
                ++var1;
            } catch (Exception var5) {
                if (this.aborted()) {
                    return;
                }

                LOGGER.error("Couldn't reset world");
                this.error(var5.toString());
                return;
            }
        }

    }
}
