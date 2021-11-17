package net.minecraft.nbt.visitors;

import net.minecraft.nbt.StreamTagVisitor;
import net.minecraft.nbt.TagType;

public interface SkipAll extends StreamTagVisitor {
    SkipAll INSTANCE = new SkipAll() {
    };

    @Override
    default StreamTagVisitor.ValueResult visitEnd() {
        return StreamTagVisitor.ValueResult.CONTINUE;
    }

    @Override
    default StreamTagVisitor.ValueResult visit(String param0) {
        return StreamTagVisitor.ValueResult.CONTINUE;
    }

    @Override
    default StreamTagVisitor.ValueResult visit(byte param0) {
        return StreamTagVisitor.ValueResult.CONTINUE;
    }

    @Override
    default StreamTagVisitor.ValueResult visit(short param0) {
        return StreamTagVisitor.ValueResult.CONTINUE;
    }

    @Override
    default StreamTagVisitor.ValueResult visit(int param0) {
        return StreamTagVisitor.ValueResult.CONTINUE;
    }

    @Override
    default StreamTagVisitor.ValueResult visit(long param0) {
        return StreamTagVisitor.ValueResult.CONTINUE;
    }

    @Override
    default StreamTagVisitor.ValueResult visit(float param0) {
        return StreamTagVisitor.ValueResult.CONTINUE;
    }

    @Override
    default StreamTagVisitor.ValueResult visit(double param0) {
        return StreamTagVisitor.ValueResult.CONTINUE;
    }

    @Override
    default StreamTagVisitor.ValueResult visit(byte[] param0) {
        return StreamTagVisitor.ValueResult.CONTINUE;
    }

    @Override
    default StreamTagVisitor.ValueResult visit(int[] param0) {
        return StreamTagVisitor.ValueResult.CONTINUE;
    }

    @Override
    default StreamTagVisitor.ValueResult visit(long[] param0) {
        return StreamTagVisitor.ValueResult.CONTINUE;
    }

    @Override
    default StreamTagVisitor.ValueResult visitList(TagType<?> param0, int param1) {
        return StreamTagVisitor.ValueResult.CONTINUE;
    }

    @Override
    default StreamTagVisitor.EntryResult visitElement(TagType<?> param0, int param1) {
        return StreamTagVisitor.EntryResult.SKIP;
    }

    @Override
    default StreamTagVisitor.EntryResult visitEntry(TagType<?> param0) {
        return StreamTagVisitor.EntryResult.SKIP;
    }

    @Override
    default StreamTagVisitor.EntryResult visitEntry(TagType<?> param0, String param1) {
        return StreamTagVisitor.EntryResult.SKIP;
    }

    @Override
    default StreamTagVisitor.ValueResult visitContainerEnd() {
        return StreamTagVisitor.ValueResult.CONTINUE;
    }

    @Override
    default StreamTagVisitor.ValueResult visitRootEntry(TagType<?> param0) {
        return StreamTagVisitor.ValueResult.CONTINUE;
    }
}
