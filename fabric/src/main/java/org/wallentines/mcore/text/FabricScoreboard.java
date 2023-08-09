package org.wallentines.mcore.text;

import net.minecraft.server.ServerScoreboard;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import org.wallentines.mcore.MidnightCoreAPI;
import org.wallentines.mcore.Player;
import org.wallentines.mcore.ScoreboardHolder;
import org.wallentines.mcore.util.ConversionUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class FabricScoreboard extends CustomScoreboard {

    private final Map<UUID, BoardInfo> boards = new HashMap<>();

    public FabricScoreboard(Component title) {
        super(title);
    }


    @Override
    protected void updateTitle(Player player) {

        BoardInfo info = boards.get(player.getUUID());
        if(info == null) {
            MidnightCoreAPI.LOGGER.warn("Could not update scoreboard title! Scoreboard has not been created!");
            return;
        }

        info.updateTitle();
    }

    @Override
    protected void sendToPlayer(Player player) {

        if(boards.containsKey(player.getUUID())) {
            return;
        }

        ServerPlayer spl = ConversionUtil.validate(player);
        BoardInfo info = new BoardInfo(generateRandomId(), spl);

        boards.put(spl.getUUID(), info);
        info.init();
    }

    @Override
    protected void clearForPlayer(Player player) {

        if(!boards.containsKey(player.getUUID())) {
            return;
        }

        boards.get(player.getUUID()).clear();
    }

    @Override
    protected void updateLine(int line, Player player) {
        if(!boards.containsKey(player.getUUID())) {
            MidnightCoreAPI.LOGGER.warn("Could not update scoreboard line! Scoreboard has not been created!");
            return;
        }
        boards.get(player.getUUID()).updateLine(line);
    }

    private class BoardInfo {

        String objectiveId;
        PlayerTeam[] teams;
        ServerScoreboard board;
        ServerPlayer player;

        public BoardInfo(String objectiveId, ServerPlayer spl) {
            this.objectiveId = objectiveId;
            this.teams = new PlayerTeam[15];
            this.board = new ServerScoreboard(spl.server);
            this.player = spl;
        }

        public void init() {

            Objective obj = board.addObjective(
                    objectiveId,
                    ObjectiveCriteria.DUMMY,
                    WrappedComponent.resolved(title, player),
                    ObjectiveCriteria.RenderType.INTEGER);

            board.startTrackingObjective(obj);
            board.setDisplayObjective(Scoreboard.DISPLAY_SLOT_SIDEBAR, obj);

            for(int i = 0 ; i < 15 ; i++) {
                updateLine(i);
            }

            ((ScoreboardHolder) player).setScoreboard(board);
        }

        public void clear() {

            ((ScoreboardHolder) player).setScoreboard(null);
        }

        public void updateTitle() {

            Objective obj = board.getObjective(objectiveId);
            if(obj == null) {
                throw new IllegalStateException("Attempt to update scoreboard before initialization!");
            }

            obj.setDisplayName(WrappedComponent.resolved(title, player));
        }

        public void updateLine(int line) {

            Objective obj = board.getObjective(objectiveId);
            if(obj == null) {
                throw new IllegalStateException("Attempt to update scoreboard before initialization!");
            }

            String hexIndex = Integer.toHexString(line);
            String teamName = objectiveId.substring(0, 14) + hexIndex;
            String playerName = "§" + hexIndex;

            PlayerTeam team = board.getPlayersTeam(teamName);
            if(team == null) {
                team = board.addPlayerTeam(teamName);
                board.addPlayerToTeam(playerName, team);
            }

            if(entries[line] == null) {
                board.resetPlayerScore(playerName, obj);
            } else {
                team.setPlayerPrefix(WrappedComponent.resolved(entries[line], player));
                board.getOrCreatePlayerScore(playerName, obj).setScore(line);
            }
        }
    }

    private static String generateRandomId() {

        String values = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random rand = new Random();

        StringBuilder builder = new StringBuilder();
        for(int i = 0 ; i < 16 ; i++) {

            int index = rand.nextInt(values.length());
            builder.append(values.charAt(index));
        }

        return builder.toString();
    }

}
