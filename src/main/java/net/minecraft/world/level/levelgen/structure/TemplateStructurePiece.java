package net.minecraft.world.level.levelgen.structure;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.logging.LogUtils;
import java.util.function.Function;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.StructureMode;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import org.slf4j.Logger;

public abstract class TemplateStructurePiece extends StructurePiece {
    private static final Logger LOGGER = LogUtils.getLogger();
    protected final String templateName;
    protected StructureTemplate template;
    protected StructurePlaceSettings placeSettings;
    protected BlockPos templatePosition;

    public TemplateStructurePiece(
        StructurePieceType param0,
        int param1,
        StructureTemplateManager param2,
        ResourceLocation param3,
        String param4,
        StructurePlaceSettings param5,
        BlockPos param6
    ) {
        super(param0, param1, param2.getOrCreate(param3).getBoundingBox(param5, param6));
        this.setOrientation(Direction.NORTH);
        this.templateName = param4;
        this.templatePosition = param6;
        this.template = param2.getOrCreate(param3);
        this.placeSettings = param5;
    }

    public TemplateStructurePiece(
        StructurePieceType param0, CompoundTag param1, StructureTemplateManager param2, Function<ResourceLocation, StructurePlaceSettings> param3
    ) {
        super(param0, param1);
        this.setOrientation(Direction.NORTH);
        this.templateName = param1.getString("Template");
        this.templatePosition = new BlockPos(param1.getInt("TPX"), param1.getInt("TPY"), param1.getInt("TPZ"));
        ResourceLocation var0 = this.makeTemplateLocation();
        this.template = param2.getOrCreate(var0);
        this.placeSettings = param3.apply(var0);
        this.boundingBox = this.template.getBoundingBox(this.placeSettings, this.templatePosition);
    }

    protected ResourceLocation makeTemplateLocation() {
        return new ResourceLocation(this.templateName);
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext param0, CompoundTag param1) {
        param1.putInt("TPX", this.templatePosition.getX());
        param1.putInt("TPY", this.templatePosition.getY());
        param1.putInt("TPZ", this.templatePosition.getZ());
        param1.putString("Template", this.templateName);
    }

    @Override
    public void postProcess(
        WorldGenLevel param0, StructureManager param1, ChunkGenerator param2, RandomSource param3, BoundingBox param4, ChunkPos param5, BlockPos param6
    ) {
        this.placeSettings.setBoundingBox(param4);
        this.boundingBox = this.template.getBoundingBox(this.placeSettings, this.templatePosition);
        if (this.template.placeInWorld(param0, this.templatePosition, param6, this.placeSettings, param3, 2)) {
            for(StructureTemplate.StructureBlockInfo var1 : this.template.filterBlocks(this.templatePosition, this.placeSettings, Blocks.STRUCTURE_BLOCK)) {
                if (var1.nbt() != null) {
                    StructureMode var2 = StructureMode.valueOf(var1.nbt().getString("mode"));
                    if (var2 == StructureMode.DATA) {
                        this.handleDataMarker(var1.nbt().getString("metadata"), var1.pos(), param0, param3, param4);
                    }
                }
            }

            for(StructureTemplate.StructureBlockInfo var4 : this.template.filterBlocks(this.templatePosition, this.placeSettings, Blocks.JIGSAW)) {
                if (var4.nbt() != null) {
                    String var5 = var4.nbt().getString("final_state");
                    BlockState var6 = Blocks.AIR.defaultBlockState();

                    try {
                        var6 = BlockStateParser.parseForBlock(param0.holderLookup(Registries.BLOCK), var5, true).blockState();
                    } catch (CommandSyntaxException var15) {
                        LOGGER.error("Error while parsing blockstate {} in jigsaw block @ {}", var5, var4.pos());
                    }

                    param0.setBlock(var4.pos(), var6, 3);
                }
            }
        }

    }

    protected abstract void handleDataMarker(String var1, BlockPos var2, ServerLevelAccessor var3, RandomSource var4, BoundingBox var5);

    @Deprecated
    @Override
    public void move(int param0, int param1, int param2) {
        super.move(param0, param1, param2);
        this.templatePosition = this.templatePosition.offset(param0, param1, param2);
    }

    @Override
    public Rotation getRotation() {
        return this.placeSettings.getRotation();
    }

    public StructureTemplate template() {
        return this.template;
    }

    public BlockPos templatePosition() {
        return this.templatePosition;
    }

    public StructurePlaceSettings placeSettings() {
        return this.placeSettings;
    }
}
