package org.wallentines.midnightcore.fabric.text;

import io.netty.buffer.Unpooled;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetPlayerTeamPacket;
import net.minecraft.world.scores.Team;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class TeamBuilder {

    private final String id;
    private Component displayName = Component.empty();
    private boolean allowFriendlyFire = false;
    private boolean seeFriendlyInvisibles = false;
    private Team.Visibility visibility = Team.Visibility.ALWAYS;
    private Team.CollisionRule collision = Team.CollisionRule.ALWAYS;
    private ChatFormatting color = ChatFormatting.WHITE;
    private Component prefix = Component.empty();
    private Component suffix = Component.empty();
    private final Set<String> playerNames = new HashSet<>();

    public TeamBuilder(String id) {
        this.id = id;
    }

    public TeamBuilder displayName(Component displayName) {
        this.displayName = displayName;
        return this;
    }

    public TeamBuilder allowFriendlyFire() {
        this.allowFriendlyFire = true;
        return this;
    }

    public TeamBuilder seeFriendlyInvisibles() {
        this.seeFriendlyInvisibles = true;
        return this;
    }

    public TeamBuilder nameTagVisibility(Team.Visibility visibility) {
        this.visibility = visibility;
        return this;
    }

    public TeamBuilder collision(Team.CollisionRule collision) {
        this.collision = collision;
        return this;
    }

    public TeamBuilder color(ChatFormatting color) {
        this.color = color;
        return this;
    }

    public TeamBuilder prefix(Component prefix) {
        this.prefix = prefix;
        return this;
    }

    public TeamBuilder suffix(Component suffix) {
        this.suffix = suffix;
        return this;
    }

    public TeamBuilder addMember(String member) {
        this.playerNames.add(member);
        return this;
    }

    public TeamBuilder removeMember(String member) {
        this.playerNames.remove(member);
        return this;
    }

    public String getId() {
        return id;
    }

    public Component getDisplayName() {
        return displayName;
    }

    public boolean isAllowFriendlyFire() {
        return allowFriendlyFire;
    }

    public boolean isSeeFriendlyInvisibles() {
        return seeFriendlyInvisibles;
    }

    public Team.Visibility getNameTagVisibility() {
        return visibility;
    }

    public Team.CollisionRule getCollision() {
        return collision;
    }

    public ChatFormatting getColor() {
        return color;
    }

    public Component getPrefix() {
        return prefix;
    }

    public Component getSuffix() {
        return suffix;
    }

    public Set<String> getMembers() {
        return playerNames;
    }

    public ClientboundSetPlayerTeamPacket addPacket() {
        return addOrUpdate(true);
    }

    public ClientboundSetPlayerTeamPacket updatePacket() {
        return addOrUpdate(false);
    }

    public ClientboundSetPlayerTeamPacket addMembersPacket(Collection<String> members) {
        return players(true, members);
    }

    public ClientboundSetPlayerTeamPacket removeMembersPacket(Collection<String> members) {
        return players(false, members);
    }

    public ClientboundSetPlayerTeamPacket removePacket() {

        FriendlyByteBuf fakeBuf = new FriendlyByteBuf(Unpooled.buffer());
        fakeBuf.writeUtf(id); // Team name
        fakeBuf.writeByte(1); // Action (1 = REMOVE)

        return new ClientboundSetPlayerTeamPacket(fakeBuf);
    }

    private ClientboundSetPlayerTeamPacket addOrUpdate(boolean add) {

        FriendlyByteBuf fakeBuf = new FriendlyByteBuf(Unpooled.buffer());
        fakeBuf.writeUtf(id); // Team ID
        fakeBuf.writeByte(add ? 0 : 2); // Action (0 = ADD, 2 = UPDATE)

        // Pack Options
        int options = 0;
        if (allowFriendlyFire) options |= 1;
        if (seeFriendlyInvisibles) options |= 2;

        // Parameters
        fakeBuf.writeComponent(displayName == null ? Component.empty() : displayName); // Team Display Name
        fakeBuf.writeByte(options); // Options
        fakeBuf.writeUtf(visibility.name, 40); // Name Tag Visibility
        fakeBuf.writeUtf(collision.name, 40); // Collision
        fakeBuf.writeEnum(color); // Team Color
        fakeBuf.writeComponent(prefix == null ? Component.empty() : prefix); // Team Prefix
        fakeBuf.writeComponent(suffix == null ? Component.empty() : suffix); // Team Suffix

        // Players
        if(add) {
            fakeBuf.writeVarInt(playerNames.size());

            for(String s : playerNames) {
                fakeBuf.writeUtf(s);
            }
        }

        return new ClientboundSetPlayerTeamPacket(fakeBuf);
    }

    private ClientboundSetPlayerTeamPacket players(boolean add, Collection<String> players) {

        FriendlyByteBuf fakeBuf = new FriendlyByteBuf(Unpooled.buffer());
        fakeBuf.writeUtf(id); // Team ID
        fakeBuf.writeByte(add ? 3 : 4); // Action (3 = ADD, 4 = REMOVE)

        // Players
        if(players.size() > 0) {
            fakeBuf.writeVarInt(players.size());

            for(String s : players) {
                fakeBuf.writeUtf(s);
            }
        }

        return new ClientboundSetPlayerTeamPacket(fakeBuf);
    }

}
