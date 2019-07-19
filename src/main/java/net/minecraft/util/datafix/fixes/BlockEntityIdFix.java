package net.minecraft.util.datafix.fixes;

import com.google.common.collect.Maps;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.templates.TaggedChoice.TaggedChoiceType;
import java.util.Map;

public class BlockEntityIdFix extends DataFix {
    private static final Map<String, String> ID_MAP = DataFixUtils.make(Maps.newHashMap(), param0 -> {
        param0.put("Airportal", "minecraft:end_portal");
        param0.put("Banner", "minecraft:banner");
        param0.put("Beacon", "minecraft:beacon");
        param0.put("Cauldron", "minecraft:brewing_stand");
        param0.put("Chest", "minecraft:chest");
        param0.put("Comparator", "minecraft:comparator");
        param0.put("Control", "minecraft:command_block");
        param0.put("DLDetector", "minecraft:daylight_detector");
        param0.put("Dropper", "minecraft:dropper");
        param0.put("EnchantTable", "minecraft:enchanting_table");
        param0.put("EndGateway", "minecraft:end_gateway");
        param0.put("EnderChest", "minecraft:ender_chest");
        param0.put("FlowerPot", "minecraft:flower_pot");
        param0.put("Furnace", "minecraft:furnace");
        param0.put("Hopper", "minecraft:hopper");
        param0.put("MobSpawner", "minecraft:mob_spawner");
        param0.put("Music", "minecraft:noteblock");
        param0.put("Piston", "minecraft:piston");
        param0.put("RecordPlayer", "minecraft:jukebox");
        param0.put("Sign", "minecraft:sign");
        param0.put("Skull", "minecraft:skull");
        param0.put("Structure", "minecraft:structure_block");
        param0.put("Trap", "minecraft:dispenser");
    });

    public BlockEntityIdFix(Schema param0, boolean param1) {
        super(param0, param1);
    }

    @Override
    public TypeRewriteRule makeRule() {
        Type<?> var0 = this.getInputSchema().getType(References.ITEM_STACK);
        Type<?> var1 = this.getOutputSchema().getType(References.ITEM_STACK);
        TaggedChoiceType<String> var2 = this.getInputSchema().findChoiceType(References.BLOCK_ENTITY);
        TaggedChoiceType<String> var3 = this.getOutputSchema().findChoiceType(References.BLOCK_ENTITY);
        return TypeRewriteRule.seq(
            this.convertUnchecked("item stack block entity name hook converter", var0, var1),
            this.fixTypeEverywhere("BlockEntityIdFix", var2, var3, param0 -> param0x -> param0x.mapFirst(param0xx -> ID_MAP.getOrDefault(param0xx, param0xx)))
        );
    }
}
