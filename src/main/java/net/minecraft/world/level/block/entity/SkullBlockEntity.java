package net.minecraft.world.level.block.entity;

import com.google.common.collect.Iterables;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.properties.Property;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.Util;
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
    private static Executor mainThreadExecutor;
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

    public static void setMainThreadExecutor(Executor param0) {
        mainThreadExecutor = param0;
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
        synchronized(this) {
            this.owner = param0;
        }

        this.updateOwnerProfile();
    }

    private void updateOwnerProfile() {
        updateGameprofile(this.owner, param0 -> {
            this.owner = param0;
            this.setChanged();
        });
    }

    public static void updateGameprofile(@Nullable GameProfile param0, Consumer<GameProfile> param1) {
        if (param0 != null
            && !StringUtil.isNullOrEmpty(param0.getName())
            && (!param0.isComplete() || !param0.getProperties().containsKey("textures"))
            && profileCache != null
            && sessionService != null) {
            profileCache.getAsync(param0.getName(), param2 -> Util.backgroundExecutor().execute(() -> Util.ifElse(param2, param1x -> {
                        Property var0x = Iterables.getFirst(param1x.getProperties().get("textures"), null);
                        if (var0x == null) {
                            param1x = sessionService.fillProfileProperties(param1x, true);
                        }

                        GameProfile var1x = param1x;
                        mainThreadExecutor.execute(() -> {
                            profileCache.add(var1x);
                            param1.accept(var1x);
                        });
                    }, () -> mainThreadExecutor.execute(() -> param1.accept(param0)))));
        } else {
            param1.accept(param0);
        }
    }
}
