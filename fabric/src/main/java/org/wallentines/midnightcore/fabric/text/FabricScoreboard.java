package org.wallentines.midnightcore.fabric.text;

import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundSetDisplayObjectivePacket;
import net.minecraft.network.protocol.game.ClientboundSetObjectivePacket;
import net.minecraft.network.protocol.game.ClientboundSetPlayerTeamPacket;
import net.minecraft.network.protocol.game.ClientboundSetScorePacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Score;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import org.wallentines.midnightcore.api.player.MPlayer;
import org.wallentines.midnightcore.api.server.MServer;
import org.wallentines.midnightcore.api.text.MComponent;
import org.wallentines.midnightcore.common.text.AbstractScoreboard;
import org.wallentines.midnightcore.fabric.player.FabricPlayer;
import org.wallentines.midnightcore.fabric.server.FabricServer;
import org.wallentines.midnightcore.fabric.util.ConversionUtil;

import java.util.ArrayList;
import java.util.List;

public class FabricScoreboard extends AbstractScoreboard {

    private final boolean[] updated = new boolean[15];
    private final Component[] lines = new Component[15];
    private Component mcTitle;
    private ServerData serverData;

    public FabricScoreboard(String id, MComponent title) {

        super(id, title);
        mcTitle = ConversionUtil.toComponent(title);
    }

    private ServerData getServerData(MServer server) {
        if(serverData == null) {
            serverData = new ServerData(server);
        }
        return serverData;
    }

    @Override
    public void setTitle(MComponent cmp) {
        super.setTitle(cmp);
        mcTitle = ConversionUtil.toComponent(cmp);

        if(serverData == null) return;
        serverData.objective.setDisplayName(mcTitle);
    }

    public void setLine(int line, MComponent message) {

        if(line < 1 || line > 15) return;

        Component mcLine = ConversionUtil.toComponent(message);
        lines[line] = mcLine;

        if(serverData == null) return;

        updateLine(line, serverData);
        updated[line] = true;
    }

    @Override
    protected void onPlayerAdded(MPlayer u) {

        ServerData data = getServerData(u.getServer());

        ServerPlayer player = FabricPlayer.getInternal(u);
        if(player == null) return;

        player.connection.send(new ClientboundSetObjectivePacket(data.objective, 0));

        for(int i = 0 ; i < data.teams.length ; i++) {
            player.connection.send(ClientboundSetPlayerTeamPacket.createAddOrModifyPacket(data.teams[i], true));

            String name = "§" + Integer.toHexString(i);
            Score s = data.board.getOrCreatePlayerScore(name, data.objective);
            if(s.getScore() > 0) {
                player.connection.send(new ClientboundSetScorePacket(ServerScoreboard.Method.CHANGE, data.objective.getName(), name, s.getScore()));
            }
        }

        player.connection.send(new ClientboundSetDisplayObjectivePacket(1, data.objective));

    }

    @Override
    protected void onPlayerRemoved(MPlayer u) {

        ServerData data = getServerData(u.getServer());

        ServerPlayer player = FabricPlayer.getInternal(u);
        if(player == null) return;

        player.connection.send(new ClientboundSetDisplayObjectivePacket(1, null));

        for(int i = 0 ; i < data.teams.length ; i++) {
            player.connection.send(ClientboundSetPlayerTeamPacket.createRemovePacket(data.teams[i]));

            String name = "§" + Integer.toHexString(i);
            player.connection.send(new ClientboundSetScorePacket(ServerScoreboard.Method.REMOVE, data.objective.getName(), name, 0));

        }

        player.connection.send(new ClientboundSetObjectivePacket(data.objective, 1));

        if(players.size() == 0) {
            data.destroy();
            serverData = null;
        }

    }

    public void update() {

        if(serverData == null) return;

        List<Packet<?>> packets = new ArrayList<>();

        if(serverData.objective.getDisplayName() != mcTitle) {

            serverData.objective.setDisplayName(mcTitle);
            packets.add(new ClientboundSetObjectivePacket(serverData.objective, 2));
        }

        for(int i = 0 ; i < serverData.teams.length ; i++) {
            if(updated[i]) {

                packets.add(ClientboundSetPlayerTeamPacket.createAddOrModifyPacket(serverData.teams[i], false));
                packets.add(new ClientboundSetScorePacket(ServerScoreboard.Method.CHANGE, serverData.objective.getName(), "§" + Integer.toHexString(i), i));

                updated[i] = false;
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

    private void updateLine(int line, ServerData data) {

        Component text = lines[line];
        if(text == null) {
            data.teams[line].setPlayerPrefix(null);
            data.board.resetPlayerScore("§" + Integer.toHexString(line), serverData.objective);
        } else {
            data.board.getOrCreatePlayerScore("§" + Integer.toHexString(line), serverData.objective).setScore(line);
            data.teams[line].setPlayerPrefix(text);
        }
    }


    private class ServerData {

        final ServerScoreboard board;
        final Objective objective;
        final PlayerTeam[] teams = new PlayerTeam[15];

        ServerData(MServer server) {

            MinecraftServer mc = ((FabricServer) server).getInternal();

            this.board = mc.getScoreboard();
            this.objective = new Objective(board, id, ObjectiveCriteria.DUMMY, mcTitle, ObjectiveCriteria.RenderType.INTEGER);

            String teamPrefix = id;
            if(teamPrefix.length() > 15) {
                teamPrefix = teamPrefix.substring(0,14);
            }

            for(int i = 0 ; i < teams.length ; i++) {

                teams[i] = new PlayerTeam(board, teamPrefix + Integer.toHexString(i));
                teams[i].getPlayers().add("§" + Integer.toHexString(i));

                updateLine(i, serverData);

            }
        }

        void destroy() {
            board.removeObjective(objective);
            for(PlayerTeam team : teams) {
                board.removePlayerTeam(team);
            }
        }

    }

}
