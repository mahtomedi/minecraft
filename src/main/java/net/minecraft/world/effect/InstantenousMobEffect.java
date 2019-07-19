package net.minecraft.world.effect;

public class InstantenousMobEffect extends MobEffect {
    public InstantenousMobEffect(MobEffectCategory param0, int param1) {
        super(param0, param1);
    }

    @Override
    public boolean isInstantenous() {
        return true;
    }

    @Override
    public boolean isDurationEffectTick(int param0, int param1) {
        return param0 >= 1;
    }
}
