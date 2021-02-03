package net.minecraft.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class RisingParticle extends TextureSheetParticle {
    protected RisingParticle(ClientLevel param0, double param1, double param2, double param3, double param4, double param5, double param6) {
        super(param0, param1, param2, param3, param4, param5, param6);
        this.friction = 0.96F;
        this.xd = this.xd * 0.01F + param4;
        this.yd = this.yd * 0.01F + param5;
        this.zd = this.zd * 0.01F + param6;
        this.x += (double)((this.random.nextFloat() - this.random.nextFloat()) * 0.05F);
        this.y += (double)((this.random.nextFloat() - this.random.nextFloat()) * 0.05F);
        this.z += (double)((this.random.nextFloat() - this.random.nextFloat()) * 0.05F);
        this.lifetime = (int)(8.0 / (Math.random() * 0.8 + 0.2)) + 4;
    }
}
