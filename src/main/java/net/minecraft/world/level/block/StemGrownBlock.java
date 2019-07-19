package net.minecraft.world.level.block;

public abstract class StemGrownBlock extends Block {
    public StemGrownBlock(Block.Properties param0) {
        super(param0);
    }

    public abstract StemBlock getStem();

    public abstract AttachedStemBlock getAttachedStem();
}
