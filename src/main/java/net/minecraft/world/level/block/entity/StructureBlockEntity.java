package net.minecraft.world.level.block.entity;

import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.ResourceLocationException;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringUtil;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.StructureBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.StructureMode;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockRotProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

public class StructureBlockEntity extends BlockEntity {
    private static final int SCAN_CORNER_BLOCKS_RANGE = 5;
    public static final int MAX_OFFSET_PER_AXIS = 48;
    public static final int MAX_SIZE_PER_AXIS = 48;
    public static final String AUTHOR_TAG = "author";
    @Nullable
    private ResourceLocation structureName;
    private String author = "";
    private String metaData = "";
    private BlockPos structurePos = new BlockPos(0, 1, 0);
    private Vec3i structureSize = Vec3i.ZERO;
    private Mirror mirror = Mirror.NONE;
    private Rotation rotation = Rotation.NONE;
    private StructureMode mode;
    private boolean ignoreEntities = true;
    private boolean powered;
    private boolean showAir;
    private boolean showBoundingBox = true;
    private float integrity = 1.0F;
    private long seed;

    public StructureBlockEntity(BlockPos param0, BlockState param1) {
        super(BlockEntityType.STRUCTURE_BLOCK, param0, param1);
        this.mode = param1.getValue(StructureBlock.MODE);
    }

    @Override
    protected void saveAdditional(CompoundTag param0) {
        super.saveAdditional(param0);
        param0.putString("name", this.getStructureName());
        param0.putString("author", this.author);
        param0.putString("metadata", this.metaData);
        param0.putInt("posX", this.structurePos.getX());
        param0.putInt("posY", this.structurePos.getY());
        param0.putInt("posZ", this.structurePos.getZ());
        param0.putInt("sizeX", this.structureSize.getX());
        param0.putInt("sizeY", this.structureSize.getY());
        param0.putInt("sizeZ", this.structureSize.getZ());
        param0.putString("rotation", this.rotation.toString());
        param0.putString("mirror", this.mirror.toString());
        param0.putString("mode", this.mode.toString());
        param0.putBoolean("ignoreEntities", this.ignoreEntities);
        param0.putBoolean("powered", this.powered);
        param0.putBoolean("showair", this.showAir);
        param0.putBoolean("showboundingbox", this.showBoundingBox);
        param0.putFloat("integrity", this.integrity);
        param0.putLong("seed", this.seed);
    }

    @Override
    public void load(CompoundTag param0) {
        super.load(param0);
        this.setStructureName(param0.getString("name"));
        this.author = param0.getString("author");
        this.metaData = param0.getString("metadata");
        int var0 = Mth.clamp(param0.getInt("posX"), -48, 48);
        int var1 = Mth.clamp(param0.getInt("posY"), -48, 48);
        int var2 = Mth.clamp(param0.getInt("posZ"), -48, 48);
        this.structurePos = new BlockPos(var0, var1, var2);
        int var3 = Mth.clamp(param0.getInt("sizeX"), 0, 48);
        int var4 = Mth.clamp(param0.getInt("sizeY"), 0, 48);
        int var5 = Mth.clamp(param0.getInt("sizeZ"), 0, 48);
        this.structureSize = new Vec3i(var3, var4, var5);

        try {
            this.rotation = Rotation.valueOf(param0.getString("rotation"));
        } catch (IllegalArgumentException var11) {
            this.rotation = Rotation.NONE;
        }

        try {
            this.mirror = Mirror.valueOf(param0.getString("mirror"));
        } catch (IllegalArgumentException var10) {
            this.mirror = Mirror.NONE;
        }

        try {
            this.mode = StructureMode.valueOf(param0.getString("mode"));
        } catch (IllegalArgumentException var9) {
            this.mode = StructureMode.DATA;
        }

        this.ignoreEntities = param0.getBoolean("ignoreEntities");
        this.powered = param0.getBoolean("powered");
        this.showAir = param0.getBoolean("showair");
        this.showBoundingBox = param0.getBoolean("showboundingbox");
        if (param0.contains("integrity")) {
            this.integrity = param0.getFloat("integrity");
        } else {
            this.integrity = 1.0F;
        }

        this.seed = param0.getLong("seed");
        this.updateBlockState();
    }

