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
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.LockCode;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.BeaconMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.block.BeaconBeamBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class BeaconBlockEntity extends BlockEntity implements MenuProvider, TickableBlockEntity {
    public static final MobEffect[][] BEACON_EFFECTS = new MobEffect[][]{
        {MobEffects.MOVEMENT_SPEED, MobEffects.DIG_SPEED},
        {MobEffects.DAMAGE_RESISTANCE, MobEffects.JUMP},
        {MobEffects.DAMAGE_BOOST},
        {MobEffects.REGENERATION}
    };
    private static final Set<MobEffect> VALID_EFFECTS = Arrays.stream(BEACON_EFFECTS).flatMap(Arrays::stream).collect(Collectors.toSet());
    private List<BeaconBlockEntity.BeaconBeamSection> beamSections = Lists.newArrayList();
    private List<BeaconBlockEntity.BeaconBeamSection> checkingBeamSections = Lists.newArrayList();
    private int levels;
    private int lastCheckY = -1;
    @Nullable
    private MobEffect primaryPower;
    @Nullable
    private MobEffect secondaryPower;
    @Nullable
    private Component name;
    private LockCode lockKey = LockCode.NO_LOCK;
    private final ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int param0) {
            switch(param0) {
                case 0:
                    return BeaconBlockEntity.this.levels;
                case 1:
                    return MobEffect.getId(BeaconBlockEntity.this.primaryPower);
                case 2:
                    return MobEffect.getId(BeaconBlockEntity.this.secondaryPower);
                default:
                    return 0;
            }
        }

        @Override
        public void set(int param0, int param1) {
            switch(param0) {
                case 0:
                    BeaconBlockEntity.this.levels = param1;
                    break;
                case 1:
                    if (!BeaconBlockEntity.this.level.isClientSide && !BeaconBlockEntity.this.beamSections.isEmpty()) {
                        BeaconBlockEntity.this.playSound(SoundEvents.BEACON_POWER_SELECT);
                    }

                    BeaconBlockEntity.this.primaryPower = BeaconBlockEntity.getValidEffectById(param1);
                    break;
                case 2:
                    BeaconBlockEntity.this.secondaryPower = BeaconBlockEntity.getValidEffectById(param1);
            }

        }

        @Override
        public int getCount() {
            return 3;
        }
    };

    public BeaconBlockEntity() {
        super(BlockEntityType.BEACON);
    }

    @Override
    public void tick() {
        int var0 = this.worldPosition.getX();
        int var1 = this.worldPosition.getY();
        int var2 = this.worldPosition.getZ();
        BlockPos var3;
        if (this.lastCheckY < var1) {
            var3 = this.worldPosition;
            this.checkingBeamSections = Lists.newArrayList();
            this.lastCheckY = var3.getY() - 1;
        } else {
            var3 = new BlockPos(var0, this.lastCheckY + 1, var2);
        }

        BeaconBlockEntity.BeaconBeamSection var5 = this.checkingBeamSections.isEmpty()
            ? null
            : this.checkingBeamSections.get(this.checkingBeamSections.size() - 1);
        int var6 = this.level.getHeight(Heightmap.Types.WORLD_SURFACE, var0, var2);

        for(int var7 = 0; var7 < 10 && var3.getY() <= var6; ++var7) {
            BlockState var8 = this.level.getBlockState(var3);
            Block var9 = var8.getBlock();
            if (var9 instanceof BeaconBeamBlock) {
                float[] var10 = ((BeaconBeamBlock)var9).getColor().getTextureDiffuseColors();
                if (this.checkingBeamSections.size() <= 1) {
                    var5 = new BeaconBlockEntity.BeaconBeamSection(var10);
                    this.checkingBeamSections.add(var5);
                } else if (var5 != null) {
                    if (Arrays.equals(var10, var5.color)) {
                        var5.increaseHeight();
                    } else {
                        var5 = new BeaconBlockEntity.BeaconBeamSection(
                            new float[]{(var5.color[0] + var10[0]) / 2.0F, (var5.color[1] + var10[1]) / 2.0F, (var5.color[2] + var10[2]) / 2.0F}
                        );
                        this.checkingBeamSections.add(var5);
                    }
                }
            } else {
                if (var5 == null || var8.getLightBlock(this.level, var3) >= 15 && var9 != Blocks.BEDROCK) {
                    this.checkingBeamSections.clear();
                    this.lastCheckY = var6;
                    break;
                }

                var5.increaseHeight();
            }

            var3 = var3.above();
            ++this.lastCheckY;
        }

        int var11 = this.levels;
        if (this.level.getGameTime() % 80L == 0L) {
            if (!this.beamSections.isEmpty()) {
                this.updateBase(var0, var1, var2);
            }

            if (this.levels > 0 && !this.beamSections.isEmpty()) {
                this.applyEffects();
                this.playSound(SoundEvents.BEACON_AMBIENT);
            }
        }

        if (this.lastCheckY >= var6) {
            this.lastCheckY = -1;
            boolean var12 = var11 > 0;
            this.beamSections = this.checkingBeamSections;
            if (!this.level.isClientSide) {
                boolean var13 = this.levels > 0;
                if (!var12 && var13) {
                    this.playSound(SoundEvents.BEACON_ACTIVATE);

                    for(ServerPlayer var14 : this.level
                        .getEntitiesOfClass(
                            ServerPlayer.class,
                            new AABB((double)var0, (double)var1, (double)var2, (double)var0, (double)(var1 - 4), (double)var2).inflate(10.0, 5.0, 10.0)
                        )) {
                        CriteriaTriggers.CONSTRUCT_BEACON.trigger(var14, this);
                    }
                } else if (var12 && !var13) {
                    this.playSound(SoundEvents.BEACON_DEACTIVATE);
                }
            }
        }

    }

    private void updateBase(int param0, int param1, int param2) {
        this.levels = 0;

        for(int var0 = 1; var0 <= 4; this.levels = var0++) {
            int var1 = param1 - var0;
            if (var1 < 0) {
                break;
            }

            boolean var2 = true;

            for(int var3 = param0 - var0; var3 <= param0 + var0 && var2; ++var3) {
                for(int var4 = param2 - var0; var4 <= param2 + var0; ++var4) {
                    Block var5 = this.level.getBlockState(new BlockPos(var3, var1, var4)).getBlock();
                    if (var5 != Blocks.EMERALD_BLOCK && var5 != Blocks.GOLD_BLOCK && var5 != Blocks.DIAMOND_BLOCK && var5 != Blocks.IRON_BLOCK) {
                        var2 = false;
                        break;
                    }
                }
            }

            if (!var2) {
                break;
            }
        }

    }

    @Override
    public void setRemoved() {
        this.playSound(SoundEvents.BEACON_DEACTIVATE);
        super.setRemoved();
    }

    private void applyEffects() {
        if (!this.level.isClientSide && this.primaryPower != null) {
            double var0 = (double)(this.levels * 10 + 10);
            int var1 = 0;
            if (this.levels >= 4 && this.primaryPower == this.secondaryPower) {
                var1 = 1;
            }

            int var2 = (9 + this.levels * 2) * 20;
            AABB var3 = new AABB(this.worldPosition).inflate(var0).expandTowards(0.0, (double)this.level.getMaxBuildHeight(), 0.0);
            List<Player> var4 = this.level.getEntitiesOfClass(Player.class, var3);

            for(Player var5 : var4) {
                var5.addEffect(new MobEffectInstance(this.primaryPower, var2, var1, true, true));
            }

            if (this.levels >= 4 && this.primaryPower != this.secondaryPower && this.secondaryPower != null) {
                for(Player var6 : var4) {
                    var6.addEffect(new MobEffectInstance(this.secondaryPower, var2, 0, true, true));
                }
            }

        }
    }

    public void playSound(SoundEvent param0) {
        this.level.playSound(null, this.worldPosition, param0, SoundSource.BLOCKS, 1.0F, 1.0F);
    }

    @OnlyIn(Dist.CLIENT)
    public List<BeaconBlockEntity.BeaconBeamSection> getBeamSections() {
        return (List<BeaconBlockEntity.BeaconBeamSection>)(this.levels == 0 ? ImmutableList.of() : this.beamSections);
    }

    public int getLevels() {
        return this.levels;
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return new ClientboundBlockEntityDataPacket(this.worldPosition, 3, this.getUpdateTag());
    }

    @Override
    public CompoundTag getUpdateTag() {
        return this.save(new CompoundTag());
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public double getViewDistance() {
        return 65536.0;
    }

    @Nullable
    private static MobEffect getValidEffectById(int param0) {
        MobEffect var0 = MobEffect.byId(param0);
        return VALID_EFFECTS.contains(var0) ? var0 : null;
    }

    @Override
    public void load(CompoundTag param0) {
        super.load(param0);
        this.primaryPower = getValidEffectById(param0.getInt("Primary"));
        this.secondaryPower = getValidEffectById(param0.getInt("Secondary"));
        if (param0.contains("CustomName", 8)) {
            this.name = Component.Serializer.fromJson(param0.getString("CustomName"));
        }

        this.lockKey = LockCode.fromTag(param0);
    }

    @Override
    public CompoundTag save(CompoundTag param0) {
        super.save(param0);
        param0.putInt("Primary", MobEffect.getId(this.primaryPower));
        param0.putInt("Secondary", MobEffect.getId(this.secondaryPower));
        param0.putInt("Levels", this.levels);
        if (this.name != null) {
            param0.putString("CustomName", Component.Serializer.toJson(this.name));
        }

        this.lockKey.addToTag(param0);
        return param0;
    }

    public void setCustomName(@Nullable Component param0) {
        this.name = param0;
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
        return (Component)(this.name != null ? this.name : new TranslatableComponent("container.beacon"));
    }

    public static class BeaconBeamSection {
        private final float[] color;
        private int height;

        public BeaconBeamSection(float[] param0) {
            this.color = param0;
            this.height = 1;
        }

        protected void increaseHeight() {
            ++this.height;
        }

        @OnlyIn(Dist.CLIENT)
        public float[] getColor() {
            return this.color;
        }

        @OnlyIn(Dist.CLIENT)
        public int getHeight() {
            return this.height;
        }
    }
}
