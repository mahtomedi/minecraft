package net.minecraft.server.packs;

public enum PackType {
    CLIENT_RESOURCES("assets"),
    SERVER_DATA("data");

    private final String directory;

    private PackType(String param0) {
        this.directory = param0;
    }

    public String getDirectory() {
        return this.directory;
    }
}
