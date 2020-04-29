package net.minecraft.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TrackingEmitter extends NoRenderParticle {
    private final Entity entity;
    private int life;
    private final int lifeTime;
    private final ParticleOptions particleType;

    public TrackingEmitter(ClientLevel param0, Entity param1, ParticleOptions param2) {
        this(param0, param1, param2, 3);
    }

    public TrackingEmitter(ClientLevel param0, Entity param1, ParticleOptions param2, int param3) {
        this(param0, param1, param2, param3, param1.getDeltaMovement());
    }

    private TrackingEmitter(ClientLevel param0, Entity param1, ParticleOptions param2, int param3, Vec3 param4) {
        super(param0, param1.getX(), param1.getY(0.5), param1.getZ(), param4.x, param4.y, param4.z);
        this.entity = param1;
        this.lifeTime = param3;
        this.particleType = param2;
        this.tick();
    }

    @Override
    public void tick() {
        for(int var0 = 0; var0 < 16; ++var0) {
            double var1 = (double)(this.random.nextFloat() * 2.0F - 1.0F);
            double var2 = (double)(this.random.nextFloat() * 2.0F - 1.0F);
            double var3 = (double)(this.random.nextFloat() * 2.0F - 1.0F);
            if (!(var1 * var1 + var2 * var2 + var3 * var3 > 1.0)) {
                double var4 = this.entity.getX(var1 / 4.0);
                double var5 = this.entity.getY(0.5 + var2 / 4.0);
                double var6 = this.entity.getZ(var3 / 4.0);
                this.level.addParticle(this.particleType, false, var4, var5, var6, var1, var2 + 0.2, var3);
            }
        }

        ++this.life;
        if (this.life >= this.lifeTime) {
            this.remove();
        }

    }
}
