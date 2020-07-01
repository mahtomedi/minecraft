package net.minecraft.world.level.levelgen.structure;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Random;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.StructureMode;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.StructurePieceType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class TemplateStructurePiece extends StructurePiece {
    private static final Logger LOGGER = LogManager.getLogger();
    protected StructureTemplate template;
    protected StructurePlaceSettings placeSettings;
    protected BlockPos templatePosition;

    public TemplateStructurePiece(StructurePieceType param0, int param1) {
        super(param0, param1);
    }

    public TemplateStructurePiece(StructurePieceType param0, CompoundTag param1) {
        super(param0, param1);
        this.templatePosition = new BlockPos(param1.getInt("TPX"), param1.getInt("TPY"), param1.getInt("TPZ"));
    }

    protected void setup(StructureTemplate param0, BlockPos param1, StructurePlaceSettings param2) {
        this.template = param0;
        this.setOrientation(Direction.NORTH);
        this.templatePosition = param1;
        this.placeSettings = param2;
        this.boundingBox = param0.getBoundingBox(param2, param1);
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag param0) {
        param0.putInt("TPX", this.templatePosition.getX());
        param0.putInt("TPY", this.templatePosition.getY());
        param0.putInt("TPZ", this.templatePosition.getZ());
    }

    @Override
    public boolean postProcess(
        WorldGenLevel param0, StructureFeatureManager param1, ChunkGenerator param2, Random param3, BoundingBox param4, ChunkPos param5, BlockPos param6
    ) {
        this.placeSettings.setBoundingBox(param4);
        this.boundingBox = this.template.getBoundingBox(this.placeSettings, this.templatePosition);
        if (this.template.placeInWorld(param0, this.templatePosition, param6, this.placeSettings, param3, 2)) {
            for(StructureTemplate.StructureBlockInfo var1 : this.template.filterBlocks(this.templatePosition, this.placeSettings, Blocks.STRUCTURE_BLOCK)) {
                if (var1.nbt != null) {
                    StructureMode var2 = StructureMode.valueOf(var1.nbt.getString("mode"));
                    if (var2 == StructureMode.DATA) {
                        this.handleDataMarker(var1.nbt.getString("metadata"), var1.pos, param0, param3, param4);
                    }
                }
            }

            for(StructureTemplate.StructureBlockInfo var4 : this.template.filterBlocks(this.templatePosition, this.placeSettings, Blocks.JIGSAW)) {
                if (var4.nbt != null) {
                    String var5 = var4.nbt.getString("final_state");
                    BlockStateParser var6 = new BlockStateParser(new StringReader(var5), false);
                    BlockState var7 = Blocks.AIR.defaultBlockState();

                    try {
                        var6.parse(true);
                        BlockState var8 = var6.getState();
                        if (var8 != null) {
                            var7 = var8;
                        } else {
                            LOGGER.error("Error while parsing blockstate {} in jigsaw block @ {}", var5, var4.pos);
                        }
                    } catch (CommandSyntaxException var16) {
                        LOGGER.error("Error while parsing blockstate {} in jigsaw block @ {}", var5, var4.pos);
                    }

                    param0.setBlock(var4.pos, var7, 3);
                }
            }
        }

        return true;
    }

    protected abstract void handleDataMarker(String var1, BlockPos var2, ServerLevelAccessor var3, Random var4, BoundingBox var5);

    @Override
    public void move(int param0, int param1, int param2) {
        super.move(param0, param1, param2);
        this.templatePosition = this.templatePosition.offset(param0, param1, param2);
    }

    @Override
    public Rotation getRotation() {
        return this.placeSettings.getRotation();
    }
}
