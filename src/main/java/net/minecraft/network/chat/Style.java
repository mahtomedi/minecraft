package net.minecraft.network.chat;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;
import java.util.Objects;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.util.GsonHelper;

public class Style {
    private Style parent;
    private ChatFormatting color;
    private Boolean bold;
    private Boolean italic;
    private Boolean underlined;
    private Boolean strikethrough;
    private Boolean obfuscated;
    private ClickEvent clickEvent;
    private HoverEvent hoverEvent;
    private String insertion;
    private static final Style ROOT = new Style() {
        @Nullable
        @Override
        public ChatFormatting getColor() {
            return null;
        }

        @Override
        public boolean isBold() {
            return false;
        }

        @Override
        public boolean isItalic() {
            return false;
        }

        @Override
        public boolean isStrikethrough() {
            return false;
        }

        @Override
        public boolean isUnderlined() {
            return false;
        }

        @Override
        public boolean isObfuscated() {
            return false;
        }

        @Nullable
        @Override
        public ClickEvent getClickEvent() {
            return null;
        }

        @Nullable
        @Override
        public HoverEvent getHoverEvent() {
            return null;
        }

        @Nullable
        @Override
        public String getInsertion() {
            return null;
        }

        @Override
        public Style setColor(ChatFormatting param0) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Style setBold(Boolean param0) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Style setItalic(Boolean param0) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Style setStrikethrough(Boolean param0) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Style setUnderlined(Boolean param0) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Style setObfuscated(Boolean param0) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Style setClickEvent(ClickEvent param0) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Style setHoverEvent(HoverEvent param0) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Style inheritFrom(Style param0) {
            throw new UnsupportedOperationException();
        }

        @Override
        public String toString() {
            return "Style.ROOT";
        }

        @Override
        public Style copy() {
            return this;
        }

        @Override
        public Style flatCopy() {
            return this;
        }

        @Override
        public String getLegacyFormatCodes() {
            return "";
        }
    };

    @Nullable
    public ChatFormatting getColor() {
        return this.color == null ? this.getParent().getColor() : this.color;
    }

    public boolean isBold() {
        return this.bold == null ? this.getParent().isBold() : this.bold;
    }

    public boolean isItalic() {
        return this.italic == null ? this.getParent().isItalic() : this.italic;
    }

    public boolean isStrikethrough() {
        return this.strikethrough == null ? this.getParent().isStrikethrough() : this.strikethrough;
    }

    public boolean isUnderlined() {
        return this.underlined == null ? this.getParent().isUnderlined() : this.underlined;
    }

    public boolean isObfuscated() {
        return this.obfuscated == null ? this.getParent().isObfuscated() : this.obfuscated;
    }

    public boolean isEmpty() {
        return this.bold == null
            && this.italic == null
            && this.strikethrough == null
            && this.underlined == null
            && this.obfuscated == null
            && this.color == null
            && this.clickEvent == null
            && this.hoverEvent == null
            && this.insertion == null;
    }

    @Nullable
    public ClickEvent getClickEvent() {
        return this.clickEvent == null ? this.getParent().getClickEvent() : this.clickEvent;
    }

    @Nullable
    public HoverEvent getHoverEvent() {
        return this.hoverEvent == null ? this.getParent().getHoverEvent() : this.hoverEvent;
    }

    @Nullable
    public String getInsertion() {
        return this.insertion == null ? this.getParent().getInsertion() : this.insertion;
    }

    public Style setColor(ChatFormatting param0) {
        this.color = param0;
        return this;
    }

    public Style setBold(Boolean param0) {
        this.bold = param0;
        return this;
    }

    public Style setItalic(Boolean param0) {
        this.italic = param0;
        return this;
    }

    public Style setStrikethrough(Boolean param0) {
        this.strikethrough = param0;
        return this;
    }

    public Style setUnderlined(Boolean param0) {
        this.underlined = param0;
        return this;
    }

    public Style setObfuscated(Boolean param0) {
        this.obfuscated = param0;
        return this;
    }

