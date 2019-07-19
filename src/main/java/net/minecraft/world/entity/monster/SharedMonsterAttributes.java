package net.minecraft.world.entity.monster;

import java.util.Collection;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.BaseAttributeMap;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SharedMonsterAttributes {
    private static final Logger LOGGER = LogManager.getLogger();
    public static final Attribute MAX_HEALTH = new RangedAttribute(null, "generic.maxHealth", 20.0, 0.0, 1024.0)
        .importLegacyName("Max Health")
        .setSyncable(true);
    public static final Attribute FOLLOW_RANGE = new RangedAttribute(null, "generic.followRange", 32.0, 0.0, 2048.0).importLegacyName("Follow Range");
    public static final Attribute KNOCKBACK_RESISTANCE = new RangedAttribute(null, "generic.knockbackResistance", 0.0, 0.0, 1.0)
        .importLegacyName("Knockback Resistance");
    public static final Attribute MOVEMENT_SPEED = new RangedAttribute(null, "generic.movementSpeed", 0.7F, 0.0, 1024.0)
        .importLegacyName("Movement Speed")
        .setSyncable(true);
    public static final Attribute FLYING_SPEED = new RangedAttribute(null, "generic.flyingSpeed", 0.4F, 0.0, 1024.0)
        .importLegacyName("Flying Speed")
        .setSyncable(true);
    public static final Attribute ATTACK_DAMAGE = new RangedAttribute(null, "generic.attackDamage", 2.0, 0.0, 2048.0);
    public static final Attribute ATTACK_KNOCKBACK = new RangedAttribute(null, "generic.attackKnockback", 0.0, 0.0, 5.0);
    public static final Attribute ATTACK_SPEED = new RangedAttribute(null, "generic.attackSpeed", 4.0, 0.0, 1024.0).setSyncable(true);
    public static final Attribute ARMOR = new RangedAttribute(null, "generic.armor", 0.0, 0.0, 30.0).setSyncable(true);
    public static final Attribute ARMOR_TOUGHNESS = new RangedAttribute(null, "generic.armorToughness", 0.0, 0.0, 20.0).setSyncable(true);
    public static final Attribute LUCK = new RangedAttribute(null, "generic.luck", 0.0, -1024.0, 1024.0).setSyncable(true);

    public static ListTag saveAttributes(BaseAttributeMap param0) {
        ListTag var0 = new ListTag();

        for(AttributeInstance var1 : param0.getAttributes()) {
            var0.add(saveAttribute(var1));
        }

        return var0;
    }

    private static CompoundTag saveAttribute(AttributeInstance param0) {
        CompoundTag var0 = new CompoundTag();
        Attribute var1 = param0.getAttribute();
        var0.putString("Name", var1.getName());
        var0.putDouble("Base", param0.getBaseValue());
        Collection<AttributeModifier> var2 = param0.getModifiers();
        if (var2 != null && !var2.isEmpty()) {
            ListTag var3 = new ListTag();

            for(AttributeModifier var4 : var2) {
                if (var4.isSerializable()) {
                    var3.add(saveAttributeModifier(var4));
                }
            }

            var0.put("Modifiers", var3);
        }

        return var0;
    }

    public static CompoundTag saveAttributeModifier(AttributeModifier param0) {
        CompoundTag var0 = new CompoundTag();
        var0.putString("Name", param0.getName());
        var0.putDouble("Amount", param0.getAmount());
        var0.putInt("Operation", param0.getOperation().toValue());
        var0.putUUID("UUID", param0.getId());
        return var0;
    }

    public static void loadAttributes(BaseAttributeMap param0, ListTag param1) {
        for(int var0 = 0; var0 < param1.size(); ++var0) {
            CompoundTag var1 = param1.getCompound(var0);
            AttributeInstance var2 = param0.getInstance(var1.getString("Name"));
            if (var2 == null) {
                LOGGER.warn("Ignoring unknown attribute '{}'", var1.getString("Name"));
            } else {
                loadAttribute(var2, var1);
            }
        }

    }

    private static void loadAttribute(AttributeInstance param0, CompoundTag param1) {
        param0.setBaseValue(param1.getDouble("Base"));
        if (param1.contains("Modifiers", 9)) {
            ListTag var0 = param1.getList("Modifiers", 10);

            for(int var1 = 0; var1 < var0.size(); ++var1) {
                AttributeModifier var2 = loadAttributeModifier(var0.getCompound(var1));
                if (var2 != null) {
                    AttributeModifier var3 = param0.getModifier(var2.getId());
                    if (var3 != null) {
                        param0.removeModifier(var3);
                    }

                    param0.addModifier(var2);
                }
            }
        }

    }

    @Nullable
    public static AttributeModifier loadAttributeModifier(CompoundTag param0) {
        UUID var0 = param0.getUUID("UUID");

        try {
            AttributeModifier.Operation var1 = AttributeModifier.Operation.fromValue(param0.getInt("Operation"));
            return new AttributeModifier(var0, param0.getString("Name"), param0.getDouble("Amount"), var1);
        } catch (Exception var3) {
            LOGGER.warn("Unable to create attribute: {}", var3.getMessage());
            return null;
        }
    }
}
