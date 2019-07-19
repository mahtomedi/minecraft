package net.minecraft.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;

public class EndTag implements Tag {
    @Override
    public void load(DataInput param0, int param1, NbtAccounter param2) throws IOException {
        param2.accountBits(64L);
    }

    @Override
    public void write(DataOutput param0) throws IOException {
    }

    @Override
    public byte getId() {
        return 0;
    }

    @Override
    public String toString() {
        return "END";
    }

    public EndTag copy() {
        return new EndTag();
    }

    @Override
    public Component getPrettyDisplay(String param0, int param1) {
        return new TextComponent("");
    }

    @Override
    public boolean equals(Object param0) {
        return param0 instanceof EndTag;
    }

    @Override
    public int hashCode() {
        return this.getId();
    }
}
