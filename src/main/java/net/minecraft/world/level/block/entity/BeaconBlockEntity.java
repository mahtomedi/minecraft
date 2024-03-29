package net.minecraft.world.level.block.entity;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.LockCode;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.Nameable;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.BeaconMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BeaconBeamBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;

public class BeaconBlockEntity extends BlockEntity implements MenuProvider, Nameable {
    private static final int MAX_LEVELS = 4;
    public static final MobEffect[][] BEACON_EFFECTS = new MobEffect[][]{
        {MobEffects.MOVEMENT_SPEED, MobEffects.DIG_SPEED},
        {MobEffects.DAMAGE_RESISTANCE, MobEffects.JUMP},
        {MobEffects.DAMAGE_BOOST},
        {MobEffects.REGENERATION}
    };
    private static final Set<MobEffect> VALID_EFFECTS = Arrays.stream(BEACON_EFFECTS).flatMap(Arrays::stream).collect(Collectors.toSet());
    public static final int DATA_LEVELS = 0;
    public static final int DATA_PRIMARY = 1;
    public static final int DATA_SECONDARY = 2;
    public static final int NUM_DATA_VALUES = 3;
    private static final int BLOCKS_CHECK_PER_TICK = 10;
    private static final Component DEFAULT_NAME = Component.translatable("container.beacon");
    private static final String TAG_PRIMARY = "primary_effect";
    private static final String TAG_SECONDARY = "secondary_effect";
    List<BeaconBlockEntity.BeaconBeamSection> beamSections = Lists.newArrayList();
    private List<BeaconBlockEntity.BeaconBeamSection> checkingBeamSections = Lists.newArrayList();
    int levels;
    private int lastCheckY;
    @Nullable
    MobEffect primaryPower;
    @Nullable
    MobEffect secondaryPower;
    @Nullable
    private Component name;
    private LockCode lockKey = LockCode.NO_LOCK;
    private final ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int param0) {
            return switch(param0) {
                case 0 -> BeaconBlockEntity.this.levels;
                case 1 -> BeaconMenu.encodeEffect(BeaconBlockEntity.this.primaryPower);
                case 2 -> BeaconMenu.encodeEffect(BeaconBlockEntity.this.secondaryPower);
                default -> 0;
            };
        }

        @Override
        public void set(int param0, int param1) {
            switch(param0) {
                case 0:
                    BeaconBlockEntity.this.levels = param1;
                    break;
                case 1:
                    if (!BeaconBlockEntity.this.level.isClientSide && !BeaconBlockEntity.this.beamSections.isEmpty()) {
                        BeaconBlockEntity.playSound(BeaconBlockEntity.this.level, BeaconBlockEntity.this.worldPosition, SoundEvents.BEACON_POWER_SELECT);
                    }

                    BeaconBlockEntity.this.primaryPower = BeaconBlockEntity.filterEffect(BeaconMenu.decodeEffect(param1));
                    break;
                case 2:
                    BeaconBlockEntity.this.secondaryPower = BeaconBlockEntity.filterEffect(BeaconMenu.decodeEffect(param1));
            }

        }

        @Override
        public int getCount() {
            return 3;
        }
    };

    @Nullable
    static MobEffect filterEffect(@Nullable MobEffect param0) {
        return VALID_EFFECTS.contains(param0) ? param0 : null;
    }

    public BeaconBlockEntity(BlockPos param0, BlockState param1) {
        super(BlockEntityType.BEACON, param0, param1);
    }

    public static void tick(Level param0, BlockPos param1, BlockState param2, BeaconBlockEntity param3) {
        int var0 = param1.getX();
        int var1 = param1.getY();
        int var2 = param1.getZ();
        BlockPos var3;
        if (param3.lastCheckY < var1) {
            var3 = param1;
            param3.checkingBeamSections = Lists.newArrayList();
            param3.lastCheckY = param1.getY() - 1;
        } else {
            var3 = new BlockPos(var0, param3.lastCheckY + 1, var2);
        }

        BeaconBlockEntity.BeaconBeamSection var5 = param3.checkingBeamSections.isEmpty()
            ? null
            : param3.checkingBeamSections.get(param3.checkingBeamSections.size() - 1);
        int var6 = param0.getHeight(Heightmap.Types.WORLD_SURFACE, var0, var2);

        for(int var7 = 0; var7 < 10 && var3.getY() <= var6; ++var7) {
            BlockState var8 = param0.getBlockState(var3);
            Block var9 = var8.getBlock();
            if (var9 instanceof BeaconBeamBlock) {
                float[] var10 = ((BeaconBeamBlock)var9).getColor().getTextureDiffuseColors();
                if (param3.checkingBeamSections.size() <= 1) {
                    var5 = new BeaconBlockEntity.BeaconBeamSection(var10);
                    param3.checkingBeamSections.add(var5);
                } else if (var5 != null) {
                    if (Arrays.equals(var10, var5.color)) {
                        var5.increaseHeight();
                    } else {
                        var5 = new BeaconBlockEntity.BeaconBeamSection(
                            new float[]{(var5.color[0] + var10[0]) / 2.0F, (var5.color[1] + var10[1]) / 2.0F, (var5.color[2] + var10[2]) / 2.0F}
                        );
                        param3.checkingBeamSections.add(var5);
                    }
                }
            } else {
                if (var5 == null || var8.getLightBlock(param0, var3) >= 15 && !var8.is(Blocks.BEDROCK)) {
                    param3.checkingBeamSections.clear();
                    param3.lastCheckY = var6;
                    break;
                }

                var5.increaseHeight();
            }

            var3 = var3.above();
            ++param3.lastCheckY;
        }

        int var11 = param3.levels;
        if (param0.getGameTime() % 80L == 0L) {
            if (!param3.beamSections.isEmpty()) {
                param3.levels = updateBase(param0, var0, var1, var2);
            }

            if (param3.levels > 0 && !param3.beamSections.isEmpty()) {
                applyEffects(param0, param1, param3.levels, param3.primaryPower, param3.secondaryPower);
                playSound(param0, param1, SoundEvents.BEACON_AMBIENT);
            }
        }

        if (param3.lastCheckY >= var6) {
            param3.lastCheckY = param0.getMinBuildHeight() - 1;
            boolean var12 = var11 > 0;
            param3.beamSections = param3.checkingBeamSections;
            if (!param0.isClientSide) {
                boolean var13 = param3.levels > 0;
                if (!var12 && var13) {
                    playSound(param0, param1, SoundEvents.BEACON_ACTIVATE);

                    for(ServerPlayer var14 : param0.getEntitiesOfClass(
                        ServerPlayer.class,
                        new AABB((double)var0, (double)var1, (double)var2, (double)var0, (double)(var1 - 4), (double)var2).inflate(10.0, 5.0, 10.0)
                    )) {
                        CriteriaTriggers.CONSTRUCT_BEACON.trigger(var14, param3.levels);
                    }
                } else if (var12 && !var13) {
                    playSound(param0, param1, SoundEvents.BEACON_DEACTIVATE);
                }
            }
        }

    }

    private static int updateBase(Level param0, int param1, int param2, int param3) {
        int var0 = 0;

        for(int var1 = 1; var1 <= 4; var0 = var1++) {
            int var2 = param2 - var1;
            if (var2 < param0.getMinBuildHeight()) {
                break;
            }

            boolean var3 = true;

            for(int var4 = param1 - var1; var4 <= param1 + var1 && var3; ++var4) {
                for(int var5 = param3 - var1; var5 <= param3 + var1; ++var5) {
                    if (!param0.getBlockState(new BlockPos(var4, var2, var5)).is(BlockTags.BEACON_BASE_BLOCKS)) {
                        var3 = false;
                        break;
                    }
                }
            }

            if (!var3) {
                break;
            }
        }

        return var0;
    }

    @Override
    public void setRemoved() {
        playSound(this.level, this.worldPosition, SoundEvents.BEACON_DEACTIVATE);
        super.setRemoved();
    }

    private static void applyEffects(Level param0, BlockPos param1, int param2, @Nullable MobEffect param3, @Nullable MobEffect param4) {
        if (!param0.isClientSide && param3 != null) {
            double var0 = (double)(param2 * 10 + 10);
            int var1 = 0;
            if (param2 >= 4 && param3 == param4) {
                var1 = 1;
            }

            int var2 = (9 + param2 * 2) * 20;
            AABB var3 = new AABB(param1).inflate(var0).expandTowards(0.0, (double)param0.getHeight(), 0.0);
            List<Player> var4 = param0.getEntitiesOfClass(Player.class, var3);

            for(Player var5 : var4) {
                var5.addEffect(new MobEffectInstance(param3, var2, var1, true, true));
            }

            if (param2 >= 4 && param3 != param4 && param4 != null) {
                for(Player var6 : var4) {
                    var6.addEffect(new MobEffectInstance(param4, var2, 0, true, true));
                }
            }

        }
    }

    public static void playSound(Level param0, BlockPos param1, SoundEvent param2) {
        param0.playSound(null, param1, param2, SoundSource.BLOCKS, 1.0F, 1.0F);
    }

    public List<BeaconBlockEntity.BeaconBeamSection> getBeamSections() {
        return (List<BeaconBlockEntity.BeaconBeamSection>)(this.levels == 0 ? ImmutableList.of() : this.beamSections);
    }

    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return this.saveWithoutMetadata();
    }

    private static void storeEffect(CompoundTag param0, String param1, @Nullable MobEffect param2) {
        if (param2 != null) {
            ResourceLocation var0 = BuiltInRegistries.MOB_EFFECT.getKey(param2);
            if (var0 != null) {
                param0.putString(param1, var0.toString());
            }
        }

    }

    @Nullable
    private static MobEffect loadEffect(CompoundTag param0, String param1) {
        if (param0.contains(param1, 8)) {
            ResourceLocation var0 = ResourceLocation.tryParse(param0.getString(param1));
            return filterEffect(BuiltInRegistries.MOB_EFFECT.get(var0));
        } else {
            return null;
        }
    }

    @Override
    public void load(CompoundTag param0) {
        super.load(param0);
        this.primaryPower = loadEffect(param0, "primary_effect");
        this.secondaryPower = loadEffect(param0, "secondary_effect");
        if (param0.contains("CustomName", 8)) {
            this.name = Component.Serializer.fromJson(param0.getString("CustomName"));
        }

        this.lockKey = LockCode.fromTag(param0);
    }

    @Override
    protected void saveAdditional(CompoundTag param0) {
        super.saveAdditional(param0);
        storeEffect(param0, "primary_effect", this.primaryPower);
        storeEffect(param0, "secondary_effect", this.secondaryPower);
        param0.putInt("Levels", this.levels);
        if (this.name != null) {
            param0.putString("CustomName", Component.Serializer.toJson(this.name));
        }

        this.lockKey.addToTag(param0);
    }

    public void setCustomName(@Nullable Component param0) {
        this.name = param0;
    }

    @Nullable
    @Override
    public Component getCustomName() {
        return this.name;
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int param0, Inventory param1, Player param2) {
        return BaseContainerBlockEntity.canUnlock(param2, this.lockKey, this.getDisplayName())
            ? new BeaconMenu(param0, param1, this.dataAccess, ContainerLevelAccess.create(this.level, this.getBlockPos()))
            : null;
    }

    @Override
    public Component getDisplayName() {
        return this.getName();
    }

    @Override
    public Component getName() {
        return this.name != null ? this.name : DEFAULT_NAME;
    }

    @Override
    public void setLevel(Level param0) {
        super.setLevel(param0);
        this.lastCheckY = param0.getMinBuildHeight() - 1;
    }

    public static class BeaconBeamSection {
        final float[] color;
        private int height;

        public BeaconBeamSection(float[] param0) {
            this.color = param0;
            this.height = 1;
        }

        protected void increaseHeight() {
            ++this.height;
        }

        public float[] getColor() {
            return this.color;
        }

        public int getHeight() {
            return this.height;
        }
    }
}
