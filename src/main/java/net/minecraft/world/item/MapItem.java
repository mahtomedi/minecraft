package net.minecraft.world.item;

import com.google.common.collect.Iterables;
import com.google.common.collect.LinkedHashMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multisets;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

public class MapItem extends ComplexItem {
    public static final int IMAGE_WIDTH = 128;
    public static final int IMAGE_HEIGHT = 128;
    private static final int DEFAULT_MAP_COLOR = -12173266;
    private static final String TAG_MAP = "map";

    public MapItem(Item.Properties param0) {
        super(param0);
    }

    public static ItemStack create(Level param0, int param1, int param2, byte param3, boolean param4, boolean param5) {
        ItemStack var0 = new ItemStack(Items.FILLED_MAP);
        createAndStoreSavedData(var0, param0, param1, param2, param3, param4, param5, param0.dimension());
        return var0;
    }

    @Nullable
    public static MapItemSavedData getSavedData(@Nullable Integer param0, Level param1) {
        return param0 == null ? null : param1.getMapData(makeKey(param0));
    }

    @Nullable
    public static MapItemSavedData getSavedData(ItemStack param0, Level param1) {
        Integer var0 = getMapId(param0);
        return getSavedData(var0, param1);
    }

    @Nullable
    public static Integer getMapId(ItemStack param0) {
        CompoundTag var0 = param0.getTag();
        return var0 != null && var0.contains("map", 99) ? var0.getInt("map") : null;
    }

    private static int createNewSavedData(Level param0, int param1, int param2, int param3, boolean param4, boolean param5, ResourceKey<Level> param6) {
        MapItemSavedData var0 = MapItemSavedData.createFresh((double)param1, (double)param2, (byte)param3, param4, param5, param6);
        int var1 = param0.getFreeMapId();
        param0.setMapData(makeKey(var1), var0);
        return var1;
    }

    private static void storeMapData(ItemStack param0, int param1) {
        param0.getOrCreateTag().putInt("map", param1);
    }

    private static void createAndStoreSavedData(
        ItemStack param0, Level param1, int param2, int param3, int param4, boolean param5, boolean param6, ResourceKey<Level> param7
    ) {
        int var0 = createNewSavedData(param1, param2, param3, param4, param5, param6, param7);
        storeMapData(param0, var0);
    }

    public static String makeKey(int param0) {
        return "map_" + param0;
    }

