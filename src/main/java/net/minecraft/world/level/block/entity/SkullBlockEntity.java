package net.minecraft.world.level.block.entity;

import com.google.common.collect.Iterables;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.properties.Property;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.players.GameProfileCache;
import net.minecraft.util.StringUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class SkullBlockEntity extends BlockEntity {
    public static final String TAG_SKULL_OWNER = "SkullOwner";
    @Nullable
    private static GameProfileCache profileCache;
    @Nullable
    private static MinecraftSessionService sessionService;
    @Nullable
    private GameProfile owner;
    private int mouthTickCount;
    private boolean isMovingMouth;

    public SkullBlockEntity(BlockPos param0, BlockState param1) {
        super(BlockEntityType.SKULL, param0, param1);
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
    public void load(CompoundTag param0) {
        super.load(param0);
        if (param0.contains("SkullOwner", 10)) {
            this.setOwner(NbtUtils.readGameProfile(param0.getCompound("SkullOwner")));
        } else if (param0.contains("ExtraType", 8)) {
            String var0 = param0.getString("ExtraType");
            if (!StringUtil.isNullOrEmpty(var0)) {
                this.setOwner(new GameProfile(null, var0));
            }
        }

    }

    public static void dragonHeadAnimation(Level param0, BlockPos param1, BlockState param2, SkullBlockEntity param3) {
        if (param0.hasNeighborSignal(param1)) {
            param3.isMovingMouth = true;
            ++param3.mouthTickCount;
        } else {
            param3.isMovingMouth = false;
        }

    }

    public float getMouthAnimation(float param0) {
        return this.isMovingMouth ? (float)this.mouthTickCount + param0 : (float)this.mouthTickCount;
    }

    @Nullable
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

    @Nullable
    public static GameProfile updateGameprofile(@Nullable GameProfile param0) {
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
