package com.mojang.realmsclient.util;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class WorldGenerationInfo {
    private final String seed;
    private final LevelType levelType;
    private final boolean generateStructures;

    public WorldGenerationInfo(String param0, LevelType param1, boolean param2) {
        this.seed = param0;
        this.levelType = param1;
        this.generateStructures = param2;
    }

    public String getSeed() {
        return this.seed;
    }

    public LevelType getLevelType() {
        return this.levelType;
    }

    public boolean shouldGenerateStructures() {
        return this.generateStructures;
    }
}
