package net.minecraft.world.level.block;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.Map;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.BlockLayer;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.end.TheEndDimension;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class FireBlock extends Block {
    public static final IntegerProperty AGE = BlockStateProperties.AGE_15;
    public static final BooleanProperty NORTH = PipeBlock.NORTH;
    public static final BooleanProperty EAST = PipeBlock.EAST;
    public static final BooleanProperty SOUTH = PipeBlock.SOUTH;
    public static final BooleanProperty WEST = PipeBlock.WEST;
    public static final BooleanProperty UP = PipeBlock.UP;
    private static final Map<Direction, BooleanProperty> PROPERTY_BY_DIRECTION = PipeBlock.PROPERTY_BY_DIRECTION
        .entrySet()
        .stream()
        .filter(param0 -> param0.getKey() != Direction.DOWN)
        .collect(Util.toMap());
    private final Object2IntMap<Block> flameOdds = new Object2IntOpenHashMap<>();
    private final Object2IntMap<Block> burnOdds = new Object2IntOpenHashMap<>();

    protected FireBlock(Block.Properties param0) {
        super(param0);
        this.registerDefaultState(
            this.stateDefinition
                .any()
                .setValue(AGE, Integer.valueOf(0))
                .setValue(NORTH, Boolean.valueOf(false))
                .setValue(EAST, Boolean.valueOf(false))
                .setValue(SOUTH, Boolean.valueOf(false))
                .setValue(WEST, Boolean.valueOf(false))
                .setValue(UP, Boolean.valueOf(false))
        );
    }

    @Override
    public VoxelShape getShape(BlockState param0, BlockGetter param1, BlockPos param2, CollisionContext param3) {
        return Shapes.empty();
    }

    @Override
    public BlockState updateShape(BlockState param0, Direction param1, BlockState param2, LevelAccessor param3, BlockPos param4, BlockPos param5) {
        return this.canSurvive(param0, param3, param4)
            ? this.getStateForPlacement(param3, param4).setValue(AGE, param0.getValue(AGE))
            : Blocks.AIR.defaultBlockState();
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext param0) {
        return this.getStateForPlacement(param0.getLevel(), param0.getClickedPos());
    }

    public BlockState getStateForPlacement(BlockGetter param0, BlockPos param1) {
        BlockPos var0 = param1.below();
        BlockState var1 = param0.getBlockState(var0);
        if (!this.canBurn(var1) && !var1.isFaceSturdy(param0, var0, Direction.UP)) {
            BlockState var2 = this.defaultBlockState();

            for(Direction var3 : Direction.values()) {
                BooleanProperty var4 = PROPERTY_BY_DIRECTION.get(var3);
                if (var4 != null) {
                    var2 = var2.setValue(var4, Boolean.valueOf(this.canBurn(param0.getBlockState(param1.relative(var3)))));
                }
            }

            return var2;
        } else {
            return this.defaultBlockState();
        }
    }

    @Override
    public boolean canSurvive(BlockState param0, LevelReader param1, BlockPos param2) {
        BlockPos var0 = param2.below();
        return param1.getBlockState(var0).isFaceSturdy(param1, var0, Direction.UP) || this.isValidFireLocation(param1, param2);
    }

    @Override
    public int getTickDelay(LevelReader param0) {
        return 30;
    }

    @Override
    public void tick(BlockState param0, Level param1, BlockPos param2, Random param3) {
        if (param1.getGameRules().getBoolean(GameRules.RULE_DOFIRETICK)) {
            if (!param0.canSurvive(param1, param2)) {
                param1.removeBlock(param2, false);
            }

            Block var0 = param1.getBlockState(param2.below()).getBlock();
            boolean var1 = param1.dimension instanceof TheEndDimension && var0 == Blocks.BEDROCK || var0 == Blocks.NETHERRACK || var0 == Blocks.MAGMA_BLOCK;
            int var2 = param0.getValue(AGE);
            if (!var1 && param1.isRaining() && this.isNearRain(param1, param2) && param3.nextFloat() < 0.2F + (float)var2 * 0.03F) {
                param1.removeBlock(param2, false);
            } else {
                int var3 = Math.min(15, var2 + param3.nextInt(3) / 2);
                if (var2 != var3) {
                    param0 = param0.setValue(AGE, Integer.valueOf(var3));
                    param1.setBlock(param2, param0, 4);
                }

                if (!var1) {
                    param1.getBlockTicks().scheduleTick(param2, this, this.getTickDelay(param1) + param3.nextInt(10));
                    if (!this.isValidFireLocation(param1, param2)) {
                        BlockPos var4 = param2.below();
                        if (!param1.getBlockState(var4).isFaceSturdy(param1, var4, Direction.UP) || var2 > 3) {
                            param1.removeBlock(param2, false);
                        }

                        return;
                    }

                    if (var2 == 15 && param3.nextInt(4) == 0 && !this.canBurn(param1.getBlockState(param2.below()))) {
                        param1.removeBlock(param2, false);
                        return;
                    }
                }

                boolean var5 = param1.isHumidAt(param2);
                int var6 = var5 ? -50 : 0;
                this.checkBurnOut(param1, param2.east(), 300 + var6, param3, var2);
                this.checkBurnOut(param1, param2.west(), 300 + var6, param3, var2);
                this.checkBurnOut(param1, param2.below(), 250 + var6, param3, var2);
                this.checkBurnOut(param1, param2.above(), 250 + var6, param3, var2);
                this.checkBurnOut(param1, param2.north(), 300 + var6, param3, var2);
                this.checkBurnOut(param1, param2.south(), 300 + var6, param3, var2);
                BlockPos.MutableBlockPos var7 = new BlockPos.MutableBlockPos();

                for(int var8 = -1; var8 <= 1; ++var8) {
                    for(int var9 = -1; var9 <= 1; ++var9) {
                        for(int var10 = -1; var10 <= 4; ++var10) {
                            if (var8 != 0 || var10 != 0 || var9 != 0) {
                                int var11 = 100;
                                if (var10 > 1) {
                                    var11 += (var10 - 1) * 100;
                                }

                                var7.set(param2).move(var8, var10, var9);
                                int var12 = this.getFireOdds(param1, var7);
                                if (var12 > 0) {
                                    int var13 = (var12 + 40 + param1.getDifficulty().getId() * 7) / (var2 + 30);
                                    if (var5) {
                                        var13 /= 2;
                                    }

                                    if (var13 > 0 && param3.nextInt(var11) <= var13 && (!param1.isRaining() || !this.isNearRain(param1, var7))) {
                                        int var14 = Math.min(15, var2 + param3.nextInt(5) / 4);
                                        param1.setBlock(var7, this.getStateForPlacement(param1, var7).setValue(AGE, Integer.valueOf(var14)), 3);
                                    }
                                }
                            }
                        }
                    }
                }

            }
        }
    }

    protected boolean isNearRain(Level param0, BlockPos param1) {
        return param0.isRainingAt(param1)
            || param0.isRainingAt(param1.west())
            || param0.isRainingAt(param1.east())
            || param0.isRainingAt(param1.north())
            || param0.isRainingAt(param1.south());
    }

    private int getBurnOdd(BlockState param0) {
        return param0.hasProperty(BlockStateProperties.WATERLOGGED) && param0.getValue(BlockStateProperties.WATERLOGGED)
            ? 0
            : this.burnOdds.getInt(param0.getBlock());
    }

    private int getFlameOdds(BlockState param0) {
        return param0.hasProperty(BlockStateProperties.WATERLOGGED) && param0.getValue(BlockStateProperties.WATERLOGGED)
            ? 0
            : this.flameOdds.getInt(param0.getBlock());
    }

    private void checkBurnOut(Level param0, BlockPos param1, int param2, Random param3, int param4) {
        int var0 = this.getBurnOdd(param0.getBlockState(param1));
        if (param3.nextInt(param2) < var0) {
            BlockState var1 = param0.getBlockState(param1);
            if (param3.nextInt(param4 + 10) < 5 && !param0.isRainingAt(param1)) {
                int var2 = Math.min(param4 + param3.nextInt(5) / 4, 15);
                param0.setBlock(param1, this.getStateForPlacement(param0, param1).setValue(AGE, Integer.valueOf(var2)), 3);
            } else {
                param0.removeBlock(param1, false);
            }

            Block var3 = var1.getBlock();
            if (var3 instanceof TntBlock) {
                TntBlock.explode(param0, param1);
            }
        }

    }

    private boolean isValidFireLocation(BlockGetter param0, BlockPos param1) {
        for(Direction var0 : Direction.values()) {
            if (this.canBurn(param0.getBlockState(param1.relative(var0)))) {
                return true;
            }
        }

        return false;
    }

    private int getFireOdds(LevelReader param0, BlockPos param1) {
        if (!param0.isEmptyBlock(param1)) {
            return 0;
        } else {
            int var0 = 0;

            for(Direction var1 : Direction.values()) {
                BlockState var2 = param0.getBlockState(param1.relative(var1));
                var0 = Math.max(this.getFlameOdds(var2), var0);
            }

            return var0;
        }
    }

    public boolean canBurn(BlockState param0) {
        return this.getFlameOdds(param0) > 0;
    }

    @Override
    public void onPlace(BlockState param0, Level param1, BlockPos param2, BlockState param3, boolean param4) {
        if (param3.getBlock() != param0.getBlock()) {
            if (param1.dimension.getType() != DimensionType.OVERWORLD && param1.dimension.getType() != DimensionType.NETHER
                || !((NetherPortalBlock)Blocks.NETHER_PORTAL).trySpawnPortal(param1, param2)) {
                if (!param0.canSurvive(param1, param2)) {
                    param1.removeBlock(param2, false);
                } else {
                    param1.getBlockTicks().scheduleTick(param2, this, this.getTickDelay(param1) + param1.random.nextInt(10));
                }
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void animateTick(BlockState param0, Level param1, BlockPos param2, Random param3) {
        if (param3.nextInt(24) == 0) {
            param1.playLocalSound(
                (double)((float)param2.getX() + 0.5F),
                (double)((float)param2.getY() + 0.5F),
                (double)((float)param2.getZ() + 0.5F),
                SoundEvents.FIRE_AMBIENT,
                SoundSource.BLOCKS,
                1.0F + param3.nextFloat(),
                param3.nextFloat() * 0.7F + 0.3F,
                false
            );
        }

        BlockPos var0 = param2.below();
        BlockState var1 = param1.getBlockState(var0);
        if (!this.canBurn(var1) && !var1.isFaceSturdy(param1, var0, Direction.UP)) {
            if (this.canBurn(param1.getBlockState(param2.west()))) {
                for(int var6 = 0; var6 < 2; ++var6) {
                    double var7 = (double)param2.getX() + param3.nextDouble() * 0.1F;
                    double var8 = (double)param2.getY() + param3.nextDouble();
                    double var9 = (double)param2.getZ() + param3.nextDouble();
                    param1.addParticle(ParticleTypes.LARGE_SMOKE, var7, var8, var9, 0.0, 0.0, 0.0);
                }
            }

            if (this.canBurn(param1.getBlockState(param2.east()))) {
                for(int var10 = 0; var10 < 2; ++var10) {
                    double var11 = (double)(param2.getX() + 1) - param3.nextDouble() * 0.1F;
                    double var12 = (double)param2.getY() + param3.nextDouble();
                    double var13 = (double)param2.getZ() + param3.nextDouble();
                    param1.addParticle(ParticleTypes.LARGE_SMOKE, var11, var12, var13, 0.0, 0.0, 0.0);
                }
            }

            if (this.canBurn(param1.getBlockState(param2.north()))) {
                for(int var14 = 0; var14 < 2; ++var14) {
                    double var15 = (double)param2.getX() + param3.nextDouble();
                    double var16 = (double)param2.getY() + param3.nextDouble();
                    double var17 = (double)param2.getZ() + param3.nextDouble() * 0.1F;
                    param1.addParticle(ParticleTypes.LARGE_SMOKE, var15, var16, var17, 0.0, 0.0, 0.0);
                }
            }

            if (this.canBurn(param1.getBlockState(param2.south()))) {
                for(int var18 = 0; var18 < 2; ++var18) {
                    double var19 = (double)param2.getX() + param3.nextDouble();
                    double var20 = (double)param2.getY() + param3.nextDouble();
                    double var21 = (double)(param2.getZ() + 1) - param3.nextDouble() * 0.1F;
                    param1.addParticle(ParticleTypes.LARGE_SMOKE, var19, var20, var21, 0.0, 0.0, 0.0);
                }
            }

            if (this.canBurn(param1.getBlockState(param2.above()))) {
                for(int var22 = 0; var22 < 2; ++var22) {
                    double var23 = (double)param2.getX() + param3.nextDouble();
                    double var24 = (double)(param2.getY() + 1) - param3.nextDouble() * 0.1F;
                    double var25 = (double)param2.getZ() + param3.nextDouble();
                    param1.addParticle(ParticleTypes.LARGE_SMOKE, var23, var24, var25, 0.0, 0.0, 0.0);
                }
            }
        } else {
            for(int var2 = 0; var2 < 3; ++var2) {
                double var3 = (double)param2.getX() + param3.nextDouble();
                double var4 = (double)param2.getY() + param3.nextDouble() * 0.5 + 0.5;
                double var5 = (double)param2.getZ() + param3.nextDouble();
                param1.addParticle(ParticleTypes.LARGE_SMOKE, var3, var4, var5, 0.0, 0.0, 0.0);
            }
        }

    }

    @Override
    public BlockLayer getRenderLayer() {
        return BlockLayer.CUTOUT;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> param0) {
        param0.add(AGE, NORTH, EAST, SOUTH, WEST, UP);
    }

    public void setFlammable(Block param0, int param1, int param2) {
        this.flameOdds.put(param0, param1);
        this.burnOdds.put(param0, param2);
    }

    public static void bootStrap() {
        FireBlock var0 = (FireBlock)Blocks.FIRE;
        var0.setFlammable(Blocks.OAK_PLANKS, 5, 20);
        var0.setFlammable(Blocks.SPRUCE_PLANKS, 5, 20);
        var0.setFlammable(Blocks.BIRCH_PLANKS, 5, 20);
        var0.setFlammable(Blocks.JUNGLE_PLANKS, 5, 20);
        var0.setFlammable(Blocks.ACACIA_PLANKS, 5, 20);
        var0.setFlammable(Blocks.DARK_OAK_PLANKS, 5, 20);
        var0.setFlammable(Blocks.OAK_SLAB, 5, 20);
        var0.setFlammable(Blocks.SPRUCE_SLAB, 5, 20);
        var0.setFlammable(Blocks.BIRCH_SLAB, 5, 20);
        var0.setFlammable(Blocks.JUNGLE_SLAB, 5, 20);
        var0.setFlammable(Blocks.ACACIA_SLAB, 5, 20);
        var0.setFlammable(Blocks.DARK_OAK_SLAB, 5, 20);
        var0.setFlammable(Blocks.OAK_FENCE_GATE, 5, 20);
        var0.setFlammable(Blocks.SPRUCE_FENCE_GATE, 5, 20);
        var0.setFlammable(Blocks.BIRCH_FENCE_GATE, 5, 20);
        var0.setFlammable(Blocks.JUNGLE_FENCE_GATE, 5, 20);
        var0.setFlammable(Blocks.DARK_OAK_FENCE_GATE, 5, 20);
        var0.setFlammable(Blocks.ACACIA_FENCE_GATE, 5, 20);
        var0.setFlammable(Blocks.OAK_FENCE, 5, 20);
        var0.setFlammable(Blocks.SPRUCE_FENCE, 5, 20);
        var0.setFlammable(Blocks.BIRCH_FENCE, 5, 20);
        var0.setFlammable(Blocks.JUNGLE_FENCE, 5, 20);
        var0.setFlammable(Blocks.DARK_OAK_FENCE, 5, 20);
        var0.setFlammable(Blocks.ACACIA_FENCE, 5, 20);
        var0.setFlammable(Blocks.OAK_STAIRS, 5, 20);
        var0.setFlammable(Blocks.BIRCH_STAIRS, 5, 20);
        var0.setFlammable(Blocks.SPRUCE_STAIRS, 5, 20);
        var0.setFlammable(Blocks.JUNGLE_STAIRS, 5, 20);
        var0.setFlammable(Blocks.ACACIA_STAIRS, 5, 20);
        var0.setFlammable(Blocks.DARK_OAK_STAIRS, 5, 20);
        var0.setFlammable(Blocks.OAK_LOG, 5, 5);
        var0.setFlammable(Blocks.SPRUCE_LOG, 5, 5);
        var0.setFlammable(Blocks.BIRCH_LOG, 5, 5);
        var0.setFlammable(Blocks.JUNGLE_LOG, 5, 5);
        var0.setFlammable(Blocks.ACACIA_LOG, 5, 5);
        var0.setFlammable(Blocks.DARK_OAK_LOG, 5, 5);
        var0.setFlammable(Blocks.STRIPPED_OAK_LOG, 5, 5);
        var0.setFlammable(Blocks.STRIPPED_SPRUCE_LOG, 5, 5);
        var0.setFlammable(Blocks.STRIPPED_BIRCH_LOG, 5, 5);
        var0.setFlammable(Blocks.STRIPPED_JUNGLE_LOG, 5, 5);
        var0.setFlammable(Blocks.STRIPPED_ACACIA_LOG, 5, 5);
        var0.setFlammable(Blocks.STRIPPED_DARK_OAK_LOG, 5, 5);
        var0.setFlammable(Blocks.STRIPPED_OAK_WOOD, 5, 5);
        var0.setFlammable(Blocks.STRIPPED_SPRUCE_WOOD, 5, 5);
        var0.setFlammable(Blocks.STRIPPED_BIRCH_WOOD, 5, 5);
        var0.setFlammable(Blocks.STRIPPED_JUNGLE_WOOD, 5, 5);
        var0.setFlammable(Blocks.STRIPPED_ACACIA_WOOD, 5, 5);
        var0.setFlammable(Blocks.STRIPPED_DARK_OAK_WOOD, 5, 5);
        var0.setFlammable(Blocks.OAK_WOOD, 5, 5);
        var0.setFlammable(Blocks.SPRUCE_WOOD, 5, 5);
        var0.setFlammable(Blocks.BIRCH_WOOD, 5, 5);
        var0.setFlammable(Blocks.JUNGLE_WOOD, 5, 5);
        var0.setFlammable(Blocks.ACACIA_WOOD, 5, 5);
        var0.setFlammable(Blocks.DARK_OAK_WOOD, 5, 5);
        var0.setFlammable(Blocks.OAK_LEAVES, 30, 60);
        var0.setFlammable(Blocks.SPRUCE_LEAVES, 30, 60);
        var0.setFlammable(Blocks.BIRCH_LEAVES, 30, 60);
        var0.setFlammable(Blocks.JUNGLE_LEAVES, 30, 60);
        var0.setFlammable(Blocks.ACACIA_LEAVES, 30, 60);
        var0.setFlammable(Blocks.DARK_OAK_LEAVES, 30, 60);
        var0.setFlammable(Blocks.BOOKSHELF, 30, 20);
        var0.setFlammable(Blocks.TNT, 15, 100);
        var0.setFlammable(Blocks.GRASS, 60, 100);
        var0.setFlammable(Blocks.FERN, 60, 100);
        var0.setFlammable(Blocks.DEAD_BUSH, 60, 100);
        var0.setFlammable(Blocks.SUNFLOWER, 60, 100);
        var0.setFlammable(Blocks.LILAC, 60, 100);
        var0.setFlammable(Blocks.ROSE_BUSH, 60, 100);
        var0.setFlammable(Blocks.PEONY, 60, 100);
        var0.setFlammable(Blocks.TALL_GRASS, 60, 100);
        var0.setFlammable(Blocks.LARGE_FERN, 60, 100);
        var0.setFlammable(Blocks.DANDELION, 60, 100);
        var0.setFlammable(Blocks.POPPY, 60, 100);
        var0.setFlammable(Blocks.BLUE_ORCHID, 60, 100);
        var0.setFlammable(Blocks.ALLIUM, 60, 100);
        var0.setFlammable(Blocks.AZURE_BLUET, 60, 100);
        var0.setFlammable(Blocks.RED_TULIP, 60, 100);
        var0.setFlammable(Blocks.ORANGE_TULIP, 60, 100);
        var0.setFlammable(Blocks.WHITE_TULIP, 60, 100);
        var0.setFlammable(Blocks.PINK_TULIP, 60, 100);
        var0.setFlammable(Blocks.OXEYE_DAISY, 60, 100);
        var0.setFlammable(Blocks.CORNFLOWER, 60, 100);
        var0.setFlammable(Blocks.LILY_OF_THE_VALLEY, 60, 100);
        var0.setFlammable(Blocks.WITHER_ROSE, 60, 100);
        var0.setFlammable(Blocks.WHITE_WOOL, 30, 60);
        var0.setFlammable(Blocks.ORANGE_WOOL, 30, 60);
        var0.setFlammable(Blocks.MAGENTA_WOOL, 30, 60);
        var0.setFlammable(Blocks.LIGHT_BLUE_WOOL, 30, 60);
        var0.setFlammable(Blocks.YELLOW_WOOL, 30, 60);
        var0.setFlammable(Blocks.LIME_WOOL, 30, 60);
        var0.setFlammable(Blocks.PINK_WOOL, 30, 60);
        var0.setFlammable(Blocks.GRAY_WOOL, 30, 60);
        var0.setFlammable(Blocks.LIGHT_GRAY_WOOL, 30, 60);
        var0.setFlammable(Blocks.CYAN_WOOL, 30, 60);
        var0.setFlammable(Blocks.PURPLE_WOOL, 30, 60);
        var0.setFlammable(Blocks.BLUE_WOOL, 30, 60);
        var0.setFlammable(Blocks.BROWN_WOOL, 30, 60);
        var0.setFlammable(Blocks.GREEN_WOOL, 30, 60);
        var0.setFlammable(Blocks.RED_WOOL, 30, 60);
        var0.setFlammable(Blocks.BLACK_WOOL, 30, 60);
        var0.setFlammable(Blocks.VINE, 15, 100);
        var0.setFlammable(Blocks.COAL_BLOCK, 5, 5);
        var0.setFlammable(Blocks.HAY_BLOCK, 60, 20);
        var0.setFlammable(Blocks.WHITE_CARPET, 60, 20);
        var0.setFlammable(Blocks.ORANGE_CARPET, 60, 20);
        var0.setFlammable(Blocks.MAGENTA_CARPET, 60, 20);
        var0.setFlammable(Blocks.LIGHT_BLUE_CARPET, 60, 20);
        var0.setFlammable(Blocks.YELLOW_CARPET, 60, 20);
        var0.setFlammable(Blocks.LIME_CARPET, 60, 20);
        var0.setFlammable(Blocks.PINK_CARPET, 60, 20);
        var0.setFlammable(Blocks.GRAY_CARPET, 60, 20);
        var0.setFlammable(Blocks.LIGHT_GRAY_CARPET, 60, 20);
        var0.setFlammable(Blocks.CYAN_CARPET, 60, 20);
        var0.setFlammable(Blocks.PURPLE_CARPET, 60, 20);
        var0.setFlammable(Blocks.BLUE_CARPET, 60, 20);
        var0.setFlammable(Blocks.BROWN_CARPET, 60, 20);
        var0.setFlammable(Blocks.GREEN_CARPET, 60, 20);
        var0.setFlammable(Blocks.RED_CARPET, 60, 20);
        var0.setFlammable(Blocks.BLACK_CARPET, 60, 20);
        var0.setFlammable(Blocks.DRIED_KELP_BLOCK, 30, 60);
        var0.setFlammable(Blocks.BAMBOO, 60, 60);
        var0.setFlammable(Blocks.SCAFFOLDING, 60, 60);
        var0.setFlammable(Blocks.LECTERN, 30, 20);
        var0.setFlammable(Blocks.COMPOSTER, 5, 20);
        var0.setFlammable(Blocks.SWEET_BERRY_BUSH, 60, 100);
    }
}
