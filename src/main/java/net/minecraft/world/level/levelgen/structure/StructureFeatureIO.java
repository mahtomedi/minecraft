package net.minecraft.world.level.levelgen.structure;

import java.util.Locale;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.StructurePieceType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class StructureFeatureIO {
    private static final Logger LOGGER = LogManager.getLogger();
    public static final StructureFeature<?> MINESHAFT = register("Mineshaft", Feature.MINESHAFT);
    public static final StructureFeature<?> PILLAGER_OUTPOST = register("Pillager_Outpost", Feature.PILLAGER_OUTPOST);
    public static final StructureFeature<?> NETHER_FORTRESS = register("Fortress", Feature.NETHER_BRIDGE);
    public static final StructureFeature<?> STRONGHOLD = register("Stronghold", Feature.STRONGHOLD);
    public static final StructureFeature<?> JUNGLE_PYRAMID = register("Jungle_Pyramid", Feature.JUNGLE_TEMPLE);
    public static final StructureFeature<?> OCEAN_RUIN = register("Ocean_Ruin", Feature.OCEAN_RUIN);
    public static final StructureFeature<?> DESERT_PYRAMID = register("Desert_Pyramid", Feature.DESERT_PYRAMID);
    public static final StructureFeature<?> IGLOO = register("Igloo", Feature.IGLOO);
    public static final StructureFeature<?> SWAMP_HUT = register("Swamp_Hut", Feature.SWAMP_HUT);
    public static final StructureFeature<?> OCEAN_MONUMENT = register("Monument", Feature.OCEAN_MONUMENT);
    public static final StructureFeature<?> END_CITY = register("EndCity", Feature.END_CITY);
    public static final StructureFeature<?> WOODLAND_MANSION = register("Mansion", Feature.WOODLAND_MANSION);
    public static final StructureFeature<?> BURIED_TREASURE = register("Buried_Treasure", Feature.BURIED_TREASURE);
    public static final StructureFeature<?> SHIPWRECK = register("Shipwreck", Feature.SHIPWRECK);
    public static final StructureFeature<?> VILLAGE = register("Village", Feature.VILLAGE);

    private static StructureFeature<?> register(String param0, StructureFeature<?> param1) {
        return Registry.register(Registry.STRUCTURE_FEATURE, param0.toLowerCase(Locale.ROOT), param1);
    }

    public static void bootstrap() {
    }

    @Nullable
    public static StructureStart loadStaticStart(ChunkGenerator<?> param0, StructureManager param1, BiomeSource param2, CompoundTag param3) {
        String var0 = param3.getString("id");
        if ("INVALID".equals(var0)) {
            return StructureStart.INVALID_START;
        } else {
            StructureFeature<?> var1 = Registry.STRUCTURE_FEATURE.get(new ResourceLocation(var0.toLowerCase(Locale.ROOT)));
            if (var1 == null) {
                LOGGER.error("Unknown feature id: {}", var0);
                return null;
            } else {
                int var2 = param3.getInt("ChunkX");
                int var3 = param3.getInt("ChunkZ");
                Biome var4 = param3.contains("biome")
                    ? Registry.BIOME.get(new ResourceLocation(param3.getString("biome")))
                    : param2.getBiome(new BlockPos((var2 << 4) + 9, 0, (var3 << 4) + 9));
                BoundingBox var5 = param3.contains("BB") ? new BoundingBox(param3.getIntArray("BB")) : BoundingBox.getUnknownBox();
                ListTag var6 = param3.getList("Children", 10);

                try {
                    StructureStart var7 = var1.getStartFactory().create(var1, var2, var3, var4, var5, 0, param0.getSeed());

                    for(int var8 = 0; var8 < var6.size(); ++var8) {
                        CompoundTag var9 = var6.getCompound(var8);
                        String var10 = var9.getString("id");
                        StructurePieceType var11 = Registry.STRUCTURE_PIECE.get(new ResourceLocation(var10.toLowerCase(Locale.ROOT)));
                        if (var11 == null) {
                            LOGGER.error("Unknown structure piece id: {}", var10);
                        } else {
                            try {
                                StructurePiece var12 = var11.load(param1, var9);
                                var7.pieces.add(var12);
                            } catch (Exception var17) {
                                LOGGER.error("Exception loading structure piece with id {}", var10, var17);
                            }
                        }
                    }

                    return var7;
                } catch (Exception var18) {
                    LOGGER.error("Failed Start with id {}", var0, var18);
                    return null;
                }
            }
        }
    }
}
