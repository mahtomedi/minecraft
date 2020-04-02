package net.minecraft.world.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.ImmutableMultimap.Builder;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class DiggerItem extends TieredItem {
    private final Set<Block> blocks;
    protected final float speed;
    private final float attackDamageBaseline;
    private final Multimap<Attribute, AttributeModifier> defaultModifiers;

    protected DiggerItem(float param0, float param1, Tier param2, Set<Block> param3, Item.Properties param4) {
        super(param2, param4);
        this.blocks = param3;
        this.speed = param2.getSpeed();
        this.attackDamageBaseline = param0 + param2.getAttackDamageBonus();
        Builder<Attribute, AttributeModifier> var0 = ImmutableMultimap.builder();
        var0.put(
            Attributes.ATTACK_DAMAGE,
            new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Tool modifier", (double)this.attackDamageBaseline, AttributeModifier.Operation.ADDITION)
        );
        var0.put(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Tool modifier", (double)param1, AttributeModifier.Operation.ADDITION));
        this.defaultModifiers = var0.build();
    }

    @Override
    public float getDestroySpeed(ItemStack param0, BlockState param1) {
        return this.blocks.contains(param1.getBlock()) ? this.speed : 1.0F;
    }

    @Override
    public boolean hurtEnemy(ItemStack param0, LivingEntity param1, LivingEntity param2) {
        param0.hurtAndBreak(2, param2, param0x -> param0x.broadcastBreakEvent(EquipmentSlot.MAINHAND));
        return true;
    }

    @Override
    public boolean mineBlock(ItemStack param0, Level param1, BlockState param2, BlockPos param3, LivingEntity param4) {
        if (!param1.isClientSide && param2.getDestroySpeed(param1, param3) != 0.0F) {
            param0.hurtAndBreak(1, param4, param0x -> param0x.broadcastBreakEvent(EquipmentSlot.MAINHAND));
        }

        return true;
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot param0) {
        return param0 == EquipmentSlot.MAINHAND ? this.defaultModifiers : super.getDefaultAttributeModifiers(param0);
    }

    public float getAttackDamage() {
        return this.attackDamageBaseline;
    }
}
