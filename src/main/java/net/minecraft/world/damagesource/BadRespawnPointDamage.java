package net.minecraft.world.damagesource;

import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.world.entity.LivingEntity;

public class BadRespawnPointDamage extends DamageSource {
    protected BadRespawnPointDamage() {
        super("badRespawnPoint");
        this.setScalesWithDifficulty();
        this.setExplosion();
    }

    @Override
    public Component getLocalizedDeathMessage(LivingEntity param0) {
        Component var0 = ComponentUtils.wrapInSquareBrackets(Component.translatable("death.attack.badRespawnPoint.link"))
            .withStyle(
                param0x -> param0x.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://bugs.mojang.com/browse/MCPE-28723"))
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("MCPE-28723")))
            );
        return Component.translatable("death.attack.badRespawnPoint.message", param0.getDisplayName(), var0);
    }
}
