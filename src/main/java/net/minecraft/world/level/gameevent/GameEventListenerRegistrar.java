package net.minecraft.world.level.gameevent;

import java.util.Optional;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;

public class GameEventListenerRegistrar {
    private final GameEventListener listener;
    @Nullable
    private SectionPos sectionPos;

    public GameEventListenerRegistrar(GameEventListener param0) {
        this.listener = param0;
    }

    public void onListenerRemoved(Level param0) {
        this.ifEventDispatcherExists(param0, this.sectionPos, param0x -> param0x.unregister(this.listener));
    }

    public void onListenerMove(Level param0) {
        Optional<BlockPos> var0 = this.listener.getListenerSource().getPosition(param0);
        if (var0.isPresent()) {
            long var1 = SectionPos.blockToSection(var0.get().asLong());
            if (this.sectionPos == null || this.sectionPos.asLong() != var1) {
                SectionPos var2 = this.sectionPos;
                this.sectionPos = SectionPos.of(var1);
                this.ifEventDispatcherExists(param0, var2, param0x -> param0x.unregister(this.listener));
                this.ifEventDispatcherExists(param0, this.sectionPos, param0x -> param0x.register(this.listener));
            }
        }

    }

    private void ifEventDispatcherExists(Level param0, @Nullable SectionPos param1, Consumer<GameEventDispatcher> param2) {
        if (param1 != null) {
            ChunkAccess var0 = param0.getChunk(param1.x(), param1.z(), ChunkStatus.FULL, false);
            if (var0 != null) {
                param2.accept(var0.getEventDispatcher(param1.y()));
            }

        }
    }
}
