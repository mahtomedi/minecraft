package net.minecraft.client.renderer;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PanoramaRenderer {
    private final Minecraft minecraft;
    private final CubeMap cubeMap;
    private float time;

    public PanoramaRenderer(CubeMap param0) {
        this.cubeMap = param0;
        this.minecraft = Minecraft.getInstance();
    }

    public void render(float param0, float param1) {
        this.time += param0;
        this.cubeMap.render(this.minecraft, Mth.sin(this.time * 0.001F) * 5.0F + 25.0F, -this.time * 0.1F, param1);
        this.minecraft.getWindow().setupGuiState(Minecraft.ON_OSX);
    }
}
