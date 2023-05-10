package net.minecraft.client.renderer.debug;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.List;
import java.util.Optional;
import net.minecraft.Util;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEventListener;
import net.minecraft.world.level.gameevent.PositionSource;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GameEventListenerRenderer implements DebugRenderer.SimpleDebugRenderer {
    private final Minecraft minecraft;
    private static final int LISTENER_RENDER_DIST = 32;
    private static final float BOX_HEIGHT = 1.0F;
    private final List<GameEventListenerRenderer.TrackedGameEvent> trackedGameEvents = Lists.newArrayList();
    private final List<GameEventListenerRenderer.TrackedListener> trackedListeners = Lists.newArrayList();

    public GameEventListenerRenderer(Minecraft param0) {
        this.minecraft = param0;
    }

    @Override
    public void render(PoseStack param0, MultiBufferSource param1, double param2, double param3, double param4) {
        Level var0 = this.minecraft.level;
        if (var0 == null) {
            this.trackedGameEvents.clear();
            this.trackedListeners.clear();
        } else {
            Vec3 var1 = new Vec3(param2, 0.0, param4);
            this.trackedGameEvents.removeIf(GameEventListenerRenderer.TrackedGameEvent::isExpired);
            this.trackedListeners.removeIf(param2x -> param2x.isExpired(var0, var1));
            VertexConsumer var2 = param1.getBuffer(RenderType.lines());

            for(GameEventListenerRenderer.TrackedListener var3 : this.trackedListeners) {
                var3.getPosition(var0)
                    .ifPresent(
                        param6 -> {
                            double var0x = param6.x() - (double)var3.getListenerRadius();
                            double var1x = param6.y() - (double)var3.getListenerRadius();
                            double var2x = param6.z() - (double)var3.getListenerRadius();
                            double var3x = param6.x() + (double)var3.getListenerRadius();
                            double var4x = param6.y() + (double)var3.getListenerRadius();
                            double var5x = param6.z() + (double)var3.getListenerRadius();
                            LevelRenderer.renderVoxelShape(
                                param0,
                                var2,
                                Shapes.create(new AABB(var0x, var1x, var2x, var3x, var4x, var5x)),
                                -param2,
                                -param3,
                                -param4,
                                1.0F,
                                1.0F,
                                0.0F,
                                0.35F,
                                true
                            );
                        }
                    );
            }

            VertexConsumer var4 = param1.getBuffer(RenderType.debugFilledBox());

            for(GameEventListenerRenderer.TrackedListener var5 : this.trackedListeners) {
                var5.getPosition(var0)
                    .ifPresent(
                        param5 -> LevelRenderer.addChainedFilledBoxVertices(
                                param0,
                                var4,
                                param5.x() - 0.25 - param2,
                                param5.y() - param3,
                                param5.z() - 0.25 - param4,
                                param5.x() + 0.25 - param2,
                                param5.y() - param3 + 1.0,
                                param5.z() + 0.25 - param4,
                                1.0F,
                                1.0F,
                                0.0F,
                                0.35F
                            )
                    );
            }

            for(GameEventListenerRenderer.TrackedListener var6 : this.trackedListeners) {
                var6.getPosition(var0)
                    .ifPresent(
                        param2x -> {
                            DebugRenderer.renderFloatingText(param0, param1, "Listener Origin", param2x.x(), param2x.y() + 1.8F, param2x.z(), -1, 0.025F);
                            DebugRenderer.renderFloatingText(
                                param0, param1, BlockPos.containing(param2x).toString(), param2x.x(), param2x.y() + 1.5, param2x.z(), -6959665, 0.025F
                            );
                        }
                    );
            }

            for(GameEventListenerRenderer.TrackedGameEvent var7 : this.trackedGameEvents) {
                Vec3 var8 = var7.position;
                double var9 = 0.2F;
                double var10 = var8.x - 0.2F;
                double var11 = var8.y - 0.2F;
                double var12 = var8.z - 0.2F;
                double var13 = var8.x + 0.2F;
                double var14 = var8.y + 0.2F + 0.5;
                double var15 = var8.z + 0.2F;
                renderFilledBox(param0, param1, new AABB(var10, var11, var12, var13, var14, var15), 1.0F, 1.0F, 1.0F, 0.2F);
                DebugRenderer.renderFloatingText(param0, param1, var7.gameEvent.getName(), var8.x, var8.y + 0.85F, var8.z, -7564911, 0.0075F);
            }

        }
    }

    private static void renderFilledBox(PoseStack param0, MultiBufferSource param1, AABB param2, float param3, float param4, float param5, float param6) {
        Camera var0 = Minecraft.getInstance().gameRenderer.getMainCamera();
        if (var0.isInitialized()) {
            Vec3 var1 = var0.getPosition().reverse();
            DebugRenderer.renderFilledBox(param0, param1, param2.move(var1), param3, param4, param5, param6);
        }
    }

    public void trackGameEvent(GameEvent param0, Vec3 param1) {
        this.trackedGameEvents.add(new GameEventListenerRenderer.TrackedGameEvent(Util.getMillis(), param0, param1));
    }

    public void trackListener(PositionSource param0, int param1) {
        this.trackedListeners.add(new GameEventListenerRenderer.TrackedListener(param0, param1));
    }

    @OnlyIn(Dist.CLIENT)
    static record TrackedGameEvent(long timeStamp, GameEvent gameEvent, Vec3 position) {
        public boolean isExpired() {
            return Util.getMillis() - this.timeStamp > 3000L;
        }
    }

    @OnlyIn(Dist.CLIENT)
    static class TrackedListener implements GameEventListener {
        public final PositionSource listenerSource;
        public final int listenerRange;

        public TrackedListener(PositionSource param0, int param1) {
            this.listenerSource = param0;
            this.listenerRange = param1;
        }

        public boolean isExpired(Level param0, Vec3 param1) {
            return this.listenerSource.getPosition(param0).filter(param1x -> param1x.distanceToSqr(param1) <= 1024.0).isPresent();
        }

        public Optional<Vec3> getPosition(Level param0) {
            return this.listenerSource.getPosition(param0);
        }

        @Override
        public PositionSource getListenerSource() {
            return this.listenerSource;
        }

        @Override
        public int getListenerRadius() {
            return this.listenerRange;
        }

        @Override
        public boolean handleGameEvent(ServerLevel param0, GameEvent param1, GameEvent.Context param2, Vec3 param3) {
            return false;
        }
    }
}
