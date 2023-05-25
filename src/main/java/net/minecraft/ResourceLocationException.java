package net.minecraft;

import org.apache.commons.lang3.StringEscapeUtils;

public class ResourceLocationException extends RuntimeException {
    public ResourceLocationException(String param0) {
        super(StringEscapeUtils.escapeJava(param0));
    }

    public ResourceLocationException(String param0, Throwable param1) {
        super(StringEscapeUtils.escapeJava(param0), param1);
    }
}
