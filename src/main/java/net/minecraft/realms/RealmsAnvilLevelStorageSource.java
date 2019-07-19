package net.minecraft.realms;

import com.google.common.collect.Lists;
import java.util.List;
import net.minecraft.util.ProgressListener;
import net.minecraft.world.level.storage.LevelStorageException;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.LevelSummary;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RealmsAnvilLevelStorageSource {
    private final LevelStorageSource levelStorageSource;

    public RealmsAnvilLevelStorageSource(LevelStorageSource param0) {
        this.levelStorageSource = param0;
    }

    public String getName() {
        return this.levelStorageSource.getName();
    }

    public boolean levelExists(String param0) {
        return this.levelStorageSource.levelExists(param0);
    }

    public boolean convertLevel(String param0, ProgressListener param1) {
        return this.levelStorageSource.convertLevel(param0, param1);
    }

    public boolean requiresConversion(String param0) {
        return this.levelStorageSource.requiresConversion(param0);
    }

    public boolean isNewLevelIdAcceptable(String param0) {
        return this.levelStorageSource.isNewLevelIdAcceptable(param0);
    }

    public boolean deleteLevel(String param0) {
        return this.levelStorageSource.deleteLevel(param0);
    }

    public void renameLevel(String param0, String param1) {
        this.levelStorageSource.renameLevel(param0, param1);
    }

    public List<RealmsLevelSummary> getLevelList() throws LevelStorageException {
        List<RealmsLevelSummary> var0 = Lists.newArrayList();

        for(LevelSummary var1 : this.levelStorageSource.getLevelList()) {
            var0.add(new RealmsLevelSummary(var1));
        }

        return var0;
    }
}
