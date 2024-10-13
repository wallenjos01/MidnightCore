package org.wallentines.mcore;

import net.minecraft.network.chat.numbers.BlankFormat;
import net.minecraft.network.chat.numbers.FixedFormat;
import net.minecraft.network.chat.numbers.StyledFormat;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.scores.*;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import org.wallentines.mcore.lang.UnresolvedComponent;
import org.wallentines.mcore.text.WrappedComponent;
import org.wallentines.mcore.util.ConversionUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FabricScoreboard extends CustomScoreboard {

    private final Map<UUID, BoardInfo> boards = new HashMap<>();

    public FabricScoreboard(UnresolvedComponent title) {
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

    @Override
    protected void updateNumberFormat(Player player) {
        if(!boards.containsKey(player.getUUID())) {
            return;
        }
        boards.get(player.getUUID()).updateNumberFormat();
    }

    @Override
    protected void updateNumberFormat(int line, Player player) {
        if(!boards.containsKey(player.getUUID())) {
            return;
        }
        boards.get(player.getUUID()).updateNumberFormat(line);
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
                    new WrappedComponent(title.resolveFor(player)),
                    ObjectiveCriteria.RenderType.INTEGER,
                    false,
                    null);

            board.startTrackingObjective(obj);
            board.setDisplayObjective(DisplaySlot.SIDEBAR, obj);

            updateNumberFormat();

            for(int i = 0 ; i < 15 ; i++) {
                updateLine(i);
                updateNumberFormat(i);
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

            obj.setDisplayName(new WrappedComponent(title.resolveFor(player)));
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

            ScoreHolder sh = ScoreHolder.forNameOnly(playerName);

            if(entries[line] == null) {
                board.resetSinglePlayerScore(sh, obj);
            } else {
                team.setPlayerPrefix(new WrappedComponent(entries[line].line().resolveFor(player)));
                board.getOrCreatePlayerScore(sh, obj).set(line);
            }
        }

        public void updateNumberFormat() {
            Objective obj = board.getObjective(objectiveId);
            if(obj == null) {
                throw new IllegalStateException("Attempt to update scoreboard before initialization!");
            }

            if(numberFormat == null) return;

            switch (numberFormat.type) {
                case DEFAULT -> obj.setNumberFormat(null);
                case BLANK -> obj.setNumberFormat(BlankFormat.INSTANCE);
                case STYLED -> obj.setNumberFormat(new StyledFormat(ConversionUtil.getStyle(numberFormat.argument.resolveFor(player))));
                case FIXED -> obj.setNumberFormat(new FixedFormat(new WrappedComponent(numberFormat.argument.resolveFor(player))));
            }
        }

        public void updateNumberFormat(int line) {
            Objective obj = board.getObjective(objectiveId);
            if(obj == null) {
                throw new IllegalStateException("Attempt to update scoreboard before initialization!");
            }
            if(entries[line] == null) return;

            String hexIndex = Integer.toHexString(line);
            String playerName = "§" + hexIndex;

            ScoreHolder sh = ScoreHolder.forNameOnly(playerName);
            ScoreAccess acc = board.getOrCreatePlayerScore(sh, obj);

            NumberFormat fmt = entries[line].format();
            if(fmt == null) {
                acc.numberFormatOverride(null);
                return;
            }

            switch (fmt.type) {
                case DEFAULT -> acc.numberFormatOverride(null);
                case BLANK -> acc.numberFormatOverride(BlankFormat.INSTANCE);
                case STYLED -> acc.numberFormatOverride(new StyledFormat(ConversionUtil.getStyle(fmt.argument.resolveFor(player))));
                case FIXED -> acc.numberFormatOverride(new FixedFormat(new WrappedComponent(fmt.argument.resolveFor(player))));
            }
        }
    }

}
