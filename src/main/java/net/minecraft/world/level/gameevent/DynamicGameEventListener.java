package net.minecraft.world.level.gameevent;

import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;

public class DynamicGameEventListener<T extends GameEventListener> {
    private T listener;
    @Nullable
    private SectionPos lastSection;

    public DynamicGameEventListener(T param0) {
        this.listener = param0;
    }

    public void add(ServerLevel param0) {
        this.move(param0);
    }

    public void updateListener(T param0, @Nullable Level param1) {
        T var0 = this.listener;
        if (var0 != param0) {
            if (param1 instanceof ServerLevel var1) {
                ifChunkExists(var1, this.lastSection, param1x -> param1x.unregister(var0));
                ifChunkExists(var1, this.lastSection, param1x -> param1x.register(param0));
            }

            this.listener = param0;
        }
    }

    public T getListener() {
        return this.listener;
    }

    public void remove(ServerLevel param0) {
        ifChunkExists(param0, this.lastSection, param0x -> param0x.unregister(this.listener));
    }

    public void move(ServerLevel param0) {
        this.listener.getListenerSource().getPosition(param0).map(SectionPos::of).ifPresent(param1 -> {
            if (this.lastSection == null || !this.lastSection.equals(param1)) {
                ifChunkExists(param0, this.lastSection, param0x -> param0x.unregister(this.listener));
                this.lastSection = param1;
                ifChunkExists(param0, this.lastSection, param0x -> param0x.register(this.listener));
            }

        });
    }

    private static void ifChunkExists(LevelReader param0, @Nullable SectionPos param1, Consumer<GameEventListenerRegistry> param2) {
        if (param1 != null) {
            ChunkAccess var0 = param0.getChunk(param1.x(), param1.z(), ChunkStatus.FULL, false);
            if (var0 != null) {
                param2.accept(var0.getListenerRegistry(param1.y()));
            }

        }
    }
}
