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
import net.minecraft.core.Holder;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

public class MapItem extends ComplexItem {
    public static final int IMAGE_WIDTH = 128;
    public static final int IMAGE_HEIGHT = 128;
    private static final int DEFAULT_MAP_COLOR = -12173266;
    private static final String TAG_MAP = "map";
    public static final String MAP_SCALE_TAG = "map_scale_direction";
    public static final String MAP_LOCK_TAG = "map_to_lock";

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
            int var1 = param2.centerX;
            int var2 = param2.centerZ;
            int var3 = Mth.floor(param1.getX() - (double)var1) / var0 + 64;
            int var4 = Mth.floor(param1.getZ() - (double)var2) / var0 + 64;
            int var5 = 128 / var0;
            if (param0.dimensionType().hasCeiling()) {
                var5 /= 2;
            }

            MapItemSavedData.HoldingPlayer var6 = param2.getHoldingPlayer((Player)param1);
            ++var6.step;
            BlockPos.MutableBlockPos var7 = new BlockPos.MutableBlockPos();
            BlockPos.MutableBlockPos var8 = new BlockPos.MutableBlockPos();
            boolean var9 = false;

            for(int var10 = var3 - var5 + 1; var10 < var3 + var5; ++var10) {
                if ((var10 & 15) == (var6.step & 15) || var9) {
                    var9 = false;
                    double var11 = 0.0;

                    for(int var12 = var4 - var5 - 1; var12 < var4 + var5; ++var12) {
                        if (var10 >= 0 && var12 >= -1 && var10 < 128 && var12 < 128) {
                            int var13 = Mth.square(var10 - var3) + Mth.square(var12 - var4);
                            boolean var14 = var13 > (var5 - 2) * (var5 - 2);
                            int var15 = (var1 / var0 + var10 - 64) * var0;
                            int var16 = (var2 / var0 + var12 - 64) * var0;
                            Multiset<MapColor> var17 = LinkedHashMultiset.create();
                            LevelChunk var18 = param0.getChunk(SectionPos.blockToSectionCoord(var15), SectionPos.blockToSectionCoord(var16));
                            if (!var18.isEmpty()) {
                                int var19 = 0;
                                double var20 = 0.0;
                                if (param0.dimensionType().hasCeiling()) {
                                    int var21 = var15 + var16 * 231871;
                                    var21 = var21 * var21 * 31287121 + var21 * 11;
                                    if ((var21 >> 20 & 1) == 0) {
                                        var17.add(Blocks.DIRT.defaultBlockState().getMapColor(param0, BlockPos.ZERO), 10);
                                    } else {
                                        var17.add(Blocks.STONE.defaultBlockState().getMapColor(param0, BlockPos.ZERO), 100);
                                    }

                                    var20 = 100.0;
                                } else {
                                    for(int var22 = 0; var22 < var0; ++var22) {
                                        for(int var23 = 0; var23 < var0; ++var23) {
                                            var7.set(var15 + var22, 0, var16 + var23);
                                            int var24 = var18.getHeight(Heightmap.Types.WORLD_SURFACE, var7.getX(), var7.getZ()) + 1;
                                            BlockState var28;
                                            if (var24 <= param0.getMinBuildHeight() + 1) {
                                                var28 = Blocks.BEDROCK.defaultBlockState();
                                            } else {
                                                do {
                                                    var7.setY(--var24);
                                                    var28 = var18.getBlockState(var7);
                                                } while(var28.getMapColor(param0, var7) == MapColor.NONE && var24 > param0.getMinBuildHeight());

                                                if (var24 > param0.getMinBuildHeight() && !var28.getFluidState().isEmpty()) {
                                                    int var26 = var24 - 1;
                                                    var8.set(var7);

                                                    BlockState var27;
                                                    do {
                                                        var8.setY(var26--);
                                                        var27 = var18.getBlockState(var8);
                                                        ++var19;
                                                    } while(var26 > param0.getMinBuildHeight() && !var27.getFluidState().isEmpty());

                                                    var28 = this.getCorrectStateForFluidBlock(param0, var28, var7);
                                                }
                                            }

                                            param2.checkBanners(param0, var7.getX(), var7.getZ());
                                            var20 += (double)var24 / (double)(var0 * var0);
                                            var17.add(var28.getMapColor(param0, var7));
                                        }
                                    }
                                }

                                var19 /= var0 * var0;
                                MapColor var29 = Iterables.getFirst(Multisets.copyHighestCountFirst(var17), MapColor.NONE);
                                MapColor.Brightness var31;
                                if (var29 == MapColor.WATER) {
                                    double var30 = (double)var19 * 0.1 + (double)(var10 + var12 & 1) * 0.2;
                                    if (var30 < 0.5) {
                                        var31 = MapColor.Brightness.HIGH;
                                    } else if (var30 > 0.9) {
                                        var31 = MapColor.Brightness.LOW;
                                    } else {
                                        var31 = MapColor.Brightness.NORMAL;
                                    }
                                } else {
                                    double var34 = (var20 - var11) * 4.0 / (double)(var0 + 4) + ((double)(var10 + var12 & 1) - 0.5) * 0.4;
                                    if (var34 > 0.6) {
                                        var31 = MapColor.Brightness.HIGH;
                                    } else if (var34 < -0.6) {
                                        var31 = MapColor.Brightness.LOW;
                                    } else {
                                        var31 = MapColor.Brightness.NORMAL;
                                    }
                                }

                                var11 = var20;
                                if (var12 >= 0 && var13 < var5 * var5 && (!var14 || (var10 + var12 & 1) != 0)) {
                                    var9 |= param2.updateColor(var10, var12, var29.getPackedId(var31));
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

    private static boolean isBiomeWatery(boolean[] param0, int param1, int param2) {
        return param0[param2 * 128 + param1];
    }

    public static void renderBiomePreviewMap(ServerLevel param0, ItemStack param1) {
        MapItemSavedData var0 = getSavedData(param1, param0);
        if (var0 != null) {
            if (param0.dimension() == var0.dimension) {
                int var1 = 1 << var0.scale;
                int var2 = var0.centerX;
                int var3 = var0.centerZ;
                boolean[] var4 = new boolean[16384];
                int var5 = var2 / var1 - 64;
                int var6 = var3 / var1 - 64;
                BlockPos.MutableBlockPos var7 = new BlockPos.MutableBlockPos();

                for(int var8 = 0; var8 < 128; ++var8) {
                    for(int var9 = 0; var9 < 128; ++var9) {
                        Holder<Biome> var10 = param0.getBiome(var7.set((var5 + var9) * var1, 0, (var6 + var8) * var1));
                        var4[var8 * 128 + var9] = var10.is(BiomeTags.WATER_ON_MAP_OUTLINES);
                    }
                }

                for(int var11 = 1; var11 < 127; ++var11) {
                    for(int var12 = 1; var12 < 127; ++var12) {
                        int var13 = 0;

                        for(int var14 = -1; var14 < 2; ++var14) {
                            for(int var15 = -1; var15 < 2; ++var15) {
                                if ((var14 != 0 || var15 != 0) && isBiomeWatery(var4, var11 + var14, var12 + var15)) {
                                    ++var13;
                                }
                            }
                        }

                        MapColor.Brightness var16 = MapColor.Brightness.LOWEST;
                        MapColor var17 = MapColor.NONE;
                        if (isBiomeWatery(var4, var11, var12)) {
                            var17 = MapColor.COLOR_ORANGE;
                            if (var13 > 7 && var12 % 2 == 0) {
                                switch((var11 + (int)(Mth.sin((float)var12 + 0.0F) * 7.0F)) / 8 % 5) {
                                    case 0:
                                    case 4:
                                        var16 = MapColor.Brightness.LOW;
                                        break;
                                    case 1:
                                    case 3:
                                        var16 = MapColor.Brightness.NORMAL;
                                        break;
                                    case 2:
                                        var16 = MapColor.Brightness.HIGH;
                                }
                            } else if (var13 > 7) {
                                var17 = MapColor.NONE;
                            } else if (var13 > 5) {
                                var16 = MapColor.Brightness.NORMAL;
                            } else if (var13 > 3) {
                                var16 = MapColor.Brightness.LOW;
                            } else if (var13 > 1) {
                                var16 = MapColor.Brightness.LOW;
                            }
                        } else if (var13 > 0) {
                            var17 = MapColor.COLOR_BROWN;
                            if (var13 > 3) {
                                var16 = MapColor.Brightness.NORMAL;
                            } else {
                                var16 = MapColor.Brightness.LOWEST;
                            }
                        }

                        if (var17 != MapColor.NONE) {
                            var0.setColor(var11, var12, var17.getPackedId(var16));
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
        CompoundTag var2 = param0.getTag();
        boolean var3;
        byte var4;
        if (var2 != null) {
            var3 = var2.getBoolean("map_to_lock");
            var4 = var2.getByte("map_scale_direction");
        } else {
            var3 = false;
            var4 = 0;
        }

        if (var1 != null && (var1.locked || var3)) {
            param2.add(Component.translatable("filled_map.locked", var0).withStyle(ChatFormatting.GRAY));
        }

        if (param3.isAdvanced()) {
            if (var1 != null) {
                if (!var3 && var4 == 0) {
                    param2.add(getTooltipForId(var0));
                }

                int var7 = Math.min(var1.scale + var4, 4);
                param2.add(Component.translatable("filled_map.scale", 1 << var7).withStyle(ChatFormatting.GRAY));
                param2.add(Component.translatable("filled_map.level", var7, 4).withStyle(ChatFormatting.GRAY));
            } else {
                param2.add(Component.translatable("filled_map.unknown").withStyle(ChatFormatting.GRAY));
            }
        }

    }

    private static Component getTooltipForId(int param0) {
        return Component.translatable("filled_map.id", param0).withStyle(ChatFormatting.GRAY);
    }

    public static Component getTooltipForId(ItemStack param0) {
        return getTooltipForId(getMapId(param0));
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
