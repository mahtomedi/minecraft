package com.mojang.realmsclient.dto;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PlayerInfo extends ValueObject {
    private String name;
    private String uuid;
    private boolean operator = false;
    private boolean accepted = false;
    private boolean online = false;

    public String getName() {
        return this.name;
    }

    public void setName(String param0) {
        this.name = param0;
    }

    public String getUuid() {
        return this.uuid;
    }

    public void setUuid(String param0) {
        this.uuid = param0;
    }

    public boolean isOperator() {
        return this.operator;
    }

    public void setOperator(boolean param0) {
        this.operator = param0;
    }

    public boolean getAccepted() {
        return this.accepted;
    }

    public void setAccepted(boolean param0) {
        this.accepted = param0;
    }

    public boolean getOnline() {
        return this.online;
    }

    public void setOnline(boolean param0) {
        this.online = param0;
    }
}