    public void update(Level param0, Entity param1, MapItemSavedData param2) {
        if (param0.dimension() == param2.dimension && param1 instanceof Player) {
            int var0 = 1 << param2.scale;
            int var1 = param2.x;
            int var2 = param2.z;
            int var3 = Mth.floor(param1.getX() - (double)var1) / var0 + 64;
            int var4 = Mth.floor(param1.getZ() - (double)var2) / var0 + 64;
            int var5 = 128 / var0;
            if (param0.dimensionType().hasCeiling()) {
                var5 /= 2;
            }

            MapItemSavedData.HoldingPlayer var6 = param2.getHoldingPlayer((Player)param1);
            ++var6.step;
            boolean var7 = false;

            for(int var8 = var3 - var5 + 1; var8 < var3 + var5; ++var8) {
                if ((var8 & 15) == (var6.step & 15) || var7) {
                    var7 = false;
                    double var9 = 0.0;

                    for(int var10 = var4 - var5 - 1; var10 < var4 + var5; ++var10) {
                        if (var8 >= 0 && var10 >= -1 && var8 < 128 && var10 < 128) {
                            int var11 = var8 - var3;
                            int var12 = var10 - var4;
                            boolean var13 = var11 * var11 + var12 * var12 > (var5 - 2) * (var5 - 2);
                            int var14 = (var1 / var0 + var8 - 64) * var0;
                            int var15 = (var2 / var0 + var10 - 64) * var0;
                            Multiset<MaterialColor> var16 = LinkedHashMultiset.create();
                            LevelChunk var17 = param0.getChunkAt(new BlockPos(var14, 0, var15));
                            if (!var17.isEmpty()) {
                                ChunkPos var18 = var17.getPos();
                                int var19 = var14 & 15;
                                int var20 = var15 & 15;
                                int var21 = 0;
                                double var22 = 0.0;
                                if (param0.dimensionType().hasCeiling()) {
                                    int var23 = var14 + var15 * 231871;
                                    var23 = var23 * var23 * 31287121 + var23 * 11;
                                    if ((var23 >> 20 & 1) == 0) {
                                        var16.add(Blocks.DIRT.defaultBlockState().getMapColor(param0, BlockPos.ZERO), 10);
                                    } else {
                                        var16.add(Blocks.STONE.defaultBlockState().getMapColor(param0, BlockPos.ZERO), 100);
                                    }

                                    var22 = 100.0;
                                } else {
                                    BlockPos.MutableBlockPos var24 = new BlockPos.MutableBlockPos();
                                    BlockPos.MutableBlockPos var25 = new BlockPos.MutableBlockPos();

                                    for(int var26 = 0; var26 < var0; ++var26) {
                                        for(int var27 = 0; var27 < var0; ++var27) {
                                            int var28 = var17.getHeight(Heightmap.Types.WORLD_SURFACE, var26 + var19, var27 + var20) + 1;
                                            BlockState var32;
                                            if (var28 <= param0.getMinBuildHeight() + 1) {
                                                var32 = Blocks.BEDROCK.defaultBlockState();
                                            } else {
                                                do {
                                                    var24.set(var18.getMinBlockX() + var26 + var19, --var28, var18.getMinBlockZ() + var27 + var20);
                                                    var32 = var17.getBlockState(var24);
                                                } while(var32.getMapColor(param0, var24) == MaterialColor.NONE && var28 > param0.getMinBuildHeight());

                                                if (var28 > param0.getMinBuildHeight() && !var32.getFluidState().isEmpty()) {
                                                    int var30 = var28 - 1;
                                                    var25.set(var24);

                                                    BlockState var31;
                                                    do {
                                                        var25.setY(var30--);
                                                        var31 = var17.getBlockState(var25);
                                                        ++var21;
                                                    } while(var30 > param0.getMinBuildHeight() && !var31.getFluidState().isEmpty());

                                                    var32 = this.getCorrectStateForFluidBlock(param0, var32, var24);
                                                }
                                            }

                                            param2.checkBanners(param0, var18.getMinBlockX() + var26 + var19, var18.getMinBlockZ() + var27 + var20);
                                            var22 += (double)var28 / (double)(var0 * var0);
                                            var16.add(var32.getMapColor(param0, var24));
                                        }
                                    }
                                }

                                var21 /= var0 * var0;
                                MaterialColor var33 = Iterables.getFirst(Multisets.copyHighestCountFirst(var16), MaterialColor.NONE);
                                MaterialColor.Brightness var35;
                                if (var33 == MaterialColor.WATER) {
                                    double var34 = (double)var21 * 0.1 + (double)(var8 + var10 & 1) * 0.2;
                                    if (var34 < 0.5) {
                                        var35 = MaterialColor.Brightness.HIGH;
                                    } else if (var34 > 0.9) {
                                        var35 = MaterialColor.Brightness.LOW;
                                    } else {
                                        var35 = MaterialColor.Brightness.NORMAL;
                                    }
                                } else {
                                    double var38 = (var22 - var9) * 4.0 / (double)(var0 + 4) + ((double)(var8 + var10 & 1) - 0.5) * 0.4;
                                    if (var38 > 0.6) {
                                        var35 = MaterialColor.Brightness.HIGH;
                                    } else if (var38 < -0.6) {
                                        var35 = MaterialColor.Brightness.LOW;
                                    } else {
                                        var35 = MaterialColor.Brightness.NORMAL;
                                    }
                                }

                                var9 = var22;
                                if (var10 >= 0 && var11 * var11 + var12 * var12 < var5 * var5 && (!var13 || (var8 + var10 & 1) != 0)) {
                                    var7 |= param2.updateColor(var8, var10, var33.getPackedId(var35));
                                }
                            }
                        }
                    }
                }
            }

        }
    }

    private BlockState getCorrectStateForFluidBlock(Level param0, BlockState param1, BlockPos param2) {
        FluidState var0 = param1.getFluidState();
        return !var0.isEmpty() && !param1.isFaceSturdy(param0, param2, Direction.UP) ? var0.createLegacyBlock() : param1;
    }

