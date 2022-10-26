package net.minecraft.client.renderer.debug;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.util.List;
import java.util.Optional;
import net.minecraft.Util;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
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
import org.joml.Vector3f;

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
            RenderSystem.disableTexture();
            RenderSystem.enableDepthTest();
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
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
                            Vector3f var6x = new Vector3f(1.0F, 1.0F, 0.0F);
                            LevelRenderer.renderVoxelShape(
                                param0,
                                var2,
                                Shapes.create(new AABB(var0x, var1x, var2x, var3x, var4x, var5x)),
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

            RenderSystem.setShader(GameRenderer::getPositionColorShader);
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
                                param4x.x() - 0.25 - param2,
                                param4x.y() - param3,
                                param4x.z() - 0.25 - param4,
                                param4x.x() + 0.25 - param2,
                                param4x.y() - param3 + 1.0,
                                param4x.z() + 0.25 - param4,
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
                var7.getPosition(var0).ifPresent(param0x -> {
                    DebugRenderer.renderFloatingText("Listener Origin", param0x.x(), param0x.y() + 1.8F, param0x.z(), -1, 0.025F);
                    DebugRenderer.renderFloatingText(new BlockPos(param0x).toString(), param0x.x(), param0x.y() + 1.5, param0x.z(), -6959665, 0.025F);
                });
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
