package net.minecraft.client;

import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ServerboundBlockEntityTagQuery;
import net.minecraft.network.protocol.game.ServerboundEntityTagQuery;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class DebugQueryHandler {
    private final ClientPacketListener connection;
    private int transactionId = -1;
    @Nullable
    private Consumer<CompoundTag> callback;

    public DebugQueryHandler(ClientPacketListener param0) {
        this.connection = param0;
    }

    public boolean handleResponse(int param0, @Nullable CompoundTag param1) {
        if (this.transactionId == param0 && this.callback != null) {
            this.callback.accept(param1);
            this.callback = null;
            return true;
        } else {
            return false;
        }
    }

    private int startTransaction(Consumer<CompoundTag> param0) {
        this.callback = param0;
        return ++this.transactionId;
    }

    public void queryEntityTag(int param0, Consumer<CompoundTag> param1) {
        int var0 = this.startTransaction(param1);
        this.connection.send(new ServerboundEntityTagQuery(var0, param0));
    }

    public void queryBlockEntityTag(BlockPos param0, Consumer<CompoundTag> param1) {
        int var0 = this.startTransaction(param1);
        this.connection.send(new ServerboundBlockEntityTagQuery(var0, param0));
    }
}
