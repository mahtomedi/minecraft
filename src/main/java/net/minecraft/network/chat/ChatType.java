package net.minecraft.network.chat;

public enum ChatType {
    CHAT((byte)0, false),
    SYSTEM((byte)1, true),
    GAME_INFO((byte)2, true);

    private final byte index;
    private final boolean interrupt;

    private ChatType(byte param0, boolean param1) {
        this.index = param0;
        this.interrupt = param1;
    }

    public byte getIndex() {
        return this.index;
    }

    public static ChatType getForIndex(byte param0) {
        for(ChatType var0 : values()) {
            if (param0 == var0.index) {
                return var0;
            }
        }

        return CHAT;
    }

    public boolean shouldInterrupt() {
        return this.interrupt;
    }
}