    public Style setClickEvent(ClickEvent param0) {
        this.clickEvent = param0;
        return this;
    }

    public Style setHoverEvent(HoverEvent param0) {
        this.hoverEvent = param0;
        return this;
    }

    public Style setInsertion(String param0) {
        this.insertion = param0;
        return this;
    }

    public Style inheritFrom(Style param0) {
        this.parent = param0;
        return this;
    }

    public String getLegacyFormatCodes() {
        if (this.isEmpty()) {
            return this.parent != null ? this.parent.getLegacyFormatCodes() : "";
        } else {
            StringBuilder var0 = new StringBuilder();
            if (this.getColor() != null) {
                var0.append(this.getColor());
            }

            if (this.isBold()) {
                var0.append(ChatFormatting.BOLD);
            }

            if (this.isItalic()) {
                var0.append(ChatFormatting.ITALIC);
            }

            if (this.isUnderlined()) {
                var0.append(ChatFormatting.UNDERLINE);
            }

            if (this.isObfuscated()) {
                var0.append(ChatFormatting.OBFUSCATED);
            }

            if (this.isStrikethrough()) {
                var0.append(ChatFormatting.STRIKETHROUGH);
            }

            return var0.toString();
        }
    }

    private Style getParent() {
        return this.parent == null ? ROOT : this.parent;
    }

    @Override
    public String toString() {
        return "Style{hasParent="
            + (this.parent != null)
            + ", color="
            + this.color
            + ", bold="
            + this.bold
            + ", italic="
            + this.italic
            + ", underlined="
            + this.underlined
            + ", obfuscated="
            + this.obfuscated
            + ", clickEvent="
            + this.getClickEvent()
            + ", hoverEvent="
            + this.getHoverEvent()
            + ", insertion="
            + this.getInsertion()
            + '}';
    }

    @Override
    public boolean equals(Object param0) {
        if (this == param0) {
            return true;
        } else if (!(param0 instanceof Style)) {
            return false;
        } else {
            Style var0 = (Style)param0;
            if (this.isBold() == var0.isBold()
                && this.getColor() == var0.getColor()
                && this.isItalic() == var0.isItalic()
                && this.isObfuscated() == var0.isObfuscated()
                && this.isStrikethrough() == var0.isStrikethrough()
                && this.isUnderlined() == var0.isUnderlined()) {
                if (this.getClickEvent() != null) {
                    if (!this.getClickEvent().equals(var0.getClickEvent())) {
                        return false;
                    }
                } else if (var0.getClickEvent() != null) {
                    return false;
                }

                if (this.getHoverEvent() != null) {
                    if (!this.getHoverEvent().equals(var0.getHoverEvent())) {
                        return false;
                    }
                } else if (var0.getHoverEvent() != null) {
                    return false;
                }

                if (this.getInsertion() != null) {
                    if (this.getInsertion().equals(var0.getInsertion())) {
                        return true;
                    }
                } else if (var0.getInsertion() == null) {
                    return true;
                }
            }

            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            this.color, this.bold, this.italic, this.underlined, this.strikethrough, this.obfuscated, this.clickEvent, this.hoverEvent, this.insertion
        );
    }

    public Style copy() {
        Style var0 = new Style();
        var0.bold = this.bold;
        var0.italic = this.italic;
        var0.strikethrough = this.strikethrough;
        var0.underlined = this.underlined;
        var0.obfuscated = this.obfuscated;
        var0.color = this.color;
        var0.clickEvent = this.clickEvent;
        var0.hoverEvent = this.hoverEvent;
        var0.parent = this.parent;
        var0.insertion = this.insertion;
        return var0;
    }

    public Style flatCopy() {
        Style var0 = new Style();
        var0.setBold(this.isBold());
        var0.setItalic(this.isItalic());
        var0.setStrikethrough(this.isStrikethrough());
        var0.setUnderlined(this.isUnderlined());
        var0.setObfuscated(this.isObfuscated());
        var0.setColor(this.getColor());
        var0.setClickEvent(this.getClickEvent());
        var0.setHoverEvent(this.getHoverEvent());
        var0.setInsertion(this.getInsertion());
        return var0;
    }

