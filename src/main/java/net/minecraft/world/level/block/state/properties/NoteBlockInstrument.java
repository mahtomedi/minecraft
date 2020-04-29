package net.minecraft.world.level.block.state.properties;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;

public enum NoteBlockInstrument implements StringRepresentable {
    HARP("harp", SoundEvents.NOTE_BLOCK_HARP),
    BASEDRUM("basedrum", SoundEvents.NOTE_BLOCK_BASEDRUM),
    SNARE("snare", SoundEvents.NOTE_BLOCK_SNARE),
    HAT("hat", SoundEvents.NOTE_BLOCK_HAT),
    BASS("bass", SoundEvents.NOTE_BLOCK_BASS),
    FLUTE("flute", SoundEvents.NOTE_BLOCK_FLUTE),
    BELL("bell", SoundEvents.NOTE_BLOCK_BELL),
    GUITAR("guitar", SoundEvents.NOTE_BLOCK_GUITAR),
    CHIME("chime", SoundEvents.NOTE_BLOCK_CHIME),
    XYLOPHONE("xylophone", SoundEvents.NOTE_BLOCK_XYLOPHONE),
    IRON_XYLOPHONE("iron_xylophone", SoundEvents.NOTE_BLOCK_IRON_XYLOPHONE),
    COW_BELL("cow_bell", SoundEvents.NOTE_BLOCK_COW_BELL),
    DIDGERIDOO("didgeridoo", SoundEvents.NOTE_BLOCK_DIDGERIDOO),
    BIT("bit", SoundEvents.NOTE_BLOCK_BIT),
    BANJO("banjo", SoundEvents.NOTE_BLOCK_BANJO),
    PLING("pling", SoundEvents.NOTE_BLOCK_PLING);

    private final String name;
    private final SoundEvent soundEvent;

    private NoteBlockInstrument(String param0, SoundEvent param1) {
        this.name = param0;
        this.soundEvent = param1;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }

    public SoundEvent getSoundEvent() {
        return this.soundEvent;
    }

    public static NoteBlockInstrument byState(BlockState param0) {
        if (param0.is(Blocks.CLAY)) {
            return FLUTE;
        } else if (param0.is(Blocks.GOLD_BLOCK)) {
            return BELL;
        } else if (param0.is(BlockTags.WOOL)) {
            return GUITAR;
        } else if (param0.is(Blocks.PACKED_ICE)) {
            return CHIME;
        } else if (param0.is(Blocks.BONE_BLOCK)) {
            return XYLOPHONE;
        } else if (param0.is(Blocks.IRON_BLOCK)) {
            return IRON_XYLOPHONE;
        } else if (param0.is(Blocks.SOUL_SAND)) {
            return COW_BELL;
        } else if (param0.is(Blocks.PUMPKIN)) {
            return DIDGERIDOO;
        } else if (param0.is(Blocks.EMERALD_BLOCK)) {
            return BIT;
        } else if (param0.is(Blocks.HAY_BLOCK)) {
            return BANJO;
        } else if (param0.is(Blocks.GLOWSTONE)) {
            return PLING;
        } else {
            Material var0 = param0.getMaterial();
            if (var0 == Material.STONE) {
                return BASEDRUM;
            } else if (var0 == Material.SAND) {
                return SNARE;
            } else if (var0 == Material.GLASS) {
                return HAT;
            } else {
                return var0 != Material.WOOD && var0 != Material.NETHER_WOOD ? HARP : BASS;
            }
        }
    }
}
