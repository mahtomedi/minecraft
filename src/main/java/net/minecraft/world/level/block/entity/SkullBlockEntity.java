package net.minecraft.world.level.block.entity;

import com.google.common.collect.Iterables;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.properties.Property;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.players.GameProfileCache;
import net.minecraft.util.StringUtil;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class SkullBlockEntity extends BlockEntity implements TickableBlockEntity {
    private GameProfile owner;
    private int mouthTickCount;
    private boolean isMovingMouth;
    private static GameProfileCache profileCache;
    private static MinecraftSessionService sessionService;

    public SkullBlockEntity() {
        super(BlockEntityType.SKULL);
    }

    public static void setProfileCache(GameProfileCache param0) {
        profileCache = param0;
    }

    public static void setSessionService(MinecraftSessionService param0) {
        sessionService = param0;
    }

    @Override
    public CompoundTag save(CompoundTag param0) {
        super.save(param0);
        if (this.owner != null) {
            CompoundTag var0 = new CompoundTag();
            NbtUtils.writeGameProfile(var0, this.owner);
            param0.put("SkullOwner", var0);
        }

        return param0;
    }

    @Override
    public void load(BlockState param0, CompoundTag param1) {
        super.load(param0, param1);
        if (param1.contains("SkullOwner", 10)) {
            this.setOwner(NbtUtils.readGameProfile(param1.getCompound("SkullOwner")));
        } else if (param1.contains("ExtraType", 8)) {
            String var0 = param1.getString("ExtraType");
            if (!StringUtil.isNullOrEmpty(var0)) {
                this.setOwner(new GameProfile(null, var0));
            }
        }

    }

    @Override
    public void tick() {
        BlockState var0 = this.getBlockState();
        if (var0.is(Blocks.DRAGON_HEAD) || var0.is(Blocks.DRAGON_WALL_HEAD)) {
            if (this.level.hasNeighborSignal(this.worldPosition)) {
                this.isMovingMouth = true;
                ++this.mouthTickCount;
            } else {
                this.isMovingMouth = false;
            }
        }

    }

    @OnlyIn(Dist.CLIENT)
    public float getMouthAnimation(float param0) {
        return this.isMovingMouth ? (float)this.mouthTickCount + param0 : (float)this.mouthTickCount;
    }

    @Nullable
    @OnlyIn(Dist.CLIENT)
    public GameProfile getOwnerProfile() {
        return this.owner;
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return new ClientboundBlockEntityDataPacket(this.worldPosition, 4, this.getUpdateTag());
    }

    @Override
    public CompoundTag getUpdateTag() {
        return this.save(new CompoundTag());
    }

    public void setOwner(@Nullable GameProfile param0) {
        this.owner = param0;
        this.updateOwnerProfile();
    }

    private void updateOwnerProfile() {
        this.owner = updateGameprofile(this.owner);
        this.setChanged();
    }

    public static GameProfile updateGameprofile(GameProfile param0) {
        if (param0 != null && !StringUtil.isNullOrEmpty(param0.getName())) {
            if (param0.isComplete() && param0.getProperties().containsKey("textures")) {
                return param0;
            } else if (profileCache != null && sessionService != null) {
                GameProfile var0 = profileCache.get(param0.getName());
                if (var0 == null) {
                    return param0;
                } else {
                    Property var1 = Iterables.getFirst(var0.getProperties().get("textures"), null);
                    if (var1 == null) {
                        var0 = sessionService.fillProfileProperties(var0, true);
                    }

                    return var0;
                }
            } else {
                return param0;
            }
        } else {
            return param0;
        }
    }
}
