package net.minecraft.nbt.visitors;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.nbt.ByteArrayTag;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.EndTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.ShortTag;
import net.minecraft.nbt.StreamTagVisitor;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagType;

public class CollectToTag implements StreamTagVisitor {
    private String lastId = "";
    @Nullable
    private Tag rootTag;
    private final Deque<Consumer<Tag>> consumerStack = new ArrayDeque<>();

    @Nullable
    public Tag getResult() {
        return this.rootTag;
    }

    protected int depth() {
        return this.consumerStack.size();
    }

    private void appendEntry(Tag param0) {
        this.consumerStack.getLast().accept(param0);
    }

    @Override
    public StreamTagVisitor.ValueResult visitEnd() {
        this.appendEntry(EndTag.INSTANCE);
        return StreamTagVisitor.ValueResult.CONTINUE;
    }

    @Override
    public StreamTagVisitor.ValueResult visit(String param0) {
        this.appendEntry(StringTag.valueOf(param0));
        return StreamTagVisitor.ValueResult.CONTINUE;
    }

    @Override
    public StreamTagVisitor.ValueResult visit(byte param0) {
        this.appendEntry(ByteTag.valueOf(param0));
        return StreamTagVisitor.ValueResult.CONTINUE;
    }

    @Override
    public StreamTagVisitor.ValueResult visit(short param0) {
        this.appendEntry(ShortTag.valueOf(param0));
        return StreamTagVisitor.ValueResult.CONTINUE;
    }

    @Override
    public StreamTagVisitor.ValueResult visit(int param0) {
        this.appendEntry(IntTag.valueOf(param0));
        return StreamTagVisitor.ValueResult.CONTINUE;
    }

    @Override
    public StreamTagVisitor.ValueResult visit(long param0) {
        this.appendEntry(LongTag.valueOf(param0));
        return StreamTagVisitor.ValueResult.CONTINUE;
    }

    @Override
    public StreamTagVisitor.ValueResult visit(float param0) {
        this.appendEntry(FloatTag.valueOf(param0));
        return StreamTagVisitor.ValueResult.CONTINUE;
    }

    @Override
    public StreamTagVisitor.ValueResult visit(double param0) {
        this.appendEntry(DoubleTag.valueOf(param0));
        return StreamTagVisitor.ValueResult.CONTINUE;
    }

    @Override
    public StreamTagVisitor.ValueResult visit(byte[] param0) {
        this.appendEntry(new ByteArrayTag(param0));
        return StreamTagVisitor.ValueResult.CONTINUE;
    }

    @Override
    public StreamTagVisitor.ValueResult visit(int[] param0) {
        this.appendEntry(new IntArrayTag(param0));
        return StreamTagVisitor.ValueResult.CONTINUE;
    }

    @Override
    public StreamTagVisitor.ValueResult visit(long[] param0) {
        this.appendEntry(new LongArrayTag(param0));
        return StreamTagVisitor.ValueResult.CONTINUE;
    }

    @Override
    public StreamTagVisitor.ValueResult visitList(TagType<?> param0, int param1) {
        return StreamTagVisitor.ValueResult.CONTINUE;
    }

    @Override
    public StreamTagVisitor.EntryResult visitElement(TagType<?> param0, int param1) {
        this.enterContainerIfNeeded(param0);
        return StreamTagVisitor.EntryResult.ENTER;
    }

    @Override
    public StreamTagVisitor.EntryResult visitEntry(TagType<?> param0) {
        return StreamTagVisitor.EntryResult.ENTER;
    }

    @Override
    public StreamTagVisitor.EntryResult visitEntry(TagType<?> param0, String param1) {
        this.lastId = param1;
        this.enterContainerIfNeeded(param0);
        return StreamTagVisitor.EntryResult.ENTER;
    }

    private void enterContainerIfNeeded(TagType<?> param0) {
        if (param0 == ListTag.TYPE) {
            ListTag var0 = new ListTag();
            this.appendEntry(var0);
            this.consumerStack.addLast(var0::add);
        } else if (param0 == CompoundTag.TYPE) {
            CompoundTag var1 = new CompoundTag();
            this.appendEntry(var1);
            this.consumerStack.addLast(param1 -> var1.put(this.lastId, param1));
        }

    }

    @Override
    public StreamTagVisitor.ValueResult visitContainerEnd() {
        this.consumerStack.removeLast();
        return StreamTagVisitor.ValueResult.CONTINUE;
    }

    @Override
    public StreamTagVisitor.ValueResult visitRootEntry(TagType<?> param0) {
        if (param0 == ListTag.TYPE) {
            ListTag var0 = new ListTag();
            this.rootTag = var0;
            this.consumerStack.addLast(var0::add);
        } else if (param0 == CompoundTag.TYPE) {
            CompoundTag var1 = new CompoundTag();
            this.rootTag = var1;
            this.consumerStack.addLast(param1 -> var1.put(this.lastId, param1));
        } else {
            this.consumerStack.addLast(param0x -> this.rootTag = param0x);
        }

        return StreamTagVisitor.ValueResult.CONTINUE;
    }
}
