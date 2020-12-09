package me.maxouxax.vortex.forwarding;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.UUID;

public class ForwardedChannel {

    private UUID uuid;
    private Guild guild;
    private TextChannel source, target;
    private Role role;

    public ForwardedChannel(UUID uuid, Guild guild, TextChannel source, TextChannel target, Role role) {
        this.uuid = uuid;
        this.guild = guild;
        this.source = source;
        this.target = target;
        this.role = role;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public Guild getGuild() {
        return guild;
    }

    public void setGuild(Guild guild) {
        this.guild = guild;
    }

    public TextChannel getSource() {
        return source;
    }

    public void setSource(TextChannel source) {
        this.source = source;
    }

    public TextChannel getTarget() {
        return target;
    }

    public void setTarget(TextChannel target) {
        this.target = target;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}
