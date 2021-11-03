package net.minecraft.client.renderer.debug;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.Collections;
import java.util.List;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CollisionBoxRenderer implements DebugRenderer.SimpleDebugRenderer {
    private final Minecraft minecraft;
    private double lastUpdateTime = Double.MIN_VALUE;
    private List<VoxelShape> shapes = Collections.emptyList();

    public CollisionBoxRenderer(Minecraft param0) {
        this.minecraft = param0;
    }

    @Override
    public void render(PoseStack param0, MultiBufferSource param1, double param2, double param3, double param4) {
        double var0 = (double)Util.getNanos();
        if (var0 - this.lastUpdateTime > 1.0E8) {
            this.lastUpdateTime = var0;
            Entity var1 = this.minecraft.gameRenderer.getMainCamera().getEntity();
            this.shapes = ImmutableList.copyOf(var1.level.getCollisions(var1, var1.getBoundingBox().inflate(6.0)));
        }

        VertexConsumer var2 = param1.getBuffer(RenderType.lines());

        for(VoxelShape var3 : this.shapes) {
            LevelRenderer.renderVoxelShape(param0, var2, var3, -param2, -param3, -param4, 1.0F, 1.0F, 1.0F, 1.0F);
        }

    }
}
