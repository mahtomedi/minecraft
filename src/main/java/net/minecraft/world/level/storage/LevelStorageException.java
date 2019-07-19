package net.minecraft.world.level.storage;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class LevelStorageException extends Exception {
    public LevelStorageException(String param0) {
        super(param0);
    }
}
