package net.minecraft.client.particle;

import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class RisingParticle extends TextureSheetParticle {
    protected RisingParticle(Level param0, double param1, double param2, double param3, double param4, double param5, double param6) {
        super(param0, param1, param2, param3, param4, param5, param6);
        this.xd = this.xd * 0.01F + param4;
        this.yd = this.yd * 0.01F + param5;
        this.zd = this.zd * 0.01F + param6;
        this.x += (double)((this.random.nextFloat() - this.random.nextFloat()) * 0.05F);
        this.y += (double)((this.random.nextFloat() - this.random.nextFloat()) * 0.05F);
        this.z += (double)((this.random.nextFloat() - this.random.nextFloat()) * 0.05F);
        this.lifetime = (int)(8.0 / (Math.random() * 0.8 + 0.2)) + 4;
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (this.age++ >= this.lifetime) {
            this.remove();
        } else {
            this.move(this.xd, this.yd, this.zd);
            this.xd *= 0.96F;
            this.yd *= 0.96F;
            this.zd *= 0.96F;
            if (this.onGround) {
                this.xd *= 0.7F;
                this.zd *= 0.7F;
            }

        }
    }
}
