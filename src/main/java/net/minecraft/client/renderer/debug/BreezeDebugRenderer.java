package net.minecraft.client.renderer.debug;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.protocol.common.custom.BreezeDebugPayload;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;

@OnlyIn(Dist.CLIENT)
public class BreezeDebugRenderer {
    private static final int JUMP_TARGET_LINE_COLOR = FastColor.ARGB32.color(255, 255, 100, 255);
    private static final int TARGET_LINE_COLOR = FastColor.ARGB32.color(255, 100, 255, 255);
    private static final int INNER_CIRCLE_COLOR = FastColor.ARGB32.color(255, 0, 255, 0);
    private static final int MIDDLE_CIRCLE_COLOR = FastColor.ARGB32.color(255, 255, 165, 0);
    private static final int OUTER_CIRCLE_COLOR = FastColor.ARGB32.color(255, 255, 0, 0);
    private static final int CIRCLE_VERTICES = 20;
    private static final float SEGMENT_SIZE_RADIANS = (float) (Math.PI / 10);
    private final Minecraft minecraft;
    private final Map<Integer, BreezeDebugPayload.BreezeInfo> perEntity = new HashMap<>();

    public BreezeDebugRenderer(Minecraft param0) {
        this.minecraft = param0;
    }

    public void render(PoseStack param0, MultiBufferSource param1, double param2, double param3, double param4) {
        LocalPlayer var0 = this.minecraft.player;
        var0.level()
            .getEntities(EntityType.BREEZE, var0.getBoundingBox().inflate(100.0), param0x -> true)
            .forEach(
                param6 -> {
                    Optional<BreezeDebugPayload.BreezeInfo> var0x = Optional.ofNullable(this.perEntity.get(param6.getId()));
                    var0x.map(BreezeDebugPayload.BreezeInfo::attackTarget)
                        .map(param1x -> var0.level().getEntity(param1x))
                        .map(param0x -> param0x.getPosition(this.minecraft.getFrameTime()))
                        .ifPresent(
                            param6x -> {
                                drawLine(param0, param1, param2, param3, param4, param6.position(), param6x, TARGET_LINE_COLOR);
                                Vec3 var0xx = param6x.add(0.0, 0.01F, 0.0);
                                drawCircle(
                                    param0.last().pose(),
                                    param2,
                                    param3,
                                    param4,
                                    param1.getBuffer(RenderType.debugLineStrip(2.0)),
                                    var0xx,
                                    4.0F,
                                    INNER_CIRCLE_COLOR
                                );
                                drawCircle(
                                    param0.last().pose(),
                                    param2,
                                    param3,
                                    param4,
                                    param1.getBuffer(RenderType.debugLineStrip(2.0)),
                                    var0xx,
                                    8.0F,
                                    MIDDLE_CIRCLE_COLOR
                                );
                                drawCircle(
                                    param0.last().pose(),
                                    param2,
                                    param3,
                                    param4,
                                    param1.getBuffer(RenderType.debugLineStrip(2.0)),
                                    var0xx,
                                    20.0F,
                                    OUTER_CIRCLE_COLOR
                                );
                            }
                        );
                    var0x.map(BreezeDebugPayload.BreezeInfo::jumpTarget)
                        .ifPresent(
                            param6x -> {
                                drawLine(param0, param1, param2, param3, param4, param6.position(), param6x.getCenter(), JUMP_TARGET_LINE_COLOR);
                                DebugRenderer.renderFilledBox(
                                    param0,
                                    param1,
                                    AABB.unitCubeFromLowerCorner(Vec3.atLowerCornerOf(param6x)).move(-param2, -param3, -param4),
                                    1.0F,
                                    0.0F,
                                    0.0F,
                                    1.0F
                                );
                            }
                        );
                }
            );
    }

    private static void drawLine(PoseStack param0, MultiBufferSource param1, double param2, double param3, double param4, Vec3 param5, Vec3 param6, int param7) {
        VertexConsumer var0 = param1.getBuffer(RenderType.debugLineStrip(2.0));
        var0.vertex(param0.last().pose(), (float)(param5.x - param2), (float)(param5.y - param3), (float)(param5.z - param4)).color(param7).endVertex();
        var0.vertex(param0.last().pose(), (float)(param6.x - param2), (float)(param6.y - param3), (float)(param6.z - param4)).color(param7).endVertex();
    }

    private static void drawCircle(Matrix4f param0, double param1, double param2, double param3, VertexConsumer param4, Vec3 param5, float param6, int param7) {
        for(int var0 = 0; var0 < 20; ++var0) {
            drawCircleVertex(var0, param0, param1, param2, param3, param4, param5, param6, param7);
        }

        drawCircleVertex(0, param0, param1, param2, param3, param4, param5, param6, param7);
    }

    private static void drawCircleVertex(
        int param0, Matrix4f param1, double param2, double param3, double param4, VertexConsumer param5, Vec3 param6, float param7, int param8
    ) {
        float var0 = (float)param0 * (float) (Math.PI / 10);
        Vec3 var1 = param6.add((double)param7 * Math.cos((double)var0), 0.0, (double)param7 * Math.sin((double)var0));
        param5.vertex(param1, (float)(var1.x - param2), (float)(var1.y - param3), (float)(var1.z - param4)).color(param8).endVertex();
    }

    public void clear() {
        this.perEntity.clear();
    }

    public void add(BreezeDebugPayload.BreezeInfo param0) {
        this.perEntity.put(param0.id(), param0);
    }
}