    public static class Serializer implements JsonDeserializer<Style>, JsonSerializer<Style> {
        @Nullable
        public Style deserialize(JsonElement param0, Type param1, JsonDeserializationContext param2) throws JsonParseException {
            if (param0.isJsonObject()) {
                Style var0 = new Style();
                JsonObject var1 = param0.getAsJsonObject();
                if (var1 == null) {
                    return null;
                } else {
                    if (var1.has("bold")) {
                        var0.bold = var1.get("bold").getAsBoolean();
                    }

                    if (var1.has("italic")) {
                        var0.italic = var1.get("italic").getAsBoolean();
                    }

                    if (var1.has("underlined")) {
                        var0.underlined = var1.get("underlined").getAsBoolean();
                    }

                    if (var1.has("strikethrough")) {
                        var0.strikethrough = var1.get("strikethrough").getAsBoolean();
                    }

                    if (var1.has("obfuscated")) {
                        var0.obfuscated = var1.get("obfuscated").getAsBoolean();
                    }

                    if (var1.has("color")) {
                        var0.color = param2.deserialize(var1.get("color"), ChatFormatting.class);
                    }

                    if (var1.has("insertion")) {
                        var0.insertion = var1.get("insertion").getAsString();
                    }

                    if (var1.has("clickEvent")) {
                        JsonObject var2 = GsonHelper.getAsJsonObject(var1, "clickEvent");
                        String var3 = GsonHelper.getAsString(var2, "action", null);
                        ClickEvent.Action var4 = var3 == null ? null : ClickEvent.Action.getByName(var3);
                        String var5 = GsonHelper.getAsString(var2, "value", null);
                        if (var4 != null && var5 != null && var4.isAllowedFromServer()) {
                            var0.clickEvent = new ClickEvent(var4, var5);
                        }
                    }

                    if (var1.has("hoverEvent")) {
                        JsonObject var6 = GsonHelper.getAsJsonObject(var1, "hoverEvent");
                        String var7 = GsonHelper.getAsString(var6, "action", null);
                        HoverEvent.Action var8 = var7 == null ? null : HoverEvent.Action.getByName(var7);
                        Component var9 = param2.deserialize(var6.get("value"), Component.class);
                        if (var8 != null && var9 != null && var8.isAllowedFromServer()) {
                            var0.hoverEvent = new HoverEvent(var8, var9);
                        }
                    }

                    return var0;
                }
            } else {
                return null;
            }
        }

        @Nullable
        public JsonElement serialize(Style param0, Type param1, JsonSerializationContext param2) {
            if (param0.isEmpty()) {
                return null;
            } else {
                JsonObject var0 = new JsonObject();
                if (param0.bold != null) {
                    var0.addProperty("bold", param0.bold);
                }

                if (param0.italic != null) {
                    var0.addProperty("italic", param0.italic);
                }

                if (param0.underlined != null) {
                    var0.addProperty("underlined", param0.underlined);
                }

                if (param0.strikethrough != null) {
                    var0.addProperty("strikethrough", param0.strikethrough);
                }

                if (param0.obfuscated != null) {
                    var0.addProperty("obfuscated", param0.obfuscated);
                }

                if (param0.color != null) {
                    var0.add("color", param2.serialize(param0.color));
                }

                if (param0.insertion != null) {
                    var0.add("insertion", param2.serialize(param0.insertion));
                }

                if (param0.clickEvent != null) {
                    JsonObject var1 = new JsonObject();
                    var1.addProperty("action", param0.clickEvent.getAction().getName());
                    var1.addProperty("value", param0.clickEvent.getValue());
                    var0.add("clickEvent", var1);
                }

                if (param0.hoverEvent != null) {
                    JsonObject var2 = new JsonObject();
                    var2.addProperty("action", param0.hoverEvent.getAction().getName());
                    var2.add("value", param2.serialize(param0.hoverEvent.getValue()));
                    var0.add("hoverEvent", var2);
                }

                return var0;
            }
        }
    }
}
