package org.wallentines.midnightcore.fabric.text;

import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundSetDisplayObjectivePacket;
import net.minecraft.network.protocol.game.ClientboundSetObjectivePacket;
import net.minecraft.network.protocol.game.ClientboundSetPlayerTeamPacket;
import net.minecraft.network.protocol.game.ClientboundSetScorePacket;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Score;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import org.wallentines.midnightcore.api.player.MPlayer;
import org.wallentines.midnightcore.api.text.MComponent;
import org.wallentines.midnightcore.common.text.AbstractScoreboard;
import org.wallentines.midnightcore.fabric.MidnightCore;
import org.wallentines.midnightcore.fabric.player.FabricPlayer;
import org.wallentines.midnightcore.fabric.util.ConversionUtil;

import java.util.ArrayList;
import java.util.List;

public class FabricScoreboard extends AbstractScoreboard {

    private final PlayerTeam[] teams = new PlayerTeam[15];
    private final ServerScoreboard board;
    private final Objective objective;

    private final boolean[] updated = new boolean[15];

    private Component mcName;

    public FabricScoreboard(String id, MComponent title) {

        super(id, title);

        mcName = ConversionUtil.toComponent(title);

        board = MidnightCore.getInstance().getServer().getScoreboard();
        objective = new Objective(board, id, ObjectiveCriteria.DUMMY, mcName, ObjectiveCriteria.RenderType.INTEGER);

        for(int i = 0 ; i < teams.length ; i++) {

            if(id.length() > 15) {
                id = id.substring(0,14);
            }

            teams[i] = new PlayerTeam(board, id + Integer.toHexString(i));
            teams[i].getPlayers().add("§" + Integer.toHexString(i));
        }
    }

    @Override
    public void setTitle(MComponent cmp) {
        super.setTitle(cmp);

        mcName = ConversionUtil.toComponent(cmp);
        objective.setDisplayName(mcName);
    }

    public void setLine(int line, MComponent message) {

        if(line < 1 || line > 15) return;

        Component mcLine = ConversionUtil.toComponent(message);

        if(mcLine == null) {
            teams[line].setPlayerPrefix(null);
            board.resetPlayerScore("§" + Integer.toHexString(line), objective);
        } else {
            board.getOrCreatePlayerScore("§" + Integer.toHexString(line), objective).setScore(line);
            teams[line].setPlayerPrefix(mcLine);
        }

        updated[line] = true;
    }

    @Override
    protected void onPlayerAdded(MPlayer u) {

        ServerPlayer player = FabricPlayer.getInternal(u);
        if(player == null) return;

        player.connection.send(new ClientboundSetObjectivePacket(objective, 0));

        for(int i = 0 ; i < teams.length ; i++) {
            player.connection.send(ClientboundSetPlayerTeamPacket.createAddOrModifyPacket(teams[i], true));

            String name = "§" + Integer.toHexString(i);
            Score s = board.getOrCreatePlayerScore(name, objective);
            if(s.getScore() > 0) {
                player.connection.send(new ClientboundSetScorePacket(ServerScoreboard.Method.CHANGE, objective.getName(), name, s.getScore()));
            }
        }

        player.connection.send(new ClientboundSetDisplayObjectivePacket(1, objective));

    }

    @Override
    protected void onPlayerRemoved(MPlayer u) {

        ServerPlayer player = FabricPlayer.getInternal(u);
        if(player == null) return;

        player.connection.send(new ClientboundSetDisplayObjectivePacket(1, null));

        for(int i = 0 ; i < teams.length ; i++) {
            player.connection.send(ClientboundSetPlayerTeamPacket.createRemovePacket(teams[i]));

            String name = "§" + Integer.toHexString(i);
            player.connection.send(new ClientboundSetScorePacket(ServerScoreboard.Method.REMOVE, objective.getName(), name, 0));

        }

        player.connection.send(new ClientboundSetObjectivePacket(objective, 1));


    }

    public void update() {

        List<Packet<?>> packets = new ArrayList<>();

        if(objective.getDisplayName() != mcName) {

            objective.setDisplayName(mcName);
            packets.add(new ClientboundSetObjectivePacket(objective, 2));
        }

        for(int i = 0 ; i < teams.length ; i++) {
            if(updated[i]) {

                packets.add(ClientboundSetPlayerTeamPacket.createAddOrModifyPacket(teams[i], false));

                if(teams[i].getPlayerPrefix() == null) {
                    packets.add(new ClientboundSetScorePacket(ServerScoreboard.Method.REMOVE, objective.getName(), "§" + Integer.toHexString(i), 0));
                } else {
                    packets.add(new ClientboundSetScorePacket(ServerScoreboard.Method.CHANGE, objective.getName(), "§" + Integer.toHexString(i), i));
                }
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


}