    private static boolean isBiomeWatery(boolean[] param0, int param1, int param2, int param3) {
        return param0[param2 * param1 + param3 * param1 * 128 * param1];
    }

    public static void renderBiomePreviewMap(ServerLevel param0, ItemStack param1) {
        MapItemSavedData var0 = getSavedData(param1, param0);
        if (var0 != null) {
            if (param0.dimension() == var0.dimension) {
                int var1 = 1 << var0.scale;
                int var2 = var0.x;
                int var3 = var0.z;
                boolean[] var4 = new boolean[128 * var1 * 128 * var1];

                for(int var5 = 0; var5 < 128 * var1; ++var5) {
                    for(int var6 = 0; var6 < 128 * var1; ++var6) {
                        Biome.BiomeCategory var7 = Biome.getBiomeCategory(
                            param0.getBiome(new BlockPos((var2 / var1 - 64) * var1 + var6, 0, (var3 / var1 - 64) * var1 + var5))
                        );
                        var4[var5 * 128 * var1 + var6] = var7 == Biome.BiomeCategory.OCEAN
                            || var7 == Biome.BiomeCategory.RIVER
                            || var7 == Biome.BiomeCategory.SWAMP;
                    }
                }

                for(int var8 = 0; var8 < 128; ++var8) {
                    for(int var9 = 0; var9 < 128; ++var9) {
                        if (var8 > 0 && var9 > 0 && var8 < 127 && var9 < 127) {
                            int var10 = 8;
                            if (!isBiomeWatery(var4, var1, var8 - 1, var9 - 1)) {
                                --var10;
                            }

                            if (!isBiomeWatery(var4, var1, var8 - 1, var9 + 1)) {
                                --var10;
                            }

                            if (!isBiomeWatery(var4, var1, var8 - 1, var9)) {
                                --var10;
                            }

                            if (!isBiomeWatery(var4, var1, var8 + 1, var9 - 1)) {
                                --var10;
                            }

                            if (!isBiomeWatery(var4, var1, var8 + 1, var9 + 1)) {
                                --var10;
                            }

                            if (!isBiomeWatery(var4, var1, var8 + 1, var9)) {
                                --var10;
                            }

                            if (!isBiomeWatery(var4, var1, var8, var9 - 1)) {
                                --var10;
                            }

                            if (!isBiomeWatery(var4, var1, var8, var9 + 1)) {
                                --var10;
                            }

                            MaterialColor.Brightness var11 = MaterialColor.Brightness.LOWEST;
                            MaterialColor var12 = MaterialColor.NONE;
                            if (isBiomeWatery(var4, var1, var8, var9)) {
                                var12 = MaterialColor.COLOR_ORANGE;
                                if (var10 > 7 && var9 % 2 == 0) {
                                    switch((var8 + (int)(Mth.sin((float)var9 + 0.0F) * 7.0F)) / 8 % 5) {
                                        case 0:
                                        case 4:
                                            var11 = MaterialColor.Brightness.LOW;
                                            break;
                                        case 1:
                                        case 3:
                                            var11 = MaterialColor.Brightness.NORMAL;
                                            break;
                                        case 2:
                                            var11 = MaterialColor.Brightness.HIGH;
                                    }
                                } else if (var10 > 7) {
                                    var12 = MaterialColor.NONE;
                                } else if (var10 > 5) {
                                    var11 = MaterialColor.Brightness.NORMAL;
                                } else if (var10 > 3) {
                                    var11 = MaterialColor.Brightness.LOW;
                                } else if (var10 > 1) {
                                    var11 = MaterialColor.Brightness.LOW;
                                }
                            } else if (var10 > 0) {
                                var12 = MaterialColor.COLOR_BROWN;
                                if (var10 > 3) {
                                    var11 = MaterialColor.Brightness.NORMAL;
                                } else {
                                    var11 = MaterialColor.Brightness.LOWEST;
                                }
                            }

                            if (var12 != MaterialColor.NONE) {
                                var0.setColor(var8, var9, var12.getPackedId(var11));
                            }
                        }
                    }
                }

            }
        }
    }

