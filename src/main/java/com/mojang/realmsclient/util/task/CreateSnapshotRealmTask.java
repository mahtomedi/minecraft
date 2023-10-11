package com.mojang.realmsclient.util.task;

import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.RealmsMainScreen;
import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.exception.RealmsServiceException;
import com.mojang.realmsclient.gui.screens.RealmsResetWorldScreen;
import com.mojang.realmsclient.util.WorldGenerationInfo;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class CreateSnapshotRealmTask extends LongRunningTask {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Component TITLE = Component.translatable("mco.snapshot.creating");
    private final long parentId;
    private final WorldGenerationInfo generationInfo;
    private final String name;
    private final String description;
    private final RealmsMainScreen realmsMainScreen;
    @Nullable
    private WorldCreationTask creationTask;
    @Nullable
    private ResettingGeneratedWorldTask generateWorldTask;

    public CreateSnapshotRealmTask(RealmsMainScreen param0, long param1, WorldGenerationInfo param2, String param3, String param4) {
        this.parentId = param1;
        this.generationInfo = param2;
        this.name = param3;
        this.description = param4;
        this.realmsMainScreen = param0;
    }

    @Override
    public void run() {
        RealmsClient var0 = RealmsClient.create();

        try {
            RealmsServer var1 = var0.createSnapshotRealm(this.parentId);
            this.creationTask = new WorldCreationTask(var1.id, this.name, this.description);
            this.generateWorldTask = new ResettingGeneratedWorldTask(
                this.generationInfo,
                var1.id,
                RealmsResetWorldScreen.CREATE_WORLD_RESET_TASK_TITLE,
                () -> Minecraft.getInstance().execute(() -> RealmsMainScreen.play(var1, this.realmsMainScreen, true))
            );
            if (this.aborted()) {
                return;
            }

            this.creationTask.run();
            if (this.aborted()) {
                return;
            }

            this.generateWorldTask.run();
        } catch (RealmsServiceException var31) {
            LOGGER.error("Couldn't create snapshot world", (Throwable)var31);
            this.error(var31);
        } catch (Exception var4) {
            LOGGER.error("Couldn't create snapshot world", (Throwable)var4);
            this.error(var4);
        }

    }

    @Override
    public Component getTitle() {
        return TITLE;
    }

    @Override
    public void abortTask() {
        super.abortTask();
        if (this.creationTask != null) {
            this.creationTask.abortTask();
        }

        if (this.generateWorldTask != null) {
            this.generateWorldTask.abortTask();
        }

    }
}
