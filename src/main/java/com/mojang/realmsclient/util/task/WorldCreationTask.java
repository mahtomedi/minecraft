package com.mojang.realmsclient.util.task;

import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.exception.RealmsServiceException;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class WorldCreationTask extends LongRunningTask {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Component TITLE = Component.translatable("mco.create.world.wait");
    private final String name;
    private final String motd;
    private final long worldId;

    public WorldCreationTask(long param0, String param1, String param2) {
        this.worldId = param0;
        this.name = param1;
        this.motd = param2;
    }

    @Override
    public void run() {
        RealmsClient var0 = RealmsClient.create();

        try {
            var0.initializeWorld(this.worldId, this.name, this.motd);
        } catch (RealmsServiceException var3) {
            LOGGER.error("Couldn't create world", (Throwable)var3);
            this.error(var3);
        } catch (Exception var4) {
            LOGGER.error("Could not create world", (Throwable)var4);
            this.error(var4);
        }

    }

    @Override
    public Component getTitle() {
        return TITLE;
    }
}
