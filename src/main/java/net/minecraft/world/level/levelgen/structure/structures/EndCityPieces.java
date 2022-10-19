package net.minecraft.world.level.levelgen.structure.structures;

import com.google.common.collect.Lists;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.TemplateStructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;

public class EndCityPieces {
    private static final int MAX_GEN_DEPTH = 8;
    static final EndCityPieces.SectionGenerator HOUSE_TOWER_GENERATOR = new EndCityPieces.SectionGenerator() {
        @Override
        public void init() {
        }

        @Override
        public boolean generate(
            StructureTemplateManager param0, int param1, EndCityPieces.EndCityPiece param2, BlockPos param3, List<StructurePiece> param4, RandomSource param5
        ) {
            if (param1 > 8) {
                return false;
            } else {
                Rotation var0 = param2.placeSettings().getRotation();
                EndCityPieces.EndCityPiece var1 = EndCityPieces.addHelper(param4, EndCityPieces.addPiece(param0, param2, param3, "base_floor", var0, true));
                int var2 = param5.nextInt(3);
                if (var2 == 0) {
                    var1 = EndCityPieces.addHelper(param4, EndCityPieces.addPiece(param0, var1, new BlockPos(-1, 4, -1), "base_roof", var0, true));
                } else if (var2 == 1) {
                    var1 = EndCityPieces.addHelper(param4, EndCityPieces.addPiece(param0, var1, new BlockPos(-1, 0, -1), "second_floor_2", var0, false));
                    var1 = EndCityPieces.addHelper(param4, EndCityPieces.addPiece(param0, var1, new BlockPos(-1, 8, -1), "second_roof", var0, false));
                    EndCityPieces.recursiveChildren(param0, EndCityPieces.TOWER_GENERATOR, param1 + 1, var1, null, param4, param5);
                } else if (var2 == 2) {
                    var1 = EndCityPieces.addHelper(param4, EndCityPieces.addPiece(param0, var1, new BlockPos(-1, 0, -1), "second_floor_2", var0, false));
                    var1 = EndCityPieces.addHelper(param4, EndCityPieces.addPiece(param0, var1, new BlockPos(-1, 4, -1), "third_floor_2", var0, false));
                    var1 = EndCityPieces.addHelper(param4, EndCityPieces.addPiece(param0, var1, new BlockPos(-1, 8, -1), "third_roof", var0, true));
                    EndCityPieces.recursiveChildren(param0, EndCityPieces.TOWER_GENERATOR, param1 + 1, var1, null, param4, param5);
                }

                return true;
            }
        }
    };
    static final List<Tuple<Rotation, BlockPos>> TOWER_BRIDGES = Lists.newArrayList(
        new Tuple<>(Rotation.NONE, new BlockPos(1, -1, 0)),
        new Tuple<>(Rotation.CLOCKWISE_90, new BlockPos(6, -1, 1)),
        new Tuple<>(Rotation.COUNTERCLOCKWISE_90, new BlockPos(0, -1, 5)),
        new Tuple<>(Rotation.CLOCKWISE_180, new BlockPos(5, -1, 6))
    );
    static final EndCityPieces.SectionGenerator TOWER_GENERATOR = new EndCityPieces.SectionGenerator() {
        @Override
        public void init() {
        }

        @Override
        public boolean generate(
            StructureTemplateManager param0, int param1, EndCityPieces.EndCityPiece param2, BlockPos param3, List<StructurePiece> param4, RandomSource param5
        ) {
            Rotation var0 = param2.placeSettings().getRotation();
            EndCityPieces.EndCityPiece var1 = EndCityPieces.addHelper(
                param4, EndCityPieces.addPiece(param0, param2, new BlockPos(3 + param5.nextInt(2), -3, 3 + param5.nextInt(2)), "tower_base", var0, true)
            );
            var1 = EndCityPieces.addHelper(param4, EndCityPieces.addPiece(param0, var1, new BlockPos(0, 7, 0), "tower_piece", var0, true));
            EndCityPieces.EndCityPiece var2 = param5.nextInt(3) == 0 ? var1 : null;
            int var3 = 1 + param5.nextInt(3);

            for(int var4 = 0; var4 < var3; ++var4) {
                var1 = EndCityPieces.addHelper(param4, EndCityPieces.addPiece(param0, var1, new BlockPos(0, 4, 0), "tower_piece", var0, true));
                if (var4 < var3 - 1 && param5.nextBoolean()) {
                    var2 = var1;
                }
            }

            if (var2 != null) {
                for(Tuple<Rotation, BlockPos> var5 : EndCityPieces.TOWER_BRIDGES) {
                    if (param5.nextBoolean()) {
                        EndCityPieces.EndCityPiece var6 = EndCityPieces.addHelper(
                            param4, EndCityPieces.addPiece(param0, var2, var5.getB(), "bridge_end", var0.getRotated(var5.getA()), true)
                        );
                        EndCityPieces.recursiveChildren(param0, EndCityPieces.TOWER_BRIDGE_GENERATOR, param1 + 1, var6, null, param4, param5);
                    }
                }

                var1 = EndCityPieces.addHelper(param4, EndCityPieces.addPiece(param0, var1, new BlockPos(-1, 4, -1), "tower_top", var0, true));
            } else {
                if (param1 != 7) {
                    return EndCityPieces.recursiveChildren(param0, EndCityPieces.FAT_TOWER_GENERATOR, param1 + 1, var1, null, param4, param5);
                }

                var1 = EndCityPieces.addHelper(param4, EndCityPieces.addPiece(param0, var1, new BlockPos(-1, 4, -1), "tower_top", var0, true));
            }

            return true;
        }
    };
    static final EndCityPieces.SectionGenerator TOWER_BRIDGE_GENERATOR = new EndCityPieces.SectionGenerator() {
        public boolean shipCreated;

        @Override
        public void init() {
            this.shipCreated = false;
        }

        @Override
        public boolean generate(
            StructureTemplateManager param0, int param1, EndCityPieces.EndCityPiece param2, BlockPos param3, List<StructurePiece> param4, RandomSource param5
        ) {
            Rotation var0 = param2.placeSettings().getRotation();
            int var1 = param5.nextInt(4) + 1;
            EndCityPieces.EndCityPiece var2 = EndCityPieces.addHelper(
                param4, EndCityPieces.addPiece(param0, param2, new BlockPos(0, 0, -4), "bridge_piece", var0, true)
            );
            var2.setGenDepth(-1);
            int var3 = 0;

            for(int var4 = 0; var4 < var1; ++var4) {
                if (param5.nextBoolean()) {
                    var2 = EndCityPieces.addHelper(param4, EndCityPieces.addPiece(param0, var2, new BlockPos(0, var3, -4), "bridge_piece", var0, true));
                    var3 = 0;
                } else {
                    if (param5.nextBoolean()) {
                        var2 = EndCityPieces.addHelper(
                            param4, EndCityPieces.addPiece(param0, var2, new BlockPos(0, var3, -4), "bridge_steep_stairs", var0, true)
                        );
                    } else {
                        var2 = EndCityPieces.addHelper(
                            param4, EndCityPieces.addPiece(param0, var2, new BlockPos(0, var3, -8), "bridge_gentle_stairs", var0, true)
                        );
                    }

                    var3 = 4;
                }
            }

            if (!this.shipCreated && param5.nextInt(10 - param1) == 0) {
                EndCityPieces.addHelper(
                    param4, EndCityPieces.addPiece(param0, var2, new BlockPos(-8 + param5.nextInt(8), var3, -70 + param5.nextInt(10)), "ship", var0, true)
                );
                this.shipCreated = true;
            } else if (!EndCityPieces.recursiveChildren(
                param0, EndCityPieces.HOUSE_TOWER_GENERATOR, param1 + 1, var2, new BlockPos(-3, var3 + 1, -11), param4, param5
            )) {
                return false;
            }

            var2 = EndCityPieces.addHelper(
                param4, EndCityPieces.addPiece(param0, var2, new BlockPos(4, var3, 0), "bridge_end", var0.getRotated(Rotation.CLOCKWISE_180), true)
            );
            var2.setGenDepth(-1);
            return true;
        }
    };
    static final List<Tuple<Rotation, BlockPos>> FAT_TOWER_BRIDGES = Lists.newArrayList(
        new Tuple<>(Rotation.NONE, new BlockPos(4, -1, 0)),
        new Tuple<>(Rotation.CLOCKWISE_90, new BlockPos(12, -1, 4)),
        new Tuple<>(Rotation.COUNTERCLOCKWISE_90, new BlockPos(0, -1, 8)),
        new Tuple<>(Rotation.CLOCKWISE_180, new BlockPos(8, -1, 12))
    );
    static final EndCityPieces.SectionGenerator FAT_TOWER_GENERATOR = new EndCityPieces.SectionGenerator() {
        @Override
        public void init() {
        }

        @Override
        public boolean generate(
            StructureTemplateManager param0, int param1, EndCityPieces.EndCityPiece param2, BlockPos param3, List<StructurePiece> param4, RandomSource param5
        ) {
            Rotation var0 = param2.placeSettings().getRotation();
            EndCityPieces.EndCityPiece var1 = EndCityPieces.addHelper(
                param4, EndCityPieces.addPiece(param0, param2, new BlockPos(-3, 4, -3), "fat_tower_base", var0, true)
            );
            var1 = EndCityPieces.addHelper(param4, EndCityPieces.addPiece(param0, var1, new BlockPos(0, 4, 0), "fat_tower_middle", var0, true));

            for(int var2 = 0; var2 < 2 && param5.nextInt(3) != 0; ++var2) {
                var1 = EndCityPieces.addHelper(param4, EndCityPieces.addPiece(param0, var1, new BlockPos(0, 8, 0), "fat_tower_middle", var0, true));

                for(Tuple<Rotation, BlockPos> var3 : EndCityPieces.FAT_TOWER_BRIDGES) {
                    if (param5.nextBoolean()) {
                        EndCityPieces.EndCityPiece var4 = EndCityPieces.addHelper(
                            param4, EndCityPieces.addPiece(param0, var1, var3.getB(), "bridge_end", var0.getRotated(var3.getA()), true)
                        );
                        EndCityPieces.recursiveChildren(param0, EndCityPieces.TOWER_BRIDGE_GENERATOR, param1 + 1, var4, null, param4, param5);
                    }
                }
            }

            var1 = EndCityPieces.addHelper(param4, EndCityPieces.addPiece(param0, var1, new BlockPos(-2, 8, -2), "fat_tower_top", var0, true));
            return true;
        }
    };

