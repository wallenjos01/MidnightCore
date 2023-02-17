package org.wallentines.midnightcore.fabric.text;

import io.netty.buffer.Unpooled;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundSetDisplayObjectivePacket;
import net.minecraft.network.protocol.game.ClientboundSetObjectivePacket;
import net.minecraft.network.protocol.game.ClientboundSetPlayerTeamPacket;
import net.minecraft.network.protocol.game.ClientboundSetScorePacket;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.scores.Team;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import org.wallentines.midnightcore.api.player.MPlayer;
import org.wallentines.midnightcore.api.text.MComponent;
import org.wallentines.midnightcore.common.text.AbstractScoreboard;
import org.wallentines.midnightcore.fabric.player.FabricPlayer;
import org.wallentines.midnightcore.fabric.util.ConversionUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class FabricScoreboard extends AbstractScoreboard {

    private final TeamInfo[] teams = new TeamInfo[15];
    private boolean titleUpdated = false;
    private Component mcTitle;

    public FabricScoreboard(String id, MComponent title) {

        super(id, title);
        mcTitle = ConversionUtil.toComponent(title);

        String teamPrefix = id;
        if(teamPrefix.length() > 15) {
            teamPrefix = teamPrefix.substring(0,14);
        }

        for(int i = 0 ; i < teams.length ; i++) {
            teams[i] = new TeamInfo(teamPrefix, i, null);
        }
    }

    @Override
    public void setTitle(MComponent cmp) {
        super.setTitle(cmp);

        mcTitle = ConversionUtil.toComponent(cmp);
        titleUpdated = true;
    }

    public void setLine(int line, MComponent message) {

        if(line < 1 || line > 15) return;

        Component mcLine = ConversionUtil.toComponent(message);
        teams[line].setPrefix(mcLine);
    }

    @Override
    protected void onPlayerAdded(MPlayer u) {

        ServerPlayer player = FabricPlayer.getInternal(u);
        if(player == null) return;

        player.connection.send(createUpdateObjectivePacket(id, mcTitle, true));

        for (TeamInfo team : teams) {

            ClientboundSetPlayerTeamPacket addTeam = team.addPacket();
            player.connection.send(addTeam);
            if (team.getPrefix() != null) {
                player.connection.send(team.scorePacket());
            }
        }

        player.connection.send(createDisplayObjectivePacket(id));

    }

    @Override
    protected void onPlayerRemoved(MPlayer u) {

        ServerPlayer player = FabricPlayer.getInternal(u);
        if(player == null) return;

        player.connection.send(createDisplayObjectivePacket(null));

        for (TeamInfo team : teams) {
            player.connection.send(team.removePacket());
            player.connection.send(team.removeScorePacket());
        }

        player.connection.send(createRemoveObjectivePacket(id));
    }

    public void update() {

        List<Packet<?>> packets = new ArrayList<>();

        if(titleUpdated) {
            packets.add(createUpdateObjectivePacket(id, mcTitle, false));
        }

        for (TeamInfo team : teams) {
            if (team.isUpdated()) {
                packets.add(team.updatePacket());
                packets.add(team.scorePacket());
            }
        }

        for(MPlayer u : players) {

            ServerPlayer player = FabricPlayer.getInternal(u);
            if(player == null) continue;

            for(Packet<?> pck : packets) {
                player.connection.send(pck);
            }
        }
    }

    private class TeamInfo {

        private final int index;
        private final String userName;
        private final TeamBuilder builder;
        private boolean updated = false;

        public TeamInfo(String name, int index, Component prefix) {

            String hexIndex = Integer.toHexString(index);

            this.index = index;
            this.userName = "§" + hexIndex;
            this.builder = new TeamBuilder(name + hexIndex).prefix(prefix).addMember(userName);
        }

        public boolean isUpdated() {
            return updated;
        }

        public Component getPrefix() {
            return this.builder.getPrefix();
        }

        public void setPrefix(Component prefix) {
            this.builder.prefix(prefix);
            this.updated = true;
        }

        public ClientboundSetPlayerTeamPacket addPacket() {
            return builder.addPacket();
        }

        public ClientboundSetPlayerTeamPacket updatePacket() {
            updated = false;
            return builder.updatePacket();
        }

        public ClientboundSetPlayerTeamPacket removePacket() {
            return builder.removePacket();
        }

        public ClientboundSetScorePacket scorePacket() {

            if(builder.getPrefix() == null) {
                return removeScorePacket();
            }

            return new ClientboundSetScorePacket(ServerScoreboard.Method.CHANGE, id, userName, index);
        }

        public ClientboundSetScorePacket removeScorePacket() {
            return new ClientboundSetScorePacket(ServerScoreboard.Method.REMOVE, id, userName, 0);
        }
    }

    @Deprecated
    public static ClientboundSetPlayerTeamPacket createUpdateTeamPacket(String name, Component prefix, String playerName, boolean add) {
        return createUpdateTeamPacket(name, prefix, playerName == null ? new ArrayList<>() : List.of(playerName), add, ChatFormatting.WHITE);
    }
    @Deprecated
    public static ClientboundSetPlayerTeamPacket createUpdateTeamPacket(String name, Component prefix, Collection<String> playerNames, boolean add, ChatFormatting color) {

        FriendlyByteBuf fakeBuf = new FriendlyByteBuf(Unpooled.buffer());
        fakeBuf.writeUtf(name); // Team name
        fakeBuf.writeByte(add ? 0 : 2); // Action (0 = ADD, 2 = UPDATE)

        Component realPrefix = prefix == null ? Component.empty() : prefix;

        // Parameters
        fakeBuf.writeComponent(Component.empty()); // Team Display Name
        fakeBuf.writeByte(0); // Options
        fakeBuf.writeUtf(Team.Visibility.ALWAYS.name, 40); // Name Tag Visibility
        fakeBuf.writeUtf(Team.CollisionRule.ALWAYS.name, 40); // Collision
        fakeBuf.writeEnum(color); // Team Color
        fakeBuf.writeComponent(realPrefix); // Team Prefix
        fakeBuf.writeComponent(Component.empty()); // Team Suffix

        // Players
        if(playerNames.size() > 0) {
            fakeBuf.writeVarInt(playerNames.size());

            for(String s : playerNames) {
                fakeBuf.writeUtf(s);
            }
        }

        return new ClientboundSetPlayerTeamPacket(fakeBuf);
    }

    @Deprecated
    public static ClientboundSetPlayerTeamPacket createRemoveTeamPacket(String name) {

        FriendlyByteBuf fakeBuf = new FriendlyByteBuf(Unpooled.buffer());
        fakeBuf.writeUtf(name); // Team name
        fakeBuf.writeByte(1); // Action (1 = REMOVE)

        return new ClientboundSetPlayerTeamPacket(fakeBuf);
    }

    private static ClientboundSetObjectivePacket createUpdateObjectivePacket(String id, Component title, boolean add) {


        FriendlyByteBuf fakeBuf = new FriendlyByteBuf(Unpooled.buffer());
        fakeBuf.writeUtf(id); // Objective ID
        fakeBuf.writeByte(add ? 0 : 2); // Action (0 = ADD, 2 = UPDATE)

        fakeBuf.writeComponent(title);
        fakeBuf.writeEnum(ObjectiveCriteria.RenderType.INTEGER);

        return new ClientboundSetObjectivePacket(fakeBuf);

    }

    private static ClientboundSetObjectivePacket createRemoveObjectivePacket(String id) {

        FriendlyByteBuf fakeBuf = new FriendlyByteBuf(Unpooled.buffer());
        fakeBuf.writeUtf(id); // Objective ID
        fakeBuf.writeByte(1); // Action (1 = REMOVE)

        return new ClientboundSetObjectivePacket(fakeBuf);
    }

    private static ClientboundSetDisplayObjectivePacket createDisplayObjectivePacket(String id) {

        FriendlyByteBuf fakeBuf = new FriendlyByteBuf(Unpooled.buffer());
        fakeBuf.writeByte(1); // Position (1 = SIDEBAR)
        fakeBuf.writeUtf(id == null ? "" : id); // Objective ID

        return new ClientboundSetDisplayObjectivePacket(fakeBuf);
    }

}
