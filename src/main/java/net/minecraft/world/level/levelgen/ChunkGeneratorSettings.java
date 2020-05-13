package net.minecraft.world.level.levelgen;

import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ChunkGeneratorSettings {
    protected int villagesSpacing = 32;
    protected final int villagesSeparation = 8;
    protected int monumentsSpacing = 32;
    protected int monumentsSeparation = 5;
    protected int strongholdsDistance = 32;
    protected int strongholdsCount = 128;
    protected int strongholdsSpread = 3;
    protected int templesSpacing = 32;
    protected final int templesSeparation = 8;
    protected final int oceanRuinSpacing = 20;
    protected final int oceanRuinSeparation = 8;
    protected int endCitySpacing = 20;
    protected final int endCitySeparation = 11;
    protected final int shipwreckSpacing = 24;
    protected final int shipwreckSeparation = 4;
    protected int woodlandMansionSpacing = 80;
    protected final int woodlandMansionSeparation = 20;
    protected final int rareNetherStructureSpacing = 30;
    protected final int rareNetherStructureSeparation = 4;
    protected final int rareNetherStructureSalt = 30084232;
    protected int ruinedPortalSpacing = 40;
    protected int ruinedPortalSeparation = 15;

    public int getRareNetherStructureSpacing() {
        return 30;
    }

    public int getRareNetherStructureSeparation() {
        return 4;
    }

    public int getRareNetherStructureSalt() {
        return 30084232;
    }

    public int getVillagesSpacing() {
        return this.villagesSpacing;
    }

    public int getVillagesSeparation() {
        return 8;
    }

    public int getMonumentsSpacing() {
        return this.monumentsSpacing;
    }

    public int getMonumentsSeparation() {
        return this.monumentsSeparation;
    }

    public int getStrongholdsDistance() {
        return this.strongholdsDistance;
    }

    public int getStrongholdsCount() {
        return this.strongholdsCount;
    }

    public int getStrongholdsSpread() {
        return this.strongholdsSpread;
    }

    public int getTemplesSpacing() {
        return this.templesSpacing;
    }

    public int getTemplesSeparation() {
        return 8;
    }

    public int getShipwreckSpacing() {
        return 24;
    }

    public int getShipwreckSeparation() {
        return 4;
    }

    public int getOceanRuinSpacing() {
        return 20;
    }

    public int getOceanRuinSeparation() {
        return 8;
    }

    public int getEndCitySpacing() {
        return this.endCitySpacing;
    }

    public int getEndCitySeparation() {
        return 11;
    }

    public int getWoodlandMansionSpacing() {
        return this.woodlandMansionSpacing;
    }

    public int getWoodlandMansionSeparation() {
        return 20;
    }

    public int getRuinedPortalSpacing() {
        return this.ruinedPortalSpacing;
    }

    public int getRuinedPortalSeparation() {
        return this.ruinedPortalSeparation;
    }

    @OnlyIn(Dist.CLIENT)
    public void setOption(String param0, String param1, String param2) {
        if ("village".equals(param0) && "distance".equals(param1)) {
            this.villagesSpacing = Mth.getInt(param2, this.villagesSpacing, 9);
        }

        if ("biome_1".equals(param0) && "distance".equals(param1)) {
            this.templesSpacing = Mth.getInt(param2, this.templesSpacing, 9);
        }

        if ("stronghold".equals(param0)) {
            if ("distance".equals(param1)) {
                this.strongholdsDistance = Mth.getInt(param2, this.strongholdsDistance, 1);
            } else if ("count".equals(param1)) {
                this.strongholdsCount = Mth.getInt(param2, this.strongholdsCount, 1);
            } else if ("spread".equals(param1)) {
                this.strongholdsSpread = Mth.getInt(param2, this.strongholdsSpread, 1);
            }
        }

        if ("oceanmonument".equals(param0)) {
            if ("separation".equals(param1)) {
                this.monumentsSeparation = Mth.getInt(param2, this.monumentsSeparation, 1);
            } else if ("spacing".equals(param1)) {
                this.monumentsSpacing = Mth.getInt(param2, this.monumentsSpacing, 1);
            }
        }

        if ("endcity".equals(param0) && "distance".equals(param1)) {
            this.endCitySpacing = Mth.getInt(param2, this.endCitySpacing, 1);
        }

        if ("mansion".equals(param0) && "distance".equals(param1)) {
            this.woodlandMansionSpacing = Mth.getInt(param2, this.woodlandMansionSpacing, 1);
        }

    }
}
