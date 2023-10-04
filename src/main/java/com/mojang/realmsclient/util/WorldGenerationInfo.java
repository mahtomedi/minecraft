package com.mojang.realmsclient.util;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public record WorldGenerationInfo(String seed, LevelType levelType, boolean generateStructures) {
}
