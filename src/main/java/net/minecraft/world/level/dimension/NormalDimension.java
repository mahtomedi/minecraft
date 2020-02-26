package net.minecraft.world.level.dimension;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.JsonOps;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelType;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.BiomeSourceType;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.CheckerboardBiomeSourceSettings;
import net.minecraft.world.level.biome.CheckerboardColumnBiomeSource;
import net.minecraft.world.level.biome.FixedBiomeSource;
import net.minecraft.world.level.biome.FixedBiomeSourceSettings;
import net.minecraft.world.level.biome.OverworldBiomeSource;
import net.minecraft.world.level.biome.OverworldBiomeSourceSettings;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkGeneratorType;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;
import net.minecraft.world.level.levelgen.DebugGeneratorSettings;
import net.minecraft.world.level.levelgen.DebugLevelSource;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.NetherGeneratorSettings;
import net.minecraft.world.level.levelgen.NetherLevelSource;
import net.minecraft.world.level.levelgen.OverworldGeneratorSettings;
import net.minecraft.world.level.levelgen.OverworldLevelSource;
import net.minecraft.world.level.levelgen.TheEndGeneratorSettings;
import net.minecraft.world.level.levelgen.TheEndLevelSource;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class NormalDimension extends Dimension {
    public NormalDimension(Level param0, DimensionType param1) {
        super(param0, param1, 0.0F);
    }

    @Override
    public DimensionType getType() {
        return DimensionType.OVERWORLD;
    }

    @Override
    public ChunkGenerator<? extends ChunkGeneratorSettings> createRandomLevelGenerator() {
        LevelType var0 = this.level.getLevelData().getGeneratorType();
        ChunkGeneratorType<FlatLevelGeneratorSettings, FlatLevelSource> var1 = ChunkGeneratorType.FLAT;
        ChunkGeneratorType<DebugGeneratorSettings, DebugLevelSource> var2 = ChunkGeneratorType.DEBUG;
        ChunkGeneratorType<NetherGeneratorSettings, NetherLevelSource> var3 = ChunkGeneratorType.CAVES;
        ChunkGeneratorType<TheEndGeneratorSettings, TheEndLevelSource> var4 = ChunkGeneratorType.FLOATING_ISLANDS;
        ChunkGeneratorType<OverworldGeneratorSettings, OverworldLevelSource> var5 = ChunkGeneratorType.SURFACE;
        BiomeSourceType<FixedBiomeSourceSettings, FixedBiomeSource> var6 = BiomeSourceType.FIXED;
        BiomeSourceType<OverworldBiomeSourceSettings, OverworldBiomeSource> var7 = BiomeSourceType.VANILLA_LAYERED;
        BiomeSourceType<CheckerboardBiomeSourceSettings, CheckerboardColumnBiomeSource> var8 = BiomeSourceType.CHECKERBOARD;
        if (var0 == LevelType.FLAT) {
            FlatLevelGeneratorSettings var9 = FlatLevelGeneratorSettings.fromObject(
                new Dynamic<>(NbtOps.INSTANCE, this.level.getLevelData().getGeneratorOptions())
            );
            FixedBiomeSourceSettings var10 = var6.createSettings(this.level.getLevelData()).setBiome(var9.getBiome());
            return var1.create(this.level, var6.create(var10), var9);
        } else if (var0 == LevelType.DEBUG_ALL_BLOCK_STATES) {
            FixedBiomeSourceSettings var11 = var6.createSettings(this.level.getLevelData()).setBiome(Biomes.PLAINS);
            return var2.create(this.level, var6.create(var11), var2.createSettings());
        } else if (var0 != LevelType.BUFFET) {
            OverworldGeneratorSettings var35 = var5.createSettings();
            OverworldBiomeSourceSettings var36 = var7.createSettings(this.level.getLevelData()).setGeneratorSettings(var35);
            return var5.create(this.level, var7.create(var36), var35);
        } else {
            BiomeSource var12 = null;
            JsonElement var13 = Dynamic.convert(NbtOps.INSTANCE, JsonOps.INSTANCE, this.level.getLevelData().getGeneratorOptions());
            JsonObject var14 = var13.getAsJsonObject();
            JsonObject var15 = var14.getAsJsonObject("biome_source");
            if (var15 != null && var15.has("type") && var15.has("options")) {
                BiomeSourceType<?, ?> var16 = Registry.BIOME_SOURCE_TYPE.get(new ResourceLocation(var15.getAsJsonPrimitive("type").getAsString()));
                JsonObject var17 = var15.getAsJsonObject("options");
                Biome[] var18 = new Biome[]{Biomes.OCEAN};
                if (var17.has("biomes")) {
                    JsonArray var19 = var17.getAsJsonArray("biomes");
                    var18 = var19.size() > 0 ? new Biome[var19.size()] : new Biome[]{Biomes.OCEAN};

                    for(int var20 = 0; var20 < var19.size(); ++var20) {
                        var18[var20] = Registry.BIOME.getOptional(new ResourceLocation(var19.get(var20).getAsString())).orElse(Biomes.OCEAN);
                    }
                }

                if (BiomeSourceType.FIXED == var16) {
                    FixedBiomeSourceSettings var21 = var6.createSettings(this.level.getLevelData()).setBiome(var18[0]);
                    var12 = var6.create(var21);
                }

                if (BiomeSourceType.CHECKERBOARD == var16) {
                    int var22 = var17.has("size") ? var17.getAsJsonPrimitive("size").getAsInt() : 2;
                    CheckerboardBiomeSourceSettings var23 = var8.createSettings(this.level.getLevelData()).setAllowedBiomes(var18).setSize(var22);
                    var12 = var8.create(var23);
                }

                if (BiomeSourceType.VANILLA_LAYERED == var16) {
                    OverworldBiomeSourceSettings var24 = var7.createSettings(this.level.getLevelData());
                    var12 = var7.create(var24);
                }
            }

            if (var12 == null) {
                var12 = var6.create(var6.createSettings(this.level.getLevelData()).setBiome(Biomes.OCEAN));
            }

            BlockState var25 = Blocks.STONE.defaultBlockState();
            BlockState var26 = Blocks.WATER.defaultBlockState();
            JsonObject var27 = var14.getAsJsonObject("chunk_generator");
            if (var27 != null && var27.has("options")) {
                JsonObject var28 = var27.getAsJsonObject("options");
                if (var28.has("default_block")) {
                    String var29 = var28.getAsJsonPrimitive("default_block").getAsString();
                    var25 = Registry.BLOCK.get(new ResourceLocation(var29)).defaultBlockState();
                }

                if (var28.has("default_fluid")) {
                    String var30 = var28.getAsJsonPrimitive("default_fluid").getAsString();
                    var26 = Registry.BLOCK.get(new ResourceLocation(var30)).defaultBlockState();
                }
            }

            if (var27 != null && var27.has("type")) {
                ChunkGeneratorType<?, ?> var31 = Registry.CHUNK_GENERATOR_TYPE.get(new ResourceLocation(var27.getAsJsonPrimitive("type").getAsString()));
                if (ChunkGeneratorType.CAVES == var31) {
                    NetherGeneratorSettings var32 = var3.createSettings();
                    var32.setDefaultBlock(var25);
                    var32.setDefaultFluid(var26);
                    return var3.create(this.level, var12, var32);
                }

                if (ChunkGeneratorType.FLOATING_ISLANDS == var31) {
                    TheEndGeneratorSettings var33 = var4.createSettings();
                    var33.setSpawnPosition(new BlockPos(0, 64, 0));
                    var33.setDefaultBlock(var25);
                    var33.setDefaultFluid(var26);
                    return var4.create(this.level, var12, var33);
                }
            }

            OverworldGeneratorSettings var34 = var5.createSettings();
            var34.setDefaultBlock(var25);
            var34.setDefaultFluid(var26);
            return var5.create(this.level, var12, var34);
        }
    }

    @Nullable
    @Override
    public BlockPos getSpawnPosInChunk(ChunkPos param0, boolean param1) {
        for(int var0 = param0.getMinBlockX(); var0 <= param0.getMaxBlockX(); ++var0) {
            for(int var1 = param0.getMinBlockZ(); var1 <= param0.getMaxBlockZ(); ++var1) {
                BlockPos var2 = this.getValidSpawnPosition(var0, var1, param1);
                if (var2 != null) {
                    return var2;
                }
            }
        }

        return null;
    }

    @Nullable
    @Override
    public BlockPos getValidSpawnPosition(int param0, int param1, boolean param2) {
        BlockPos.MutableBlockPos var0 = new BlockPos.MutableBlockPos(param0, 0, param1);
        Biome var1 = this.level.getBiome(var0);
        BlockState var2 = var1.getSurfaceBuilderConfig().getTopMaterial();
        if (param2 && !var2.getBlock().is(BlockTags.VALID_SPAWN)) {
            return null;
        } else {
            LevelChunk var3 = this.level.getChunk(param0 >> 4, param1 >> 4);
            int var4 = var3.getHeight(Heightmap.Types.MOTION_BLOCKING, param0 & 15, param1 & 15);
            if (var4 < 0) {
                return null;
            } else if (var3.getHeight(Heightmap.Types.WORLD_SURFACE, param0 & 15, param1 & 15)
                > var3.getHeight(Heightmap.Types.OCEAN_FLOOR, param0 & 15, param1 & 15)) {
                return null;
            } else {
                for(int var5 = var4 + 1; var5 >= 0; --var5) {
                    var0.set(param0, var5, param1);
                    BlockState var6 = this.level.getBlockState(var0);
                    if (!var6.getFluidState().isEmpty()) {
                        break;
                    }

                    if (var6.equals(var2)) {
                        return var0.above().immutable();
                    }
                }

                return null;
            }
        }
    }

    @Override
    public float getTimeOfDay(long param0, float param1) {
        double var0 = Mth.frac((double)param0 / 24000.0 - 0.25);
        double var1 = 0.5 - Math.cos(var0 * Math.PI) / 2.0;
        return (float)(var0 * 2.0 + var1) / 3.0F;
    }

    @Override
    public boolean isNaturalDimension() {
        return true;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public Vec3 getBrightnessDependentFogColor(Vec3 param0, float param1) {
        return param0.multiply((double)(param1 * 0.94F + 0.06F), (double)(param1 * 0.94F + 0.06F), (double)(param1 * 0.91F + 0.09F));
    }

    @Override
    public boolean mayRespawn() {
        return true;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public boolean isFoggyAt(int param0, int param1) {
        return false;
    }
}
