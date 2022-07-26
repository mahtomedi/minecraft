package net.minecraft.core.particles;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import net.minecraft.commands.arguments.item.ItemInput;
import net.minecraft.commands.arguments.item.ItemParser;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

public class ItemParticleOption implements ParticleOptions {
    public static final ParticleOptions.Deserializer<ItemParticleOption> DESERIALIZER = new ParticleOptions.Deserializer<ItemParticleOption>() {
        public ItemParticleOption fromCommand(ParticleType<ItemParticleOption> param0, StringReader param1) throws CommandSyntaxException {
            param1.expect(' ');
            ItemParser.ItemResult var0 = ItemParser.parseForItem(BuiltInRegistries.ITEM.asLookup(), param1);
            ItemStack var1 = new ItemInput(var0.item(), var0.nbt()).createItemStack(1, false);
            return new ItemParticleOption(param0, var1);
        }

        public ItemParticleOption fromNetwork(ParticleType<ItemParticleOption> param0, FriendlyByteBuf param1) {
            return new ItemParticleOption(param0, param1.readItem());
        }
    };
    private final ParticleType<ItemParticleOption> type;
    private final ItemStack itemStack;

    public static Codec<ItemParticleOption> codec(ParticleType<ItemParticleOption> param0) {
        return ItemStack.CODEC.xmap(param1 -> new ItemParticleOption(param0, param1), param0x -> param0x.itemStack);
    }

    public ItemParticleOption(ParticleType<ItemParticleOption> param0, ItemStack param1) {
        this.type = param0;
        this.itemStack = param1;
    }

    @Override
    public void writeToNetwork(FriendlyByteBuf param0) {
        param0.writeItem(this.itemStack);
    }

    @Override
    public String writeToString() {
        return BuiltInRegistries.PARTICLE_TYPE.getKey(this.getType())
            + " "
            + new ItemInput(this.itemStack.getItemHolder(), this.itemStack.getTag()).serialize();
    }

    @Override
    public ParticleType<ItemParticleOption> getType() {
        return this.type;
    }

    public ItemStack getItem() {
        return this.itemStack;
    }
}
