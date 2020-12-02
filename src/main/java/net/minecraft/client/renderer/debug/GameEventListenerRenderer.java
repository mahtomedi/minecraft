package net.minecraft.client.renderer.debug;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Vector3f;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
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
            BlockPos var1 = new BlockPos(param2, 0.0, param4);
            this.trackedGameEvents.removeIf(GameEventListenerRenderer.TrackedGameEvent::isExpired);
            this.trackedListeners.removeIf(param2x -> param2x.isExpired(var0, var1));
            RenderSystem.pushMatrix();
            RenderSystem.disableTexture();
            RenderSystem.enableDepthTest();
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            VertexConsumer var2 = param1.getBuffer(RenderType.lines());

            for(GameEventListenerRenderer.TrackedListener var3 : this.trackedListeners) {
                var3.getPosition(var0)
                    .ifPresent(
                        param6 -> {
                            int var0x = param6.getX() - var3.getListenerRadius();
                            int var1x = param6.getY() - var3.getListenerRadius();
                            int var2x = param6.getZ() - var3.getListenerRadius();
                            int var3x = param6.getX() + var3.getListenerRadius();
                            int var4x = param6.getY() + var3.getListenerRadius();
                            int var5x = param6.getZ() + var3.getListenerRadius();
                            Vector3f var6x = new Vector3f(1.0F, 1.0F, 0.0F);
                            LevelRenderer.renderVoxelShape(
                                param0,
                                var2,
                                Shapes.create(new AABB((double)var0x, (double)var1x, (double)var2x, (double)var3x, (double)var4x, (double)var5x)),
                                -param2,
                                -param3,
                                -param4,
                                var6x.x(),
                                var6x.y(),
                                var6x.z(),
                                0.35F
                            );
                        }
                    );
            }

            Tesselator var4 = Tesselator.getInstance();
            BufferBuilder var5 = var4.getBuilder();
            var5.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);

            for(GameEventListenerRenderer.TrackedListener var6 : this.trackedListeners) {
                var6.getPosition(var0)
                    .ifPresent(
                        param4x -> {
                            Vector3f var0x = new Vector3f(1.0F, 1.0F, 0.0F);
                            LevelRenderer.addChainedFilledBoxVertices(
                                var5,
                                (double)((float)param4x.getX() - 0.25F) - param2,
                                (double)param4x.getY() - param3,
                                (double)((float)param4x.getZ() - 0.25F) - param4,
                                (double)((float)param4x.getX() + 0.25F) - param2,
                                (double)param4x.getY() - param3 + 1.0,
                                (double)((float)param4x.getZ() + 0.25F) - param4,
                                var0x.x(),
                                var0x.y(),
                                var0x.z(),
                                0.35F
                            );
                        }
                    );
            }

            var4.end();
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.lineWidth(2.0F);
            RenderSystem.depthMask(false);

            for(GameEventListenerRenderer.TrackedListener var7 : this.trackedListeners) {
                var7.getPosition(var0)
                    .ifPresent(
                        param0x -> {
                            DebugRenderer.renderFloatingText(
                                "Listener Origin", (double)param0x.getX(), (double)((float)param0x.getY() + 1.8F), (double)param0x.getZ(), -1, 0.025F
                            );
                            DebugRenderer.renderFloatingText(
                                new BlockPos(param0x).toString(),
                                (double)param0x.getX(),
                                (double)((float)param0x.getY() + 1.5F),
                                (double)param0x.getZ(),
                                -6959665,
                                0.025F
                            );
                        }
                    );
            }

            for(GameEventListenerRenderer.TrackedGameEvent var8 : this.trackedGameEvents) {
                Vec3 var9 = var8.position;
                double var10 = 0.2F;
                double var11 = var9.x - 0.2F;
                double var12 = var9.y - 0.2F;
                double var13 = var9.z - 0.2F;
                double var14 = var9.x + 0.2F;
                double var15 = var9.y + 0.2F + 0.5;
                double var16 = var9.z + 0.2F;
                renderTransparentFilledBox(new AABB(var11, var12, var13, var14, var15, var16), 1.0F, 1.0F, 1.0F, 0.2F);
                DebugRenderer.renderFloatingText(var8.gameEvent.getName(), var9.x, var9.y + 0.85F, var9.z, -7564911, 0.0075F);
            }

            RenderSystem.depthMask(true);
            RenderSystem.enableTexture();
            RenderSystem.disableBlend();
            RenderSystem.popMatrix();
        }
    }

    private static void renderTransparentFilledBox(AABB param0, float param1, float param2, float param3, float param4) {
        Camera var0 = Minecraft.getInstance().gameRenderer.getMainCamera();
        if (var0.isInitialized()) {
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            Vec3 var1 = var0.getPosition().reverse();
            DebugRenderer.renderFilledBox(param0.move(var1), param1, param2, param3, param4);
        }
    }

    public void trackGameEvent(GameEvent param0, BlockPos param1) {
        this.trackedGameEvents.add(new GameEventListenerRenderer.TrackedGameEvent(Util.getMillis(), param0, Vec3.atBottomCenterOf(param1)));
    }

    public void trackListener(PositionSource param0, int param1) {
        this.trackedListeners.add(new GameEventListenerRenderer.TrackedListener(param0, param1));
    }

    @OnlyIn(Dist.CLIENT)
    static class TrackedGameEvent {
        public final long timeStamp;
        public final GameEvent gameEvent;
        public final Vec3 position;

        public TrackedGameEvent(long param0, GameEvent param1, Vec3 param2) {
            this.timeStamp = param0;
            this.gameEvent = param1;
            this.position = param2;
        }

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

        public boolean isExpired(Level param0, BlockPos param1) {
            Optional<BlockPos> var0 = this.listenerSource.getPosition(param0);
            return !var0.isPresent() || var0.get().distSqr(param1) <= 1024.0;
        }

        public Optional<BlockPos> getPosition(Level param0) {
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
        public boolean handleGameEvent(Level param0, GameEvent param1, @Nullable Entity param2, BlockPos param3) {
            return false;
        }
    }
}
