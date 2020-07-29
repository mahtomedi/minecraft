package net.minecraft.data.models;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonElement;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.annotation.Nullable;
import net.minecraft.core.Direction;
import net.minecraft.core.FrontAndTop;
import net.minecraft.data.models.blockstates.BlockStateGenerator;
import net.minecraft.data.models.blockstates.Condition;
import net.minecraft.data.models.blockstates.MultiPartGenerator;
import net.minecraft.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.data.models.blockstates.PropertyDispatch;
import net.minecraft.data.models.blockstates.Variant;
import net.minecraft.data.models.blockstates.VariantProperties;
import net.minecraft.data.models.model.DelegatedModel;
import net.minecraft.data.models.model.ModelLocationUtils;
import net.minecraft.data.models.model.ModelTemplate;
import net.minecraft.data.models.model.ModelTemplates;
import net.minecraft.data.models.model.TextureMapping;
import net.minecraft.data.models.model.TextureSlot;
import net.minecraft.data.models.model.TexturedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BambooLeaves;
import net.minecraft.world.level.block.state.properties.BellAttachType;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.ComparatorMode;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.PistonType;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.level.block.state.properties.RedstoneSide;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.block.state.properties.StairsShape;
import net.minecraft.world.level.block.state.properties.WallSide;

public class BlockModelGenerators {
    private final Consumer<BlockStateGenerator> blockStateOutput;
    private final BiConsumer<ResourceLocation, Supplier<JsonElement>> modelOutput;
    private final Consumer<Item> skippedAutoModelsOutput;

    public BlockModelGenerators(Consumer<BlockStateGenerator> param0, BiConsumer<ResourceLocation, Supplier<JsonElement>> param1, Consumer<Item> param2) {
        this.blockStateOutput = param0;
        this.modelOutput = param1;
        this.skippedAutoModelsOutput = param2;
    }

    private void skipAutoItemBlock(Block param0) {
        this.skippedAutoModelsOutput.accept(param0.asItem());
    }

    private void delegateItemModel(Block param0, ResourceLocation param1) {
        this.modelOutput.accept(ModelLocationUtils.getModelLocation(param0.asItem()), new DelegatedModel(param1));
    }

    private void delegateItemModel(Item param0, ResourceLocation param1) {
        this.modelOutput.accept(ModelLocationUtils.getModelLocation(param0), new DelegatedModel(param1));
    }

    private void createSimpleFlatItemModel(Item param0) {
        ModelTemplates.FLAT_ITEM.create(ModelLocationUtils.getModelLocation(param0), TextureMapping.layer0(param0), this.modelOutput);
    }

    private void createSimpleFlatItemModel(Block param0) {
        Item var0 = param0.asItem();
        if (var0 != Items.AIR) {
            ModelTemplates.FLAT_ITEM.create(ModelLocationUtils.getModelLocation(var0), TextureMapping.layer0(param0), this.modelOutput);
        }

    }

    private void createSimpleFlatItemModel(Block param0, String param1) {
        Item var0 = param0.asItem();
        ModelTemplates.FLAT_ITEM
            .create(ModelLocationUtils.getModelLocation(var0), TextureMapping.layer0(TextureMapping.getBlockTexture(param0, param1)), this.modelOutput);
    }

