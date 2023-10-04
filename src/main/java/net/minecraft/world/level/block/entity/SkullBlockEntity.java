package net.minecraft.world.level.block.entity;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.yggdrasil.ProfileResult;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BooleanSupplier;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.Services;
import net.minecraft.util.StringUtil;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.state.BlockState;

public class SkullBlockEntity extends BlockEntity {
    public static final String TAG_SKULL_OWNER = "SkullOwner";
    public static final String TAG_NOTE_BLOCK_SOUND = "note_block_sound";
    @Nullable
    private static Executor mainThreadExecutor;
    @Nullable
    private static LoadingCache<String, CompletableFuture<Optional<GameProfile>>> profileCache;
    private static final Executor CHECKED_MAIN_THREAD_EXECUTOR = param0 -> {
        Executor var0 = mainThreadExecutor;
        if (var0 != null) {
            var0.execute(param0);
        }

    };
    @Nullable
    private GameProfile owner;
    @Nullable
    private ResourceLocation noteBlockSound;
    private int animationTickCount;
    private boolean isAnimating;

    public SkullBlockEntity(BlockPos param0, BlockState param1) {
        super(BlockEntityType.SKULL, param0, param1);
    }

    public static void setup(final Services param0, Executor param1) {
        mainThreadExecutor = param1;
        final BooleanSupplier var0 = () -> profileCache == null;
        profileCache = CacheBuilder.newBuilder()
            .expireAfterAccess(Duration.ofMinutes(10L))
            .maximumSize(256L)
            .build(new CacheLoader<String, CompletableFuture<Optional<GameProfile>>>() {
                public CompletableFuture<Optional<GameProfile>> load(String param0x) {
                    return var0.getAsBoolean() ? CompletableFuture.completedFuture(Optional.empty()) : SkullBlockEntity.loadProfile(param0, param0, var0);
                }
            });
    }

    public static void clear() {
        mainThreadExecutor = null;
        profileCache = null;
    }

    static CompletableFuture<Optional<GameProfile>> loadProfile(String param0, Services param1, BooleanSupplier param2) {
        return param1.profileCache().getAsync(param0).thenApplyAsync(param2x -> {
            if (param2x.isPresent() && !param2.getAsBoolean()) {
                UUID var0x = param2x.get().getId();
                ProfileResult var1x = param1.sessionService().fetchProfile(var0x, true);
                return var1x != null ? Optional.ofNullable(var1x.profile()) : param2x;
            } else {
                return Optional.empty();
            }
        }, Util.backgroundExecutor());
    }

    @Override
    protected void saveAdditional(CompoundTag param0) {
        super.saveAdditional(param0);
        if (this.owner != null) {
            CompoundTag var0 = new CompoundTag();
            NbtUtils.writeGameProfile(var0, this.owner);
            param0.put("SkullOwner", var0);
        }

        if (this.noteBlockSound != null) {
            param0.putString("note_block_sound", this.noteBlockSound.toString());
        }

    }

    @Override
    public void load(CompoundTag param0) {
        super.load(param0);
        if (param0.contains("SkullOwner", 10)) {
            this.setOwner(NbtUtils.readGameProfile(param0.getCompound("SkullOwner")));
        } else if (param0.contains("ExtraType", 8)) {
            String var0 = param0.getString("ExtraType");
            if (!StringUtil.isNullOrEmpty(var0)) {
                this.setOwner(new GameProfile(Util.NIL_UUID, var0));
            }
        }

        if (param0.contains("note_block_sound", 8)) {
            this.noteBlockSound = ResourceLocation.tryParse(param0.getString("note_block_sound"));
        }

    }

    public static void animation(Level param0, BlockPos param1, BlockState param2, SkullBlockEntity param3) {
        if (param2.hasProperty(SkullBlock.POWERED) && param2.getValue(SkullBlock.POWERED)) {
            param3.isAnimating = true;
            ++param3.animationTickCount;
        } else {
            param3.isAnimating = false;
        }

    }

    public float getAnimation(float param0) {
        return this.isAnimating ? (float)this.animationTickCount + param0 : (float)this.animationTickCount;
    }

    @Nullable
    public GameProfile getOwnerProfile() {
        return this.owner;
    }

    @Nullable
    public ResourceLocation getNoteBlockSound() {
        return this.noteBlockSound;
    }

    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return this.saveWithoutMetadata();
    }

    public void setOwner(@Nullable GameProfile param0) {
        synchronized(this) {
            this.owner = param0;
        }

        this.updateOwnerProfile();
    }

    private void updateOwnerProfile() {
        if (this.owner != null && !Util.isBlank(this.owner.getName()) && !hasTextures(this.owner)) {
            fetchGameProfile(this.owner.getName()).thenAcceptAsync(param0 -> {
                this.owner = param0.orElse(this.owner);
                this.setChanged();
            }, CHECKED_MAIN_THREAD_EXECUTOR);
        } else {
            this.setChanged();
        }
    }

    @Nullable
    public static GameProfile getOrResolveGameProfile(CompoundTag param0) {
        if (param0.contains("SkullOwner", 10)) {
            return NbtUtils.readGameProfile(param0.getCompound("SkullOwner"));
        } else {
            if (param0.contains("SkullOwner", 8)) {
                String var0 = param0.getString("SkullOwner");
                if (!Util.isBlank(var0)) {
                    param0.remove("SkullOwner");
                    resolveGameProfile(param0, var0);
                }
            }

            return null;
        }
    }

    public static void resolveGameProfile(CompoundTag param0) {
        String var0 = param0.getString("SkullOwner");
        if (!Util.isBlank(var0)) {
            resolveGameProfile(param0, var0);
        }

    }

    private static void resolveGameProfile(CompoundTag param0, String param1) {
        fetchGameProfile(param1)
            .thenAccept(param2 -> param0.put("SkullOwner", NbtUtils.writeGameProfile(new CompoundTag(), param2.orElse(new GameProfile(Util.NIL_UUID, param1)))));
    }

    private static CompletableFuture<Optional<GameProfile>> fetchGameProfile(String param0) {
        LoadingCache<String, CompletableFuture<Optional<GameProfile>>> var0 = profileCache;
        return var0 != null && Player.isValidUsername(param0) ? var0.getUnchecked(param0) : CompletableFuture.completedFuture(Optional.empty());
    }

    private static boolean hasTextures(GameProfile param0) {
        return param0.getProperties().containsKey("textures");
    }
}