    @Override
    public void inventoryTick(ItemStack param0, Level param1, Entity param2, int param3, boolean param4) {
        if (!param1.isClientSide) {
            MapItemSavedData var0 = getSavedData(param0, param1);
            if (var0 != null) {
                if (param2 instanceof Player var1) {
                    var0.tickCarriedBy(var1, param0);
                }

                if (!var0.locked && (param4 || param2 instanceof Player && ((Player)param2).getOffhandItem() == param0)) {
                    this.update(param1, param2, var0);
                }

            }
        }
    }

    @Nullable
    @Override
    public Packet<?> getUpdatePacket(ItemStack param0, Level param1, Player param2) {
        Integer var0 = getMapId(param0);
        MapItemSavedData var1 = getSavedData(var0, param1);
        return var1 != null ? var1.getUpdatePacket(var0, param2) : null;
    }

    @Override
    public void onCraftedBy(ItemStack param0, Level param1, Player param2) {
        CompoundTag var0 = param0.getTag();
        if (var0 != null && var0.contains("map_scale_direction", 99)) {
            scaleMap(param0, param1, var0.getInt("map_scale_direction"));
            var0.remove("map_scale_direction");
        } else if (var0 != null && var0.contains("map_to_lock", 1) && var0.getBoolean("map_to_lock")) {
            lockMap(param1, param0);
            var0.remove("map_to_lock");
        }

    }

    private static void scaleMap(ItemStack param0, Level param1, int param2) {
        MapItemSavedData var0 = getSavedData(param0, param1);
        if (var0 != null) {
            int var1 = param1.getFreeMapId();
            param1.setMapData(makeKey(var1), var0.scaled(param2));
            storeMapData(param0, var1);
        }

    }

    public static void lockMap(Level param0, ItemStack param1) {
        MapItemSavedData var0 = getSavedData(param1, param0);
        if (var0 != null) {
            int var1 = param0.getFreeMapId();
            String var2 = makeKey(var1);
            MapItemSavedData var3 = var0.locked();
            param0.setMapData(var2, var3);
            storeMapData(param1, var1);
        }

    }

    @Override
    public void appendHoverText(ItemStack param0, @Nullable Level param1, List<Component> param2, TooltipFlag param3) {
        Integer var0 = getMapId(param0);
        MapItemSavedData var1 = param1 == null ? null : getSavedData(var0, param1);
        if (var1 != null && var1.locked) {
            param2.add(new TranslatableComponent("filled_map.locked", var0).withStyle(ChatFormatting.GRAY));
        }

        if (param3.isAdvanced()) {
            if (var1 != null) {
                param2.add(new TranslatableComponent("filled_map.id", var0).withStyle(ChatFormatting.GRAY));
                param2.add(new TranslatableComponent("filled_map.scale", 1 << var1.scale).withStyle(ChatFormatting.GRAY));
                param2.add(new TranslatableComponent("filled_map.level", var1.scale, 4).withStyle(ChatFormatting.GRAY));
            } else {
                param2.add(new TranslatableComponent("filled_map.unknown").withStyle(ChatFormatting.GRAY));
            }
        }

    }

    public static int getColor(ItemStack param0) {
        CompoundTag var0 = param0.getTagElement("display");
        if (var0 != null && var0.contains("MapColor", 99)) {
            int var1 = var0.getInt("MapColor");
            return 0xFF000000 | var1 & 16777215;
        } else {
            return -12173266;
        }
    }

    @Override
    public InteractionResult useOn(UseOnContext param0) {
        BlockState var0 = param0.getLevel().getBlockState(param0.getClickedPos());
        if (var0.is(BlockTags.BANNERS)) {
            if (!param0.getLevel().isClientSide) {
                MapItemSavedData var1 = getSavedData(param0.getItemInHand(), param0.getLevel());
                if (var1 != null && !var1.toggleBanner(param0.getLevel(), param0.getClickedPos())) {
                    return InteractionResult.FAIL;
                }
            }

            return InteractionResult.sidedSuccess(param0.getLevel().isClientSide);
        } else {
            return super.useOn(param0);
        }
    }
}