    private static PropertyDispatch createHorizontalFacingDispatch() {
        return PropertyDispatch.property(BlockStateProperties.HORIZONTAL_FACING)
            .select(Direction.EAST, Variant.variant().with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90))
            .select(Direction.SOUTH, Variant.variant().with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180))
            .select(Direction.WEST, Variant.variant().with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270))
            .select(Direction.NORTH, Variant.variant());
    }

    private static PropertyDispatch createHorizontalFacingDispatchAlt() {
        return PropertyDispatch.property(BlockStateProperties.HORIZONTAL_FACING)
            .select(Direction.SOUTH, Variant.variant())
            .select(Direction.WEST, Variant.variant().with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90))
            .select(Direction.NORTH, Variant.variant().with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180))
            .select(Direction.EAST, Variant.variant().with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270));
    }

    private static PropertyDispatch createTorchHorizontalDispatch() {
        return PropertyDispatch.property(BlockStateProperties.HORIZONTAL_FACING)
            .select(Direction.EAST, Variant.variant())
            .select(Direction.SOUTH, Variant.variant().with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90))
            .select(Direction.WEST, Variant.variant().with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180))
            .select(Direction.NORTH, Variant.variant().with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270));
    }

    private static PropertyDispatch createFacingDispatch() {
        return PropertyDispatch.property(BlockStateProperties.FACING)
            .select(Direction.DOWN, Variant.variant().with(VariantProperties.X_ROT, VariantProperties.Rotation.R90))
            .select(Direction.UP, Variant.variant().with(VariantProperties.X_ROT, VariantProperties.Rotation.R270))
            .select(Direction.NORTH, Variant.variant())
            .select(Direction.SOUTH, Variant.variant().with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180))
            .select(Direction.WEST, Variant.variant().with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270))
            .select(Direction.EAST, Variant.variant().with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90));
    }

    private static MultiVariantGenerator createRotatedVariant(Block param0, ResourceLocation param1) {
        return MultiVariantGenerator.multiVariant(param0, createRotatedVariants(param1));
    }

    private static Variant[] createRotatedVariants(ResourceLocation param0) {
        return new Variant[]{
            Variant.variant().with(VariantProperties.MODEL, param0),
            Variant.variant().with(VariantProperties.MODEL, param0).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90),
            Variant.variant().with(VariantProperties.MODEL, param0).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180),
            Variant.variant().with(VariantProperties.MODEL, param0).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
        };
    }

    private static MultiVariantGenerator createRotatedVariant(Block param0, ResourceLocation param1, ResourceLocation param2) {
        return MultiVariantGenerator.multiVariant(
            param0,
            Variant.variant().with(VariantProperties.MODEL, param1),
            Variant.variant().with(VariantProperties.MODEL, param2),
            Variant.variant().with(VariantProperties.MODEL, param1).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180),
            Variant.variant().with(VariantProperties.MODEL, param2).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
        );
    }

    private static PropertyDispatch createBooleanModelDispatch(BooleanProperty param0, ResourceLocation param1, ResourceLocation param2) {
        return PropertyDispatch.property(param0)
            .select(true, Variant.variant().with(VariantProperties.MODEL, param1))
            .select(false, Variant.variant().with(VariantProperties.MODEL, param2));
    }

    private void createRotatedMirroredVariantBlock(Block param0) {
        ResourceLocation var0 = TexturedModel.CUBE.create(param0, this.modelOutput);
        ResourceLocation var1 = TexturedModel.CUBE_MIRRORED.create(param0, this.modelOutput);
        this.blockStateOutput.accept(createRotatedVariant(param0, var0, var1));
    }

    private void createRotatedVariantBlock(Block param0) {
        ResourceLocation var0 = TexturedModel.CUBE.create(param0, this.modelOutput);
        this.blockStateOutput.accept(createRotatedVariant(param0, var0));
    }

    private static BlockStateGenerator createButton(Block param0, ResourceLocation param1, ResourceLocation param2) {
        return MultiVariantGenerator.multiVariant(param0)
            .with(
                PropertyDispatch.property(BlockStateProperties.POWERED)
                    .select(false, Variant.variant().with(VariantProperties.MODEL, param1))
                    .select(true, Variant.variant().with(VariantProperties.MODEL, param2))
            )
            .with(
                PropertyDispatch.properties(BlockStateProperties.ATTACH_FACE, BlockStateProperties.HORIZONTAL_FACING)
                    .select(AttachFace.FLOOR, Direction.EAST, Variant.variant().with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90))
                    .select(AttachFace.FLOOR, Direction.WEST, Variant.variant().with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270))
                    .select(AttachFace.FLOOR, Direction.SOUTH, Variant.variant().with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180))
                    .select(AttachFace.FLOOR, Direction.NORTH, Variant.variant())
                    .select(
                        AttachFace.WALL,
                        Direction.EAST,
                        Variant.variant()
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                            .with(VariantProperties.X_ROT, VariantProperties.Rotation.R90)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .select(
                        AttachFace.WALL,
                        Direction.WEST,
                        Variant.variant()
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                            .with(VariantProperties.X_ROT, VariantProperties.Rotation.R90)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .select(
                        AttachFace.WALL,
                        Direction.SOUTH,
                        Variant.variant()
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                            .with(VariantProperties.X_ROT, VariantProperties.Rotation.R90)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .select(
                        AttachFace.WALL,
                        Direction.NORTH,
                        Variant.variant().with(VariantProperties.X_ROT, VariantProperties.Rotation.R90).with(VariantProperties.UV_LOCK, true)
                    )
                    .select(
                        AttachFace.CEILING,
                        Direction.EAST,
                        Variant.variant()
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                            .with(VariantProperties.X_ROT, VariantProperties.Rotation.R180)
                    )
                    .select(
                        AttachFace.CEILING,
                        Direction.WEST,
                        Variant.variant()
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                            .with(VariantProperties.X_ROT, VariantProperties.Rotation.R180)
                    )
                    .select(AttachFace.CEILING, Direction.SOUTH, Variant.variant().with(VariantProperties.X_ROT, VariantProperties.Rotation.R180))
                    .select(
                        AttachFace.CEILING,
                        Direction.NORTH,
                        Variant.variant()
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                            .with(VariantProperties.X_ROT, VariantProperties.Rotation.R180)
                    )
            );
    }

    private static PropertyDispatch.C4<Direction, DoubleBlockHalf, DoorHingeSide, Boolean> configureDoorHalf(
        PropertyDispatch.C4<Direction, DoubleBlockHalf, DoorHingeSide, Boolean> param0,
        DoubleBlockHalf param1,
        ResourceLocation param2,
        ResourceLocation param3
    ) {
        return param0.select(Direction.EAST, param1, DoorHingeSide.LEFT, false, Variant.variant().with(VariantProperties.MODEL, param2))
            .select(
                Direction.SOUTH,
                param1,
                DoorHingeSide.LEFT,
                false,
                Variant.variant().with(VariantProperties.MODEL, param2).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
            )
            .select(
                Direction.WEST,
                param1,
                DoorHingeSide.LEFT,
                false,
                Variant.variant().with(VariantProperties.MODEL, param2).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
            )
            .select(
                Direction.NORTH,
                param1,
                DoorHingeSide.LEFT,
                false,
                Variant.variant().with(VariantProperties.MODEL, param2).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
            )
            .select(Direction.EAST, param1, DoorHingeSide.RIGHT, false, Variant.variant().with(VariantProperties.MODEL, param3))
            .select(
                Direction.SOUTH,
                param1,
                DoorHingeSide.RIGHT,
                false,
                Variant.variant().with(VariantProperties.MODEL, param3).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
            )
            .select(
                Direction.WEST,
                param1,
                DoorHingeSide.RIGHT,
                false,
                Variant.variant().with(VariantProperties.MODEL, param3).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
            )
            .select(
                Direction.NORTH,
                param1,
                DoorHingeSide.RIGHT,
                false,
                Variant.variant().with(VariantProperties.MODEL, param3).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
            )
            .select(
                Direction.EAST,
                param1,
                DoorHingeSide.LEFT,
                true,
                Variant.variant().with(VariantProperties.MODEL, param3).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
            )
            .select(
                Direction.SOUTH,
                param1,
                DoorHingeSide.LEFT,
                true,
                Variant.variant().with(VariantProperties.MODEL, param3).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
            )
            .select(
                Direction.WEST,
                param1,
                DoorHingeSide.LEFT,
                true,
                Variant.variant().with(VariantProperties.MODEL, param3).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
            )
            .select(Direction.NORTH, param1, DoorHingeSide.LEFT, true, Variant.variant().with(VariantProperties.MODEL, param3))
            .select(
                Direction.EAST,
                param1,
                DoorHingeSide.RIGHT,
                true,
                Variant.variant().with(VariantProperties.MODEL, param2).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
            )
            .select(Direction.SOUTH, param1, DoorHingeSide.RIGHT, true, Variant.variant().with(VariantProperties.MODEL, param2))
            .select(
                Direction.WEST,
                param1,
                DoorHingeSide.RIGHT,
                true,
                Variant.variant().with(VariantProperties.MODEL, param2).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
            )
            .select(
                Direction.NORTH,
                param1,
                DoorHingeSide.RIGHT,
                true,
                Variant.variant().with(VariantProperties.MODEL, param2).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
            );
    }

    private static BlockStateGenerator createDoor(
        Block param0, ResourceLocation param1, ResourceLocation param2, ResourceLocation param3, ResourceLocation param4
    ) {
        return MultiVariantGenerator.multiVariant(param0)
            .with(
                configureDoorHalf(
                    configureDoorHalf(
                        PropertyDispatch.properties(
                            BlockStateProperties.HORIZONTAL_FACING,
                            BlockStateProperties.DOUBLE_BLOCK_HALF,
                            BlockStateProperties.DOOR_HINGE,
                            BlockStateProperties.OPEN
                        ),
                        DoubleBlockHalf.LOWER,
                        param1,
                        param2
                    ),
                    DoubleBlockHalf.UPPER,
                    param3,
                    param4
                )
            );
    }

    private static BlockStateGenerator createFence(Block param0, ResourceLocation param1, ResourceLocation param2) {
        return MultiPartGenerator.multiPart(param0)
            .with(Variant.variant().with(VariantProperties.MODEL, param1))
            .with(
                Condition.condition().term(BlockStateProperties.NORTH, true),
                Variant.variant().with(VariantProperties.MODEL, param2).with(VariantProperties.UV_LOCK, true)
            )
            .with(
                Condition.condition().term(BlockStateProperties.EAST, true),
                Variant.variant()
                    .with(VariantProperties.MODEL, param2)
                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                    .with(VariantProperties.UV_LOCK, true)
            )
            .with(
                Condition.condition().term(BlockStateProperties.SOUTH, true),
                Variant.variant()
                    .with(VariantProperties.MODEL, param2)
                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                    .with(VariantProperties.UV_LOCK, true)
            )
            .with(
                Condition.condition().term(BlockStateProperties.WEST, true),
                Variant.variant()
                    .with(VariantProperties.MODEL, param2)
                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                    .with(VariantProperties.UV_LOCK, true)
            );
    }

    private static BlockStateGenerator createWall(Block param0, ResourceLocation param1, ResourceLocation param2, ResourceLocation param3) {
        return MultiPartGenerator.multiPart(param0)
            .with(Condition.condition().term(BlockStateProperties.UP, true), Variant.variant().with(VariantProperties.MODEL, param1))
            .with(
                Condition.condition().term(BlockStateProperties.NORTH_WALL, WallSide.LOW),
                Variant.variant().with(VariantProperties.MODEL, param2).with(VariantProperties.UV_LOCK, true)
            )
            .with(
                Condition.condition().term(BlockStateProperties.EAST_WALL, WallSide.LOW),
                Variant.variant()
                    .with(VariantProperties.MODEL, param2)
                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                    .with(VariantProperties.UV_LOCK, true)
            )
            .with(
                Condition.condition().term(BlockStateProperties.SOUTH_WALL, WallSide.LOW),
                Variant.variant()
                    .with(VariantProperties.MODEL, param2)
                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                    .with(VariantProperties.UV_LOCK, true)
            )
            .with(
                Condition.condition().term(BlockStateProperties.WEST_WALL, WallSide.LOW),
                Variant.variant()
                    .with(VariantProperties.MODEL, param2)
                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                    .with(VariantProperties.UV_LOCK, true)
            )
            .with(
                Condition.condition().term(BlockStateProperties.NORTH_WALL, WallSide.TALL),
                Variant.variant().with(VariantProperties.MODEL, param3).with(VariantProperties.UV_LOCK, true)
            )
            .with(
                Condition.condition().term(BlockStateProperties.EAST_WALL, WallSide.TALL),
                Variant.variant()
                    .with(VariantProperties.MODEL, param3)
                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                    .with(VariantProperties.UV_LOCK, true)
            )
            .with(
                Condition.condition().term(BlockStateProperties.SOUTH_WALL, WallSide.TALL),
                Variant.variant()
                    .with(VariantProperties.MODEL, param3)
                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                    .with(VariantProperties.UV_LOCK, true)
            )
            .with(
                Condition.condition().term(BlockStateProperties.WEST_WALL, WallSide.TALL),
                Variant.variant()
                    .with(VariantProperties.MODEL, param3)
                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                    .with(VariantProperties.UV_LOCK, true)
            );
    }

    private static BlockStateGenerator createFenceGate(
        Block param0, ResourceLocation param1, ResourceLocation param2, ResourceLocation param3, ResourceLocation param4
    ) {
        return MultiVariantGenerator.multiVariant(param0, Variant.variant().with(VariantProperties.UV_LOCK, true))
            .with(createHorizontalFacingDispatchAlt())
            .with(
                PropertyDispatch.properties(BlockStateProperties.IN_WALL, BlockStateProperties.OPEN)
                    .select(false, false, Variant.variant().with(VariantProperties.MODEL, param2))
                    .select(true, false, Variant.variant().with(VariantProperties.MODEL, param4))
                    .select(false, true, Variant.variant().with(VariantProperties.MODEL, param1))
                    .select(true, true, Variant.variant().with(VariantProperties.MODEL, param3))
            );
    }

    private static BlockStateGenerator createStairs(Block param0, ResourceLocation param1, ResourceLocation param2, ResourceLocation param3) {
        return MultiVariantGenerator.multiVariant(param0)
            .with(
                PropertyDispatch.properties(BlockStateProperties.HORIZONTAL_FACING, BlockStateProperties.HALF, BlockStateProperties.STAIRS_SHAPE)
                    .select(Direction.EAST, Half.BOTTOM, StairsShape.STRAIGHT, Variant.variant().with(VariantProperties.MODEL, param2))
                    .select(
                        Direction.WEST,
                        Half.BOTTOM,
                        StairsShape.STRAIGHT,
                        Variant.variant()
                            .with(VariantProperties.MODEL, param2)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .select(
                        Direction.SOUTH,
                        Half.BOTTOM,
                        StairsShape.STRAIGHT,
                        Variant.variant()
                            .with(VariantProperties.MODEL, param2)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .select(
                        Direction.NORTH,
                        Half.BOTTOM,
                        StairsShape.STRAIGHT,
                        Variant.variant()
                            .with(VariantProperties.MODEL, param2)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .select(Direction.EAST, Half.BOTTOM, StairsShape.OUTER_RIGHT, Variant.variant().with(VariantProperties.MODEL, param3))
                    .select(
                        Direction.WEST,
                        Half.BOTTOM,
                        StairsShape.OUTER_RIGHT,
                        Variant.variant()
                            .with(VariantProperties.MODEL, param3)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .select(
                        Direction.SOUTH,
                        Half.BOTTOM,
                        StairsShape.OUTER_RIGHT,
                        Variant.variant()
                            .with(VariantProperties.MODEL, param3)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .select(
                        Direction.NORTH,
                        Half.BOTTOM,
                        StairsShape.OUTER_RIGHT,
                        Variant.variant()
                            .with(VariantProperties.MODEL, param3)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .select(
                        Direction.EAST,
                        Half.BOTTOM,
                        StairsShape.OUTER_LEFT,
                        Variant.variant()
                            .with(VariantProperties.MODEL, param3)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .select(
                        Direction.WEST,
                        Half.BOTTOM,
                        StairsShape.OUTER_LEFT,
                        Variant.variant()
                            .with(VariantProperties.MODEL, param3)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .select(Direction.SOUTH, Half.BOTTOM, StairsShape.OUTER_LEFT, Variant.variant().with(VariantProperties.MODEL, param3))
                    .select(
                        Direction.NORTH,
                        Half.BOTTOM,
                        StairsShape.OUTER_LEFT,
                        Variant.variant()
                            .with(VariantProperties.MODEL, param3)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .select(Direction.EAST, Half.BOTTOM, StairsShape.INNER_RIGHT, Variant.variant().with(VariantProperties.MODEL, param1))
                    .select(
                        Direction.WEST,
                        Half.BOTTOM,
                        StairsShape.INNER_RIGHT,
                        Variant.variant()
                            .with(VariantProperties.MODEL, param1)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .select(
                        Direction.SOUTH,
                        Half.BOTTOM,
                        StairsShape.INNER_RIGHT,
                        Variant.variant()
                            .with(VariantProperties.MODEL, param1)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .select(
                        Direction.NORTH,
                        Half.BOTTOM,
                        StairsShape.INNER_RIGHT,
                        Variant.variant()
                            .with(VariantProperties.MODEL, param1)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .select(
                        Direction.EAST,
                        Half.BOTTOM,
                        StairsShape.INNER_LEFT,
                        Variant.variant()
                            .with(VariantProperties.MODEL, param1)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .select(
                        Direction.WEST,
                        Half.BOTTOM,
                        StairsShape.INNER_LEFT,
                        Variant.variant()
                            .with(VariantProperties.MODEL, param1)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .select(Direction.SOUTH, Half.BOTTOM, StairsShape.INNER_LEFT, Variant.variant().with(VariantProperties.MODEL, param1))
                    .select(
                        Direction.NORTH,
                        Half.BOTTOM,
                        StairsShape.INNER_LEFT,
                        Variant.variant()
                            .with(VariantProperties.MODEL, param1)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .select(
                        Direction.EAST,
                        Half.TOP,
                        StairsShape.STRAIGHT,
                        Variant.variant()
                            .with(VariantProperties.MODEL, param2)
                            .with(VariantProperties.X_ROT, VariantProperties.Rotation.R180)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .select(
                        Direction.WEST,
                        Half.TOP,
                        StairsShape.STRAIGHT,
                        Variant.variant()
                            .with(VariantProperties.MODEL, param2)
                            .with(VariantProperties.X_ROT, VariantProperties.Rotation.R180)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .select(
                        Direction.SOUTH,
                        Half.TOP,
                        StairsShape.STRAIGHT,
                        Variant.variant()
                            .with(VariantProperties.MODEL, param2)
                            .with(VariantProperties.X_ROT, VariantProperties.Rotation.R180)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .select(
                        Direction.NORTH,
                        Half.TOP,
                        StairsShape.STRAIGHT,
                        Variant.variant()
                            .with(VariantProperties.MODEL, param2)
                            .with(VariantProperties.X_ROT, VariantProperties.Rotation.R180)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .select(
                        Direction.EAST,
                        Half.TOP,
                        StairsShape.OUTER_RIGHT,
                        Variant.variant()
                            .with(VariantProperties.MODEL, param3)
                            .with(VariantProperties.X_ROT, VariantProperties.Rotation.R180)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .select(
                        Direction.WEST,
                        Half.TOP,
                        StairsShape.OUTER_RIGHT,
                        Variant.variant()
                            .with(VariantProperties.MODEL, param3)
                            .with(VariantProperties.X_ROT, VariantProperties.Rotation.R180)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .select(
                        Direction.SOUTH,
                        Half.TOP,
                        StairsShape.OUTER_RIGHT,
                        Variant.variant()
                            .with(VariantProperties.MODEL, param3)
                            .with(VariantProperties.X_ROT, VariantProperties.Rotation.R180)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .select(
                        Direction.NORTH,
                        Half.TOP,
                        StairsShape.OUTER_RIGHT,
                        Variant.variant()
                            .with(VariantProperties.MODEL, param3)
                            .with(VariantProperties.X_ROT, VariantProperties.Rotation.R180)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .select(
                        Direction.EAST,
                        Half.TOP,
                        StairsShape.OUTER_LEFT,
                        Variant.variant()
                            .with(VariantProperties.MODEL, param3)
                            .with(VariantProperties.X_ROT, VariantProperties.Rotation.R180)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .select(
                        Direction.WEST,
                        Half.TOP,
                        StairsShape.OUTER_LEFT,
                        Variant.variant()
                            .with(VariantProperties.MODEL, param3)
                            .with(VariantProperties.X_ROT, VariantProperties.Rotation.R180)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .select(
                        Direction.SOUTH,
                        Half.TOP,
                        StairsShape.OUTER_LEFT,
                        Variant.variant()
                            .with(VariantProperties.MODEL, param3)
                            .with(VariantProperties.X_ROT, VariantProperties.Rotation.R180)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .select(
                        Direction.NORTH,
                        Half.TOP,
                        StairsShape.OUTER_LEFT,
                        Variant.variant()
                            .with(VariantProperties.MODEL, param3)
                            .with(VariantProperties.X_ROT, VariantProperties.Rotation.R180)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .select(
                        Direction.EAST,
                        Half.TOP,
                        StairsShape.INNER_RIGHT,
                        Variant.variant()
                            .with(VariantProperties.MODEL, param1)
                            .with(VariantProperties.X_ROT, VariantProperties.Rotation.R180)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .select(
                        Direction.WEST,
                        Half.TOP,
                        StairsShape.INNER_RIGHT,
                        Variant.variant()
                            .with(VariantProperties.MODEL, param1)
                            .with(VariantProperties.X_ROT, VariantProperties.Rotation.R180)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .select(
                        Direction.SOUTH,
                        Half.TOP,
                        StairsShape.INNER_RIGHT,
                        Variant.variant()
                            .with(VariantProperties.MODEL, param1)
                            .with(VariantProperties.X_ROT, VariantProperties.Rotation.R180)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .select(
                        Direction.NORTH,
                        Half.TOP,
                        StairsShape.INNER_RIGHT,
                        Variant.variant()
                            .with(VariantProperties.MODEL, param1)
                            .with(VariantProperties.X_ROT, VariantProperties.Rotation.R180)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .select(
                        Direction.EAST,
                        Half.TOP,
                        StairsShape.INNER_LEFT,
                        Variant.variant()
                            .with(VariantProperties.MODEL, param1)
                            .with(VariantProperties.X_ROT, VariantProperties.Rotation.R180)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .select(
                        Direction.WEST,
                        Half.TOP,
                        StairsShape.INNER_LEFT,
                        Variant.variant()
                            .with(VariantProperties.MODEL, param1)
                            .with(VariantProperties.X_ROT, VariantProperties.Rotation.R180)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .select(
                        Direction.SOUTH,
                        Half.TOP,
                        StairsShape.INNER_LEFT,
                        Variant.variant()
                            .with(VariantProperties.MODEL, param1)
                            .with(VariantProperties.X_ROT, VariantProperties.Rotation.R180)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .select(
                        Direction.NORTH,
                        Half.TOP,
                        StairsShape.INNER_LEFT,
                        Variant.variant()
                            .with(VariantProperties.MODEL, param1)
                            .with(VariantProperties.X_ROT, VariantProperties.Rotation.R180)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                            .with(VariantProperties.UV_LOCK, true)
                    )
            );
    }

    private static BlockStateGenerator createOrientableTrapdoor(Block param0, ResourceLocation param1, ResourceLocation param2, ResourceLocation param3) {
        return MultiVariantGenerator.multiVariant(param0)
            .with(
                PropertyDispatch.properties(BlockStateProperties.HORIZONTAL_FACING, BlockStateProperties.HALF, BlockStateProperties.OPEN)
                    .select(Direction.NORTH, Half.BOTTOM, false, Variant.variant().with(VariantProperties.MODEL, param2))
                    .select(
                        Direction.SOUTH,
                        Half.BOTTOM,
                        false,
                        Variant.variant().with(VariantProperties.MODEL, param2).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                    )
                    .select(
                        Direction.EAST,
                        Half.BOTTOM,
                        false,
                        Variant.variant().with(VariantProperties.MODEL, param2).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                    )
                    .select(
                        Direction.WEST,
                        Half.BOTTOM,
                        false,
                        Variant.variant().with(VariantProperties.MODEL, param2).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                    )
                    .select(Direction.NORTH, Half.TOP, false, Variant.variant().with(VariantProperties.MODEL, param1))
                    .select(
                        Direction.SOUTH,
                        Half.TOP,
                        false,
                        Variant.variant().with(VariantProperties.MODEL, param1).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                    )
                    .select(
                        Direction.EAST,
                        Half.TOP,
                        false,
                        Variant.variant().with(VariantProperties.MODEL, param1).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                    )
                    .select(
                        Direction.WEST,
                        Half.TOP,
                        false,
                        Variant.variant().with(VariantProperties.MODEL, param1).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                    )
                    .select(Direction.NORTH, Half.BOTTOM, true, Variant.variant().with(VariantProperties.MODEL, param3))
                    .select(
                        Direction.SOUTH,
                        Half.BOTTOM,
                        true,
                        Variant.variant().with(VariantProperties.MODEL, param3).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                    )
                    .select(
                        Direction.EAST,
                        Half.BOTTOM,
                        true,
                        Variant.variant().with(VariantProperties.MODEL, param3).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                    )
                    .select(
                        Direction.WEST,
                        Half.BOTTOM,
                        true,
                        Variant.variant().with(VariantProperties.MODEL, param3).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                    )
                    .select(
                        Direction.NORTH,
                        Half.TOP,
                        true,
                        Variant.variant()
                            .with(VariantProperties.MODEL, param3)
                            .with(VariantProperties.X_ROT, VariantProperties.Rotation.R180)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                    )
                    .select(
                        Direction.SOUTH,
                        Half.TOP,
                        true,
                        Variant.variant()
                            .with(VariantProperties.MODEL, param3)
                            .with(VariantProperties.X_ROT, VariantProperties.Rotation.R180)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R0)
                    )
                    .select(
                        Direction.EAST,
                        Half.TOP,
                        true,
                        Variant.variant()
                            .with(VariantProperties.MODEL, param3)
                            .with(VariantProperties.X_ROT, VariantProperties.Rotation.R180)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                    )
                    .select(
                        Direction.WEST,
                        Half.TOP,
                        true,
                        Variant.variant()
                            .with(VariantProperties.MODEL, param3)
                            .with(VariantProperties.X_ROT, VariantProperties.Rotation.R180)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                    )
            );
    }

    private static BlockStateGenerator createTrapdoor(Block param0, ResourceLocation param1, ResourceLocation param2, ResourceLocation param3) {
        return MultiVariantGenerator.multiVariant(param0)
            .with(
                PropertyDispatch.properties(BlockStateProperties.HORIZONTAL_FACING, BlockStateProperties.HALF, BlockStateProperties.OPEN)
                    .select(Direction.NORTH, Half.BOTTOM, false, Variant.variant().with(VariantProperties.MODEL, param2))
                    .select(Direction.SOUTH, Half.BOTTOM, false, Variant.variant().with(VariantProperties.MODEL, param2))
                    .select(Direction.EAST, Half.BOTTOM, false, Variant.variant().with(VariantProperties.MODEL, param2))
                    .select(Direction.WEST, Half.BOTTOM, false, Variant.variant().with(VariantProperties.MODEL, param2))
                    .select(Direction.NORTH, Half.TOP, false, Variant.variant().with(VariantProperties.MODEL, param1))
                    .select(Direction.SOUTH, Half.TOP, false, Variant.variant().with(VariantProperties.MODEL, param1))
                    .select(Direction.EAST, Half.TOP, false, Variant.variant().with(VariantProperties.MODEL, param1))
                    .select(Direction.WEST, Half.TOP, false, Variant.variant().with(VariantProperties.MODEL, param1))
                    .select(Direction.NORTH, Half.BOTTOM, true, Variant.variant().with(VariantProperties.MODEL, param3))
                    .select(
                        Direction.SOUTH,
                        Half.BOTTOM,
                        true,
                        Variant.variant().with(VariantProperties.MODEL, param3).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                    )
                    .select(
                        Direction.EAST,
                        Half.BOTTOM,
                        true,
                        Variant.variant().with(VariantProperties.MODEL, param3).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                    )
                    .select(
                        Direction.WEST,
                        Half.BOTTOM,
                        true,
                        Variant.variant().with(VariantProperties.MODEL, param3).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                    )
                    .select(Direction.NORTH, Half.TOP, true, Variant.variant().with(VariantProperties.MODEL, param3))
                    .select(
                        Direction.SOUTH,
                        Half.TOP,
                        true,
                        Variant.variant().with(VariantProperties.MODEL, param3).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                    )
                    .select(
                        Direction.EAST,
                        Half.TOP,
                        true,
                        Variant.variant().with(VariantProperties.MODEL, param3).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                    )
                    .select(
                        Direction.WEST,
                        Half.TOP,
                        true,
                        Variant.variant().with(VariantProperties.MODEL, param3).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                    )
            );
    }

    private static MultiVariantGenerator createSimpleBlock(Block param0, ResourceLocation param1) {
        return MultiVariantGenerator.multiVariant(param0, Variant.variant().with(VariantProperties.MODEL, param1));
    }

    private static PropertyDispatch createRotatedPillar() {
        return PropertyDispatch.property(BlockStateProperties.AXIS)
            .select(Direction.Axis.Y, Variant.variant())
            .select(Direction.Axis.Z, Variant.variant().with(VariantProperties.X_ROT, VariantProperties.Rotation.R90))
            .select(
                Direction.Axis.X,
                Variant.variant().with(VariantProperties.X_ROT, VariantProperties.Rotation.R90).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
            );
    }

    private static BlockStateGenerator createAxisAlignedPillarBlock(Block param0, ResourceLocation param1) {
        return MultiVariantGenerator.multiVariant(param0, Variant.variant().with(VariantProperties.MODEL, param1)).with(createRotatedPillar());
    }

    private void createAxisAlignedPillarBlockCustomModel(Block param0, ResourceLocation param1) {
        this.blockStateOutput.accept(createAxisAlignedPillarBlock(param0, param1));
    }

    private void createAxisAlignedPillarBlock(Block param0, TexturedModel.Provider param1) {
        ResourceLocation var0 = param1.create(param0, this.modelOutput);
        this.blockStateOutput.accept(createAxisAlignedPillarBlock(param0, var0));
    }

    private void createHorizontallyRotatedBlock(Block param0, TexturedModel.Provider param1) {
        ResourceLocation var0 = param1.create(param0, this.modelOutput);
        this.blockStateOutput
            .accept(MultiVariantGenerator.multiVariant(param0, Variant.variant().with(VariantProperties.MODEL, var0)).with(createHorizontalFacingDispatch()));
    }

    private static BlockStateGenerator createRotatedPillarWithHorizontalVariant(Block param0, ResourceLocation param1, ResourceLocation param2) {
        return MultiVariantGenerator.multiVariant(param0)
            .with(
                PropertyDispatch.property(BlockStateProperties.AXIS)
                    .select(Direction.Axis.Y, Variant.variant().with(VariantProperties.MODEL, param1))
                    .select(
                        Direction.Axis.Z, Variant.variant().with(VariantProperties.MODEL, param2).with(VariantProperties.X_ROT, VariantProperties.Rotation.R90)
                    )
                    .select(
                        Direction.Axis.X,
                        Variant.variant()
                            .with(VariantProperties.MODEL, param2)
                            .with(VariantProperties.X_ROT, VariantProperties.Rotation.R90)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                    )
            );
    }

    private void createRotatedPillarWithHorizontalVariant(Block param0, TexturedModel.Provider param1, TexturedModel.Provider param2) {
        ResourceLocation var0 = param1.create(param0, this.modelOutput);
        ResourceLocation var1 = param2.create(param0, this.modelOutput);
        this.blockStateOutput.accept(createRotatedPillarWithHorizontalVariant(param0, var0, var1));
    }

    private ResourceLocation createSuffixedVariant(Block param0, String param1, ModelTemplate param2, Function<ResourceLocation, TextureMapping> param3) {
        return param2.createWithSuffix(param0, param1, param3.apply(TextureMapping.getBlockTexture(param0, param1)), this.modelOutput);
    }

    private static BlockStateGenerator createPressurePlate(Block param0, ResourceLocation param1, ResourceLocation param2) {
        return MultiVariantGenerator.multiVariant(param0).with(createBooleanModelDispatch(BlockStateProperties.POWERED, param2, param1));
    }

    private static BlockStateGenerator createSlab(Block param0, ResourceLocation param1, ResourceLocation param2, ResourceLocation param3) {
        return MultiVariantGenerator.multiVariant(param0)
            .with(
                PropertyDispatch.property(BlockStateProperties.SLAB_TYPE)
                    .select(SlabType.BOTTOM, Variant.variant().with(VariantProperties.MODEL, param1))
                    .select(SlabType.TOP, Variant.variant().with(VariantProperties.MODEL, param2))
                    .select(SlabType.DOUBLE, Variant.variant().with(VariantProperties.MODEL, param3))
            );
    }

    private void createTrivialCube(Block param0) {
        this.createTrivialBlock(param0, TexturedModel.CUBE);
    }

    private void createTrivialBlock(Block param0, TexturedModel.Provider param1) {
        this.blockStateOutput.accept(createSimpleBlock(param0, param1.create(param0, this.modelOutput)));
    }

    private void createTrivialBlock(Block param0, TextureMapping param1, ModelTemplate param2) {
        ResourceLocation var0 = param2.create(param0, param1, this.modelOutput);
        this.blockStateOutput.accept(createSimpleBlock(param0, var0));
    }

    private BlockModelGenerators.BlockFamilyProvider family(Block param0, TexturedModel param1) {
        return new BlockModelGenerators.BlockFamilyProvider(param1.getMapping()).fullBlock(param0, param1.getTemplate());
    }

    private BlockModelGenerators.BlockFamilyProvider family(Block param0, TexturedModel.Provider param1) {
        TexturedModel var0 = param1.get(param0);
        return new BlockModelGenerators.BlockFamilyProvider(var0.getMapping()).fullBlock(param0, var0.getTemplate());
    }

    private BlockModelGenerators.BlockFamilyProvider family(Block param0) {
        return this.family(param0, TexturedModel.CUBE);
    }

    private BlockModelGenerators.BlockFamilyProvider family(TextureMapping param0) {
        return new BlockModelGenerators.BlockFamilyProvider(param0);
    }

    private void createDoor(Block param0) {
        TextureMapping var0 = TextureMapping.door(param0);
        ResourceLocation var1 = ModelTemplates.DOOR_BOTTOM.create(param0, var0, this.modelOutput);
        ResourceLocation var2 = ModelTemplates.DOOR_BOTTOM_HINGE.create(param0, var0, this.modelOutput);
        ResourceLocation var3 = ModelTemplates.DOOR_TOP.create(param0, var0, this.modelOutput);
        ResourceLocation var4 = ModelTemplates.DOOR_TOP_HINGE.create(param0, var0, this.modelOutput);
        this.createSimpleFlatItemModel(param0.asItem());
        this.blockStateOutput.accept(createDoor(param0, var1, var2, var3, var4));
    }

    private void createOrientableTrapdoor(Block param0) {
        TextureMapping var0 = TextureMapping.defaultTexture(param0);
        ResourceLocation var1 = ModelTemplates.ORIENTABLE_TRAPDOOR_TOP.create(param0, var0, this.modelOutput);
        ResourceLocation var2 = ModelTemplates.ORIENTABLE_TRAPDOOR_BOTTOM.create(param0, var0, this.modelOutput);
        ResourceLocation var3 = ModelTemplates.ORIENTABLE_TRAPDOOR_OPEN.create(param0, var0, this.modelOutput);
        this.blockStateOutput.accept(createOrientableTrapdoor(param0, var1, var2, var3));
        this.delegateItemModel(param0, var2);
    }

    private void createTrapdoor(Block param0) {
        TextureMapping var0 = TextureMapping.defaultTexture(param0);
        ResourceLocation var1 = ModelTemplates.TRAPDOOR_TOP.create(param0, var0, this.modelOutput);
        ResourceLocation var2 = ModelTemplates.TRAPDOOR_BOTTOM.create(param0, var0, this.modelOutput);
        ResourceLocation var3 = ModelTemplates.TRAPDOOR_OPEN.create(param0, var0, this.modelOutput);
        this.blockStateOutput.accept(createTrapdoor(param0, var1, var2, var3));
        this.delegateItemModel(param0, var2);
    }

    private BlockModelGenerators.WoodProvider woodProvider(Block param0) {
        return new BlockModelGenerators.WoodProvider(TextureMapping.logColumn(param0));
    }

    private void createNonTemplateModelBlock(Block param0) {
        this.createNonTemplateModelBlock(param0, param0);
    }

    private void createNonTemplateModelBlock(Block param0, Block param1) {
        this.blockStateOutput.accept(createSimpleBlock(param0, ModelLocationUtils.getModelLocation(param1)));
    }

    private void createCrossBlockWithDefaultItem(Block param0, BlockModelGenerators.TintState param1) {
        this.createSimpleFlatItemModel(param0);
        this.createCrossBlock(param0, param1);
    }

    private void createCrossBlockWithDefaultItem(Block param0, BlockModelGenerators.TintState param1, TextureMapping param2) {
        this.createSimpleFlatItemModel(param0);
        this.createCrossBlock(param0, param1, param2);
    }

    private void createCrossBlock(Block param0, BlockModelGenerators.TintState param1) {
        TextureMapping var0 = TextureMapping.cross(param0);
        this.createCrossBlock(param0, param1, var0);
    }

    private void createCrossBlock(Block param0, BlockModelGenerators.TintState param1, TextureMapping param2) {
        ResourceLocation var0 = param1.getCross().create(param0, param2, this.modelOutput);
        this.blockStateOutput.accept(createSimpleBlock(param0, var0));
    }

    private void createPlant(Block param0, Block param1, BlockModelGenerators.TintState param2) {
        this.createCrossBlockWithDefaultItem(param0, param2);
        TextureMapping var0 = TextureMapping.plant(param0);
        ResourceLocation var1 = param2.getCrossPot().create(param1, var0, this.modelOutput);
        this.blockStateOutput.accept(createSimpleBlock(param1, var1));
    }

    private void createCoralFans(Block param0, Block param1) {
        TexturedModel var0 = TexturedModel.CORAL_FAN.get(param0);
        ResourceLocation var1 = var0.create(param0, this.modelOutput);
        this.blockStateOutput.accept(createSimpleBlock(param0, var1));
        ResourceLocation var2 = ModelTemplates.CORAL_WALL_FAN.create(param1, var0.getMapping(), this.modelOutput);
        this.blockStateOutput
            .accept(MultiVariantGenerator.multiVariant(param1, Variant.variant().with(VariantProperties.MODEL, var2)).with(createHorizontalFacingDispatch()));
        this.createSimpleFlatItemModel(param0);
    }

    private void createStems(Block param0, Block param1) {
        this.createSimpleFlatItemModel(param0.asItem());
        TextureMapping var0 = TextureMapping.stem(param0);
        TextureMapping var1 = TextureMapping.attachedStem(param0, param1);
        ResourceLocation var2 = ModelTemplates.ATTACHED_STEM.create(param1, var1, this.modelOutput);
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(param1, Variant.variant().with(VariantProperties.MODEL, var2))
                    .with(
                        PropertyDispatch.property(BlockStateProperties.HORIZONTAL_FACING)
                            .select(Direction.WEST, Variant.variant())
                            .select(Direction.SOUTH, Variant.variant().with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270))
                            .select(Direction.NORTH, Variant.variant().with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90))
                            .select(Direction.EAST, Variant.variant().with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180))
                    )
            );
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(param0)
                    .with(
                        PropertyDispatch.property(BlockStateProperties.AGE_7)
                            .generate(
                                param2 -> Variant.variant().with(VariantProperties.MODEL, ModelTemplates.STEMS[param2].create(param0, var0, this.modelOutput))
                            )
                    )
            );
    }

    private void createCoral(Block param0, Block param1, Block param2, Block param3, Block param4, Block param5, Block param6, Block param7) {
        this.createCrossBlockWithDefaultItem(param0, BlockModelGenerators.TintState.NOT_TINTED);
        this.createCrossBlockWithDefaultItem(param1, BlockModelGenerators.TintState.NOT_TINTED);
        this.createTrivialCube(param2);
        this.createTrivialCube(param3);
        this.createCoralFans(param4, param6);
        this.createCoralFans(param5, param7);
    }

    private void createDoublePlant(Block param0, BlockModelGenerators.TintState param1) {
        this.createSimpleFlatItemModel(param0, "_top");
        ResourceLocation var0 = this.createSuffixedVariant(param0, "_top", param1.getCross(), TextureMapping::cross);
        ResourceLocation var1 = this.createSuffixedVariant(param0, "_bottom", param1.getCross(), TextureMapping::cross);
        this.createDoubleBlock(param0, var0, var1);
    }

    private void createSunflower() {
        this.createSimpleFlatItemModel(Blocks.SUNFLOWER, "_front");
        ResourceLocation var0 = ModelLocationUtils.getModelLocation(Blocks.SUNFLOWER, "_top");
        ResourceLocation var1 = this.createSuffixedVariant(
            Blocks.SUNFLOWER, "_bottom", BlockModelGenerators.TintState.NOT_TINTED.getCross(), TextureMapping::cross
        );
        this.createDoubleBlock(Blocks.SUNFLOWER, var0, var1);
    }

    private void createTallSeagrass() {
        ResourceLocation var0 = this.createSuffixedVariant(Blocks.TALL_SEAGRASS, "_top", ModelTemplates.SEAGRASS, TextureMapping::defaultTexture);
        ResourceLocation var1 = this.createSuffixedVariant(Blocks.TALL_SEAGRASS, "_bottom", ModelTemplates.SEAGRASS, TextureMapping::defaultTexture);
        this.createDoubleBlock(Blocks.TALL_SEAGRASS, var0, var1);
    }

    private void createDoubleBlock(Block param0, ResourceLocation param1, ResourceLocation param2) {
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(param0)
                    .with(
                        PropertyDispatch.property(BlockStateProperties.DOUBLE_BLOCK_HALF)
                            .select(DoubleBlockHalf.LOWER, Variant.variant().with(VariantProperties.MODEL, param2))
                            .select(DoubleBlockHalf.UPPER, Variant.variant().with(VariantProperties.MODEL, param1))
                    )
            );
    }

    private void createPassiveRail(Block param0) {
        TextureMapping var0 = TextureMapping.rail(param0);
        TextureMapping var1 = TextureMapping.rail(TextureMapping.getBlockTexture(param0, "_corner"));
        ResourceLocation var2 = ModelTemplates.RAIL_FLAT.create(param0, var0, this.modelOutput);
        ResourceLocation var3 = ModelTemplates.RAIL_CURVED.create(param0, var1, this.modelOutput);
        ResourceLocation var4 = ModelTemplates.RAIL_RAISED_NE.create(param0, var0, this.modelOutput);
        ResourceLocation var5 = ModelTemplates.RAIL_RAISED_SW.create(param0, var0, this.modelOutput);
        this.createSimpleFlatItemModel(param0);
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(param0)
                    .with(
                        PropertyDispatch.property(BlockStateProperties.RAIL_SHAPE)
                            .select(RailShape.NORTH_SOUTH, Variant.variant().with(VariantProperties.MODEL, var2))
                            .select(
                                RailShape.EAST_WEST,
                                Variant.variant().with(VariantProperties.MODEL, var2).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                            )
                            .select(
                                RailShape.ASCENDING_EAST,
                                Variant.variant().with(VariantProperties.MODEL, var4).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                            )
                            .select(
                                RailShape.ASCENDING_WEST,
                                Variant.variant().with(VariantProperties.MODEL, var5).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                            )
                            .select(RailShape.ASCENDING_NORTH, Variant.variant().with(VariantProperties.MODEL, var4))
                            .select(RailShape.ASCENDING_SOUTH, Variant.variant().with(VariantProperties.MODEL, var5))
                            .select(RailShape.SOUTH_EAST, Variant.variant().with(VariantProperties.MODEL, var3))
                            .select(
                                RailShape.SOUTH_WEST,
                                Variant.variant().with(VariantProperties.MODEL, var3).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                            )
                            .select(
                                RailShape.NORTH_WEST,
                                Variant.variant().with(VariantProperties.MODEL, var3).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                            )
                            .select(
                                RailShape.NORTH_EAST,
                                Variant.variant().with(VariantProperties.MODEL, var3).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                            )
                    )
            );
    }

    private void createActiveRail(Block param0) {
        ResourceLocation var0 = this.createSuffixedVariant(param0, "", ModelTemplates.RAIL_FLAT, TextureMapping::rail);
        ResourceLocation var1 = this.createSuffixedVariant(param0, "", ModelTemplates.RAIL_RAISED_NE, TextureMapping::rail);
        ResourceLocation var2 = this.createSuffixedVariant(param0, "", ModelTemplates.RAIL_RAISED_SW, TextureMapping::rail);
        ResourceLocation var3 = this.createSuffixedVariant(param0, "_on", ModelTemplates.RAIL_FLAT, TextureMapping::rail);
        ResourceLocation var4 = this.createSuffixedVariant(param0, "_on", ModelTemplates.RAIL_RAISED_NE, TextureMapping::rail);
        ResourceLocation var5 = this.createSuffixedVariant(param0, "_on", ModelTemplates.RAIL_RAISED_SW, TextureMapping::rail);
        PropertyDispatch var6 = PropertyDispatch.properties(BlockStateProperties.POWERED, BlockStateProperties.RAIL_SHAPE_STRAIGHT)
            .generate((param6, param7) -> {
                switch(param7) {
                    case NORTH_SOUTH:
                        return Variant.variant().with(VariantProperties.MODEL, param6 ? var3 : var0);
                    case EAST_WEST:
                        return Variant.variant()
                            .with(VariantProperties.MODEL, param6 ? var3 : var0)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90);
                    case ASCENDING_EAST:
                        return Variant.variant()
                            .with(VariantProperties.MODEL, param6 ? var4 : var1)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90);
                    case ASCENDING_WEST:
                        return Variant.variant()
                            .with(VariantProperties.MODEL, param6 ? var5 : var2)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90);
                    case ASCENDING_NORTH:
                        return Variant.variant().with(VariantProperties.MODEL, param6 ? var4 : var1);
                    case ASCENDING_SOUTH:
                        return Variant.variant().with(VariantProperties.MODEL, param6 ? var5 : var2);
                    default:
                        throw new UnsupportedOperationException("Fix you generator!");
                }
            });
        this.createSimpleFlatItemModel(param0);
        this.blockStateOutput.accept(MultiVariantGenerator.multiVariant(param0).with(var6));
    }

    private BlockModelGenerators.BlockEntityModelGenerator blockEntityModels(ResourceLocation param0, Block param1) {
        return new BlockModelGenerators.BlockEntityModelGenerator(param0, param1);
    }

    private BlockModelGenerators.BlockEntityModelGenerator blockEntityModels(Block param0, Block param1) {
        return new BlockModelGenerators.BlockEntityModelGenerator(ModelLocationUtils.getModelLocation(param0), param1);
    }

    private void createAirLikeBlock(Block param0, Item param1) {
        ResourceLocation var0 = ModelTemplates.PARTICLE_ONLY.create(param0, TextureMapping.particleFromItem(param1), this.modelOutput);
        this.blockStateOutput.accept(createSimpleBlock(param0, var0));
    }

    private void createAirLikeBlock(Block param0, ResourceLocation param1) {
        ResourceLocation var0 = ModelTemplates.PARTICLE_ONLY.create(param0, TextureMapping.particle(param1), this.modelOutput);
        this.blockStateOutput.accept(createSimpleBlock(param0, var0));
    }

    private void createWoolBlocks(Block param0, Block param1) {
        this.createTrivialBlock(param0, TexturedModel.CUBE);
        ResourceLocation var0 = TexturedModel.CARPET.get(param0).create(param1, this.modelOutput);
        this.blockStateOutput.accept(createSimpleBlock(param1, var0));
    }

    private void createColoredBlockWithRandomRotations(TexturedModel.Provider param0, Block... param1) {
        for(Block var0 : param1) {
            ResourceLocation var1 = param0.create(var0, this.modelOutput);
            this.blockStateOutput.accept(createRotatedVariant(var0, var1));
        }

    }

    private void createColoredBlockWithStateRotations(TexturedModel.Provider param0, Block... param1) {
        for(Block var0 : param1) {
            ResourceLocation var1 = param0.create(var0, this.modelOutput);
            this.blockStateOutput
                .accept(
                    MultiVariantGenerator.multiVariant(var0, Variant.variant().with(VariantProperties.MODEL, var1)).with(createHorizontalFacingDispatchAlt())
                );
        }

    }

    private void createGlassBlocks(Block param0, Block param1) {
        this.createTrivialCube(param0);
        TextureMapping var0 = TextureMapping.pane(param0, param1);
        ResourceLocation var1 = ModelTemplates.STAINED_GLASS_PANE_POST.create(param1, var0, this.modelOutput);
        ResourceLocation var2 = ModelTemplates.STAINED_GLASS_PANE_SIDE.create(param1, var0, this.modelOutput);
        ResourceLocation var3 = ModelTemplates.STAINED_GLASS_PANE_SIDE_ALT.create(param1, var0, this.modelOutput);
        ResourceLocation var4 = ModelTemplates.STAINED_GLASS_PANE_NOSIDE.create(param1, var0, this.modelOutput);
        ResourceLocation var5 = ModelTemplates.STAINED_GLASS_PANE_NOSIDE_ALT.create(param1, var0, this.modelOutput);
        Item var6 = param1.asItem();
        ModelTemplates.FLAT_ITEM.create(ModelLocationUtils.getModelLocation(var6), TextureMapping.layer0(param0), this.modelOutput);
        this.blockStateOutput
            .accept(
                MultiPartGenerator.multiPart(param1)
                    .with(Variant.variant().with(VariantProperties.MODEL, var1))
                    .with(Condition.condition().term(BlockStateProperties.NORTH, true), Variant.variant().with(VariantProperties.MODEL, var2))
                    .with(
                        Condition.condition().term(BlockStateProperties.EAST, true),
                        Variant.variant().with(VariantProperties.MODEL, var2).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                    )
                    .with(Condition.condition().term(BlockStateProperties.SOUTH, true), Variant.variant().with(VariantProperties.MODEL, var3))
                    .with(
                        Condition.condition().term(BlockStateProperties.WEST, true),
                        Variant.variant().with(VariantProperties.MODEL, var3).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                    )
                    .with(Condition.condition().term(BlockStateProperties.NORTH, false), Variant.variant().with(VariantProperties.MODEL, var4))
                    .with(Condition.condition().term(BlockStateProperties.EAST, false), Variant.variant().with(VariantProperties.MODEL, var5))
                    .with(
                        Condition.condition().term(BlockStateProperties.SOUTH, false),
                        Variant.variant().with(VariantProperties.MODEL, var5).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.WEST, false),
                        Variant.variant().with(VariantProperties.MODEL, var4).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                    )
            );
    }

    private void createCommandBlock(Block param0) {
        TextureMapping var0 = TextureMapping.commandBlock(param0);
        ResourceLocation var1 = ModelTemplates.COMMAND_BLOCK.create(param0, var0, this.modelOutput);
        ResourceLocation var2 = this.createSuffixedVariant(
            param0, "_conditional", ModelTemplates.COMMAND_BLOCK, param1 -> var0.copyAndUpdate(TextureSlot.SIDE, param1)
        );
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(param0)
                    .with(createBooleanModelDispatch(BlockStateProperties.CONDITIONAL, var2, var1))
                    .with(createFacingDispatch())
            );
    }

    private void createAnvil(Block param0) {
        ResourceLocation var0 = TexturedModel.ANVIL.create(param0, this.modelOutput);
        this.blockStateOutput.accept(createSimpleBlock(param0, var0).with(createHorizontalFacingDispatchAlt()));
    }

    private List<Variant> createBambooModels(int param0) {
        String var0 = "_age" + param0;
        return IntStream.range(1, 5)
            .mapToObj(param1 -> Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.BAMBOO, param1 + var0)))
            .collect(Collectors.toList());
    }

    private void createBamboo() {
        this.skipAutoItemBlock(Blocks.BAMBOO);
        this.blockStateOutput
            .accept(
                MultiPartGenerator.multiPart(Blocks.BAMBOO)
                    .with(Condition.condition().term(BlockStateProperties.AGE_1, 0), this.createBambooModels(0))
                    .with(Condition.condition().term(BlockStateProperties.AGE_1, 1), this.createBambooModels(1))
                    .with(
                        Condition.condition().term(BlockStateProperties.BAMBOO_LEAVES, BambooLeaves.SMALL),
                        Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.BAMBOO, "_small_leaves"))
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.BAMBOO_LEAVES, BambooLeaves.LARGE),
                        Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.BAMBOO, "_large_leaves"))
                    )
            );
    }

    private PropertyDispatch createColumnWithFacing() {
        return PropertyDispatch.property(BlockStateProperties.FACING)
            .select(Direction.DOWN, Variant.variant().with(VariantProperties.X_ROT, VariantProperties.Rotation.R180))
            .select(Direction.UP, Variant.variant())
            .select(Direction.NORTH, Variant.variant().with(VariantProperties.X_ROT, VariantProperties.Rotation.R90))
            .select(
                Direction.SOUTH,
                Variant.variant().with(VariantProperties.X_ROT, VariantProperties.Rotation.R90).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
            )
            .select(
                Direction.WEST,
                Variant.variant().with(VariantProperties.X_ROT, VariantProperties.Rotation.R90).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
            )
            .select(
                Direction.EAST,
                Variant.variant().with(VariantProperties.X_ROT, VariantProperties.Rotation.R90).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
            );
    }

    private void createBarrel() {
        ResourceLocation var0 = TextureMapping.getBlockTexture(Blocks.BARREL, "_top_open");
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(Blocks.BARREL)
                    .with(this.createColumnWithFacing())
                    .with(
                        PropertyDispatch.property(BlockStateProperties.OPEN)
                            .select(
                                false, Variant.variant().with(VariantProperties.MODEL, TexturedModel.CUBE_TOP_BOTTOM.create(Blocks.BARREL, this.modelOutput))
                            )
                            .select(
                                true,
                                Variant.variant()
                                    .with(
                                        VariantProperties.MODEL,
                                        TexturedModel.CUBE_TOP_BOTTOM
                                            .get(Blocks.BARREL)
                                            .updateTextures(param1 -> param1.put(TextureSlot.TOP, var0))
                                            .createWithSuffix(Blocks.BARREL, "_open", this.modelOutput)
                                    )
                            )
                    )
            );
    }

    private static <T extends Comparable<T>> PropertyDispatch createEmptyOrFullDispatch(
        Property<T> param0, T param1, ResourceLocation param2, ResourceLocation param3
    ) {
        Variant var0 = Variant.variant().with(VariantProperties.MODEL, param2);
        Variant var1 = Variant.variant().with(VariantProperties.MODEL, param3);
        return PropertyDispatch.property(param0).generate(param3x -> {
            boolean var0x = param3x.compareTo(param1) >= 0;
            return var0x ? var0 : var1;
        });
    }

    private void createBeeNest(Block param0, Function<Block, TextureMapping> param1) {
        TextureMapping var0 = param1.apply(param0).copyForced(TextureSlot.SIDE, TextureSlot.PARTICLE);
        TextureMapping var1 = var0.copyAndUpdate(TextureSlot.FRONT, TextureMapping.getBlockTexture(param0, "_front_honey"));
        ResourceLocation var2 = ModelTemplates.CUBE_ORIENTABLE_TOP_BOTTOM.create(param0, var0, this.modelOutput);
        ResourceLocation var3 = ModelTemplates.CUBE_ORIENTABLE_TOP_BOTTOM.createWithSuffix(param0, "_honey", var1, this.modelOutput);
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(param0)
                    .with(createHorizontalFacingDispatch())
                    .with(createEmptyOrFullDispatch(BlockStateProperties.LEVEL_HONEY, 5, var3, var2))
            );
    }

    private void createCropBlock(Block param0, Property<Integer> param1, int... param2) {
        if (param1.getPossibleValues().size() != param2.length) {
            throw new IllegalArgumentException();
        } else {
            Int2ObjectMap<ResourceLocation> var0 = new Int2ObjectOpenHashMap<>();
            PropertyDispatch var1 = PropertyDispatch.property(param1)
                .generate(
                    param3 -> {
                        int var0x = param2[param3];
                        ResourceLocation var1x = var0.computeIfAbsent(
                            var0x, param2x -> this.createSuffixedVariant(param0, "_stage" + var0x, ModelTemplates.CROP, TextureMapping::crop)
                        );
                        return Variant.variant().with(VariantProperties.MODEL, var1x);
                    }
                );
            this.createSimpleFlatItemModel(param0.asItem());
            this.blockStateOutput.accept(MultiVariantGenerator.multiVariant(param0).with(var1));
        }
    }

    private void createBell() {
        ResourceLocation var0 = ModelLocationUtils.getModelLocation(Blocks.BELL, "_floor");
        ResourceLocation var1 = ModelLocationUtils.getModelLocation(Blocks.BELL, "_ceiling");
        ResourceLocation var2 = ModelLocationUtils.getModelLocation(Blocks.BELL, "_wall");
        ResourceLocation var3 = ModelLocationUtils.getModelLocation(Blocks.BELL, "_between_walls");
        this.createSimpleFlatItemModel(Items.BELL);
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(Blocks.BELL)
                    .with(
                        PropertyDispatch.properties(BlockStateProperties.HORIZONTAL_FACING, BlockStateProperties.BELL_ATTACHMENT)
                            .select(Direction.NORTH, BellAttachType.FLOOR, Variant.variant().with(VariantProperties.MODEL, var0))
                            .select(
                                Direction.SOUTH,
                                BellAttachType.FLOOR,
                                Variant.variant().with(VariantProperties.MODEL, var0).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                            )
                            .select(
                                Direction.EAST,
                                BellAttachType.FLOOR,
                                Variant.variant().with(VariantProperties.MODEL, var0).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                            )
                            .select(
                                Direction.WEST,
                                BellAttachType.FLOOR,
                                Variant.variant().with(VariantProperties.MODEL, var0).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                            )
                            .select(Direction.NORTH, BellAttachType.CEILING, Variant.variant().with(VariantProperties.MODEL, var1))
                            .select(
                                Direction.SOUTH,
                                BellAttachType.CEILING,
                                Variant.variant().with(VariantProperties.MODEL, var1).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                            )
                            .select(
                                Direction.EAST,
                                BellAttachType.CEILING,
                                Variant.variant().with(VariantProperties.MODEL, var1).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                            )
                            .select(
                                Direction.WEST,
                                BellAttachType.CEILING,
                                Variant.variant().with(VariantProperties.MODEL, var1).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                            )
                            .select(
                                Direction.NORTH,
                                BellAttachType.SINGLE_WALL,
                                Variant.variant().with(VariantProperties.MODEL, var2).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                            )
                            .select(
                                Direction.SOUTH,
                                BellAttachType.SINGLE_WALL,
                                Variant.variant().with(VariantProperties.MODEL, var2).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                            )
                            .select(Direction.EAST, BellAttachType.SINGLE_WALL, Variant.variant().with(VariantProperties.MODEL, var2))
                            .select(
                                Direction.WEST,
                                BellAttachType.SINGLE_WALL,
                                Variant.variant().with(VariantProperties.MODEL, var2).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                            )
                            .select(
                                Direction.SOUTH,
                                BellAttachType.DOUBLE_WALL,
                                Variant.variant().with(VariantProperties.MODEL, var3).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                            )
                            .select(
                                Direction.NORTH,
                                BellAttachType.DOUBLE_WALL,
                                Variant.variant().with(VariantProperties.MODEL, var3).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                            )
                            .select(Direction.EAST, BellAttachType.DOUBLE_WALL, Variant.variant().with(VariantProperties.MODEL, var3))
                            .select(
                                Direction.WEST,
                                BellAttachType.DOUBLE_WALL,
                                Variant.variant().with(VariantProperties.MODEL, var3).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                            )
                    )
            );
    }

    private void createGrindstone() {
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(
                        Blocks.GRINDSTONE, Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.GRINDSTONE))
                    )
                    .with(
                        PropertyDispatch.properties(BlockStateProperties.ATTACH_FACE, BlockStateProperties.HORIZONTAL_FACING)
                            .select(AttachFace.FLOOR, Direction.NORTH, Variant.variant())
                            .select(AttachFace.FLOOR, Direction.EAST, Variant.variant().with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90))
                            .select(AttachFace.FLOOR, Direction.SOUTH, Variant.variant().with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180))
                            .select(AttachFace.FLOOR, Direction.WEST, Variant.variant().with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270))
                            .select(AttachFace.WALL, Direction.NORTH, Variant.variant().with(VariantProperties.X_ROT, VariantProperties.Rotation.R90))
                            .select(
                                AttachFace.WALL,
                                Direction.EAST,
                                Variant.variant()
                                    .with(VariantProperties.X_ROT, VariantProperties.Rotation.R90)
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                            )
                            .select(
                                AttachFace.WALL,
                                Direction.SOUTH,
                                Variant.variant()
                                    .with(VariantProperties.X_ROT, VariantProperties.Rotation.R90)
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                            )
                            .select(
                                AttachFace.WALL,
                                Direction.WEST,
                                Variant.variant()
                                    .with(VariantProperties.X_ROT, VariantProperties.Rotation.R90)
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                            )
                            .select(AttachFace.CEILING, Direction.SOUTH, Variant.variant().with(VariantProperties.X_ROT, VariantProperties.Rotation.R180))
                            .select(
                                AttachFace.CEILING,
                                Direction.WEST,
                                Variant.variant()
                                    .with(VariantProperties.X_ROT, VariantProperties.Rotation.R180)
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                            )
                            .select(
                                AttachFace.CEILING,
                                Direction.NORTH,
                                Variant.variant()
                                    .with(VariantProperties.X_ROT, VariantProperties.Rotation.R180)
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                            )
                            .select(
                                AttachFace.CEILING,
                                Direction.EAST,
                                Variant.variant()
                                    .with(VariantProperties.X_ROT, VariantProperties.Rotation.R180)
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                            )
                    )
            );
    }

    private void createFurnace(Block param0, TexturedModel.Provider param1) {
        ResourceLocation var0 = param1.create(param0, this.modelOutput);
        ResourceLocation var1 = TextureMapping.getBlockTexture(param0, "_front_on");
        ResourceLocation var2 = param1.get(param0)
            .updateTextures(param1x -> param1x.put(TextureSlot.FRONT, var1))
            .createWithSuffix(param0, "_on", this.modelOutput);
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(param0)
                    .with(createBooleanModelDispatch(BlockStateProperties.LIT, var2, var0))
                    .with(createHorizontalFacingDispatch())
            );
    }

    private void createCampfires(Block... param0) {
        ResourceLocation var0 = ModelLocationUtils.decorateBlockModelLocation("campfire_off");

        for(Block var1 : param0) {
            ResourceLocation var2 = ModelTemplates.CAMPFIRE.create(var1, TextureMapping.campfire(var1), this.modelOutput);
            this.createSimpleFlatItemModel(var1.asItem());
            this.blockStateOutput
                .accept(
                    MultiVariantGenerator.multiVariant(var1)
                        .with(createBooleanModelDispatch(BlockStateProperties.LIT, var2, var0))
                        .with(createHorizontalFacingDispatchAlt())
                );
        }

    }

    private void createBookshelf() {
        TextureMapping var0 = TextureMapping.column(TextureMapping.getBlockTexture(Blocks.BOOKSHELF), TextureMapping.getBlockTexture(Blocks.OAK_PLANKS));
        ResourceLocation var1 = ModelTemplates.CUBE_COLUMN.create(Blocks.BOOKSHELF, var0, this.modelOutput);
        this.blockStateOutput.accept(createSimpleBlock(Blocks.BOOKSHELF, var1));
    }

    private void createRedstoneWire() {
        this.createSimpleFlatItemModel(Items.REDSTONE);
        this.blockStateOutput
            .accept(
                MultiPartGenerator.multiPart(Blocks.REDSTONE_WIRE)
                    .with(
                        Condition.or(
                            Condition.condition()
                                .term(BlockStateProperties.NORTH_REDSTONE, RedstoneSide.NONE)
                                .term(BlockStateProperties.EAST_REDSTONE, RedstoneSide.NONE)
                                .term(BlockStateProperties.SOUTH_REDSTONE, RedstoneSide.NONE)
                                .term(BlockStateProperties.WEST_REDSTONE, RedstoneSide.NONE),
                            Condition.condition()
                                .term(BlockStateProperties.NORTH_REDSTONE, RedstoneSide.SIDE, RedstoneSide.UP)
                                .term(BlockStateProperties.EAST_REDSTONE, RedstoneSide.SIDE, RedstoneSide.UP),
                            Condition.condition()
                                .term(BlockStateProperties.EAST_REDSTONE, RedstoneSide.SIDE, RedstoneSide.UP)
                                .term(BlockStateProperties.SOUTH_REDSTONE, RedstoneSide.SIDE, RedstoneSide.UP),
                            Condition.condition()
                                .term(BlockStateProperties.SOUTH_REDSTONE, RedstoneSide.SIDE, RedstoneSide.UP)
                                .term(BlockStateProperties.WEST_REDSTONE, RedstoneSide.SIDE, RedstoneSide.UP),
                            Condition.condition()
                                .term(BlockStateProperties.WEST_REDSTONE, RedstoneSide.SIDE, RedstoneSide.UP)
                                .term(BlockStateProperties.NORTH_REDSTONE, RedstoneSide.SIDE, RedstoneSide.UP)
                        ),
                        Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.decorateBlockModelLocation("redstone_dust_dot"))
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.NORTH_REDSTONE, RedstoneSide.SIDE, RedstoneSide.UP),
                        Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.decorateBlockModelLocation("redstone_dust_side0"))
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.SOUTH_REDSTONE, RedstoneSide.SIDE, RedstoneSide.UP),
                        Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.decorateBlockModelLocation("redstone_dust_side_alt0"))
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.EAST_REDSTONE, RedstoneSide.SIDE, RedstoneSide.UP),
                        Variant.variant()
                            .with(VariantProperties.MODEL, ModelLocationUtils.decorateBlockModelLocation("redstone_dust_side_alt1"))
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.WEST_REDSTONE, RedstoneSide.SIDE, RedstoneSide.UP),
                        Variant.variant()
                            .with(VariantProperties.MODEL, ModelLocationUtils.decorateBlockModelLocation("redstone_dust_side1"))
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.NORTH_REDSTONE, RedstoneSide.UP),
                        Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.decorateBlockModelLocation("redstone_dust_up"))
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.EAST_REDSTONE, RedstoneSide.UP),
                        Variant.variant()
                            .with(VariantProperties.MODEL, ModelLocationUtils.decorateBlockModelLocation("redstone_dust_up"))
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.SOUTH_REDSTONE, RedstoneSide.UP),
                        Variant.variant()
                            .with(VariantProperties.MODEL, ModelLocationUtils.decorateBlockModelLocation("redstone_dust_up"))
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.WEST_REDSTONE, RedstoneSide.UP),
                        Variant.variant()
                            .with(VariantProperties.MODEL, ModelLocationUtils.decorateBlockModelLocation("redstone_dust_up"))
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                    )
            );
    }

    private void createComparator() {
        this.createSimpleFlatItemModel(Items.COMPARATOR);
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(Blocks.COMPARATOR)
                    .with(createHorizontalFacingDispatchAlt())
                    .with(
                        PropertyDispatch.properties(BlockStateProperties.MODE_COMPARATOR, BlockStateProperties.POWERED)
                            .select(
                                ComparatorMode.COMPARE,
                                false,
                                Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.COMPARATOR))
                            )
                            .select(
                                ComparatorMode.COMPARE,
                                true,
                                Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.COMPARATOR, "_on"))
                            )
                            .select(
                                ComparatorMode.SUBTRACT,
                                false,
                                Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.COMPARATOR, "_subtract"))
                            )
                            .select(
                                ComparatorMode.SUBTRACT,
                                true,
                                Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.COMPARATOR, "_on_subtract"))
                            )
                    )
            );
    }

    private void createSmoothStoneSlab() {
        TextureMapping var0 = TextureMapping.cube(Blocks.SMOOTH_STONE);
        TextureMapping var1 = TextureMapping.column(TextureMapping.getBlockTexture(Blocks.SMOOTH_STONE_SLAB, "_side"), var0.get(TextureSlot.TOP));
        ResourceLocation var2 = ModelTemplates.SLAB_BOTTOM.create(Blocks.SMOOTH_STONE_SLAB, var1, this.modelOutput);
        ResourceLocation var3 = ModelTemplates.SLAB_TOP.create(Blocks.SMOOTH_STONE_SLAB, var1, this.modelOutput);
        ResourceLocation var4 = ModelTemplates.CUBE_COLUMN.createWithOverride(Blocks.SMOOTH_STONE_SLAB, "_double", var1, this.modelOutput);
        this.blockStateOutput.accept(createSlab(Blocks.SMOOTH_STONE_SLAB, var2, var3, var4));
        this.blockStateOutput.accept(createSimpleBlock(Blocks.SMOOTH_STONE, ModelTemplates.CUBE_ALL.create(Blocks.SMOOTH_STONE, var0, this.modelOutput)));
    }

    private void createBrewingStand() {
        this.createSimpleFlatItemModel(Items.BREWING_STAND);
        this.blockStateOutput
            .accept(
                MultiPartGenerator.multiPart(Blocks.BREWING_STAND)
                    .with(Variant.variant().with(VariantProperties.MODEL, TextureMapping.getBlockTexture(Blocks.BREWING_STAND)))
                    .with(
                        Condition.condition().term(BlockStateProperties.HAS_BOTTLE_0, true),
                        Variant.variant().with(VariantProperties.MODEL, TextureMapping.getBlockTexture(Blocks.BREWING_STAND, "_bottle0"))
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.HAS_BOTTLE_1, true),
                        Variant.variant().with(VariantProperties.MODEL, TextureMapping.getBlockTexture(Blocks.BREWING_STAND, "_bottle1"))
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.HAS_BOTTLE_2, true),
                        Variant.variant().with(VariantProperties.MODEL, TextureMapping.getBlockTexture(Blocks.BREWING_STAND, "_bottle2"))
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.HAS_BOTTLE_0, false),
                        Variant.variant().with(VariantProperties.MODEL, TextureMapping.getBlockTexture(Blocks.BREWING_STAND, "_empty0"))
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.HAS_BOTTLE_1, false),
                        Variant.variant().with(VariantProperties.MODEL, TextureMapping.getBlockTexture(Blocks.BREWING_STAND, "_empty1"))
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.HAS_BOTTLE_2, false),
                        Variant.variant().with(VariantProperties.MODEL, TextureMapping.getBlockTexture(Blocks.BREWING_STAND, "_empty2"))
                    )
            );
    }

    private void createMushroomBlock(Block param0) {
        ResourceLocation var0 = ModelTemplates.SINGLE_FACE.create(param0, TextureMapping.defaultTexture(param0), this.modelOutput);
        ResourceLocation var1 = ModelLocationUtils.decorateBlockModelLocation("mushroom_block_inside");
        this.blockStateOutput
            .accept(
                MultiPartGenerator.multiPart(param0)
                    .with(Condition.condition().term(BlockStateProperties.NORTH, true), Variant.variant().with(VariantProperties.MODEL, var0))
                    .with(
                        Condition.condition().term(BlockStateProperties.EAST, true),
                        Variant.variant()
                            .with(VariantProperties.MODEL, var0)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.SOUTH, true),
                        Variant.variant()
                            .with(VariantProperties.MODEL, var0)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.WEST, true),
                        Variant.variant()
                            .with(VariantProperties.MODEL, var0)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.UP, true),
                        Variant.variant()
                            .with(VariantProperties.MODEL, var0)
                            .with(VariantProperties.X_ROT, VariantProperties.Rotation.R270)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.DOWN, true),
                        Variant.variant()
                            .with(VariantProperties.MODEL, var0)
                            .with(VariantProperties.X_ROT, VariantProperties.Rotation.R90)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .with(Condition.condition().term(BlockStateProperties.NORTH, false), Variant.variant().with(VariantProperties.MODEL, var1))
                    .with(
                        Condition.condition().term(BlockStateProperties.EAST, false),
                        Variant.variant()
                            .with(VariantProperties.MODEL, var1)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                            .with(VariantProperties.UV_LOCK, false)
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.SOUTH, false),
                        Variant.variant()
                            .with(VariantProperties.MODEL, var1)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                            .with(VariantProperties.UV_LOCK, false)
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.WEST, false),
                        Variant.variant()
                            .with(VariantProperties.MODEL, var1)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                            .with(VariantProperties.UV_LOCK, false)
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.UP, false),
                        Variant.variant()
                            .with(VariantProperties.MODEL, var1)
                            .with(VariantProperties.X_ROT, VariantProperties.Rotation.R270)
                            .with(VariantProperties.UV_LOCK, false)
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.DOWN, false),
                        Variant.variant()
                            .with(VariantProperties.MODEL, var1)
                            .with(VariantProperties.X_ROT, VariantProperties.Rotation.R90)
                            .with(VariantProperties.UV_LOCK, false)
                    )
            );
        this.delegateItemModel(param0, TexturedModel.CUBE.createWithSuffix(param0, "_inventory", this.modelOutput));
    }

    private void createCakeBlock() {
        this.createSimpleFlatItemModel(Items.CAKE);
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(Blocks.CAKE)
                    .with(
                        PropertyDispatch.property(BlockStateProperties.BITES)
                            .select(0, Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.CAKE)))
                            .select(1, Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.CAKE, "_slice1")))
                            .select(2, Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.CAKE, "_slice2")))
                            .select(3, Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.CAKE, "_slice3")))
                            .select(4, Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.CAKE, "_slice4")))
                            .select(5, Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.CAKE, "_slice5")))
                            .select(6, Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.CAKE, "_slice6")))
                    )
            );
    }

    private void createCartographyTable() {
        TextureMapping var0 = new TextureMapping()
            .put(TextureSlot.PARTICLE, TextureMapping.getBlockTexture(Blocks.CARTOGRAPHY_TABLE, "_side3"))
            .put(TextureSlot.DOWN, TextureMapping.getBlockTexture(Blocks.DARK_OAK_PLANKS))
            .put(TextureSlot.UP, TextureMapping.getBlockTexture(Blocks.CARTOGRAPHY_TABLE, "_top"))
            .put(TextureSlot.NORTH, TextureMapping.getBlockTexture(Blocks.CARTOGRAPHY_TABLE, "_side3"))
            .put(TextureSlot.EAST, TextureMapping.getBlockTexture(Blocks.CARTOGRAPHY_TABLE, "_side3"))
            .put(TextureSlot.SOUTH, TextureMapping.getBlockTexture(Blocks.CARTOGRAPHY_TABLE, "_side1"))
            .put(TextureSlot.WEST, TextureMapping.getBlockTexture(Blocks.CARTOGRAPHY_TABLE, "_side2"));
        this.blockStateOutput.accept(createSimpleBlock(Blocks.CARTOGRAPHY_TABLE, ModelTemplates.CUBE.create(Blocks.CARTOGRAPHY_TABLE, var0, this.modelOutput)));
    }

    private void createSmithingTable() {
        TextureMapping var0 = new TextureMapping()
            .put(TextureSlot.PARTICLE, TextureMapping.getBlockTexture(Blocks.SMITHING_TABLE, "_front"))
            .put(TextureSlot.DOWN, TextureMapping.getBlockTexture(Blocks.SMITHING_TABLE, "_bottom"))
            .put(TextureSlot.UP, TextureMapping.getBlockTexture(Blocks.SMITHING_TABLE, "_top"))
            .put(TextureSlot.NORTH, TextureMapping.getBlockTexture(Blocks.SMITHING_TABLE, "_front"))
            .put(TextureSlot.SOUTH, TextureMapping.getBlockTexture(Blocks.SMITHING_TABLE, "_front"))
            .put(TextureSlot.EAST, TextureMapping.getBlockTexture(Blocks.SMITHING_TABLE, "_side"))
            .put(TextureSlot.WEST, TextureMapping.getBlockTexture(Blocks.SMITHING_TABLE, "_side"));
        this.blockStateOutput.accept(createSimpleBlock(Blocks.SMITHING_TABLE, ModelTemplates.CUBE.create(Blocks.SMITHING_TABLE, var0, this.modelOutput)));
    }

    private void createCraftingTableLike(Block param0, Block param1, BiFunction<Block, Block, TextureMapping> param2) {
        TextureMapping var0 = param2.apply(param0, param1);
        this.blockStateOutput.accept(createSimpleBlock(param0, ModelTemplates.CUBE.create(param0, var0, this.modelOutput)));
    }

    private void createPumpkins() {
        TextureMapping var0 = TextureMapping.column(Blocks.PUMPKIN);
        this.blockStateOutput.accept(createSimpleBlock(Blocks.PUMPKIN, ModelLocationUtils.getModelLocation(Blocks.PUMPKIN)));
        this.createPumpkinVariant(Blocks.CARVED_PUMPKIN, var0);
        this.createPumpkinVariant(Blocks.JACK_O_LANTERN, var0);
    }

    private void createPumpkinVariant(Block param0, TextureMapping param1) {
        ResourceLocation var0 = ModelTemplates.CUBE_ORIENTABLE
            .create(param0, param1.copyAndUpdate(TextureSlot.FRONT, TextureMapping.getBlockTexture(param0)), this.modelOutput);
        this.blockStateOutput
            .accept(MultiVariantGenerator.multiVariant(param0, Variant.variant().with(VariantProperties.MODEL, var0)).with(createHorizontalFacingDispatch()));
    }

    private void createCauldron() {
        this.createSimpleFlatItemModel(Items.CAULDRON);
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(Blocks.CAULDRON)
                    .with(
                        PropertyDispatch.property(BlockStateProperties.LEVEL_CAULDRON)
                            .select(0, Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.CAULDRON)))
                            .select(1, Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.CAULDRON, "_level1")))
                            .select(2, Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.CAULDRON, "_level2")))
                            .select(3, Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.CAULDRON, "_level3")))
                    )
            );
    }

    private void createChiseledSandsone(Block param0, Block param1) {
        TextureMapping var0 = new TextureMapping()
            .put(TextureSlot.END, TextureMapping.getBlockTexture(param1, "_top"))
            .put(TextureSlot.SIDE, TextureMapping.getBlockTexture(param0));
        this.createTrivialBlock(param0, var0, ModelTemplates.CUBE_COLUMN);
    }

    private void createChorusFlower() {
        TextureMapping var0 = TextureMapping.defaultTexture(Blocks.CHORUS_FLOWER);
        ResourceLocation var1 = ModelTemplates.CHORUS_FLOWER.create(Blocks.CHORUS_FLOWER, var0, this.modelOutput);
        ResourceLocation var2 = this.createSuffixedVariant(
            Blocks.CHORUS_FLOWER, "_dead", ModelTemplates.CHORUS_FLOWER, param1 -> var0.copyAndUpdate(TextureSlot.TEXTURE, param1)
        );
        this.blockStateOutput
            .accept(MultiVariantGenerator.multiVariant(Blocks.CHORUS_FLOWER).with(createEmptyOrFullDispatch(BlockStateProperties.AGE_5, 5, var2, var1)));
    }

    private void createDispenserBlock(Block param0) {
        TextureMapping var0 = new TextureMapping()
            .put(TextureSlot.TOP, TextureMapping.getBlockTexture(Blocks.FURNACE, "_top"))
            .put(TextureSlot.SIDE, TextureMapping.getBlockTexture(Blocks.FURNACE, "_side"))
            .put(TextureSlot.FRONT, TextureMapping.getBlockTexture(param0, "_front"));
        TextureMapping var1 = new TextureMapping()
            .put(TextureSlot.SIDE, TextureMapping.getBlockTexture(Blocks.FURNACE, "_top"))
            .put(TextureSlot.FRONT, TextureMapping.getBlockTexture(param0, "_front_vertical"));
        ResourceLocation var2 = ModelTemplates.CUBE_ORIENTABLE.create(param0, var0, this.modelOutput);
        ResourceLocation var3 = ModelTemplates.CUBE_ORIENTABLE_VERTICAL.create(param0, var1, this.modelOutput);
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(param0)
                    .with(
                        PropertyDispatch.property(BlockStateProperties.FACING)
                            .select(
                                Direction.DOWN,
                                Variant.variant().with(VariantProperties.MODEL, var3).with(VariantProperties.X_ROT, VariantProperties.Rotation.R180)
                            )
                            .select(Direction.UP, Variant.variant().with(VariantProperties.MODEL, var3))
                            .select(Direction.NORTH, Variant.variant().with(VariantProperties.MODEL, var2))
                            .select(
                                Direction.EAST,
                                Variant.variant().with(VariantProperties.MODEL, var2).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                            )
                            .select(
                                Direction.SOUTH,
                                Variant.variant().with(VariantProperties.MODEL, var2).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                            )
                            .select(
                                Direction.WEST,
                                Variant.variant().with(VariantProperties.MODEL, var2).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                            )
                    )
            );
    }

    private void createEndPortalFrame() {
        ResourceLocation var0 = ModelLocationUtils.getModelLocation(Blocks.END_PORTAL_FRAME);
        ResourceLocation var1 = ModelLocationUtils.getModelLocation(Blocks.END_PORTAL_FRAME, "_filled");
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(Blocks.END_PORTAL_FRAME)
                    .with(
                        PropertyDispatch.property(BlockStateProperties.EYE)
                            .select(false, Variant.variant().with(VariantProperties.MODEL, var0))
                            .select(true, Variant.variant().with(VariantProperties.MODEL, var1))
                    )
                    .with(createHorizontalFacingDispatchAlt())
            );
    }

    private void createChorusPlant() {
        ResourceLocation var0 = ModelLocationUtils.getModelLocation(Blocks.CHORUS_PLANT, "_side");
        ResourceLocation var1 = ModelLocationUtils.getModelLocation(Blocks.CHORUS_PLANT, "_noside");
        ResourceLocation var2 = ModelLocationUtils.getModelLocation(Blocks.CHORUS_PLANT, "_noside1");
        ResourceLocation var3 = ModelLocationUtils.getModelLocation(Blocks.CHORUS_PLANT, "_noside2");
        ResourceLocation var4 = ModelLocationUtils.getModelLocation(Blocks.CHORUS_PLANT, "_noside3");
        this.blockStateOutput
            .accept(
                MultiPartGenerator.multiPart(Blocks.CHORUS_PLANT)
                    .with(Condition.condition().term(BlockStateProperties.NORTH, true), Variant.variant().with(VariantProperties.MODEL, var0))
                    .with(
                        Condition.condition().term(BlockStateProperties.EAST, true),
                        Variant.variant()
                            .with(VariantProperties.MODEL, var0)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.SOUTH, true),
                        Variant.variant()
                            .with(VariantProperties.MODEL, var0)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.WEST, true),
                        Variant.variant()
                            .with(VariantProperties.MODEL, var0)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.UP, true),
                        Variant.variant()
                            .with(VariantProperties.MODEL, var0)
                            .with(VariantProperties.X_ROT, VariantProperties.Rotation.R270)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.DOWN, true),
                        Variant.variant()
                            .with(VariantProperties.MODEL, var0)
                            .with(VariantProperties.X_ROT, VariantProperties.Rotation.R90)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.NORTH, false),
                        Variant.variant().with(VariantProperties.MODEL, var1).with(VariantProperties.WEIGHT, 2),
                        Variant.variant().with(VariantProperties.MODEL, var2),
                        Variant.variant().with(VariantProperties.MODEL, var3),
                        Variant.variant().with(VariantProperties.MODEL, var4)
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.EAST, false),
                        Variant.variant()
                            .with(VariantProperties.MODEL, var2)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                            .with(VariantProperties.UV_LOCK, true),
                        Variant.variant()
                            .with(VariantProperties.MODEL, var3)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                            .with(VariantProperties.UV_LOCK, true),
                        Variant.variant()
                            .with(VariantProperties.MODEL, var4)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                            .with(VariantProperties.UV_LOCK, true),
                        Variant.variant()
                            .with(VariantProperties.MODEL, var1)
                            .with(VariantProperties.WEIGHT, 2)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.SOUTH, false),
                        Variant.variant()
                            .with(VariantProperties.MODEL, var3)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                            .with(VariantProperties.UV_LOCK, true),
                        Variant.variant()
                            .with(VariantProperties.MODEL, var4)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                            .with(VariantProperties.UV_LOCK, true),
                        Variant.variant()
                            .with(VariantProperties.MODEL, var1)
                            .with(VariantProperties.WEIGHT, 2)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                            .with(VariantProperties.UV_LOCK, true),
                        Variant.variant()
                            .with(VariantProperties.MODEL, var2)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.WEST, false),
                        Variant.variant()
                            .with(VariantProperties.MODEL, var4)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                            .with(VariantProperties.UV_LOCK, true),
                        Variant.variant()
                            .with(VariantProperties.MODEL, var1)
                            .with(VariantProperties.WEIGHT, 2)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                            .with(VariantProperties.UV_LOCK, true),
                        Variant.variant()
                            .with(VariantProperties.MODEL, var2)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                            .with(VariantProperties.UV_LOCK, true),
                        Variant.variant()
                            .with(VariantProperties.MODEL, var3)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.UP, false),
                        Variant.variant()
                            .with(VariantProperties.MODEL, var1)
                            .with(VariantProperties.WEIGHT, 2)
                            .with(VariantProperties.X_ROT, VariantProperties.Rotation.R270)
                            .with(VariantProperties.UV_LOCK, true),
                        Variant.variant()
                            .with(VariantProperties.MODEL, var4)
                            .with(VariantProperties.X_ROT, VariantProperties.Rotation.R270)
                            .with(VariantProperties.UV_LOCK, true),
                        Variant.variant()
                            .with(VariantProperties.MODEL, var2)
                            .with(VariantProperties.X_ROT, VariantProperties.Rotation.R270)
                            .with(VariantProperties.UV_LOCK, true),
                        Variant.variant()
                            .with(VariantProperties.MODEL, var3)
                            .with(VariantProperties.X_ROT, VariantProperties.Rotation.R270)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.DOWN, false),
                        Variant.variant()
                            .with(VariantProperties.MODEL, var4)
                            .with(VariantProperties.X_ROT, VariantProperties.Rotation.R90)
                            .with(VariantProperties.UV_LOCK, true),
                        Variant.variant()
                            .with(VariantProperties.MODEL, var3)
                            .with(VariantProperties.X_ROT, VariantProperties.Rotation.R90)
                            .with(VariantProperties.UV_LOCK, true),
                        Variant.variant()
                            .with(VariantProperties.MODEL, var2)
                            .with(VariantProperties.X_ROT, VariantProperties.Rotation.R90)
                            .with(VariantProperties.UV_LOCK, true),
                        Variant.variant()
                            .with(VariantProperties.MODEL, var1)
                            .with(VariantProperties.WEIGHT, 2)
                            .with(VariantProperties.X_ROT, VariantProperties.Rotation.R90)
                            .with(VariantProperties.UV_LOCK, true)
                    )
            );
    }

    private void createComposter() {
        this.blockStateOutput
            .accept(
                MultiPartGenerator.multiPart(Blocks.COMPOSTER)
                    .with(Variant.variant().with(VariantProperties.MODEL, TextureMapping.getBlockTexture(Blocks.COMPOSTER)))
                    .with(
                        Condition.condition().term(BlockStateProperties.LEVEL_COMPOSTER, 1),
                        Variant.variant().with(VariantProperties.MODEL, TextureMapping.getBlockTexture(Blocks.COMPOSTER, "_contents1"))
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.LEVEL_COMPOSTER, 2),
                        Variant.variant().with(VariantProperties.MODEL, TextureMapping.getBlockTexture(Blocks.COMPOSTER, "_contents2"))
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.LEVEL_COMPOSTER, 3),
                        Variant.variant().with(VariantProperties.MODEL, TextureMapping.getBlockTexture(Blocks.COMPOSTER, "_contents3"))
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.LEVEL_COMPOSTER, 4),
                        Variant.variant().with(VariantProperties.MODEL, TextureMapping.getBlockTexture(Blocks.COMPOSTER, "_contents4"))
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.LEVEL_COMPOSTER, 5),
                        Variant.variant().with(VariantProperties.MODEL, TextureMapping.getBlockTexture(Blocks.COMPOSTER, "_contents5"))
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.LEVEL_COMPOSTER, 6),
                        Variant.variant().with(VariantProperties.MODEL, TextureMapping.getBlockTexture(Blocks.COMPOSTER, "_contents6"))
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.LEVEL_COMPOSTER, 7),
                        Variant.variant().with(VariantProperties.MODEL, TextureMapping.getBlockTexture(Blocks.COMPOSTER, "_contents7"))
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.LEVEL_COMPOSTER, 8),
                        Variant.variant().with(VariantProperties.MODEL, TextureMapping.getBlockTexture(Blocks.COMPOSTER, "_contents_ready"))
                    )
            );
    }

    private void createNyliumBlock(Block param0) {
        TextureMapping var0 = new TextureMapping()
            .put(TextureSlot.BOTTOM, TextureMapping.getBlockTexture(Blocks.NETHERRACK))
            .put(TextureSlot.TOP, TextureMapping.getBlockTexture(param0))
            .put(TextureSlot.SIDE, TextureMapping.getBlockTexture(param0, "_side"));
        this.blockStateOutput.accept(createSimpleBlock(param0, ModelTemplates.CUBE_BOTTOM_TOP.create(param0, var0, this.modelOutput)));
    }

    private void createDaylightDetector() {
        ResourceLocation var0 = TextureMapping.getBlockTexture(Blocks.DAYLIGHT_DETECTOR, "_side");
        TextureMapping var1 = new TextureMapping()
            .put(TextureSlot.TOP, TextureMapping.getBlockTexture(Blocks.DAYLIGHT_DETECTOR, "_top"))
            .put(TextureSlot.SIDE, var0);
        TextureMapping var2 = new TextureMapping()
            .put(TextureSlot.TOP, TextureMapping.getBlockTexture(Blocks.DAYLIGHT_DETECTOR, "_inverted_top"))
            .put(TextureSlot.SIDE, var0);
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(Blocks.DAYLIGHT_DETECTOR)
                    .with(
                        PropertyDispatch.property(BlockStateProperties.INVERTED)
                            .select(
                                false,
                                Variant.variant()
                                    .with(VariantProperties.MODEL, ModelTemplates.DAYLIGHT_DETECTOR.create(Blocks.DAYLIGHT_DETECTOR, var1, this.modelOutput))
                            )
                            .select(
                                true,
                                Variant.variant()
                                    .with(
                                        VariantProperties.MODEL,
                                        ModelTemplates.DAYLIGHT_DETECTOR
                                            .create(ModelLocationUtils.getModelLocation(Blocks.DAYLIGHT_DETECTOR, "_inverted"), var2, this.modelOutput)
                                    )
                            )
                    )
            );
    }

    private void createRotatableColumn(Block param0) {
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(param0, Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(param0)))
                    .with(this.createColumnWithFacing())
            );
    }

    private void createFarmland() {
        TextureMapping var0 = new TextureMapping()
            .put(TextureSlot.DIRT, TextureMapping.getBlockTexture(Blocks.DIRT))
            .put(TextureSlot.TOP, TextureMapping.getBlockTexture(Blocks.FARMLAND));
        TextureMapping var1 = new TextureMapping()
            .put(TextureSlot.DIRT, TextureMapping.getBlockTexture(Blocks.DIRT))
            .put(TextureSlot.TOP, TextureMapping.getBlockTexture(Blocks.FARMLAND, "_moist"));
        ResourceLocation var2 = ModelTemplates.FARMLAND.create(Blocks.FARMLAND, var0, this.modelOutput);
        ResourceLocation var3 = ModelTemplates.FARMLAND.create(TextureMapping.getBlockTexture(Blocks.FARMLAND, "_moist"), var1, this.modelOutput);
        this.blockStateOutput
            .accept(MultiVariantGenerator.multiVariant(Blocks.FARMLAND).with(createEmptyOrFullDispatch(BlockStateProperties.MOISTURE, 7, var3, var2)));
    }

    private List<ResourceLocation> createFloorFireModels(Block param0) {
        ResourceLocation var0 = ModelTemplates.FIRE_FLOOR
            .create(ModelLocationUtils.getModelLocation(param0, "_floor0"), TextureMapping.fire0(param0), this.modelOutput);
        ResourceLocation var1 = ModelTemplates.FIRE_FLOOR
            .create(ModelLocationUtils.getModelLocation(param0, "_floor1"), TextureMapping.fire1(param0), this.modelOutput);
        return ImmutableList.of(var0, var1);
    }

    private List<ResourceLocation> createSideFireModels(Block param0) {
        ResourceLocation var0 = ModelTemplates.FIRE_SIDE
            .create(ModelLocationUtils.getModelLocation(param0, "_side0"), TextureMapping.fire0(param0), this.modelOutput);
        ResourceLocation var1 = ModelTemplates.FIRE_SIDE
            .create(ModelLocationUtils.getModelLocation(param0, "_side1"), TextureMapping.fire1(param0), this.modelOutput);
        ResourceLocation var2 = ModelTemplates.FIRE_SIDE_ALT
            .create(ModelLocationUtils.getModelLocation(param0, "_side_alt0"), TextureMapping.fire0(param0), this.modelOutput);
        ResourceLocation var3 = ModelTemplates.FIRE_SIDE_ALT
            .create(ModelLocationUtils.getModelLocation(param0, "_side_alt1"), TextureMapping.fire1(param0), this.modelOutput);
        return ImmutableList.of(var0, var1, var2, var3);
    }

    private List<ResourceLocation> createTopFireModels(Block param0) {
        ResourceLocation var0 = ModelTemplates.FIRE_UP
            .create(ModelLocationUtils.getModelLocation(param0, "_up0"), TextureMapping.fire0(param0), this.modelOutput);
        ResourceLocation var1 = ModelTemplates.FIRE_UP
            .create(ModelLocationUtils.getModelLocation(param0, "_up1"), TextureMapping.fire1(param0), this.modelOutput);
        ResourceLocation var2 = ModelTemplates.FIRE_UP_ALT
            .create(ModelLocationUtils.getModelLocation(param0, "_up_alt0"), TextureMapping.fire0(param0), this.modelOutput);
        ResourceLocation var3 = ModelTemplates.FIRE_UP_ALT
            .create(ModelLocationUtils.getModelLocation(param0, "_up_alt1"), TextureMapping.fire1(param0), this.modelOutput);
        return ImmutableList.of(var0, var1, var2, var3);
    }

    private static List<Variant> wrapModels(List<ResourceLocation> param0, UnaryOperator<Variant> param1) {
        return param0.stream().map(param0x -> Variant.variant().with(VariantProperties.MODEL, param0x)).map(param1).collect(Collectors.toList());
    }

    private void createFire() {
        Condition var0 = Condition.condition()
            .term(BlockStateProperties.NORTH, false)
            .term(BlockStateProperties.EAST, false)
            .term(BlockStateProperties.SOUTH, false)
            .term(BlockStateProperties.WEST, false)
            .term(BlockStateProperties.UP, false);
        List<ResourceLocation> var1 = this.createFloorFireModels(Blocks.FIRE);
        List<ResourceLocation> var2 = this.createSideFireModels(Blocks.FIRE);
        List<ResourceLocation> var3 = this.createTopFireModels(Blocks.FIRE);
        this.blockStateOutput
            .accept(
                MultiPartGenerator.multiPart(Blocks.FIRE)
                    .with(var0, wrapModels(var1, param0 -> param0))
                    .with(Condition.or(Condition.condition().term(BlockStateProperties.NORTH, true), var0), wrapModels(var2, param0 -> param0))
                    .with(
                        Condition.or(Condition.condition().term(BlockStateProperties.EAST, true), var0),
                        wrapModels(var2, param0 -> param0.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90))
                    )
                    .with(
                        Condition.or(Condition.condition().term(BlockStateProperties.SOUTH, true), var0),
                        wrapModels(var2, param0 -> param0.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180))
                    )
                    .with(
                        Condition.or(Condition.condition().term(BlockStateProperties.WEST, true), var0),
                        wrapModels(var2, param0 -> param0.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270))
                    )
                    .with(Condition.condition().term(BlockStateProperties.UP, true), wrapModels(var3, param0 -> param0))
            );
    }

    private void createSoulFire() {
        List<ResourceLocation> var0 = this.createFloorFireModels(Blocks.SOUL_FIRE);
        List<ResourceLocation> var1 = this.createSideFireModels(Blocks.SOUL_FIRE);
        this.blockStateOutput
            .accept(
                MultiPartGenerator.multiPart(Blocks.SOUL_FIRE)
                    .with(wrapModels(var0, param0 -> param0))
                    .with(wrapModels(var1, param0 -> param0))
                    .with(wrapModels(var1, param0 -> param0.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)))
                    .with(wrapModels(var1, param0 -> param0.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)))
                    .with(wrapModels(var1, param0 -> param0.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)))
            );
    }

    private void createLantern(Block param0) {
        ResourceLocation var0 = TexturedModel.LANTERN.create(param0, this.modelOutput);
        ResourceLocation var1 = TexturedModel.HANGING_LANTERN.create(param0, this.modelOutput);
        this.createSimpleFlatItemModel(param0.asItem());
        this.blockStateOutput.accept(MultiVariantGenerator.multiVariant(param0).with(createBooleanModelDispatch(BlockStateProperties.HANGING, var1, var0)));
    }

    private void createFrostedIce() {
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(Blocks.FROSTED_ICE)
                    .with(
                        PropertyDispatch.property(BlockStateProperties.AGE_3)
                            .select(
                                0,
                                Variant.variant()
                                    .with(
                                        VariantProperties.MODEL,
                                        this.createSuffixedVariant(Blocks.FROSTED_ICE, "_0", ModelTemplates.CUBE_ALL, TextureMapping::cube)
                                    )
                            )
                            .select(
                                1,
                                Variant.variant()
                                    .with(
                                        VariantProperties.MODEL,
                                        this.createSuffixedVariant(Blocks.FROSTED_ICE, "_1", ModelTemplates.CUBE_ALL, TextureMapping::cube)
                                    )
                            )
                            .select(
                                2,
                                Variant.variant()
                                    .with(
                                        VariantProperties.MODEL,
                                        this.createSuffixedVariant(Blocks.FROSTED_ICE, "_2", ModelTemplates.CUBE_ALL, TextureMapping::cube)
                                    )
                            )
                            .select(
                                3,
                                Variant.variant()
                                    .with(
                                        VariantProperties.MODEL,
                                        this.createSuffixedVariant(Blocks.FROSTED_ICE, "_3", ModelTemplates.CUBE_ALL, TextureMapping::cube)
                                    )
                            )
                    )
            );
    }

    private void createGrassBlocks() {
        ResourceLocation var0 = TextureMapping.getBlockTexture(Blocks.DIRT);
        TextureMapping var1 = new TextureMapping()
            .put(TextureSlot.BOTTOM, var0)
            .copyForced(TextureSlot.BOTTOM, TextureSlot.PARTICLE)
            .put(TextureSlot.TOP, TextureMapping.getBlockTexture(Blocks.GRASS_BLOCK, "_top"))
            .put(TextureSlot.SIDE, TextureMapping.getBlockTexture(Blocks.GRASS_BLOCK, "_snow"));
        Variant var2 = Variant.variant()
            .with(VariantProperties.MODEL, ModelTemplates.CUBE_BOTTOM_TOP.createWithSuffix(Blocks.GRASS_BLOCK, "_snow", var1, this.modelOutput));
        this.createGrassLikeBlock(Blocks.GRASS_BLOCK, ModelLocationUtils.getModelLocation(Blocks.GRASS_BLOCK), var2);
        ResourceLocation var3 = TexturedModel.CUBE_TOP_BOTTOM
            .get(Blocks.MYCELIUM)
            .updateTextures(param1 -> param1.put(TextureSlot.BOTTOM, var0))
            .create(Blocks.MYCELIUM, this.modelOutput);
        this.createGrassLikeBlock(Blocks.MYCELIUM, var3, var2);
        ResourceLocation var4 = TexturedModel.CUBE_TOP_BOTTOM
            .get(Blocks.PODZOL)
            .updateTextures(param1 -> param1.put(TextureSlot.BOTTOM, var0))
            .create(Blocks.PODZOL, this.modelOutput);
        this.createGrassLikeBlock(Blocks.PODZOL, var4, var2);
    }

    private void createGrassLikeBlock(Block param0, ResourceLocation param1, Variant param2) {
        List<Variant> var0 = Arrays.asList(createRotatedVariants(param1));
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(param0).with(PropertyDispatch.property(BlockStateProperties.SNOWY).select(true, param2).select(false, var0))
            );
    }

    private void createCocoa() {
        this.createSimpleFlatItemModel(Items.COCOA_BEANS);
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(Blocks.COCOA)
                    .with(
                        PropertyDispatch.property(BlockStateProperties.AGE_2)
                            .select(0, Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.COCOA, "_stage0")))
                            .select(1, Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.COCOA, "_stage1")))
                            .select(2, Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.COCOA, "_stage2")))
                    )
                    .with(createHorizontalFacingDispatchAlt())
            );
    }

    private void createGrassPath() {
        this.blockStateOutput.accept(createRotatedVariant(Blocks.GRASS_PATH, ModelLocationUtils.getModelLocation(Blocks.GRASS_PATH)));
    }

    private void createWeightedPressurePlate(Block param0, Block param1) {
        TextureMapping var0 = TextureMapping.defaultTexture(param1);
        ResourceLocation var1 = ModelTemplates.PRESSURE_PLATE_UP.create(param0, var0, this.modelOutput);
        ResourceLocation var2 = ModelTemplates.PRESSURE_PLATE_DOWN.create(param0, var0, this.modelOutput);
        this.blockStateOutput.accept(MultiVariantGenerator.multiVariant(param0).with(createEmptyOrFullDispatch(BlockStateProperties.POWER, 1, var2, var1)));
    }

    private void createHopper() {
        ResourceLocation var0 = ModelLocationUtils.getModelLocation(Blocks.HOPPER);
        ResourceLocation var1 = ModelLocationUtils.getModelLocation(Blocks.HOPPER, "_side");
        this.createSimpleFlatItemModel(Items.HOPPER);
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(Blocks.HOPPER)
                    .with(
                        PropertyDispatch.property(BlockStateProperties.FACING_HOPPER)
                            .select(Direction.DOWN, Variant.variant().with(VariantProperties.MODEL, var0))
                            .select(Direction.NORTH, Variant.variant().with(VariantProperties.MODEL, var1))
                            .select(
                                Direction.EAST,
                                Variant.variant().with(VariantProperties.MODEL, var1).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                            )
                            .select(
                                Direction.SOUTH,
                                Variant.variant().with(VariantProperties.MODEL, var1).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                            )
                            .select(
                                Direction.WEST,
                                Variant.variant().with(VariantProperties.MODEL, var1).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                            )
                    )
            );
    }

    private void copyModel(Block param0, Block param1) {
        ResourceLocation var0 = ModelLocationUtils.getModelLocation(param0);
        this.blockStateOutput.accept(MultiVariantGenerator.multiVariant(param1, Variant.variant().with(VariantProperties.MODEL, var0)));
        this.delegateItemModel(param1, var0);
    }

    private void createIronBars() {
        ResourceLocation var0 = ModelLocationUtils.getModelLocation(Blocks.IRON_BARS, "_post_ends");
        ResourceLocation var1 = ModelLocationUtils.getModelLocation(Blocks.IRON_BARS, "_post");
        ResourceLocation var2 = ModelLocationUtils.getModelLocation(Blocks.IRON_BARS, "_cap");
        ResourceLocation var3 = ModelLocationUtils.getModelLocation(Blocks.IRON_BARS, "_cap_alt");
        ResourceLocation var4 = ModelLocationUtils.getModelLocation(Blocks.IRON_BARS, "_side");
        ResourceLocation var5 = ModelLocationUtils.getModelLocation(Blocks.IRON_BARS, "_side_alt");
        this.blockStateOutput
            .accept(
                MultiPartGenerator.multiPart(Blocks.IRON_BARS)
                    .with(Variant.variant().with(VariantProperties.MODEL, var0))
                    .with(
                        Condition.condition()
                            .term(BlockStateProperties.NORTH, false)
                            .term(BlockStateProperties.EAST, false)
                            .term(BlockStateProperties.SOUTH, false)
                            .term(BlockStateProperties.WEST, false),
                        Variant.variant().with(VariantProperties.MODEL, var1)
                    )
                    .with(
                        Condition.condition()
                            .term(BlockStateProperties.NORTH, true)
                            .term(BlockStateProperties.EAST, false)
                            .term(BlockStateProperties.SOUTH, false)
                            .term(BlockStateProperties.WEST, false),
                        Variant.variant().with(VariantProperties.MODEL, var2)
                    )
                    .with(
                        Condition.condition()
                            .term(BlockStateProperties.NORTH, false)
                            .term(BlockStateProperties.EAST, true)
                            .term(BlockStateProperties.SOUTH, false)
                            .term(BlockStateProperties.WEST, false),
                        Variant.variant().with(VariantProperties.MODEL, var2).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                    )
                    .with(
                        Condition.condition()
                            .term(BlockStateProperties.NORTH, false)
                            .term(BlockStateProperties.EAST, false)
                            .term(BlockStateProperties.SOUTH, true)
                            .term(BlockStateProperties.WEST, false),
                        Variant.variant().with(VariantProperties.MODEL, var3)
                    )
                    .with(
                        Condition.condition()
                            .term(BlockStateProperties.NORTH, false)
                            .term(BlockStateProperties.EAST, false)
                            .term(BlockStateProperties.SOUTH, false)
                            .term(BlockStateProperties.WEST, true),
                        Variant.variant().with(VariantProperties.MODEL, var3).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                    )
                    .with(Condition.condition().term(BlockStateProperties.NORTH, true), Variant.variant().with(VariantProperties.MODEL, var4))
                    .with(
                        Condition.condition().term(BlockStateProperties.EAST, true),
                        Variant.variant().with(VariantProperties.MODEL, var4).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                    )
                    .with(Condition.condition().term(BlockStateProperties.SOUTH, true), Variant.variant().with(VariantProperties.MODEL, var5))
                    .with(
                        Condition.condition().term(BlockStateProperties.WEST, true),
                        Variant.variant().with(VariantProperties.MODEL, var5).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                    )
            );
        this.createSimpleFlatItemModel(Blocks.IRON_BARS);
    }

    private void createNonTemplateHorizontalBlock(Block param0) {
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(param0, Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(param0)))
                    .with(createHorizontalFacingDispatch())
            );
    }

    private void createLever() {
        ResourceLocation var0 = ModelLocationUtils.getModelLocation(Blocks.LEVER);
        ResourceLocation var1 = ModelLocationUtils.getModelLocation(Blocks.LEVER, "_on");
        this.createSimpleFlatItemModel(Blocks.LEVER);
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(Blocks.LEVER)
                    .with(createBooleanModelDispatch(BlockStateProperties.POWERED, var0, var1))
                    .with(
                        PropertyDispatch.properties(BlockStateProperties.ATTACH_FACE, BlockStateProperties.HORIZONTAL_FACING)
                            .select(
                                AttachFace.CEILING,
                                Direction.NORTH,
                                Variant.variant()
                                    .with(VariantProperties.X_ROT, VariantProperties.Rotation.R180)
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                            )
                            .select(
                                AttachFace.CEILING,
                                Direction.EAST,
                                Variant.variant()
                                    .with(VariantProperties.X_ROT, VariantProperties.Rotation.R180)
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                            )
                            .select(AttachFace.CEILING, Direction.SOUTH, Variant.variant().with(VariantProperties.X_ROT, VariantProperties.Rotation.R180))
                            .select(
                                AttachFace.CEILING,
                                Direction.WEST,
                                Variant.variant()
                                    .with(VariantProperties.X_ROT, VariantProperties.Rotation.R180)
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                            )
                            .select(AttachFace.FLOOR, Direction.NORTH, Variant.variant())
                            .select(AttachFace.FLOOR, Direction.EAST, Variant.variant().with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90))
                            .select(AttachFace.FLOOR, Direction.SOUTH, Variant.variant().with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180))
                            .select(AttachFace.FLOOR, Direction.WEST, Variant.variant().with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270))
                            .select(AttachFace.WALL, Direction.NORTH, Variant.variant().with(VariantProperties.X_ROT, VariantProperties.Rotation.R90))
                            .select(
                                AttachFace.WALL,
                                Direction.EAST,
                                Variant.variant()
                                    .with(VariantProperties.X_ROT, VariantProperties.Rotation.R90)
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                            )
                            .select(
                                AttachFace.WALL,
                                Direction.SOUTH,
                                Variant.variant()
                                    .with(VariantProperties.X_ROT, VariantProperties.Rotation.R90)
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                            )
                            .select(
                                AttachFace.WALL,
                                Direction.WEST,
                                Variant.variant()
                                    .with(VariantProperties.X_ROT, VariantProperties.Rotation.R90)
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                            )
                    )
            );
    }

    private void createLilyPad() {
        this.createSimpleFlatItemModel(Blocks.LILY_PAD);
        this.blockStateOutput.accept(createRotatedVariant(Blocks.LILY_PAD, ModelLocationUtils.getModelLocation(Blocks.LILY_PAD)));
    }

    private void createNetherPortalBlock() {
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(Blocks.NETHER_PORTAL)
                    .with(
                        PropertyDispatch.property(BlockStateProperties.HORIZONTAL_AXIS)
                            .select(
                                Direction.Axis.X,
                                Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.NETHER_PORTAL, "_ns"))
                            )
                            .select(
                                Direction.Axis.Z,
                                Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.NETHER_PORTAL, "_ew"))
                            )
                    )
            );
    }

    private void createNetherrack() {
        ResourceLocation var0 = TexturedModel.CUBE.create(Blocks.NETHERRACK, this.modelOutput);
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(
                    Blocks.NETHERRACK,
                    Variant.variant().with(VariantProperties.MODEL, var0),
                    Variant.variant().with(VariantProperties.MODEL, var0).with(VariantProperties.X_ROT, VariantProperties.Rotation.R90),
                    Variant.variant().with(VariantProperties.MODEL, var0).with(VariantProperties.X_ROT, VariantProperties.Rotation.R180),
                    Variant.variant().with(VariantProperties.MODEL, var0).with(VariantProperties.X_ROT, VariantProperties.Rotation.R270),
                    Variant.variant().with(VariantProperties.MODEL, var0).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90),
                    Variant.variant()
                        .with(VariantProperties.MODEL, var0)
                        .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                        .with(VariantProperties.X_ROT, VariantProperties.Rotation.R90),
                    Variant.variant()
                        .with(VariantProperties.MODEL, var0)
                        .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                        .with(VariantProperties.X_ROT, VariantProperties.Rotation.R180),
                    Variant.variant()
                        .with(VariantProperties.MODEL, var0)
                        .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                        .with(VariantProperties.X_ROT, VariantProperties.Rotation.R270),
                    Variant.variant().with(VariantProperties.MODEL, var0).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180),
                    Variant.variant()
                        .with(VariantProperties.MODEL, var0)
                        .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                        .with(VariantProperties.X_ROT, VariantProperties.Rotation.R90),
                    Variant.variant()
                        .with(VariantProperties.MODEL, var0)
                        .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                        .with(VariantProperties.X_ROT, VariantProperties.Rotation.R180),
                    Variant.variant()
                        .with(VariantProperties.MODEL, var0)
                        .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                        .with(VariantProperties.X_ROT, VariantProperties.Rotation.R270),
                    Variant.variant().with(VariantProperties.MODEL, var0).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270),
                    Variant.variant()
                        .with(VariantProperties.MODEL, var0)
                        .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                        .with(VariantProperties.X_ROT, VariantProperties.Rotation.R90),
                    Variant.variant()
                        .with(VariantProperties.MODEL, var0)
                        .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                        .with(VariantProperties.X_ROT, VariantProperties.Rotation.R180),
                    Variant.variant()
                        .with(VariantProperties.MODEL, var0)
                        .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                        .with(VariantProperties.X_ROT, VariantProperties.Rotation.R270)
                )
            );
    }

    private void createObserver() {
        ResourceLocation var0 = ModelLocationUtils.getModelLocation(Blocks.OBSERVER);
        ResourceLocation var1 = ModelLocationUtils.getModelLocation(Blocks.OBSERVER, "_on");
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(Blocks.OBSERVER)
                    .with(createBooleanModelDispatch(BlockStateProperties.POWERED, var1, var0))
                    .with(createFacingDispatch())
            );
    }

    private void createPistons() {
        TextureMapping var0 = new TextureMapping()
            .put(TextureSlot.BOTTOM, TextureMapping.getBlockTexture(Blocks.PISTON, "_bottom"))
            .put(TextureSlot.SIDE, TextureMapping.getBlockTexture(Blocks.PISTON, "_side"));
        ResourceLocation var1 = TextureMapping.getBlockTexture(Blocks.PISTON, "_top_sticky");
        ResourceLocation var2 = TextureMapping.getBlockTexture(Blocks.PISTON, "_top");
        TextureMapping var3 = var0.copyAndUpdate(TextureSlot.PLATFORM, var1);
        TextureMapping var4 = var0.copyAndUpdate(TextureSlot.PLATFORM, var2);
        ResourceLocation var5 = ModelLocationUtils.getModelLocation(Blocks.PISTON, "_base");
        this.createPistonVariant(Blocks.PISTON, var5, var4);
        this.createPistonVariant(Blocks.STICKY_PISTON, var5, var3);
        ResourceLocation var6 = ModelTemplates.CUBE_BOTTOM_TOP
            .createWithSuffix(Blocks.PISTON, "_inventory", var0.copyAndUpdate(TextureSlot.TOP, var2), this.modelOutput);
        ResourceLocation var7 = ModelTemplates.CUBE_BOTTOM_TOP
            .createWithSuffix(Blocks.STICKY_PISTON, "_inventory", var0.copyAndUpdate(TextureSlot.TOP, var1), this.modelOutput);
        this.delegateItemModel(Blocks.PISTON, var6);
        this.delegateItemModel(Blocks.STICKY_PISTON, var7);
    }

    private void createPistonVariant(Block param0, ResourceLocation param1, TextureMapping param2) {
        ResourceLocation var0 = ModelTemplates.PISTON.create(param0, param2, this.modelOutput);
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(param0)
                    .with(createBooleanModelDispatch(BlockStateProperties.EXTENDED, param1, var0))
                    .with(createFacingDispatch())
            );
    }

    private void createPistonHeads() {
        TextureMapping var0 = new TextureMapping()
            .put(TextureSlot.UNSTICKY, TextureMapping.getBlockTexture(Blocks.PISTON, "_top"))
            .put(TextureSlot.SIDE, TextureMapping.getBlockTexture(Blocks.PISTON, "_side"));
        TextureMapping var1 = var0.copyAndUpdate(TextureSlot.PLATFORM, TextureMapping.getBlockTexture(Blocks.PISTON, "_top_sticky"));
        TextureMapping var2 = var0.copyAndUpdate(TextureSlot.PLATFORM, TextureMapping.getBlockTexture(Blocks.PISTON, "_top"));
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(Blocks.PISTON_HEAD)
                    .with(
                        PropertyDispatch.properties(BlockStateProperties.SHORT, BlockStateProperties.PISTON_TYPE)
                            .select(
                                false,
                                PistonType.DEFAULT,
                                Variant.variant()
                                    .with(VariantProperties.MODEL, ModelTemplates.PISTON_HEAD.createWithSuffix(Blocks.PISTON, "_head", var2, this.modelOutput))
                            )
                            .select(
                                false,
                                PistonType.STICKY,
                                Variant.variant()
                                    .with(
                                        VariantProperties.MODEL,
                                        ModelTemplates.PISTON_HEAD.createWithSuffix(Blocks.PISTON, "_head_sticky", var1, this.modelOutput)
                                    )
                            )
                            .select(
                                true,
                                PistonType.DEFAULT,
                                Variant.variant()
                                    .with(
                                        VariantProperties.MODEL,
                                        ModelTemplates.PISTON_HEAD_SHORT.createWithSuffix(Blocks.PISTON, "_head_short", var2, this.modelOutput)
                                    )
                            )
                            .select(
                                true,
                                PistonType.STICKY,
                                Variant.variant()
                                    .with(
                                        VariantProperties.MODEL,
                                        ModelTemplates.PISTON_HEAD_SHORT.createWithSuffix(Blocks.PISTON, "_head_short_sticky", var1, this.modelOutput)
                                    )
                            )
                    )
                    .with(createFacingDispatch())
            );
    }

    private void createScaffolding() {
        ResourceLocation var0 = ModelLocationUtils.getModelLocation(Blocks.SCAFFOLDING, "_stable");
        ResourceLocation var1 = ModelLocationUtils.getModelLocation(Blocks.SCAFFOLDING, "_unstable");
        this.delegateItemModel(Blocks.SCAFFOLDING, var0);
        this.blockStateOutput
            .accept(MultiVariantGenerator.multiVariant(Blocks.SCAFFOLDING).with(createBooleanModelDispatch(BlockStateProperties.BOTTOM, var1, var0)));
    }

    private void createRedstoneLamp() {
        ResourceLocation var0 = TexturedModel.CUBE.create(Blocks.REDSTONE_LAMP, this.modelOutput);
        ResourceLocation var1 = this.createSuffixedVariant(Blocks.REDSTONE_LAMP, "_on", ModelTemplates.CUBE_ALL, TextureMapping::cube);
        this.blockStateOutput
            .accept(MultiVariantGenerator.multiVariant(Blocks.REDSTONE_LAMP).with(createBooleanModelDispatch(BlockStateProperties.LIT, var1, var0)));
    }

    private void createNormalTorch(Block param0, Block param1) {
        TextureMapping var0 = TextureMapping.torch(param0);
        this.blockStateOutput.accept(createSimpleBlock(param0, ModelTemplates.TORCH.create(param0, var0, this.modelOutput)));
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(
                        param1, Variant.variant().with(VariantProperties.MODEL, ModelTemplates.WALL_TORCH.create(param1, var0, this.modelOutput))
                    )
                    .with(createTorchHorizontalDispatch())
            );
        this.createSimpleFlatItemModel(param0);
        this.skipAutoItemBlock(param1);
    }

    private void createRedstoneTorch() {
        TextureMapping var0 = TextureMapping.torch(Blocks.REDSTONE_TORCH);
        TextureMapping var1 = TextureMapping.torch(TextureMapping.getBlockTexture(Blocks.REDSTONE_TORCH, "_off"));
        ResourceLocation var2 = ModelTemplates.TORCH.create(Blocks.REDSTONE_TORCH, var0, this.modelOutput);
        ResourceLocation var3 = ModelTemplates.TORCH.createWithSuffix(Blocks.REDSTONE_TORCH, "_off", var1, this.modelOutput);
        this.blockStateOutput
            .accept(MultiVariantGenerator.multiVariant(Blocks.REDSTONE_TORCH).with(createBooleanModelDispatch(BlockStateProperties.LIT, var2, var3)));
        ResourceLocation var4 = ModelTemplates.WALL_TORCH.create(Blocks.REDSTONE_WALL_TORCH, var0, this.modelOutput);
        ResourceLocation var5 = ModelTemplates.WALL_TORCH.createWithSuffix(Blocks.REDSTONE_WALL_TORCH, "_off", var1, this.modelOutput);
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(Blocks.REDSTONE_WALL_TORCH)
                    .with(createBooleanModelDispatch(BlockStateProperties.LIT, var4, var5))
                    .with(createTorchHorizontalDispatch())
            );
        this.createSimpleFlatItemModel(Blocks.REDSTONE_TORCH);
        this.skipAutoItemBlock(Blocks.REDSTONE_WALL_TORCH);
    }

    private void createRepeater() {
        this.createSimpleFlatItemModel(Items.REPEATER);
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(Blocks.REPEATER)
                    .with(
                        PropertyDispatch.properties(BlockStateProperties.DELAY, BlockStateProperties.LOCKED, BlockStateProperties.POWERED)
                            .generate((param0, param1, param2) -> {
                                StringBuilder var0 = new StringBuilder();
                                var0.append('_').append(param0).append("tick");
                                if (param2) {
                                    var0.append("_on");
                                }
                    
                                if (param1) {
                                    var0.append("_locked");
                                }
                    
                                return Variant.variant().with(VariantProperties.MODEL, TextureMapping.getBlockTexture(Blocks.REPEATER, var0.toString()));
                            })
                    )
                    .with(createHorizontalFacingDispatchAlt())
            );
    }

    private void createSeaPickle() {
        this.createSimpleFlatItemModel(Items.SEA_PICKLE);
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(Blocks.SEA_PICKLE)
                    .with(
                        PropertyDispatch.properties(BlockStateProperties.PICKLES, BlockStateProperties.WATERLOGGED)
                            .select(1, false, Arrays.asList(createRotatedVariants(ModelLocationUtils.decorateBlockModelLocation("dead_sea_pickle"))))
                            .select(2, false, Arrays.asList(createRotatedVariants(ModelLocationUtils.decorateBlockModelLocation("two_dead_sea_pickles"))))
                            .select(3, false, Arrays.asList(createRotatedVariants(ModelLocationUtils.decorateBlockModelLocation("three_dead_sea_pickles"))))
                            .select(4, false, Arrays.asList(createRotatedVariants(ModelLocationUtils.decorateBlockModelLocation("four_dead_sea_pickles"))))
                            .select(1, true, Arrays.asList(createRotatedVariants(ModelLocationUtils.decorateBlockModelLocation("sea_pickle"))))
                            .select(2, true, Arrays.asList(createRotatedVariants(ModelLocationUtils.decorateBlockModelLocation("two_sea_pickles"))))
                            .select(3, true, Arrays.asList(createRotatedVariants(ModelLocationUtils.decorateBlockModelLocation("three_sea_pickles"))))
                            .select(4, true, Arrays.asList(createRotatedVariants(ModelLocationUtils.decorateBlockModelLocation("four_sea_pickles"))))
                    )
            );
    }

    private void createSnowBlocks() {
        TextureMapping var0 = TextureMapping.cube(Blocks.SNOW);
        ResourceLocation var1 = ModelTemplates.CUBE_ALL.create(Blocks.SNOW_BLOCK, var0, this.modelOutput);
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(Blocks.SNOW)
                    .with(
                        PropertyDispatch.property(BlockStateProperties.LAYERS)
                            .generate(
                                param1 -> Variant.variant()
                                        .with(
                                            VariantProperties.MODEL,
                                            param1 < 8 ? ModelLocationUtils.getModelLocation(Blocks.SNOW, "_height" + param1 * 2) : var1
                                        )
                            )
                    )
            );
        this.delegateItemModel(Blocks.SNOW, ModelLocationUtils.getModelLocation(Blocks.SNOW, "_height2"));
        this.blockStateOutput.accept(createSimpleBlock(Blocks.SNOW_BLOCK, var1));
    }

    private void createStonecutter() {
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(
                        Blocks.STONECUTTER, Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.STONECUTTER))
                    )
                    .with(createHorizontalFacingDispatch())
            );
    }

    private void createStructureBlock() {
        ResourceLocation var0 = TexturedModel.CUBE.create(Blocks.STRUCTURE_BLOCK, this.modelOutput);
        this.delegateItemModel(Blocks.STRUCTURE_BLOCK, var0);
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(Blocks.STRUCTURE_BLOCK)
                    .with(
                        PropertyDispatch.property(BlockStateProperties.STRUCTUREBLOCK_MODE)
                            .generate(
                                param0 -> Variant.variant()
                                        .with(
                                            VariantProperties.MODEL,
                                            this.createSuffixedVariant(
                                                Blocks.STRUCTURE_BLOCK, "_" + param0.getSerializedName(), ModelTemplates.CUBE_ALL, TextureMapping::cube
                                            )
                                        )
                            )
                    )
            );
    }

    private void createSweetBerryBush() {
        this.createSimpleFlatItemModel(Items.SWEET_BERRIES);
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(Blocks.SWEET_BERRY_BUSH)
                    .with(
                        PropertyDispatch.property(BlockStateProperties.AGE_3)
                            .generate(
                                param0 -> Variant.variant()
                                        .with(
                                            VariantProperties.MODEL,
                                            this.createSuffixedVariant(Blocks.SWEET_BERRY_BUSH, "_stage" + param0, ModelTemplates.CROSS, TextureMapping::cross)
                                        )
                            )
                    )
            );
    }

    private void createTripwire() {
        this.createSimpleFlatItemModel(Items.STRING);
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(Blocks.TRIPWIRE)
                    .with(
                        PropertyDispatch.properties(
                                BlockStateProperties.ATTACHED,
                                BlockStateProperties.EAST,
                                BlockStateProperties.NORTH,
                                BlockStateProperties.SOUTH,
                                BlockStateProperties.WEST
                            )
                            .select(
                                false,
                                false,
                                false,
                                false,
                                false,
                                Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_ns"))
                            )
                            .select(
                                false,
                                true,
                                false,
                                false,
                                false,
                                Variant.variant()
                                    .with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_n"))
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                            )
                            .select(
                                false,
                                false,
                                true,
                                false,
                                false,
                                Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_n"))
                            )
                            .select(
                                false,
                                false,
                                false,
                                true,
                                false,
                                Variant.variant()
                                    .with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_n"))
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                            )
                            .select(
                                false,
                                false,
                                false,
                                false,
                                true,
                                Variant.variant()
                                    .with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_n"))
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                            )
                            .select(
                                false,
                                true,
                                true,
                                false,
                                false,
                                Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_ne"))
                            )
                            .select(
                                false,
                                true,
                                false,
                                true,
                                false,
                                Variant.variant()
                                    .with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_ne"))
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                            )
                            .select(
                                false,
                                false,
                                false,
                                true,
                                true,
                                Variant.variant()
                                    .with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_ne"))
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                            )
                            .select(
                                false,
                                false,
                                true,
                                false,
                                true,
                                Variant.variant()
                                    .with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_ne"))
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                            )
                            .select(
                                false,
                                false,
                                true,
                                true,
                                false,
                                Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_ns"))
                            )
                            .select(
                                false,
                                true,
                                false,
                                false,
                                true,
                                Variant.variant()
                                    .with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_ns"))
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                            )
                            .select(
                                false,
                                true,
                                true,
                                true,
                                false,
                                Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_nse"))
                            )
                            .select(
                                false,
                                true,
                                false,
                                true,
                                true,
                                Variant.variant()
                                    .with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_nse"))
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                            )
                            .select(
                                false,
                                false,
                                true,
                                true,
                                true,
                                Variant.variant()
                                    .with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_nse"))
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                            )
                            .select(
                                false,
                                true,
                                true,
                                false,
                                true,
                                Variant.variant()
                                    .with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_nse"))
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                            )
                            .select(
                                false,
                                true,
                                true,
                                true,
                                true,
                                Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_nsew"))
                            )
                            .select(
                                true,
                                false,
                                false,
                                false,
                                false,
                                Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_attached_ns"))
                            )
                            .select(
                                true,
                                false,
                                true,
                                false,
                                false,
                                Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_attached_n"))
                            )
                            .select(
                                true,
                                false,
                                false,
                                true,
                                false,
                                Variant.variant()
                                    .with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_attached_n"))
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                            )
                            .select(
                                true,
                                true,
                                false,
                                false,
                                false,
                                Variant.variant()
                                    .with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_attached_n"))
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                            )
                            .select(
                                true,
                                false,
                                false,
                                false,
                                true,
                                Variant.variant()
                                    .with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_attached_n"))
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                            )
                            .select(
                                true,
                                true,
                                true,
                                false,
                                false,
                                Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_attached_ne"))
                            )
                            .select(
                                true,
                                true,
                                false,
                                true,
                                false,
                                Variant.variant()
                                    .with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_attached_ne"))
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                            )
                            .select(
                                true,
                                false,
                                false,
                                true,
                                true,
                                Variant.variant()
                                    .with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_attached_ne"))
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                            )
                            .select(
                                true,
                                false,
                                true,
                                false,
                                true,
                                Variant.variant()
                                    .with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_attached_ne"))
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                            )
                            .select(
                                true,
                                false,
                                true,
                                true,
                                false,
                                Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_attached_ns"))
                            )
                            .select(
                                true,
                                true,
                                false,
                                false,
                                true,
                                Variant.variant()
                                    .with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_attached_ns"))
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                            )
                            .select(
                                true,
                                true,
                                true,
                                true,
                                false,
                                Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_attached_nse"))
                            )
                            .select(
                                true,
                                true,
                                false,
                                true,
                                true,
                                Variant.variant()
                                    .with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_attached_nse"))
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                            )
                            .select(
                                true,
                                false,
                                true,
                                true,
                                true,
                                Variant.variant()
                                    .with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_attached_nse"))
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                            )
                            .select(
                                true,
                                true,
                                true,
                                false,
                                true,
                                Variant.variant()
                                    .with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_attached_nse"))
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                            )
                            .select(
                                true,
                                true,
                                true,
                                true,
                                true,
                                Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_attached_nsew"))
                            )
                    )
            );
    }

    private void createTripwireHook() {
        this.createSimpleFlatItemModel(Blocks.TRIPWIRE_HOOK);
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(Blocks.TRIPWIRE_HOOK)
                    .with(
                        PropertyDispatch.properties(BlockStateProperties.ATTACHED, BlockStateProperties.POWERED)
                            .generate(
                                (param0, param1) -> Variant.variant()
                                        .with(
                                            VariantProperties.MODEL,
                                            TextureMapping.getBlockTexture(Blocks.TRIPWIRE_HOOK, (param0 ? "_attached" : "") + (param1 ? "_on" : ""))
                                        )
                            )
                    )
                    .with(createHorizontalFacingDispatch())
            );
    }

    private ResourceLocation createTurtleEggModel(int param0, String param1, TextureMapping param2) {
        switch(param0) {
            case 1:
                return ModelTemplates.TURTLE_EGG.create(ModelLocationUtils.decorateBlockModelLocation(param1 + "turtle_egg"), param2, this.modelOutput);
            case 2:
                return ModelTemplates.TWO_TURTLE_EGGS
                    .create(ModelLocationUtils.decorateBlockModelLocation("two_" + param1 + "turtle_eggs"), param2, this.modelOutput);
            case 3:
                return ModelTemplates.THREE_TURTLE_EGGS
                    .create(ModelLocationUtils.decorateBlockModelLocation("three_" + param1 + "turtle_eggs"), param2, this.modelOutput);
            case 4:
                return ModelTemplates.FOUR_TURTLE_EGGS
                    .create(ModelLocationUtils.decorateBlockModelLocation("four_" + param1 + "turtle_eggs"), param2, this.modelOutput);
            default:
                throw new UnsupportedOperationException();
        }
    }

    private ResourceLocation createTurtleEggModel(Integer param0, Integer param1) {
        switch(param1) {
            case 0:
                return this.createTurtleEggModel(param0, "", TextureMapping.cube(TextureMapping.getBlockTexture(Blocks.TURTLE_EGG)));
            case 1:
                return this.createTurtleEggModel(
                    param0, "slightly_cracked_", TextureMapping.cube(TextureMapping.getBlockTexture(Blocks.TURTLE_EGG, "_slightly_cracked"))
                );
            case 2:
                return this.createTurtleEggModel(
                    param0, "very_cracked_", TextureMapping.cube(TextureMapping.getBlockTexture(Blocks.TURTLE_EGG, "_very_cracked"))
                );
            default:
                throw new UnsupportedOperationException();
        }
    }

    private void createTurtleEgg() {
        this.createSimpleFlatItemModel(Items.TURTLE_EGG);
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(Blocks.TURTLE_EGG)
                    .with(
                        PropertyDispatch.properties(BlockStateProperties.EGGS, BlockStateProperties.HATCH)
                            .generateList((param0, param1) -> Arrays.asList(createRotatedVariants(this.createTurtleEggModel(param0, param1))))
                    )
            );
    }

    private void createVine() {
        this.createSimpleFlatItemModel(Blocks.VINE);
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(Blocks.VINE)
                    .with(
                        PropertyDispatch.properties(
                                BlockStateProperties.EAST,
                                BlockStateProperties.NORTH,
                                BlockStateProperties.SOUTH,
                                BlockStateProperties.UP,
                                BlockStateProperties.WEST
                            )
                            .select(
                                false,
                                false,
                                false,
                                false,
                                false,
                                Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.VINE, "_1"))
                            )
                            .select(
                                false,
                                false,
                                true,
                                false,
                                false,
                                Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.VINE, "_1"))
                            )
                            .select(
                                false,
                                false,
                                false,
                                false,
                                true,
                                Variant.variant()
                                    .with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.VINE, "_1"))
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                            )
                            .select(
                                false,
                                true,
                                false,
                                false,
                                false,
                                Variant.variant()
                                    .with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.VINE, "_1"))
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                            )
                            .select(
                                true,
                                false,
                                false,
                                false,
                                false,
                                Variant.variant()
                                    .with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.VINE, "_1"))
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                            )
                            .select(
                                true,
                                true,
                                false,
                                false,
                                false,
                                Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.VINE, "_2"))
                            )
                            .select(
                                true,
                                false,
                                true,
                                false,
                                false,
                                Variant.variant()
                                    .with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.VINE, "_2"))
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                            )
                            .select(
                                false,
                                false,
                                true,
                                false,
                                true,
                                Variant.variant()
                                    .with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.VINE, "_2"))
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                            )
                            .select(
                                false,
                                true,
                                false,
                                false,
                                true,
                                Variant.variant()
                                    .with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.VINE, "_2"))
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                            )
                            .select(
                                true,
                                false,
                                false,
                                false,
                                true,
                                Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.VINE, "_2_opposite"))
                            )
                            .select(
                                false,
                                true,
                                true,
                                false,
                                false,
                                Variant.variant()
                                    .with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.VINE, "_2_opposite"))
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                            )
                            .select(
                                true,
                                true,
                                true,
                                false,
                                false,
                                Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.VINE, "_3"))
                            )
                            .select(
                                true,
                                false,
                                true,
                                false,
                                true,
                                Variant.variant()
                                    .with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.VINE, "_3"))
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                            )
                            .select(
                                false,
                                true,
                                true,
                                false,
                                true,
                                Variant.variant()
                                    .with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.VINE, "_3"))
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                            )
                            .select(
                                true,
                                true,
                                false,
                                false,
                                true,
                                Variant.variant()
                                    .with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.VINE, "_3"))
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                            )
                            .select(
                                true,
                                true,
                                true,
                                false,
                                true,
                                Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.VINE, "_4"))
                            )
                            .select(
                                false,
                                false,
                                false,
                                true,
                                false,
                                Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.VINE, "_u"))
                            )
                            .select(
                                false,
                                false,
                                true,
                                true,
                                false,
                                Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.VINE, "_1u"))
                            )
                            .select(
                                false,
                                false,
                                false,
                                true,
                                true,
                                Variant.variant()
                                    .with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.VINE, "_1u"))
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                            )
                            .select(
                                false,
                                true,
                                false,
                                true,
                                false,
                                Variant.variant()
                                    .with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.VINE, "_1u"))
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                            )
                            .select(
                                true,
                                false,
                                false,
                                true,
                                false,
                                Variant.variant()
                                    .with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.VINE, "_1u"))
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                            )
                            .select(
                                true,
                                true,
                                false,
                                true,
                                false,
                                Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.VINE, "_2u"))
                            )
                            .select(
                                true,
                                false,
                                true,
                                true,
                                false,
                                Variant.variant()
                                    .with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.VINE, "_2u"))
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                            )
                            .select(
                                false,
                                false,
                                true,
                                true,
                                true,
                                Variant.variant()
                                    .with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.VINE, "_2u"))
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                            )
                            .select(
                                false,
                                true,
                                false,
                                true,
                                true,
                                Variant.variant()
                                    .with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.VINE, "_2u"))
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                            )
                            .select(
                                true,
                                false,
                                false,
                                true,
                                true,
                                Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.VINE, "_2u_opposite"))
                            )
                            .select(
                                false,
                                true,
                                true,
                                true,
                                false,
                                Variant.variant()
                                    .with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.VINE, "_2u_opposite"))
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                            )
                            .select(
                                true,
                                true,
                                true,
                                true,
                                false,
                                Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.VINE, "_3u"))
                            )
                            .select(
                                true,
                                false,
                                true,
                                true,
                                true,
                                Variant.variant()
                                    .with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.VINE, "_3u"))
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                            )
                            .select(
                                false,
                                true,
                                true,
                                true,
                                true,
                                Variant.variant()
                                    .with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.VINE, "_3u"))
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                            )
                            .select(
                                true,
                                true,
                                false,
                                true,
                                true,
                                Variant.variant()
                                    .with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.VINE, "_3u"))
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                            )
                            .select(
                                true,
                                true,
                                true,
                                true,
                                true,
                                Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.VINE, "_4u"))
                            )
                    )
            );
    }

    private void createMagmaBlock() {
        this.blockStateOutput
            .accept(
                createSimpleBlock(
                    Blocks.MAGMA_BLOCK,
                    ModelTemplates.CUBE_ALL
                        .create(Blocks.MAGMA_BLOCK, TextureMapping.cube(ModelLocationUtils.decorateBlockModelLocation("magma")), this.modelOutput)
                )
            );
    }

    private void createShulkerBox(Block param0) {
        this.createTrivialBlock(param0, TexturedModel.PARTICLE_ONLY);
        ModelTemplates.SHULKER_BOX_INVENTORY.create(ModelLocationUtils.getModelLocation(param0.asItem()), TextureMapping.particle(param0), this.modelOutput);
    }

    private void createGrowingPlant(Block param0, Block param1, BlockModelGenerators.TintState param2) {
        this.createCrossBlock(param0, param2);
        this.createCrossBlock(param1, param2);
    }

    private void createBedItem(Block param0, Block param1) {
        ModelTemplates.BED_INVENTORY.create(ModelLocationUtils.getModelLocation(param0.asItem()), TextureMapping.particle(param1), this.modelOutput);
    }

    private void createInfestedStone() {
        ResourceLocation var0 = ModelLocationUtils.getModelLocation(Blocks.STONE);
        ResourceLocation var1 = ModelLocationUtils.getModelLocation(Blocks.STONE, "_mirrored");
        this.blockStateOutput.accept(createRotatedVariant(Blocks.INFESTED_STONE, var0, var1));
        this.delegateItemModel(Blocks.INFESTED_STONE, var0);
    }

    private void createNetherRoots(Block param0, Block param1) {
        this.createCrossBlockWithDefaultItem(param0, BlockModelGenerators.TintState.NOT_TINTED);
        TextureMapping var0 = TextureMapping.plant(TextureMapping.getBlockTexture(param0, "_pot"));
        ResourceLocation var1 = BlockModelGenerators.TintState.NOT_TINTED.getCrossPot().create(param1, var0, this.modelOutput);
        this.blockStateOutput.accept(createSimpleBlock(param1, var1));
    }

    private void createRespawnAnchor() {
        ResourceLocation var0 = TextureMapping.getBlockTexture(Blocks.RESPAWN_ANCHOR, "_bottom");
        ResourceLocation var1 = TextureMapping.getBlockTexture(Blocks.RESPAWN_ANCHOR, "_top_off");
        ResourceLocation var2 = TextureMapping.getBlockTexture(Blocks.RESPAWN_ANCHOR, "_top");
        ResourceLocation[] var3 = new ResourceLocation[5];

        for(int var4 = 0; var4 < 5; ++var4) {
            TextureMapping var5 = new TextureMapping()
                .put(TextureSlot.BOTTOM, var0)
                .put(TextureSlot.TOP, var4 == 0 ? var1 : var2)
                .put(TextureSlot.SIDE, TextureMapping.getBlockTexture(Blocks.RESPAWN_ANCHOR, "_side" + var4));
            var3[var4] = ModelTemplates.CUBE_BOTTOM_TOP.createWithSuffix(Blocks.RESPAWN_ANCHOR, "_" + var4, var5, this.modelOutput);
        }

        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(Blocks.RESPAWN_ANCHOR)
                    .with(
                        PropertyDispatch.property(BlockStateProperties.RESPAWN_ANCHOR_CHARGES)
                            .generate(param1 -> Variant.variant().with(VariantProperties.MODEL, var3[param1]))
                    )
            );
        this.delegateItemModel(Items.RESPAWN_ANCHOR, var3[0]);
    }

    private Variant applyRotation(FrontAndTop param0, Variant param1) {
        switch(param0) {
            case DOWN_NORTH:
                return param1.with(VariantProperties.X_ROT, VariantProperties.Rotation.R90);
            case DOWN_SOUTH:
                return param1.with(VariantProperties.X_ROT, VariantProperties.Rotation.R90).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180);
            case DOWN_WEST:
                return param1.with(VariantProperties.X_ROT, VariantProperties.Rotation.R90).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270);
            case DOWN_EAST:
                return param1.with(VariantProperties.X_ROT, VariantProperties.Rotation.R90).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90);
            case UP_NORTH:
                return param1.with(VariantProperties.X_ROT, VariantProperties.Rotation.R270).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180);
            case UP_SOUTH:
                return param1.with(VariantProperties.X_ROT, VariantProperties.Rotation.R270);
            case UP_WEST:
                return param1.with(VariantProperties.X_ROT, VariantProperties.Rotation.R270).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90);
            case UP_EAST:
                return param1.with(VariantProperties.X_ROT, VariantProperties.Rotation.R270).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270);
            case NORTH_UP:
                return param1;
            case SOUTH_UP:
                return param1.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180);
            case WEST_UP:
                return param1.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270);
            case EAST_UP:
                return param1.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90);
            default:
                throw new UnsupportedOperationException("Rotation " + param0 + " can't be expressed with existing x and y values");
        }
    }

    private void createJigsaw() {
        ResourceLocation var0 = TextureMapping.getBlockTexture(Blocks.JIGSAW, "_top");
        ResourceLocation var1 = TextureMapping.getBlockTexture(Blocks.JIGSAW, "_bottom");
        ResourceLocation var2 = TextureMapping.getBlockTexture(Blocks.JIGSAW, "_side");
        ResourceLocation var3 = TextureMapping.getBlockTexture(Blocks.JIGSAW, "_lock");
        TextureMapping var4 = new TextureMapping()
            .put(TextureSlot.DOWN, var2)
            .put(TextureSlot.WEST, var2)
            .put(TextureSlot.EAST, var2)
            .put(TextureSlot.PARTICLE, var0)
            .put(TextureSlot.NORTH, var0)
            .put(TextureSlot.SOUTH, var1)
            .put(TextureSlot.UP, var3);
        ResourceLocation var5 = ModelTemplates.CUBE_DIRECTIONAL.create(Blocks.JIGSAW, var4, this.modelOutput);
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(Blocks.JIGSAW, Variant.variant().with(VariantProperties.MODEL, var5))
                    .with(PropertyDispatch.property(BlockStateProperties.ORIENTATION).generate(param0 -> this.applyRotation(param0, Variant.variant())))
            );
    }

    public void run() {
        this.createNonTemplateModelBlock(Blocks.AIR);
        this.createNonTemplateModelBlock(Blocks.CAVE_AIR, Blocks.AIR);
        this.createNonTemplateModelBlock(Blocks.VOID_AIR, Blocks.AIR);
        this.createNonTemplateModelBlock(Blocks.BEACON);
        this.createNonTemplateModelBlock(Blocks.CACTUS);
        this.createNonTemplateModelBlock(Blocks.BUBBLE_COLUMN, Blocks.WATER);
        this.createNonTemplateModelBlock(Blocks.DRAGON_EGG);
        this.createNonTemplateModelBlock(Blocks.DRIED_KELP_BLOCK);
        this.createNonTemplateModelBlock(Blocks.ENCHANTING_TABLE);
        this.createNonTemplateModelBlock(Blocks.FLOWER_POT);
        this.createSimpleFlatItemModel(Items.FLOWER_POT);
        this.createNonTemplateModelBlock(Blocks.HONEY_BLOCK);
        this.createNonTemplateModelBlock(Blocks.WATER);
        this.createNonTemplateModelBlock(Blocks.LAVA);
        this.createNonTemplateModelBlock(Blocks.SLIME_BLOCK);
        this.createSimpleFlatItemModel(Items.CHAIN);
        this.createNonTemplateModelBlock(Blocks.POTTED_BAMBOO);
        this.createNonTemplateModelBlock(Blocks.POTTED_CACTUS);
        this.createAirLikeBlock(Blocks.BARRIER, Items.BARRIER);
        this.createSimpleFlatItemModel(Items.BARRIER);
        this.createAirLikeBlock(Blocks.STRUCTURE_VOID, Items.STRUCTURE_VOID);
        this.createSimpleFlatItemModel(Items.STRUCTURE_VOID);
        this.createAirLikeBlock(Blocks.MOVING_PISTON, TextureMapping.getBlockTexture(Blocks.PISTON, "_side"));
        this.createTrivialBlock(Blocks.COAL_ORE, TexturedModel.CUBE);
        this.createTrivialBlock(Blocks.COAL_BLOCK, TexturedModel.CUBE);
        this.createTrivialBlock(Blocks.DIAMOND_ORE, TexturedModel.CUBE);
        this.createTrivialBlock(Blocks.DIAMOND_BLOCK, TexturedModel.CUBE);
        this.createTrivialBlock(Blocks.EMERALD_ORE, TexturedModel.CUBE);
        this.createTrivialBlock(Blocks.EMERALD_BLOCK, TexturedModel.CUBE);
        this.createTrivialBlock(Blocks.GOLD_ORE, TexturedModel.CUBE);
        this.createTrivialBlock(Blocks.NETHER_GOLD_ORE, TexturedModel.CUBE);
        this.createTrivialBlock(Blocks.GOLD_BLOCK, TexturedModel.CUBE);
        this.createTrivialBlock(Blocks.IRON_ORE, TexturedModel.CUBE);
        this.createTrivialBlock(Blocks.IRON_BLOCK, TexturedModel.CUBE);
        this.createTrivialBlock(Blocks.ANCIENT_DEBRIS, TexturedModel.COLUMN);
        this.createTrivialBlock(Blocks.NETHERITE_BLOCK, TexturedModel.CUBE);
        this.createTrivialBlock(Blocks.LAPIS_ORE, TexturedModel.CUBE);
        this.createTrivialBlock(Blocks.LAPIS_BLOCK, TexturedModel.CUBE);
        this.createTrivialBlock(Blocks.NETHER_QUARTZ_ORE, TexturedModel.CUBE);
        this.createTrivialBlock(Blocks.REDSTONE_ORE, TexturedModel.CUBE);
        this.createTrivialBlock(Blocks.REDSTONE_BLOCK, TexturedModel.CUBE);
        this.createTrivialBlock(Blocks.GILDED_BLACKSTONE, TexturedModel.CUBE);
        this.createTrivialBlock(Blocks.BLUE_ICE, TexturedModel.CUBE);
        this.createTrivialBlock(Blocks.CHISELED_NETHER_BRICKS, TexturedModel.CUBE);
        this.createTrivialBlock(Blocks.CLAY, TexturedModel.CUBE);
        this.createTrivialBlock(Blocks.COARSE_DIRT, TexturedModel.CUBE);
        this.createTrivialBlock(Blocks.CRACKED_NETHER_BRICKS, TexturedModel.CUBE);
        this.createTrivialBlock(Blocks.CRACKED_STONE_BRICKS, TexturedModel.CUBE);
        this.createTrivialBlock(Blocks.CRYING_OBSIDIAN, TexturedModel.CUBE);
        this.createTrivialBlock(Blocks.END_STONE, TexturedModel.CUBE);
        this.createTrivialBlock(Blocks.GLOWSTONE, TexturedModel.CUBE);
        this.createTrivialBlock(Blocks.GRAVEL, TexturedModel.CUBE);
        this.createTrivialBlock(Blocks.HONEYCOMB_BLOCK, TexturedModel.CUBE);
        this.createTrivialBlock(Blocks.ICE, TexturedModel.CUBE);
        this.createTrivialBlock(Blocks.JUKEBOX, TexturedModel.CUBE_TOP);
        this.createTrivialBlock(Blocks.LODESTONE, TexturedModel.COLUMN);
        this.createTrivialBlock(Blocks.MELON, TexturedModel.COLUMN);
        this.createTrivialBlock(Blocks.NETHER_WART_BLOCK, TexturedModel.CUBE);
        this.createTrivialBlock(Blocks.NOTE_BLOCK, TexturedModel.CUBE);
        this.createTrivialBlock(Blocks.PACKED_ICE, TexturedModel.CUBE);
        this.createTrivialBlock(Blocks.OBSIDIAN, TexturedModel.CUBE);
        this.createTrivialBlock(Blocks.QUARTZ_BRICKS, TexturedModel.CUBE);
        this.createTrivialBlock(Blocks.SEA_LANTERN, TexturedModel.CUBE);
        this.createTrivialBlock(Blocks.SHROOMLIGHT, TexturedModel.CUBE);
        this.createTrivialBlock(Blocks.SOUL_SAND, TexturedModel.CUBE);
        this.createTrivialBlock(Blocks.SOUL_SOIL, TexturedModel.CUBE);
        this.createTrivialBlock(Blocks.SPAWNER, TexturedModel.CUBE);
        this.createTrivialBlock(Blocks.SPONGE, TexturedModel.CUBE);
        this.createTrivialBlock(Blocks.SEAGRASS, TexturedModel.SEAGRASS);
        this.createSimpleFlatItemModel(Items.SEAGRASS);
        this.createTrivialBlock(Blocks.TNT, TexturedModel.CUBE_TOP_BOTTOM);
        this.createTrivialBlock(Blocks.TARGET, TexturedModel.COLUMN);
        this.createTrivialBlock(Blocks.WARPED_WART_BLOCK, TexturedModel.CUBE);
        this.createTrivialBlock(Blocks.WET_SPONGE, TexturedModel.CUBE);
        this.createTrivialBlock(Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS, TexturedModel.CUBE);
        this.createTrivialBlock(
            Blocks.CHISELED_QUARTZ_BLOCK,
            TexturedModel.COLUMN.updateTexture(param0 -> param0.put(TextureSlot.SIDE, TextureMapping.getBlockTexture(Blocks.CHISELED_QUARTZ_BLOCK)))
        );
        this.createTrivialBlock(Blocks.CHISELED_STONE_BRICKS, TexturedModel.CUBE);
        this.createChiseledSandsone(Blocks.CHISELED_SANDSTONE, Blocks.SANDSTONE);
        this.createChiseledSandsone(Blocks.CHISELED_RED_SANDSTONE, Blocks.RED_SANDSTONE);
        this.createTrivialBlock(Blocks.CHISELED_POLISHED_BLACKSTONE, TexturedModel.CUBE);
        this.createWeightedPressurePlate(Blocks.LIGHT_WEIGHTED_PRESSURE_PLATE, Blocks.GOLD_BLOCK);
        this.createWeightedPressurePlate(Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE, Blocks.IRON_BLOCK);
        this.createBookshelf();
        this.createBrewingStand();
        this.createCakeBlock();
        this.createCampfires(Blocks.CAMPFIRE, Blocks.SOUL_CAMPFIRE);
        this.createCartographyTable();
        this.createCauldron();
        this.createChorusFlower();
        this.createChorusPlant();
        this.createComposter();
        this.createDaylightDetector();
        this.createEndPortalFrame();
        this.createRotatableColumn(Blocks.END_ROD);
        this.createFarmland();
        this.createFire();
        this.createSoulFire();
        this.createFrostedIce();
        this.createGrassBlocks();
        this.createCocoa();
        this.createGrassPath();
        this.createGrindstone();
        this.createHopper();
        this.createIronBars();
        this.createLever();
        this.createLilyPad();
        this.createNetherPortalBlock();
        this.createNetherrack();
        this.createObserver();
        this.createPistons();
        this.createPistonHeads();
        this.createScaffolding();
        this.createRedstoneTorch();
        this.createRedstoneLamp();
        this.createRepeater();
        this.createSeaPickle();
        this.createSmithingTable();
        this.createSnowBlocks();
        this.createStonecutter();
        this.createStructureBlock();
        this.createSweetBerryBush();
        this.createTripwire();
        this.createTripwireHook();
        this.createTurtleEgg();
        this.createVine();
        this.createMagmaBlock();
        this.createJigsaw();
        this.createNonTemplateHorizontalBlock(Blocks.LADDER);
        this.createSimpleFlatItemModel(Blocks.LADDER);
        this.createNonTemplateHorizontalBlock(Blocks.LECTERN);
        this.createNormalTorch(Blocks.TORCH, Blocks.WALL_TORCH);
        this.createNormalTorch(Blocks.SOUL_TORCH, Blocks.SOUL_WALL_TORCH);
        this.createCraftingTableLike(Blocks.CRAFTING_TABLE, Blocks.OAK_PLANKS, TextureMapping::craftingTable);
        this.createCraftingTableLike(Blocks.FLETCHING_TABLE, Blocks.BIRCH_PLANKS, TextureMapping::fletchingTable);
        this.createNyliumBlock(Blocks.CRIMSON_NYLIUM);
        this.createNyliumBlock(Blocks.WARPED_NYLIUM);
        this.createDispenserBlock(Blocks.DISPENSER);
        this.createDispenserBlock(Blocks.DROPPER);
        this.createLantern(Blocks.LANTERN);
        this.createLantern(Blocks.SOUL_LANTERN);
        this.createAxisAlignedPillarBlockCustomModel(Blocks.CHAIN, ModelLocationUtils.getModelLocation(Blocks.CHAIN));
        this.createAxisAlignedPillarBlock(Blocks.BASALT, TexturedModel.COLUMN);
        this.createAxisAlignedPillarBlock(Blocks.POLISHED_BASALT, TexturedModel.COLUMN);
        this.createAxisAlignedPillarBlock(Blocks.BONE_BLOCK, TexturedModel.COLUMN);
        this.createRotatedVariantBlock(Blocks.DIRT);
        this.createRotatedVariantBlock(Blocks.SAND);
        this.createRotatedVariantBlock(Blocks.RED_SAND);
        this.createRotatedMirroredVariantBlock(Blocks.BEDROCK);
        this.createRotatedPillarWithHorizontalVariant(Blocks.HAY_BLOCK, TexturedModel.COLUMN, TexturedModel.COLUMN_HORIZONTAL);
        this.createRotatedPillarWithHorizontalVariant(Blocks.PURPUR_PILLAR, TexturedModel.COLUMN_ALT, TexturedModel.COLUMN_HORIZONTAL_ALT);
        this.createRotatedPillarWithHorizontalVariant(Blocks.QUARTZ_PILLAR, TexturedModel.COLUMN_ALT, TexturedModel.COLUMN_HORIZONTAL_ALT);
        this.createHorizontallyRotatedBlock(Blocks.LOOM, TexturedModel.ORIENTABLE);
        this.createPumpkins();
        this.createBeeNest(Blocks.BEE_NEST, TextureMapping::orientableCube);
        this.createBeeNest(Blocks.BEEHIVE, TextureMapping::orientableCubeSameEnds);
        this.createCropBlock(Blocks.BEETROOTS, BlockStateProperties.AGE_3, 0, 1, 2, 3);
        this.createCropBlock(Blocks.CARROTS, BlockStateProperties.AGE_7, 0, 0, 1, 1, 2, 2, 2, 3);
        this.createCropBlock(Blocks.NETHER_WART, BlockStateProperties.AGE_3, 0, 1, 1, 2);
        this.createCropBlock(Blocks.POTATOES, BlockStateProperties.AGE_7, 0, 0, 1, 1, 2, 2, 2, 3);
        this.createCropBlock(Blocks.WHEAT, BlockStateProperties.AGE_7, 0, 1, 2, 3, 4, 5, 6, 7);
        this.blockEntityModels(ModelLocationUtils.decorateBlockModelLocation("banner"), Blocks.OAK_PLANKS)
            .createWithCustomBlockItemModel(
                ModelTemplates.BANNER_INVENTORY,
                Blocks.WHITE_BANNER,
                Blocks.ORANGE_BANNER,
                Blocks.MAGENTA_BANNER,
                Blocks.LIGHT_BLUE_BANNER,
                Blocks.YELLOW_BANNER,
                Blocks.LIME_BANNER,
                Blocks.PINK_BANNER,
                Blocks.GRAY_BANNER,
                Blocks.LIGHT_GRAY_BANNER,
                Blocks.CYAN_BANNER,
                Blocks.PURPLE_BANNER,
                Blocks.BLUE_BANNER,
                Blocks.BROWN_BANNER,
                Blocks.GREEN_BANNER,
                Blocks.RED_BANNER,
                Blocks.BLACK_BANNER
            )
            .createWithoutBlockItem(
                Blocks.WHITE_WALL_BANNER,
                Blocks.ORANGE_WALL_BANNER,
                Blocks.MAGENTA_WALL_BANNER,
                Blocks.LIGHT_BLUE_WALL_BANNER,
                Blocks.YELLOW_WALL_BANNER,
                Blocks.LIME_WALL_BANNER,
                Blocks.PINK_WALL_BANNER,
                Blocks.GRAY_WALL_BANNER,
                Blocks.LIGHT_GRAY_WALL_BANNER,
                Blocks.CYAN_WALL_BANNER,
                Blocks.PURPLE_WALL_BANNER,
                Blocks.BLUE_WALL_BANNER,
                Blocks.BROWN_WALL_BANNER,
                Blocks.GREEN_WALL_BANNER,
                Blocks.RED_WALL_BANNER,
                Blocks.BLACK_WALL_BANNER
            );
        this.blockEntityModels(ModelLocationUtils.decorateBlockModelLocation("bed"), Blocks.OAK_PLANKS)
            .createWithoutBlockItem(
                Blocks.WHITE_BED,
                Blocks.ORANGE_BED,
                Blocks.MAGENTA_BED,
                Blocks.LIGHT_BLUE_BED,
                Blocks.YELLOW_BED,
                Blocks.LIME_BED,
                Blocks.PINK_BED,
                Blocks.GRAY_BED,
                Blocks.LIGHT_GRAY_BED,
                Blocks.CYAN_BED,
                Blocks.PURPLE_BED,
                Blocks.BLUE_BED,
                Blocks.BROWN_BED,
                Blocks.GREEN_BED,
                Blocks.RED_BED,
                Blocks.BLACK_BED
            );
        this.createBedItem(Blocks.WHITE_BED, Blocks.WHITE_WOOL);
        this.createBedItem(Blocks.ORANGE_BED, Blocks.ORANGE_WOOL);
        this.createBedItem(Blocks.MAGENTA_BED, Blocks.MAGENTA_WOOL);
        this.createBedItem(Blocks.LIGHT_BLUE_BED, Blocks.LIGHT_BLUE_WOOL);
        this.createBedItem(Blocks.YELLOW_BED, Blocks.YELLOW_WOOL);
        this.createBedItem(Blocks.LIME_BED, Blocks.LIME_WOOL);
        this.createBedItem(Blocks.PINK_BED, Blocks.PINK_WOOL);
        this.createBedItem(Blocks.GRAY_BED, Blocks.GRAY_WOOL);
        this.createBedItem(Blocks.LIGHT_GRAY_BED, Blocks.LIGHT_GRAY_WOOL);
        this.createBedItem(Blocks.CYAN_BED, Blocks.CYAN_WOOL);
        this.createBedItem(Blocks.PURPLE_BED, Blocks.PURPLE_WOOL);
        this.createBedItem(Blocks.BLUE_BED, Blocks.BLUE_WOOL);
        this.createBedItem(Blocks.BROWN_BED, Blocks.BROWN_WOOL);
        this.createBedItem(Blocks.GREEN_BED, Blocks.GREEN_WOOL);
        this.createBedItem(Blocks.RED_BED, Blocks.RED_WOOL);
        this.createBedItem(Blocks.BLACK_BED, Blocks.BLACK_WOOL);
        this.blockEntityModels(ModelLocationUtils.decorateBlockModelLocation("skull"), Blocks.SOUL_SAND)
            .createWithCustomBlockItemModel(
                ModelTemplates.SKULL_INVENTORY,
                Blocks.CREEPER_HEAD,
                Blocks.PLAYER_HEAD,
                Blocks.ZOMBIE_HEAD,
                Blocks.SKELETON_SKULL,
                Blocks.WITHER_SKELETON_SKULL
            )
            .create(Blocks.DRAGON_HEAD)
            .createWithoutBlockItem(
                Blocks.CREEPER_WALL_HEAD,
                Blocks.DRAGON_WALL_HEAD,
                Blocks.PLAYER_WALL_HEAD,
                Blocks.ZOMBIE_WALL_HEAD,
                Blocks.SKELETON_WALL_SKULL,
                Blocks.WITHER_SKELETON_WALL_SKULL
            );
        this.createShulkerBox(Blocks.SHULKER_BOX);
        this.createShulkerBox(Blocks.WHITE_SHULKER_BOX);
        this.createShulkerBox(Blocks.ORANGE_SHULKER_BOX);
        this.createShulkerBox(Blocks.MAGENTA_SHULKER_BOX);
        this.createShulkerBox(Blocks.LIGHT_BLUE_SHULKER_BOX);
        this.createShulkerBox(Blocks.YELLOW_SHULKER_BOX);
        this.createShulkerBox(Blocks.LIME_SHULKER_BOX);
        this.createShulkerBox(Blocks.PINK_SHULKER_BOX);
        this.createShulkerBox(Blocks.GRAY_SHULKER_BOX);
        this.createShulkerBox(Blocks.LIGHT_GRAY_SHULKER_BOX);
        this.createShulkerBox(Blocks.CYAN_SHULKER_BOX);
        this.createShulkerBox(Blocks.PURPLE_SHULKER_BOX);
        this.createShulkerBox(Blocks.BLUE_SHULKER_BOX);
        this.createShulkerBox(Blocks.BROWN_SHULKER_BOX);
        this.createShulkerBox(Blocks.GREEN_SHULKER_BOX);
        this.createShulkerBox(Blocks.RED_SHULKER_BOX);
        this.createShulkerBox(Blocks.BLACK_SHULKER_BOX);
        this.createTrivialBlock(Blocks.CONDUIT, TexturedModel.PARTICLE_ONLY);
        this.skipAutoItemBlock(Blocks.CONDUIT);
        this.blockEntityModels(ModelLocationUtils.decorateBlockModelLocation("chest"), Blocks.OAK_PLANKS)
            .createWithoutBlockItem(Blocks.CHEST, Blocks.TRAPPED_CHEST);
        this.blockEntityModels(ModelLocationUtils.decorateBlockModelLocation("ender_chest"), Blocks.OBSIDIAN).createWithoutBlockItem(Blocks.ENDER_CHEST);
        this.blockEntityModels(Blocks.END_PORTAL, Blocks.OBSIDIAN).create(Blocks.END_PORTAL, Blocks.END_GATEWAY);
        this.createTrivialCube(Blocks.WHITE_CONCRETE);
        this.createTrivialCube(Blocks.ORANGE_CONCRETE);
        this.createTrivialCube(Blocks.MAGENTA_CONCRETE);
        this.createTrivialCube(Blocks.LIGHT_BLUE_CONCRETE);
        this.createTrivialCube(Blocks.YELLOW_CONCRETE);
        this.createTrivialCube(Blocks.LIME_CONCRETE);
        this.createTrivialCube(Blocks.PINK_CONCRETE);
        this.createTrivialCube(Blocks.GRAY_CONCRETE);
        this.createTrivialCube(Blocks.LIGHT_GRAY_CONCRETE);
        this.createTrivialCube(Blocks.CYAN_CONCRETE);
        this.createTrivialCube(Blocks.PURPLE_CONCRETE);
        this.createTrivialCube(Blocks.BLUE_CONCRETE);
        this.createTrivialCube(Blocks.BROWN_CONCRETE);
        this.createTrivialCube(Blocks.GREEN_CONCRETE);
        this.createTrivialCube(Blocks.RED_CONCRETE);
        this.createTrivialCube(Blocks.BLACK_CONCRETE);
        this.createColoredBlockWithRandomRotations(
            TexturedModel.CUBE,
            Blocks.WHITE_CONCRETE_POWDER,
            Blocks.ORANGE_CONCRETE_POWDER,
            Blocks.MAGENTA_CONCRETE_POWDER,
            Blocks.LIGHT_BLUE_CONCRETE_POWDER,
            Blocks.YELLOW_CONCRETE_POWDER,
            Blocks.LIME_CONCRETE_POWDER,
            Blocks.PINK_CONCRETE_POWDER,
            Blocks.GRAY_CONCRETE_POWDER,
            Blocks.LIGHT_GRAY_CONCRETE_POWDER,
            Blocks.CYAN_CONCRETE_POWDER,
            Blocks.PURPLE_CONCRETE_POWDER,
            Blocks.BLUE_CONCRETE_POWDER,
            Blocks.BROWN_CONCRETE_POWDER,
            Blocks.GREEN_CONCRETE_POWDER,
            Blocks.RED_CONCRETE_POWDER,
            Blocks.BLACK_CONCRETE_POWDER
        );
        this.createTrivialCube(Blocks.TERRACOTTA);
        this.createTrivialCube(Blocks.WHITE_TERRACOTTA);
        this.createTrivialCube(Blocks.ORANGE_TERRACOTTA);
        this.createTrivialCube(Blocks.MAGENTA_TERRACOTTA);
        this.createTrivialCube(Blocks.LIGHT_BLUE_TERRACOTTA);
        this.createTrivialCube(Blocks.YELLOW_TERRACOTTA);
        this.createTrivialCube(Blocks.LIME_TERRACOTTA);
        this.createTrivialCube(Blocks.PINK_TERRACOTTA);
        this.createTrivialCube(Blocks.GRAY_TERRACOTTA);
        this.createTrivialCube(Blocks.LIGHT_GRAY_TERRACOTTA);
        this.createTrivialCube(Blocks.CYAN_TERRACOTTA);
        this.createTrivialCube(Blocks.PURPLE_TERRACOTTA);
        this.createTrivialCube(Blocks.BLUE_TERRACOTTA);
        this.createTrivialCube(Blocks.BROWN_TERRACOTTA);
        this.createTrivialCube(Blocks.GREEN_TERRACOTTA);
        this.createTrivialCube(Blocks.RED_TERRACOTTA);
        this.createTrivialCube(Blocks.BLACK_TERRACOTTA);
        this.createGlassBlocks(Blocks.GLASS, Blocks.GLASS_PANE);
        this.createGlassBlocks(Blocks.WHITE_STAINED_GLASS, Blocks.WHITE_STAINED_GLASS_PANE);
        this.createGlassBlocks(Blocks.ORANGE_STAINED_GLASS, Blocks.ORANGE_STAINED_GLASS_PANE);
        this.createGlassBlocks(Blocks.MAGENTA_STAINED_GLASS, Blocks.MAGENTA_STAINED_GLASS_PANE);
        this.createGlassBlocks(Blocks.LIGHT_BLUE_STAINED_GLASS, Blocks.LIGHT_BLUE_STAINED_GLASS_PANE);
        this.createGlassBlocks(Blocks.YELLOW_STAINED_GLASS, Blocks.YELLOW_STAINED_GLASS_PANE);
        this.createGlassBlocks(Blocks.LIME_STAINED_GLASS, Blocks.LIME_STAINED_GLASS_PANE);
        this.createGlassBlocks(Blocks.PINK_STAINED_GLASS, Blocks.PINK_STAINED_GLASS_PANE);
        this.createGlassBlocks(Blocks.GRAY_STAINED_GLASS, Blocks.GRAY_STAINED_GLASS_PANE);
        this.createGlassBlocks(Blocks.LIGHT_GRAY_STAINED_GLASS, Blocks.LIGHT_GRAY_STAINED_GLASS_PANE);
        this.createGlassBlocks(Blocks.CYAN_STAINED_GLASS, Blocks.CYAN_STAINED_GLASS_PANE);
        this.createGlassBlocks(Blocks.PURPLE_STAINED_GLASS, Blocks.PURPLE_STAINED_GLASS_PANE);
        this.createGlassBlocks(Blocks.BLUE_STAINED_GLASS, Blocks.BLUE_STAINED_GLASS_PANE);
        this.createGlassBlocks(Blocks.BROWN_STAINED_GLASS, Blocks.BROWN_STAINED_GLASS_PANE);
        this.createGlassBlocks(Blocks.GREEN_STAINED_GLASS, Blocks.GREEN_STAINED_GLASS_PANE);
        this.createGlassBlocks(Blocks.RED_STAINED_GLASS, Blocks.RED_STAINED_GLASS_PANE);
        this.createGlassBlocks(Blocks.BLACK_STAINED_GLASS, Blocks.BLACK_STAINED_GLASS_PANE);
        this.createColoredBlockWithStateRotations(
            TexturedModel.GLAZED_TERRACOTTA,
            Blocks.WHITE_GLAZED_TERRACOTTA,
            Blocks.ORANGE_GLAZED_TERRACOTTA,
            Blocks.MAGENTA_GLAZED_TERRACOTTA,
            Blocks.LIGHT_BLUE_GLAZED_TERRACOTTA,
            Blocks.YELLOW_GLAZED_TERRACOTTA,
            Blocks.LIME_GLAZED_TERRACOTTA,
            Blocks.PINK_GLAZED_TERRACOTTA,
            Blocks.GRAY_GLAZED_TERRACOTTA,
            Blocks.LIGHT_GRAY_GLAZED_TERRACOTTA,
            Blocks.CYAN_GLAZED_TERRACOTTA,
            Blocks.PURPLE_GLAZED_TERRACOTTA,
            Blocks.BLUE_GLAZED_TERRACOTTA,
            Blocks.BROWN_GLAZED_TERRACOTTA,
            Blocks.GREEN_GLAZED_TERRACOTTA,
            Blocks.RED_GLAZED_TERRACOTTA,
            Blocks.BLACK_GLAZED_TERRACOTTA
        );
        this.createWoolBlocks(Blocks.WHITE_WOOL, Blocks.WHITE_CARPET);
        this.createWoolBlocks(Blocks.ORANGE_WOOL, Blocks.ORANGE_CARPET);
        this.createWoolBlocks(Blocks.MAGENTA_WOOL, Blocks.MAGENTA_CARPET);
        this.createWoolBlocks(Blocks.LIGHT_BLUE_WOOL, Blocks.LIGHT_BLUE_CARPET);
        this.createWoolBlocks(Blocks.YELLOW_WOOL, Blocks.YELLOW_CARPET);
        this.createWoolBlocks(Blocks.LIME_WOOL, Blocks.LIME_CARPET);
        this.createWoolBlocks(Blocks.PINK_WOOL, Blocks.PINK_CARPET);
        this.createWoolBlocks(Blocks.GRAY_WOOL, Blocks.GRAY_CARPET);
        this.createWoolBlocks(Blocks.LIGHT_GRAY_WOOL, Blocks.LIGHT_GRAY_CARPET);
        this.createWoolBlocks(Blocks.CYAN_WOOL, Blocks.CYAN_CARPET);
        this.createWoolBlocks(Blocks.PURPLE_WOOL, Blocks.PURPLE_CARPET);
        this.createWoolBlocks(Blocks.BLUE_WOOL, Blocks.BLUE_CARPET);
        this.createWoolBlocks(Blocks.BROWN_WOOL, Blocks.BROWN_CARPET);
        this.createWoolBlocks(Blocks.GREEN_WOOL, Blocks.GREEN_CARPET);
        this.createWoolBlocks(Blocks.RED_WOOL, Blocks.RED_CARPET);
        this.createWoolBlocks(Blocks.BLACK_WOOL, Blocks.BLACK_CARPET);
        this.createPlant(Blocks.FERN, Blocks.POTTED_FERN, BlockModelGenerators.TintState.TINTED);
        this.createPlant(Blocks.DANDELION, Blocks.POTTED_DANDELION, BlockModelGenerators.TintState.NOT_TINTED);
        this.createPlant(Blocks.POPPY, Blocks.POTTED_POPPY, BlockModelGenerators.TintState.NOT_TINTED);
        this.createPlant(Blocks.BLUE_ORCHID, Blocks.POTTED_BLUE_ORCHID, BlockModelGenerators.TintState.NOT_TINTED);
        this.createPlant(Blocks.ALLIUM, Blocks.POTTED_ALLIUM, BlockModelGenerators.TintState.NOT_TINTED);
        this.createPlant(Blocks.AZURE_BLUET, Blocks.POTTED_AZURE_BLUET, BlockModelGenerators.TintState.NOT_TINTED);
        this.createPlant(Blocks.RED_TULIP, Blocks.POTTED_RED_TULIP, BlockModelGenerators.TintState.NOT_TINTED);
        this.createPlant(Blocks.ORANGE_TULIP, Blocks.POTTED_ORANGE_TULIP, BlockModelGenerators.TintState.NOT_TINTED);
        this.createPlant(Blocks.WHITE_TULIP, Blocks.POTTED_WHITE_TULIP, BlockModelGenerators.TintState.NOT_TINTED);
        this.createPlant(Blocks.PINK_TULIP, Blocks.POTTED_PINK_TULIP, BlockModelGenerators.TintState.NOT_TINTED);
        this.createPlant(Blocks.OXEYE_DAISY, Blocks.POTTED_OXEYE_DAISY, BlockModelGenerators.TintState.NOT_TINTED);
        this.createPlant(Blocks.CORNFLOWER, Blocks.POTTED_CORNFLOWER, BlockModelGenerators.TintState.NOT_TINTED);
        this.createPlant(Blocks.LILY_OF_THE_VALLEY, Blocks.POTTED_LILY_OF_THE_VALLEY, BlockModelGenerators.TintState.NOT_TINTED);
        this.createPlant(Blocks.WITHER_ROSE, Blocks.POTTED_WITHER_ROSE, BlockModelGenerators.TintState.NOT_TINTED);
        this.createPlant(Blocks.RED_MUSHROOM, Blocks.POTTED_RED_MUSHROOM, BlockModelGenerators.TintState.NOT_TINTED);
        this.createPlant(Blocks.BROWN_MUSHROOM, Blocks.POTTED_BROWN_MUSHROOM, BlockModelGenerators.TintState.NOT_TINTED);
        this.createPlant(Blocks.DEAD_BUSH, Blocks.POTTED_DEAD_BUSH, BlockModelGenerators.TintState.NOT_TINTED);
        this.createMushroomBlock(Blocks.BROWN_MUSHROOM_BLOCK);
        this.createMushroomBlock(Blocks.RED_MUSHROOM_BLOCK);
        this.createMushroomBlock(Blocks.MUSHROOM_STEM);
        this.createCrossBlockWithDefaultItem(Blocks.GRASS, BlockModelGenerators.TintState.TINTED);
        this.createCrossBlock(Blocks.SUGAR_CANE, BlockModelGenerators.TintState.TINTED);
        this.createSimpleFlatItemModel(Items.SUGAR_CANE);
        this.createGrowingPlant(Blocks.KELP, Blocks.KELP_PLANT, BlockModelGenerators.TintState.TINTED);
        this.createSimpleFlatItemModel(Items.KELP);
        this.skipAutoItemBlock(Blocks.KELP_PLANT);
        this.createGrowingPlant(Blocks.WEEPING_VINES, Blocks.WEEPING_VINES_PLANT, BlockModelGenerators.TintState.NOT_TINTED);
        this.createGrowingPlant(Blocks.TWISTING_VINES, Blocks.TWISTING_VINES_PLANT, BlockModelGenerators.TintState.NOT_TINTED);
        this.createSimpleFlatItemModel(Blocks.WEEPING_VINES, "_plant");
        this.skipAutoItemBlock(Blocks.WEEPING_VINES_PLANT);
        this.createSimpleFlatItemModel(Blocks.TWISTING_VINES, "_plant");
        this.skipAutoItemBlock(Blocks.TWISTING_VINES_PLANT);
        this.createCrossBlockWithDefaultItem(
            Blocks.BAMBOO_SAPLING, BlockModelGenerators.TintState.TINTED, TextureMapping.cross(TextureMapping.getBlockTexture(Blocks.BAMBOO, "_stage0"))
        );
        this.createBamboo();
        this.createCrossBlockWithDefaultItem(Blocks.COBWEB, BlockModelGenerators.TintState.NOT_TINTED);
        this.createDoublePlant(Blocks.LILAC, BlockModelGenerators.TintState.NOT_TINTED);
        this.createDoublePlant(Blocks.ROSE_BUSH, BlockModelGenerators.TintState.NOT_TINTED);
        this.createDoublePlant(Blocks.PEONY, BlockModelGenerators.TintState.NOT_TINTED);
        this.createDoublePlant(Blocks.TALL_GRASS, BlockModelGenerators.TintState.TINTED);
        this.createDoublePlant(Blocks.LARGE_FERN, BlockModelGenerators.TintState.TINTED);
        this.createSunflower();
        this.createTallSeagrass();
        this.createCoral(
            Blocks.TUBE_CORAL,
            Blocks.DEAD_TUBE_CORAL,
            Blocks.TUBE_CORAL_BLOCK,
            Blocks.DEAD_TUBE_CORAL_BLOCK,
            Blocks.TUBE_CORAL_FAN,
            Blocks.DEAD_TUBE_CORAL_FAN,
            Blocks.TUBE_CORAL_WALL_FAN,
            Blocks.DEAD_TUBE_CORAL_WALL_FAN
        );
        this.createCoral(
            Blocks.BRAIN_CORAL,
            Blocks.DEAD_BRAIN_CORAL,
            Blocks.BRAIN_CORAL_BLOCK,
            Blocks.DEAD_BRAIN_CORAL_BLOCK,
            Blocks.BRAIN_CORAL_FAN,
            Blocks.DEAD_BRAIN_CORAL_FAN,
            Blocks.BRAIN_CORAL_WALL_FAN,
            Blocks.DEAD_BRAIN_CORAL_WALL_FAN
        );
        this.createCoral(
            Blocks.BUBBLE_CORAL,
            Blocks.DEAD_BUBBLE_CORAL,
            Blocks.BUBBLE_CORAL_BLOCK,
            Blocks.DEAD_BUBBLE_CORAL_BLOCK,
            Blocks.BUBBLE_CORAL_FAN,
            Blocks.DEAD_BUBBLE_CORAL_FAN,
            Blocks.BUBBLE_CORAL_WALL_FAN,
            Blocks.DEAD_BUBBLE_CORAL_WALL_FAN
        );
        this.createCoral(
            Blocks.FIRE_CORAL,
            Blocks.DEAD_FIRE_CORAL,
            Blocks.FIRE_CORAL_BLOCK,
            Blocks.DEAD_FIRE_CORAL_BLOCK,
            Blocks.FIRE_CORAL_FAN,
            Blocks.DEAD_FIRE_CORAL_FAN,
            Blocks.FIRE_CORAL_WALL_FAN,
            Blocks.DEAD_FIRE_CORAL_WALL_FAN
        );
        this.createCoral(
            Blocks.HORN_CORAL,
            Blocks.DEAD_HORN_CORAL,
            Blocks.HORN_CORAL_BLOCK,
            Blocks.DEAD_HORN_CORAL_BLOCK,
            Blocks.HORN_CORAL_FAN,
            Blocks.DEAD_HORN_CORAL_FAN,
            Blocks.HORN_CORAL_WALL_FAN,
            Blocks.DEAD_HORN_CORAL_WALL_FAN
        );
        this.createStems(Blocks.MELON_STEM, Blocks.ATTACHED_MELON_STEM);
        this.createStems(Blocks.PUMPKIN_STEM, Blocks.ATTACHED_PUMPKIN_STEM);
        this.family(Blocks.ACACIA_PLANKS)
            .button(Blocks.ACACIA_BUTTON)
            .fence(Blocks.ACACIA_FENCE)
            .fenceGate(Blocks.ACACIA_FENCE_GATE)
            .pressurePlate(Blocks.ACACIA_PRESSURE_PLATE)
            .sign(Blocks.ACACIA_SIGN, Blocks.ACACIA_WALL_SIGN)
            .slab(Blocks.ACACIA_SLAB)
            .stairs(Blocks.ACACIA_STAIRS);
        this.createDoor(Blocks.ACACIA_DOOR);
        this.createOrientableTrapdoor(Blocks.ACACIA_TRAPDOOR);
        this.woodProvider(Blocks.ACACIA_LOG).logWithHorizontal(Blocks.ACACIA_LOG).wood(Blocks.ACACIA_WOOD);
        this.woodProvider(Blocks.STRIPPED_ACACIA_LOG).logWithHorizontal(Blocks.STRIPPED_ACACIA_LOG).wood(Blocks.STRIPPED_ACACIA_WOOD);
        this.createPlant(Blocks.ACACIA_SAPLING, Blocks.POTTED_ACACIA_SAPLING, BlockModelGenerators.TintState.NOT_TINTED);
        this.createTrivialBlock(Blocks.ACACIA_LEAVES, TexturedModel.LEAVES);
        this.family(Blocks.BIRCH_PLANKS)
            .button(Blocks.BIRCH_BUTTON)
            .fence(Blocks.BIRCH_FENCE)
            .fenceGate(Blocks.BIRCH_FENCE_GATE)
            .pressurePlate(Blocks.BIRCH_PRESSURE_PLATE)
            .sign(Blocks.BIRCH_SIGN, Blocks.BIRCH_WALL_SIGN)
            .slab(Blocks.BIRCH_SLAB)
            .stairs(Blocks.BIRCH_STAIRS);
        this.createDoor(Blocks.BIRCH_DOOR);
        this.createOrientableTrapdoor(Blocks.BIRCH_TRAPDOOR);
        this.woodProvider(Blocks.BIRCH_LOG).logWithHorizontal(Blocks.BIRCH_LOG).wood(Blocks.BIRCH_WOOD);
        this.woodProvider(Blocks.STRIPPED_BIRCH_LOG).logWithHorizontal(Blocks.STRIPPED_BIRCH_LOG).wood(Blocks.STRIPPED_BIRCH_WOOD);
        this.createPlant(Blocks.BIRCH_SAPLING, Blocks.POTTED_BIRCH_SAPLING, BlockModelGenerators.TintState.NOT_TINTED);
        this.createTrivialBlock(Blocks.BIRCH_LEAVES, TexturedModel.LEAVES);
        this.family(Blocks.OAK_PLANKS)
            .button(Blocks.OAK_BUTTON)
            .fence(Blocks.OAK_FENCE)
            .fenceGate(Blocks.OAK_FENCE_GATE)
            .pressurePlate(Blocks.OAK_PRESSURE_PLATE)
            .sign(Blocks.OAK_SIGN, Blocks.OAK_WALL_SIGN)
            .slab(Blocks.OAK_SLAB)
            .slab(Blocks.PETRIFIED_OAK_SLAB)
            .stairs(Blocks.OAK_STAIRS);
        this.createDoor(Blocks.OAK_DOOR);
        this.createTrapdoor(Blocks.OAK_TRAPDOOR);
        this.woodProvider(Blocks.OAK_LOG).logWithHorizontal(Blocks.OAK_LOG).wood(Blocks.OAK_WOOD);
        this.woodProvider(Blocks.STRIPPED_OAK_LOG).logWithHorizontal(Blocks.STRIPPED_OAK_LOG).wood(Blocks.STRIPPED_OAK_WOOD);
        this.createPlant(Blocks.OAK_SAPLING, Blocks.POTTED_OAK_SAPLING, BlockModelGenerators.TintState.NOT_TINTED);
        this.createTrivialBlock(Blocks.OAK_LEAVES, TexturedModel.LEAVES);
        this.family(Blocks.SPRUCE_PLANKS)
            .button(Blocks.SPRUCE_BUTTON)
            .fence(Blocks.SPRUCE_FENCE)
            .fenceGate(Blocks.SPRUCE_FENCE_GATE)
            .pressurePlate(Blocks.SPRUCE_PRESSURE_PLATE)
            .sign(Blocks.SPRUCE_SIGN, Blocks.SPRUCE_WALL_SIGN)
            .slab(Blocks.SPRUCE_SLAB)
            .stairs(Blocks.SPRUCE_STAIRS);
        this.createDoor(Blocks.SPRUCE_DOOR);
        this.createOrientableTrapdoor(Blocks.SPRUCE_TRAPDOOR);
        this.woodProvider(Blocks.SPRUCE_LOG).logWithHorizontal(Blocks.SPRUCE_LOG).wood(Blocks.SPRUCE_WOOD);
        this.woodProvider(Blocks.STRIPPED_SPRUCE_LOG).logWithHorizontal(Blocks.STRIPPED_SPRUCE_LOG).wood(Blocks.STRIPPED_SPRUCE_WOOD);
        this.createPlant(Blocks.SPRUCE_SAPLING, Blocks.POTTED_SPRUCE_SAPLING, BlockModelGenerators.TintState.NOT_TINTED);
        this.createTrivialBlock(Blocks.SPRUCE_LEAVES, TexturedModel.LEAVES);
        this.family(Blocks.DARK_OAK_PLANKS)
            .button(Blocks.DARK_OAK_BUTTON)
            .fence(Blocks.DARK_OAK_FENCE)
            .fenceGate(Blocks.DARK_OAK_FENCE_GATE)
            .pressurePlate(Blocks.DARK_OAK_PRESSURE_PLATE)
            .sign(Blocks.DARK_OAK_SIGN, Blocks.DARK_OAK_WALL_SIGN)
            .slab(Blocks.DARK_OAK_SLAB)
            .stairs(Blocks.DARK_OAK_STAIRS);
        this.createDoor(Blocks.DARK_OAK_DOOR);
        this.createTrapdoor(Blocks.DARK_OAK_TRAPDOOR);
        this.woodProvider(Blocks.DARK_OAK_LOG).logWithHorizontal(Blocks.DARK_OAK_LOG).wood(Blocks.DARK_OAK_WOOD);
        this.woodProvider(Blocks.STRIPPED_DARK_OAK_LOG).logWithHorizontal(Blocks.STRIPPED_DARK_OAK_LOG).wood(Blocks.STRIPPED_DARK_OAK_WOOD);
        this.createPlant(Blocks.DARK_OAK_SAPLING, Blocks.POTTED_DARK_OAK_SAPLING, BlockModelGenerators.TintState.NOT_TINTED);
        this.createTrivialBlock(Blocks.DARK_OAK_LEAVES, TexturedModel.LEAVES);
        this.family(Blocks.JUNGLE_PLANKS)
            .button(Blocks.JUNGLE_BUTTON)
            .fence(Blocks.JUNGLE_FENCE)
            .fenceGate(Blocks.JUNGLE_FENCE_GATE)
            .pressurePlate(Blocks.JUNGLE_PRESSURE_PLATE)
            .sign(Blocks.JUNGLE_SIGN, Blocks.JUNGLE_WALL_SIGN)
            .slab(Blocks.JUNGLE_SLAB)
            .stairs(Blocks.JUNGLE_STAIRS);
        this.createDoor(Blocks.JUNGLE_DOOR);
        this.createOrientableTrapdoor(Blocks.JUNGLE_TRAPDOOR);
        this.woodProvider(Blocks.JUNGLE_LOG).logWithHorizontal(Blocks.JUNGLE_LOG).wood(Blocks.JUNGLE_WOOD);
        this.woodProvider(Blocks.STRIPPED_JUNGLE_LOG).logWithHorizontal(Blocks.STRIPPED_JUNGLE_LOG).wood(Blocks.STRIPPED_JUNGLE_WOOD);
        this.createPlant(Blocks.JUNGLE_SAPLING, Blocks.POTTED_JUNGLE_SAPLING, BlockModelGenerators.TintState.NOT_TINTED);
        this.createTrivialBlock(Blocks.JUNGLE_LEAVES, TexturedModel.LEAVES);
        this.family(Blocks.CRIMSON_PLANKS)
            .button(Blocks.CRIMSON_BUTTON)
            .fence(Blocks.CRIMSON_FENCE)
            .fenceGate(Blocks.CRIMSON_FENCE_GATE)
            .pressurePlate(Blocks.CRIMSON_PRESSURE_PLATE)
            .sign(Blocks.CRIMSON_SIGN, Blocks.CRIMSON_WALL_SIGN)
            .slab(Blocks.CRIMSON_SLAB)
            .stairs(Blocks.CRIMSON_STAIRS);
        this.createDoor(Blocks.CRIMSON_DOOR);
        this.createOrientableTrapdoor(Blocks.CRIMSON_TRAPDOOR);
        this.woodProvider(Blocks.CRIMSON_STEM).log(Blocks.CRIMSON_STEM).wood(Blocks.CRIMSON_HYPHAE);
        this.woodProvider(Blocks.STRIPPED_CRIMSON_STEM).log(Blocks.STRIPPED_CRIMSON_STEM).wood(Blocks.STRIPPED_CRIMSON_HYPHAE);
        this.createPlant(Blocks.CRIMSON_FUNGUS, Blocks.POTTED_CRIMSON_FUNGUS, BlockModelGenerators.TintState.NOT_TINTED);
        this.createNetherRoots(Blocks.CRIMSON_ROOTS, Blocks.POTTED_CRIMSON_ROOTS);
        this.family(Blocks.WARPED_PLANKS)
            .button(Blocks.WARPED_BUTTON)
            .fence(Blocks.WARPED_FENCE)
            .fenceGate(Blocks.WARPED_FENCE_GATE)
            .pressurePlate(Blocks.WARPED_PRESSURE_PLATE)
            .sign(Blocks.WARPED_SIGN, Blocks.WARPED_WALL_SIGN)
            .slab(Blocks.WARPED_SLAB)
            .stairs(Blocks.WARPED_STAIRS);
        this.createDoor(Blocks.WARPED_DOOR);
        this.createOrientableTrapdoor(Blocks.WARPED_TRAPDOOR);
        this.woodProvider(Blocks.WARPED_STEM).log(Blocks.WARPED_STEM).wood(Blocks.WARPED_HYPHAE);
        this.woodProvider(Blocks.STRIPPED_WARPED_STEM).log(Blocks.STRIPPED_WARPED_STEM).wood(Blocks.STRIPPED_WARPED_HYPHAE);
        this.createPlant(Blocks.WARPED_FUNGUS, Blocks.POTTED_WARPED_FUNGUS, BlockModelGenerators.TintState.NOT_TINTED);
        this.createNetherRoots(Blocks.WARPED_ROOTS, Blocks.POTTED_WARPED_ROOTS);
        this.createCrossBlock(Blocks.NETHER_SPROUTS, BlockModelGenerators.TintState.NOT_TINTED);
        this.createSimpleFlatItemModel(Items.NETHER_SPROUTS);
        this.family(TextureMapping.cube(Blocks.STONE)).fullBlock(param0 -> {
            ResourceLocation var0 = ModelTemplates.CUBE_ALL.create(Blocks.STONE, param0, this.modelOutput);
            ResourceLocation var1 = ModelTemplates.CUBE_MIRRORED_ALL.create(Blocks.STONE, param0, this.modelOutput);
            this.blockStateOutput.accept(createRotatedVariant(Blocks.STONE, var0, var1));
            return var0;
        }).slab(Blocks.STONE_SLAB).pressurePlate(Blocks.STONE_PRESSURE_PLATE).button(Blocks.STONE_BUTTON).stairs(Blocks.STONE_STAIRS);
        this.createDoor(Blocks.IRON_DOOR);
        this.createTrapdoor(Blocks.IRON_TRAPDOOR);
        this.family(Blocks.STONE_BRICKS).wall(Blocks.STONE_BRICK_WALL).stairs(Blocks.STONE_BRICK_STAIRS).slab(Blocks.STONE_BRICK_SLAB);
        this.family(Blocks.MOSSY_STONE_BRICKS).wall(Blocks.MOSSY_STONE_BRICK_WALL).stairs(Blocks.MOSSY_STONE_BRICK_STAIRS).slab(Blocks.MOSSY_STONE_BRICK_SLAB);
        this.family(Blocks.COBBLESTONE).wall(Blocks.COBBLESTONE_WALL).stairs(Blocks.COBBLESTONE_STAIRS).slab(Blocks.COBBLESTONE_SLAB);
        this.family(Blocks.MOSSY_COBBLESTONE).wall(Blocks.MOSSY_COBBLESTONE_WALL).stairs(Blocks.MOSSY_COBBLESTONE_STAIRS).slab(Blocks.MOSSY_COBBLESTONE_SLAB);
        this.family(Blocks.PRISMARINE).wall(Blocks.PRISMARINE_WALL).stairs(Blocks.PRISMARINE_STAIRS).slab(Blocks.PRISMARINE_SLAB);
        this.family(Blocks.PRISMARINE_BRICKS).stairs(Blocks.PRISMARINE_BRICK_STAIRS).slab(Blocks.PRISMARINE_BRICK_SLAB);
        this.family(Blocks.DARK_PRISMARINE).stairs(Blocks.DARK_PRISMARINE_STAIRS).slab(Blocks.DARK_PRISMARINE_SLAB);
        this.family(Blocks.SANDSTONE, TexturedModel.TOP_BOTTOM_WITH_WALL)
            .wall(Blocks.SANDSTONE_WALL)
            .stairs(Blocks.SANDSTONE_STAIRS)
            .slab(Blocks.SANDSTONE_SLAB);
        this.family(Blocks.SMOOTH_SANDSTONE, TexturedModel.createAllSame(TextureMapping.getBlockTexture(Blocks.SANDSTONE, "_top")))
            .slab(Blocks.SMOOTH_SANDSTONE_SLAB)
            .stairs(Blocks.SMOOTH_SANDSTONE_STAIRS);
        this.family(
                Blocks.CUT_SANDSTONE,
                TexturedModel.COLUMN
                    .get(Blocks.SANDSTONE)
                    .updateTextures(param0 -> param0.put(TextureSlot.SIDE, TextureMapping.getBlockTexture(Blocks.CUT_SANDSTONE)))
            )
            .slab(Blocks.CUT_SANDSTONE_SLAB);
        this.family(Blocks.RED_SANDSTONE, TexturedModel.TOP_BOTTOM_WITH_WALL)
            .wall(Blocks.RED_SANDSTONE_WALL)
            .stairs(Blocks.RED_SANDSTONE_STAIRS)
            .slab(Blocks.RED_SANDSTONE_SLAB);
        this.family(Blocks.SMOOTH_RED_SANDSTONE, TexturedModel.createAllSame(TextureMapping.getBlockTexture(Blocks.RED_SANDSTONE, "_top")))
            .slab(Blocks.SMOOTH_RED_SANDSTONE_SLAB)
            .stairs(Blocks.SMOOTH_RED_SANDSTONE_STAIRS);
        this.family(
                Blocks.CUT_RED_SANDSTONE,
                TexturedModel.COLUMN
                    .get(Blocks.RED_SANDSTONE)
                    .updateTextures(param0 -> param0.put(TextureSlot.SIDE, TextureMapping.getBlockTexture(Blocks.CUT_RED_SANDSTONE)))
            )
            .slab(Blocks.CUT_RED_SANDSTONE_SLAB);
        this.family(Blocks.BRICKS).wall(Blocks.BRICK_WALL).stairs(Blocks.BRICK_STAIRS).slab(Blocks.BRICK_SLAB);
        this.family(Blocks.NETHER_BRICKS)
            .fence(Blocks.NETHER_BRICK_FENCE)
            .wall(Blocks.NETHER_BRICK_WALL)
            .stairs(Blocks.NETHER_BRICK_STAIRS)
            .slab(Blocks.NETHER_BRICK_SLAB);
        this.family(Blocks.PURPUR_BLOCK).stairs(Blocks.PURPUR_STAIRS).slab(Blocks.PURPUR_SLAB);
        this.family(Blocks.DIORITE).wall(Blocks.DIORITE_WALL).stairs(Blocks.DIORITE_STAIRS).slab(Blocks.DIORITE_SLAB);
        this.family(Blocks.POLISHED_DIORITE).stairs(Blocks.POLISHED_DIORITE_STAIRS).slab(Blocks.POLISHED_DIORITE_SLAB);
        this.family(Blocks.GRANITE).wall(Blocks.GRANITE_WALL).stairs(Blocks.GRANITE_STAIRS).slab(Blocks.GRANITE_SLAB);
        this.family(Blocks.POLISHED_GRANITE).stairs(Blocks.POLISHED_GRANITE_STAIRS).slab(Blocks.POLISHED_GRANITE_SLAB);
        this.family(Blocks.ANDESITE).wall(Blocks.ANDESITE_WALL).stairs(Blocks.ANDESITE_STAIRS).slab(Blocks.ANDESITE_SLAB);
        this.family(Blocks.POLISHED_ANDESITE).stairs(Blocks.POLISHED_ANDESITE_STAIRS).slab(Blocks.POLISHED_ANDESITE_SLAB);
        this.family(Blocks.END_STONE_BRICKS).wall(Blocks.END_STONE_BRICK_WALL).stairs(Blocks.END_STONE_BRICK_STAIRS).slab(Blocks.END_STONE_BRICK_SLAB);
        this.family(Blocks.QUARTZ_BLOCK, TexturedModel.COLUMN).stairs(Blocks.QUARTZ_STAIRS).slab(Blocks.QUARTZ_SLAB);
        this.family(Blocks.SMOOTH_QUARTZ, TexturedModel.createAllSame(TextureMapping.getBlockTexture(Blocks.QUARTZ_BLOCK, "_bottom")))
            .stairs(Blocks.SMOOTH_QUARTZ_STAIRS)
            .slab(Blocks.SMOOTH_QUARTZ_SLAB);
        this.family(Blocks.RED_NETHER_BRICKS).slab(Blocks.RED_NETHER_BRICK_SLAB).stairs(Blocks.RED_NETHER_BRICK_STAIRS).wall(Blocks.RED_NETHER_BRICK_WALL);
        this.family(Blocks.BLACKSTONE, TexturedModel.COLUMN_WITH_WALL)
            .wall(Blocks.BLACKSTONE_WALL)
            .stairs(Blocks.BLACKSTONE_STAIRS)
            .slab(Blocks.BLACKSTONE_SLAB);
        this.family(Blocks.POLISHED_BLACKSTONE_BRICKS)
            .wall(Blocks.POLISHED_BLACKSTONE_BRICK_WALL)
            .stairs(Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS)
            .slab(Blocks.POLISHED_BLACKSTONE_BRICK_SLAB);
        this.family(Blocks.POLISHED_BLACKSTONE)
            .wall(Blocks.POLISHED_BLACKSTONE_WALL)
            .pressurePlate(Blocks.POLISHED_BLACKSTONE_PRESSURE_PLATE)
            .button(Blocks.POLISHED_BLACKSTONE_BUTTON)
            .stairs(Blocks.POLISHED_BLACKSTONE_STAIRS)
            .slab(Blocks.POLISHED_BLACKSTONE_SLAB);
        this.createSmoothStoneSlab();
        this.createPassiveRail(Blocks.RAIL);
        this.createActiveRail(Blocks.POWERED_RAIL);
        this.createActiveRail(Blocks.DETECTOR_RAIL);
        this.createActiveRail(Blocks.ACTIVATOR_RAIL);
        this.createComparator();
        this.createCommandBlock(Blocks.COMMAND_BLOCK);
        this.createCommandBlock(Blocks.REPEATING_COMMAND_BLOCK);
        this.createCommandBlock(Blocks.CHAIN_COMMAND_BLOCK);
        this.createAnvil(Blocks.ANVIL);
        this.createAnvil(Blocks.CHIPPED_ANVIL);
        this.createAnvil(Blocks.DAMAGED_ANVIL);
        this.createBarrel();
        this.createBell();
        this.createFurnace(Blocks.FURNACE, TexturedModel.ORIENTABLE_ONLY_TOP);
        this.createFurnace(Blocks.BLAST_FURNACE, TexturedModel.ORIENTABLE_ONLY_TOP);
        this.createFurnace(Blocks.SMOKER, TexturedModel.ORIENTABLE);
        this.createRedstoneWire();
        this.createRespawnAnchor();
        this.copyModel(Blocks.CHISELED_STONE_BRICKS, Blocks.INFESTED_CHISELED_STONE_BRICKS);
        this.copyModel(Blocks.COBBLESTONE, Blocks.INFESTED_COBBLESTONE);
        this.copyModel(Blocks.CRACKED_STONE_BRICKS, Blocks.INFESTED_CRACKED_STONE_BRICKS);
        this.copyModel(Blocks.MOSSY_STONE_BRICKS, Blocks.INFESTED_MOSSY_STONE_BRICKS);
        this.createInfestedStone();
        this.copyModel(Blocks.STONE_BRICKS, Blocks.INFESTED_STONE_BRICKS);
        SpawnEggItem.eggs().forEach(param0 -> this.delegateItemModel(param0, ModelLocationUtils.decorateItemModelLocation("template_spawn_egg")));
    }

    class BlockEntityModelGenerator {
        private final ResourceLocation baseModel;

        public BlockEntityModelGenerator(ResourceLocation param0, Block param1) {
            this.baseModel = ModelTemplates.PARTICLE_ONLY.create(param0, TextureMapping.particle(param1), BlockModelGenerators.this.modelOutput);
        }

        public BlockModelGenerators.BlockEntityModelGenerator create(Block... param0) {
            for(Block var0 : param0) {
                BlockModelGenerators.this.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(var0, this.baseModel));
            }

            return this;
        }

        public BlockModelGenerators.BlockEntityModelGenerator createWithoutBlockItem(Block... param0) {
            for(Block var0 : param0) {
                BlockModelGenerators.this.skipAutoItemBlock(var0);
            }

            return this.create(param0);
        }

        public BlockModelGenerators.BlockEntityModelGenerator createWithCustomBlockItemModel(ModelTemplate param0, Block... param1) {
            for(Block var0 : param1) {
                param0.create(ModelLocationUtils.getModelLocation(var0.asItem()), TextureMapping.particle(var0), BlockModelGenerators.this.modelOutput);
            }

            return this.create(param1);
        }
    }

    class BlockFamilyProvider {
        private final TextureMapping mapping;
        @Nullable
        private ResourceLocation fullBlock;

        public BlockFamilyProvider(TextureMapping param0) {
            this.mapping = param0;
        }

        public BlockModelGenerators.BlockFamilyProvider fullBlock(Block param0, ModelTemplate param1) {
            this.fullBlock = param1.create(param0, this.mapping, BlockModelGenerators.this.modelOutput);
            BlockModelGenerators.this.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(param0, this.fullBlock));
            return this;
        }

        public BlockModelGenerators.BlockFamilyProvider fullBlock(Function<TextureMapping, ResourceLocation> param0) {
            this.fullBlock = param0.apply(this.mapping);
            return this;
        }

        public BlockModelGenerators.BlockFamilyProvider button(Block param0) {
            ResourceLocation var0 = ModelTemplates.BUTTON.create(param0, this.mapping, BlockModelGenerators.this.modelOutput);
            ResourceLocation var1 = ModelTemplates.BUTTON_PRESSED.create(param0, this.mapping, BlockModelGenerators.this.modelOutput);
            BlockModelGenerators.this.blockStateOutput.accept(BlockModelGenerators.createButton(param0, var0, var1));
            ResourceLocation var2 = ModelTemplates.BUTTON_INVENTORY.create(param0, this.mapping, BlockModelGenerators.this.modelOutput);
            BlockModelGenerators.this.delegateItemModel(param0, var2);
            return this;
        }

        public BlockModelGenerators.BlockFamilyProvider wall(Block param0) {
            ResourceLocation var0 = ModelTemplates.WALL_POST.create(param0, this.mapping, BlockModelGenerators.this.modelOutput);
            ResourceLocation var1 = ModelTemplates.WALL_LOW_SIDE.create(param0, this.mapping, BlockModelGenerators.this.modelOutput);
            ResourceLocation var2 = ModelTemplates.WALL_TALL_SIDE.create(param0, this.mapping, BlockModelGenerators.this.modelOutput);
            BlockModelGenerators.this.blockStateOutput.accept(BlockModelGenerators.createWall(param0, var0, var1, var2));
            ResourceLocation var3 = ModelTemplates.WALL_INVENTORY.create(param0, this.mapping, BlockModelGenerators.this.modelOutput);
            BlockModelGenerators.this.delegateItemModel(param0, var3);
            return this;
        }

        public BlockModelGenerators.BlockFamilyProvider fence(Block param0) {
            ResourceLocation var0 = ModelTemplates.FENCE_POST.create(param0, this.mapping, BlockModelGenerators.this.modelOutput);
            ResourceLocation var1 = ModelTemplates.FENCE_SIDE.create(param0, this.mapping, BlockModelGenerators.this.modelOutput);
            BlockModelGenerators.this.blockStateOutput.accept(BlockModelGenerators.createFence(param0, var0, var1));
            ResourceLocation var2 = ModelTemplates.FENCE_INVENTORY.create(param0, this.mapping, BlockModelGenerators.this.modelOutput);
            BlockModelGenerators.this.delegateItemModel(param0, var2);
            return this;
        }

        public BlockModelGenerators.BlockFamilyProvider fenceGate(Block param0) {
            ResourceLocation var0 = ModelTemplates.FENCE_GATE_OPEN.create(param0, this.mapping, BlockModelGenerators.this.modelOutput);
            ResourceLocation var1 = ModelTemplates.FENCE_GATE_CLOSED.create(param0, this.mapping, BlockModelGenerators.this.modelOutput);
            ResourceLocation var2 = ModelTemplates.FENCE_GATE_WALL_OPEN.create(param0, this.mapping, BlockModelGenerators.this.modelOutput);
            ResourceLocation var3 = ModelTemplates.FENCE_GATE_WALL_CLOSED.create(param0, this.mapping, BlockModelGenerators.this.modelOutput);
            BlockModelGenerators.this.blockStateOutput.accept(BlockModelGenerators.createFenceGate(param0, var0, var1, var2, var3));
            return this;
        }

        public BlockModelGenerators.BlockFamilyProvider pressurePlate(Block param0) {
            ResourceLocation var0 = ModelTemplates.PRESSURE_PLATE_UP.create(param0, this.mapping, BlockModelGenerators.this.modelOutput);
            ResourceLocation var1 = ModelTemplates.PRESSURE_PLATE_DOWN.create(param0, this.mapping, BlockModelGenerators.this.modelOutput);
            BlockModelGenerators.this.blockStateOutput.accept(BlockModelGenerators.createPressurePlate(param0, var0, var1));
            return this;
        }

        public BlockModelGenerators.BlockFamilyProvider sign(Block param0, Block param1) {
            ResourceLocation var0 = ModelTemplates.PARTICLE_ONLY.create(param0, this.mapping, BlockModelGenerators.this.modelOutput);
            BlockModelGenerators.this.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(param0, var0));
            BlockModelGenerators.this.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(param1, var0));
            BlockModelGenerators.this.createSimpleFlatItemModel(param0.asItem());
            BlockModelGenerators.this.skipAutoItemBlock(param1);
            return this;
        }

        public BlockModelGenerators.BlockFamilyProvider slab(Block param0) {
            if (this.fullBlock == null) {
                throw new IllegalStateException("Full block not generated yet");
            } else {
                ResourceLocation var0 = ModelTemplates.SLAB_BOTTOM.create(param0, this.mapping, BlockModelGenerators.this.modelOutput);
                ResourceLocation var1 = ModelTemplates.SLAB_TOP.create(param0, this.mapping, BlockModelGenerators.this.modelOutput);
                BlockModelGenerators.this.blockStateOutput.accept(BlockModelGenerators.createSlab(param0, var0, var1, this.fullBlock));
                return this;
            }
        }

        public BlockModelGenerators.BlockFamilyProvider stairs(Block param0) {
            ResourceLocation var0 = ModelTemplates.STAIRS_INNER.create(param0, this.mapping, BlockModelGenerators.this.modelOutput);
            ResourceLocation var1 = ModelTemplates.STAIRS_STRAIGHT.create(param0, this.mapping, BlockModelGenerators.this.modelOutput);
            ResourceLocation var2 = ModelTemplates.STAIRS_OUTER.create(param0, this.mapping, BlockModelGenerators.this.modelOutput);
            BlockModelGenerators.this.blockStateOutput.accept(BlockModelGenerators.createStairs(param0, var0, var1, var2));
            return this;
        }
    }

    static enum TintState {
        TINTED,
        NOT_TINTED;

        public ModelTemplate getCross() {
            return this == TINTED ? ModelTemplates.TINTED_CROSS : ModelTemplates.CROSS;
        }

        public ModelTemplate getCrossPot() {
            return this == TINTED ? ModelTemplates.TINTED_FLOWER_POT_CROSS : ModelTemplates.FLOWER_POT_CROSS;
        }
    }

    class WoodProvider {
        private final TextureMapping logMapping;

        public WoodProvider(TextureMapping param0) {
            this.logMapping = param0;
        }

        public BlockModelGenerators.WoodProvider wood(Block param0) {
            TextureMapping var0 = this.logMapping.copyAndUpdate(TextureSlot.END, this.logMapping.get(TextureSlot.SIDE));
            ResourceLocation var1 = ModelTemplates.CUBE_COLUMN.create(param0, var0, BlockModelGenerators.this.modelOutput);
            BlockModelGenerators.this.blockStateOutput.accept(BlockModelGenerators.createAxisAlignedPillarBlock(param0, var1));
            return this;
        }

        public BlockModelGenerators.WoodProvider log(Block param0) {
            ResourceLocation var0 = ModelTemplates.CUBE_COLUMN.create(param0, this.logMapping, BlockModelGenerators.this.modelOutput);
            BlockModelGenerators.this.blockStateOutput.accept(BlockModelGenerators.createAxisAlignedPillarBlock(param0, var0));
            return this;
        }

        public BlockModelGenerators.WoodProvider logWithHorizontal(Block param0) {
            ResourceLocation var0 = ModelTemplates.CUBE_COLUMN.create(param0, this.logMapping, BlockModelGenerators.this.modelOutput);
            ResourceLocation var1 = ModelTemplates.CUBE_COLUMN_HORIZONTAL.create(param0, this.logMapping, BlockModelGenerators.this.modelOutput);
            BlockModelGenerators.this.blockStateOutput.accept(BlockModelGenerators.createRotatedPillarWithHorizontalVariant(param0, var0, var1));
            return this;
        }
    }
}