    private void updateBlockState() {
        if (this.level != null) {
            BlockPos var0 = this.getBlockPos();
            BlockState var1 = this.level.getBlockState(var0);
            if (var1.is(Blocks.STRUCTURE_BLOCK)) {
                this.level.setBlock(var0, var1.setValue(StructureBlock.MODE, this.mode), 2);
            }

        }
    }

    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return this.saveWithoutMetadata();
    }

    public boolean usedBy(Player param0) {
        if (!param0.canUseGameMasterBlocks()) {
            return false;
        } else {
            if (param0.getCommandSenderWorld().isClientSide) {
                param0.openStructureBlock(this);
            }

            return true;
        }
    }

    public String getStructureName() {
        return this.structureName == null ? "" : this.structureName.toString();
    }

    public boolean hasStructureName() {
        return this.structureName != null;
    }

    public void setStructureName(@Nullable String param0) {
        this.setStructureName(StringUtil.isNullOrEmpty(param0) ? null : ResourceLocation.tryParse(param0));
    }

    public void setStructureName(@Nullable ResourceLocation param0) {
        this.structureName = param0;
    }

    public void createdBy(LivingEntity param0) {
        this.author = param0.getName().getString();
    }

    public BlockPos getStructurePos() {
        return this.structurePos;
    }

    public void setStructurePos(BlockPos param0) {
        this.structurePos = param0;
    }

    public Vec3i getStructureSize() {
        return this.structureSize;
    }

    public void setStructureSize(Vec3i param0) {
        this.structureSize = param0;
    }

    public Mirror getMirror() {
        return this.mirror;
    }

    public void setMirror(Mirror param0) {
        this.mirror = param0;
    }

    public Rotation getRotation() {
        return this.rotation;
    }

    public void setRotation(Rotation param0) {
        this.rotation = param0;
    }

    public String getMetaData() {
        return this.metaData;
    }

    public void setMetaData(String param0) {
        this.metaData = param0;
    }

    public StructureMode getMode() {
        return this.mode;
    }

    public void setMode(StructureMode param0) {
        this.mode = param0;
        BlockState var0 = this.level.getBlockState(this.getBlockPos());
        if (var0.is(Blocks.STRUCTURE_BLOCK)) {
            this.level.setBlock(this.getBlockPos(), var0.setValue(StructureBlock.MODE, param0), 2);
        }

    }

    public boolean isIgnoreEntities() {
        return this.ignoreEntities;
    }

    public void setIgnoreEntities(boolean param0) {
        this.ignoreEntities = param0;
    }

    public float getIntegrity() {
        return this.integrity;
    }

    public void setIntegrity(float param0) {
        this.integrity = param0;
    }

    public long getSeed() {
        return this.seed;
    }

    public void setSeed(long param0) {
        this.seed = param0;
    }

    public boolean detectSize() {
        if (this.mode != StructureMode.SAVE) {
            return false;
        } else {
            BlockPos var0 = this.getBlockPos();
            int var1 = 80;
            BlockPos var2 = new BlockPos(var0.getX() - 80, this.level.getMinBuildHeight(), var0.getZ() - 80);
            BlockPos var3 = new BlockPos(var0.getX() + 80, this.level.getMaxBuildHeight() - 1, var0.getZ() + 80);
            Stream<BlockPos> var4 = this.getRelatedCorners(var2, var3);
            return calculateEnclosingBoundingBox(var0, var4).filter(param1 -> {
                int var0x = param1.maxX() - param1.minX();
                int var1x = param1.maxY() - param1.minY();
                int var2x = param1.maxZ() - param1.minZ();
                if (var0x > 1 && var1x > 1 && var2x > 1) {
                    this.structurePos = new BlockPos(param1.minX() - var0.getX() + 1, param1.minY() - var0.getY() + 1, param1.minZ() - var0.getZ() + 1);
                    this.structureSize = new Vec3i(var0x - 1, var1x - 1, var2x - 1);
                    this.setChanged();
                    BlockState var3x = this.level.getBlockState(var0);
                    this.level.sendBlockUpdated(var0, var3x, var3x, 3);
                    return true;
                } else {
                    return false;
                }
            }).isPresent();
        }
    }

    private Stream<BlockPos> getRelatedCorners(BlockPos param0, BlockPos param1) {
        return BlockPos.betweenClosedStream(param0, param1)
            .filter(param0x -> this.level.getBlockState(param0x).is(Blocks.STRUCTURE_BLOCK))
            .map(this.level::getBlockEntity)
            .filter(param0x -> param0x instanceof StructureBlockEntity)
            .map(param0x -> (StructureBlockEntity)param0x)
            .filter(param0x -> param0x.mode == StructureMode.CORNER && Objects.equals(this.structureName, param0x.structureName))
            .map(BlockEntity::getBlockPos);
    }

    private static Optional<BoundingBox> calculateEnclosingBoundingBox(BlockPos param0, Stream<BlockPos> param1) {
        Iterator<BlockPos> var0 = param1.iterator();
        if (!var0.hasNext()) {
            return Optional.empty();
        } else {
            BlockPos var1 = var0.next();
            BoundingBox var2 = new BoundingBox(var1);
            if (var0.hasNext()) {
                var0.forEachRemaining(var2::encapsulate);
            } else {
                var2.encapsulate(param0);
            }

            return Optional.of(var2);
        }
    }

    public boolean saveStructure() {
        return this.mode != StructureMode.SAVE ? false : this.saveStructure(true);
    }

    public boolean saveStructure(boolean param0) {
        if (this.structureName == null) {
            return false;
        } else {
            BlockPos var0 = this.getBlockPos().offset(this.structurePos);
            ServerLevel var1 = (ServerLevel)this.level;
            StructureTemplateManager var2 = var1.getStructureManager();

            StructureTemplate var3;
            try {
                var3 = var2.getOrCreate(this.structureName);
            } catch (ResourceLocationException var8) {
                return false;
            }

            var3.fillFromWorld(this.level, var0, this.structureSize, !this.ignoreEntities, Blocks.STRUCTURE_VOID);
            var3.setAuthor(this.author);
            if (param0) {
                try {
                    return var2.save(this.structureName);
                } catch (ResourceLocationException var7) {
                    return false;
                }
            } else {
                return true;
            }
        }
    }

    public static RandomSource createRandom(long param0) {
        return param0 == 0L ? RandomSource.create(Util.getMillis()) : RandomSource.create(param0);
    }

    public boolean placeStructureIfSameSize(ServerLevel param0) {
        if (this.mode == StructureMode.LOAD && this.structureName != null) {
            StructureTemplate var0 = param0.getStructureManager().get(this.structureName).orElse(null);
            if (var0 == null) {
                return false;
            } else if (var0.getSize().equals(this.structureSize)) {
                this.placeStructure(param0, var0);
                return true;
            } else {
                this.loadStructureInfo(var0);
                return false;
            }
        } else {
            return false;
        }
    }

    public boolean loadStructureInfo(ServerLevel param0) {
        StructureTemplate var0 = this.getStructureTemplate(param0);
        if (var0 == null) {
            return false;
        } else {
            this.loadStructureInfo(var0);
            return true;
        }
    }

    private void loadStructureInfo(StructureTemplate param0) {
        this.author = !StringUtil.isNullOrEmpty(param0.getAuthor()) ? param0.getAuthor() : "";
        this.structureSize = param0.getSize();
        this.setChanged();
    }

    public void placeStructure(ServerLevel param0) {
        StructureTemplate var0 = this.getStructureTemplate(param0);
        if (var0 != null) {
            this.placeStructure(param0, var0);
        }

    }

    @Nullable
    private StructureTemplate getStructureTemplate(ServerLevel param0) {
        return this.structureName == null ? null : param0.getStructureManager().get(this.structureName).orElse(null);
    }

    private void placeStructure(ServerLevel param0, StructureTemplate param1) {
        this.loadStructureInfo(param1);
        StructurePlaceSettings var0 = new StructurePlaceSettings().setMirror(this.mirror).setRotation(this.rotation).setIgnoreEntities(this.ignoreEntities);
        if (this.integrity < 1.0F) {
            var0.clearProcessors().addProcessor(new BlockRotProcessor(Mth.clamp(this.integrity, 0.0F, 1.0F))).setRandom(createRandom(this.seed));
        }

        BlockPos var1 = this.getBlockPos().offset(this.structurePos);
        param1.placeInWorld(param0, var1, var1, var0, createRandom(this.seed), 2);
    }

    public void unloadStructure() {
        if (this.structureName != null) {
            ServerLevel var0 = (ServerLevel)this.level;
            StructureTemplateManager var1 = var0.getStructureManager();
            var1.remove(this.structureName);
        }
    }

    public boolean isStructureLoadable() {
        if (this.mode == StructureMode.LOAD && !this.level.isClientSide && this.structureName != null) {
            ServerLevel var0 = (ServerLevel)this.level;
            StructureTemplateManager var1 = var0.getStructureManager();

            try {
                return var1.get(this.structureName).isPresent();
            } catch (ResourceLocationException var4) {
                return false;
            }
        } else {
            return false;
        }
    }

    public boolean isPowered() {
        return this.powered;
    }

    public void setPowered(boolean param0) {
        this.powered = param0;
    }

    public boolean getShowAir() {
        return this.showAir;
    }

    public void setShowAir(boolean param0) {
        this.showAir = param0;
    }

    public boolean getShowBoundingBox() {
        return this.showBoundingBox;
    }

    public void setShowBoundingBox(boolean param0) {
        this.showBoundingBox = param0;
    }

    public static enum UpdateType {
        UPDATE_DATA,
        SAVE_AREA,
        LOAD_AREA,
        SCAN_AREA;
    }
}
