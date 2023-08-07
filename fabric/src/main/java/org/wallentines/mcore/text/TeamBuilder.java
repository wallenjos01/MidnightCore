package org.wallentines.mcore.text;

import io.netty.buffer.Unpooled;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundSetPlayerTeamPacket;
import net.minecraft.world.scores.Team;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import net.minecraft.network.chat.Component;

/**
 * A builder class for creating scoreboard team info
 */
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

    /**
     * Constructs a new team builder with the given ID
     * @param id The team's ID
     */
    public TeamBuilder(String id) {
        this.id = id;
    }

    /**
     * Sets the team's display name
     * @param displayName The team's display name
     * @return A reference to self
     */
    public TeamBuilder displayName(Component displayName) {
        this.displayName = displayName;
        return this;
    }

    /**
     * Allows friendly fire on the new team
     * @return A reference to self
     */
    public TeamBuilder allowFriendlyFire() {
        this.allowFriendlyFire = true;
        return this;
    }

    /**
     * Makes it so players on this team can see each other even when they are invisible
     * @return A reference to self
     */
    public TeamBuilder seeFriendlyInvisibles() {
        this.seeFriendlyInvisibles = true;
        return this;
    }

    /**
     * Changes the name tag visibility type of the team
     * @param visibility The new visibility type
     * @return A reference to self
     */
    public TeamBuilder nameTagVisibility(Team.Visibility visibility) {
        this.visibility = visibility;
        return this;
    }

    /**
     * Changes the collision type of the team
     * @param collision The new collision type
     * @return A reference to self
     */
    public TeamBuilder collision(Team.CollisionRule collision) {
        this.collision = collision;
        return this;
    }

    /**
     * Changes the display color of the team
     * @param color The new team color
     * @return A reference to self
     */
    public TeamBuilder color(ChatFormatting color) {
        this.color = color;
        return this;
    }

    /**
     * Changes the name tag prefix of the team
     * @param prefix The new team prefix
     * @return A reference to self
     */
    public TeamBuilder prefix(Component prefix) {
        this.prefix = prefix;
        return this;
    }

    /**
     * Changes the name tag suffix of the team
     * @param suffix The new team prefix
     * @return A reference to self
     */
    public TeamBuilder suffix(Component suffix) {
        this.suffix = suffix;
        return this;
    }

    /**
     * Adds a member entry to the team.
     * @param member The entry to add
     * @return A reference to self
     */
    public TeamBuilder addMember(String member) {
        this.playerNames.add(member);
        return this;
    }

    /**
     * Removes a member entry from the team.
     * @param member The entry to remove
     * @return A reference to self
     */
    public TeamBuilder removeMember(String member) {
        this.playerNames.remove(member);
        return this;
    }

    /**
     * Gets the team's ID
     * @return The team's ID
     */
    public String getId() {
        return id;
    }

    /**
     * Gets the team's display name
     * @return The team's display name
     */
    public Component getDisplayName() {
        return displayName;
    }

    /**
     * Gets whether the team allows friendly fire
     * @return Whether friendly fire is allowed
     */
    public boolean isAllowFriendlyFire() {
        return allowFriendlyFire;
    }

    /**
     * Gets whether members of the team can see invisible players on the same team
     * @return Whether players can see friendly invisible players
     */
    public boolean isSeeFriendlyInvisibles() {
        return seeFriendlyInvisibles;
    }

    /**
     * Gets the name tag visibility type of the team
     * @return The name tag visibility type
     */
    public Team.Visibility getNameTagVisibility() {
        return visibility;
    }

    /**
     * Gets the collision type of the team
     * @return The collision type
     */
    public Team.CollisionRule getCollision() {
        return collision;
    }

    /**
     * Gets the team's color
     * @return The team's color
     */
    public ChatFormatting getColor() {
        return color;
    }

    /**
     * Gets the team's name tag prefix
     * @return The team's prefix
     */
    public Component getPrefix() {
        return prefix;
    }

    /**
     * Gets the team's name tag suffix
     * @return The team's suffix
     */
    public Component getSuffix() {
        return suffix;
    }

    /**
     * Gets a list of member entries on the team
     * @return The team's members
     */
    public Set<String> getMembers() {
        return playerNames;
    }

    /**
     * Generates a packet which adds the team to client's scoreboards
     * @return A new packet
     */
    public ClientboundSetPlayerTeamPacket addPacket() {
        return addOrUpdate(true);
    }

    /**
     * Generates a packet which updates the team on client's scoreboards
     * @return A new packet
     */
    public ClientboundSetPlayerTeamPacket updatePacket() {
        return addOrUpdate(false);
    }

    /**
     * Generates a packet which adds players to the team on client's scoreboards
     * @return A new packet
     */
    public ClientboundSetPlayerTeamPacket addMembersPacket(Collection<String> members) {
        return players(true, members);
    }

    /**
     * Generates a packet which removes players from the team on client's scoreboards
     * @return A new packet
     */
    public ClientboundSetPlayerTeamPacket removeMembersPacket(Collection<String> members) {
        return players(false, members);
    }

    /**
     * Generates a packet which removes the team on client's scoreboards
     * @return A new packet
     */
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

        // Packed Options
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
        if(!players.isEmpty()) {
            fakeBuf.writeVarInt(players.size());

            for(String s : players) {
                fakeBuf.writeUtf(s);
            }
        }

        return new ClientboundSetPlayerTeamPacket(fakeBuf);
    }

}