    static EndCityPieces.EndCityPiece addPiece(
        StructureTemplateManager param0, EndCityPieces.EndCityPiece param1, BlockPos param2, String param3, Rotation param4, boolean param5
    ) {
        EndCityPieces.EndCityPiece var0 = new EndCityPieces.EndCityPiece(param0, param3, param1.templatePosition(), param4, param5);
        BlockPos var1 = param1.template().calculateConnectedPosition(param1.placeSettings(), param2, var0.placeSettings(), BlockPos.ZERO);
        var0.move(var1.getX(), var1.getY(), var1.getZ());
        return var0;
    }

    public static void startHouseTower(StructureTemplateManager param0, BlockPos param1, Rotation param2, List<StructurePiece> param3, RandomSource param4) {
        FAT_TOWER_GENERATOR.init();
        HOUSE_TOWER_GENERATOR.init();
        TOWER_BRIDGE_GENERATOR.init();
        TOWER_GENERATOR.init();
        EndCityPieces.EndCityPiece var0 = addHelper(param3, new EndCityPieces.EndCityPiece(param0, "base_floor", param1, param2, true));
        var0 = addHelper(param3, addPiece(param0, var0, new BlockPos(-1, 0, -1), "second_floor_1", param2, false));
        var0 = addHelper(param3, addPiece(param0, var0, new BlockPos(-1, 4, -1), "third_floor_1", param2, false));
        var0 = addHelper(param3, addPiece(param0, var0, new BlockPos(-1, 8, -1), "third_roof", param2, true));
        recursiveChildren(param0, TOWER_GENERATOR, 1, var0, null, param3, param4);
    }

