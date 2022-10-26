package net.minecraft.client.multiplayer;

import com.mojang.authlib.minecraft.UserApiService;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import net.minecraft.client.User;
import net.minecraft.world.entity.player.ProfileKeyPair;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface ProfileKeyPairManager {
    ProfileKeyPairManager EMPTY_KEY_MANAGER = new ProfileKeyPairManager() {
        @Override
        public CompletableFuture<Optional<ProfileKeyPair>> prepareKeyPair() {
            return CompletableFuture.completedFuture(Optional.empty());
        }

        @Override
        public boolean shouldRefreshKeyPair() {
            return false;
        }
    };

    static ProfileKeyPairManager create(UserApiService param0, User param1, Path param2) {
        return (ProfileKeyPairManager)(param1.getType() == User.Type.MSA
            ? new AccountProfileKeyPairManager(param0, param1.getGameProfile().getId(), param2)
            : EMPTY_KEY_MANAGER);
    }

    CompletableFuture<Optional<ProfileKeyPair>> prepareKeyPair();

    boolean shouldRefreshKeyPair();
}
