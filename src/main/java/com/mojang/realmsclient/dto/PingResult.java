package com.mojang.realmsclient.dto;

import java.util.ArrayList;
import java.util.List;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PingResult extends ValueObject {
    public List<RegionPingResult> pingResults = new ArrayList<>();
    public List<Long> worldIds = new ArrayList<>();
}