    static EndCityPieces.EndCityPiece addHelper(List<StructurePiece> param0, EndCityPieces.EndCityPiece param1) {
        param0.add(param1);
        return param1;
    }

    static boolean recursiveChildren(
        StructureTemplateManager param0,
        EndCityPieces.SectionGenerator param1,
        int param2,
        EndCityPieces.EndCityPiece param3,
        BlockPos param4,
        List<StructurePiece> param5,
        RandomSource param6
    ) {
        if (param2 > 8) {
            return false;
        } else {
            List<StructurePiece> var0 = Lists.newArrayList();
            if (param1.generate(param0, param2, param3, param4, var0, param6)) {
                boolean var1 = false;
                int var2 = param6.nextInt();

                for(StructurePiece var3 : var0) {
                    var3.setGenDepth(var2);
                    StructurePiece var4 = StructurePiece.findCollisionPiece(param5, var3.getBoundingBox());
                    if (var4 != null && var4.getGenDepth() != param3.getGenDepth()) {
                        var1 = true;
                        break;
                    }
                }

                if (!var1) {
                    param5.addAll(var0);
                    return true;
                }
            }

            return false;
        }
    }

    public static class EndCityPiece extends TemplateStructurePiece {
        public EndCityPiece(StructureTemplateManager param0, String param1, BlockPos param2, Rotation param3, boolean param4) {
            super(StructurePieceType.END_CITY_PIECE, 0, param0, makeResourceLocation(param1), param1, makeSettings(param4, param3), param2);
        }

