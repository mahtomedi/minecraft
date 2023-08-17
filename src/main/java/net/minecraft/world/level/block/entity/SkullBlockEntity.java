package net.minecraft.world.level.block.entity;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.yggdrasil.ProfileResult;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.Services;
import net.minecraft.server.players.GameProfileCache;
import net.minecraft.util.StringUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class SkullBlockEntity extends BlockEntity {
    public static final String TAG_SKULL_OWNER = "SkullOwner";
    public static final String TAG_NOTE_BLOCK_SOUND = "note_block_sound";
    @Nullable
    private static GameProfileCache profileCache;
    @Nullable
    private static MinecraftSessionService sessionService;
    @Nullable
    private static Executor mainThreadExecutor;
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

    public static void setup(Services param0, Executor param1) {
        profileCache = param0.profileCache();
        sessionService = param0.sessionService();
        mainThreadExecutor = param1;
    }

    public static void clear() {
        profileCache = null;
        sessionService = null;
        mainThreadExecutor = null;
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
        if (param0.hasNeighborSignal(param1)) {
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
        GameProfileCache var0 = profileCache;
        return var0 == null
            ? CompletableFuture.completedFuture(Optional.empty())
            : var0.getAsync(param0)
                .thenCompose(param0x -> param0x.isPresent() ? fillProfileTextures(param0x.get()) : CompletableFuture.completedFuture(Optional.empty()))
                .thenApplyAsync((Function<? super Optional, ? extends Optional<GameProfile>>)(param0x -> {
                    GameProfileCache var0x = profileCache;
                    if (var0x != null) {
                        param0x.ifPresent(var0x::add);
                        return param0x;
                    } else {
                        return Optional.empty();
                    }
                }), CHECKED_MAIN_THREAD_EXECUTOR);
    }

    private static CompletableFuture<Optional<GameProfile>> fillProfileTextures(GameProfile param0) {
        return hasTextures(param0) ? CompletableFuture.completedFuture(Optional.of(param0)) : CompletableFuture.supplyAsync(() -> {
            MinecraftSessionService var0x = sessionService;
            if (var0x != null) {
                ProfileResult var1 = var0x.fetchProfile(param0.getId(), true);
                return var1 == null ? Optional.of(param0) : Optional.of(var1.profile());
            } else {
                return Optional.empty();
            }
        }, Util.backgroundExecutor());
    }

    private static boolean hasTextures(GameProfile param0) {
        return param0.getProperties().containsKey("textures");
    }
}
