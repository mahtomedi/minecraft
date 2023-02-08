package net.minecraft.world.entity;

import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.world.level.EntityGetter;

public interface OwnableEntity {
    @Nullable
    UUID getOwnerUUID();

    EntityGetter getLevel();

    @Nullable
    default LivingEntity getOwner() {
        UUID var0 = this.getOwnerUUID();
        return var0 == null ? null : this.getLevel().getPlayerByUUID(var0);
    }
}
