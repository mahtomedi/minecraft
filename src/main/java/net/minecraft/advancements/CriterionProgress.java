package net.minecraft.advancements;

import java.time.Instant;
import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;

public class CriterionProgress {
    @Nullable
    private Instant obtained;

    public CriterionProgress() {
    }

    public CriterionProgress(Instant param0) {
        this.obtained = param0;
    }

    public boolean isDone() {
        return this.obtained != null;
    }

    public void grant() {
        this.obtained = Instant.now();
    }

    public void revoke() {
        this.obtained = null;
    }

    @Nullable
    public Instant getObtained() {
        return this.obtained;
    }

    @Override
    public String toString() {
        return "CriterionProgress{obtained=" + (this.obtained == null ? "false" : this.obtained) + "}";
    }

    public void serializeToNetwork(FriendlyByteBuf param0) {
        param0.writeNullable(this.obtained, FriendlyByteBuf::writeInstant);
    }

    public static CriterionProgress fromNetwork(FriendlyByteBuf param0) {
        CriterionProgress var0 = new CriterionProgress();
        var0.obtained = param0.readNullable(FriendlyByteBuf::readInstant);
        return var0;
    }
}
