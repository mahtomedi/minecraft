package net.minecraft.world.level.levelgen;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.Random;
import net.minecraft.Util;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

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
    protected final int woodlandMangionSeparation = 20;
    protected BlockState defaultBlock = Blocks.STONE.defaultBlockState();
    protected BlockState defaultFluid = Blocks.WATER.defaultBlockState();

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

    public int getWoodlandMangionSeparation() {
        return 20;
    }

    public BlockState getDefaultBlock() {
        return this.defaultBlock;
    }

    public BlockState getDefaultFluid() {
        return this.defaultFluid;
    }

    public void setDefaultBlock(BlockState param0) {
        this.defaultBlock = param0;
    }

    public void setDefaultFluid(BlockState param0) {
        this.defaultFluid = param0;
    }

    public int getBedrockRoofPosition() {
        return 0;
    }

    public int getBedrockFloorPosition() {
        return 256;
    }

    public <T> Dynamic<T> serialize(DynamicOps<T> param0) {
        return new Dynamic<>(
            param0,
            param0.createMap(
                ImmutableMap.<T, T>builder()
                    .put(param0.createString("defaultBlock"), BlockState.serialize(param0, this.defaultBlock).getValue())
                    .put(param0.createString("defaultFluid"), BlockState.serialize(param0, this.defaultFluid).getValue())
                    .build()
            )
        );
    }

    public BlockState randomLiquidBlock(Random param0) {
        return param0.nextInt(5) != 2
            ? (param0.nextBoolean() ? Blocks.WATER : Blocks.LAVA).defaultBlockState()
            : Util.randomObject(param0, OverworldGeneratorSettings.GROUND_BLOCKS);
    }

    public BlockState randomGroundBlock(Random param0) {
        return Util.randomObject(param0, OverworldGeneratorSettings.GROUND_BLOCKS);
    }
}