        public EndCityPiece(StructureTemplateManager param0, CompoundTag param1) {
            super(
                StructurePieceType.END_CITY_PIECE, param1, param0, param1x -> makeSettings(param1.getBoolean("OW"), Rotation.valueOf(param1.getString("Rot")))
            );
        }

        private static StructurePlaceSettings makeSettings(boolean param0, Rotation param1) {
            BlockIgnoreProcessor var0 = param0 ? BlockIgnoreProcessor.STRUCTURE_BLOCK : BlockIgnoreProcessor.STRUCTURE_AND_AIR;
            return new StructurePlaceSettings().setIgnoreEntities(true).addProcessor(var0).setRotation(param1);
        }

        @Override
        protected ResourceLocation makeTemplateLocation() {
            return makeResourceLocation(this.templateName);
        }

        private static ResourceLocation makeResourceLocation(String param0) {
            return new ResourceLocation("end_city/" + param0);
        }

        @Override
        protected void addAdditionalSaveData(StructurePieceSerializationContext param0, CompoundTag param1) {
            super.addAdditionalSaveData(param0, param1);
            param1.putString("Rot", this.placeSettings.getRotation().name());
            param1.putBoolean("OW", this.placeSettings.getProcessors().get(0) == BlockIgnoreProcessor.STRUCTURE_BLOCK);
        }

        @Override
        protected void handleDataMarker(String param0, BlockPos param1, ServerLevelAccessor param2, RandomSource param3, BoundingBox param4) {
            if (param0.startsWith("Chest")) {
                BlockPos var0 = param1.below();
                if (param4.isInside(var0)) {
                    RandomizableContainerBlockEntity.setLootTable(param2, param3, var0, BuiltInLootTables.END_CITY_TREASURE);
                }
            } else if (param4.isInside(param1) && Level.isInSpawnableBounds(param1)) {
                if (param0.startsWith("Sentry")) {
                    Shulker var1 = EntityType.SHULKER.create(param2.getLevel());
                    if (var1 != null) {
                        var1.setPos((double)param1.getX() + 0.5, (double)param1.getY(), (double)param1.getZ() + 0.5);
                        param2.addFreshEntity(var1);
                    }
                } else if (param0.startsWith("Elytra")) {
                    ItemFrame var2 = new ItemFrame(param2.getLevel(), param1, this.placeSettings.getRotation().rotate(Direction.SOUTH));
                    var2.setItem(new ItemStack(Items.ELYTRA), false);
                    param2.addFreshEntity(var2);
                }
            }

        }
    }

    interface SectionGenerator {
        void init();

        boolean generate(StructureTemplateManager var1, int var2, EndCityPieces.EndCityPiece var3, BlockPos var4, List<StructurePiece> var5, RandomSource var6);
    }
}
