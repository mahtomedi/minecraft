package com.mojang.realmsclient.util.task;

import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.exception.RetryCallException;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class SwitchSlotTask extends LongRunningTask {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Component TITLE = Component.translatable("mco.minigame.world.slot.screen.title");
    private final long worldId;
    private final int slot;
    private final Runnable callback;

    public SwitchSlotTask(long param0, int param1, Runnable param2) {
        this.worldId = param0;
        this.slot = param1;
        this.callback = param2;
    }

    @Override
    public void run() {
        RealmsClient var0 = RealmsClient.create();

        for(int var1 = 0; var1 < 25; ++var1) {
            try {
                if (this.aborted()) {
                    return;
                }

                if (var0.switchSlot(this.worldId, this.slot)) {
                    this.callback.run();
                    break;
                }
            } catch (RetryCallException var4) {
                if (this.aborted()) {
                    return;
                }

                pause((long)var4.delaySeconds);
            } catch (Exception var5) {
                if (this.aborted()) {
                    return;
                }

                LOGGER.error("Couldn't switch world!");
                this.error(var5);
            }
        }

    }

    @Override
    public Component getTitle() {
        return TITLE;
    }
}
