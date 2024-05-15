package org.wallentines.mcore;

import org.bukkit.Bukkit;
import org.bukkit.scoreboard.*;
import org.wallentines.mcore.adapter.Adapter;
import org.wallentines.mcore.lang.UnresolvedComponent;
import org.wallentines.mcore.text.Component;
import org.wallentines.mcore.util.ConversionUtil;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

public class SpigotScoreboard extends CustomScoreboard {

    private final Map<Player, BoardInfo> boards = new HashMap<>();

    public SpigotScoreboard(UnresolvedComponent title) {
        super(title);
    }

    @Override
    protected void updateTitle(Player player) {

        BoardInfo board = boards.get(player);
        if(board == null) return;

        board.updateTitle();
    }

    @Override
    protected void sendToPlayer(Player player) {
        if(boards.containsKey(player)) {
            return;
        }

        SpigotPlayer spl = ConversionUtil.validate(player);
        BoardInfo info = new BoardInfo(generateRandomId(), spl);

        boards.put(spl, info);
        info.init();
    }

    @Override
    protected void clearForPlayer(Player player) {

        BoardInfo board = boards.get(player);
        if(board == null) return;

        board.clear();
    }

    @Override
    protected void updateLine(int line, Player player) {

        BoardInfo board = boards.get(player);
        if(board == null) return;

        board.updateLine(line);
    }

    @Override
    protected void updateNumberFormat(Player player) {

        BoardInfo board = boards.get(player);
        if(board == null) return;

        board.updateNumberFormat();
    }

    @Override
    protected void updateNumberFormat(int line, Player player) {

        BoardInfo board = boards.get(player);
        if(board == null) return;

        board.updateNumberFormat(line);
    }

    private class BoardInfo {

        ScoreboardManager manager;
        Scoreboard board;
        String objectiveId;
        WeakReference<Scoreboard> originalBoard;
        SpigotPlayer player;

        BoardInfo(String objectiveId, SpigotPlayer player) {
            this.objectiveId = objectiveId;
            this.player = player;
            this.originalBoard = new WeakReference<>(player.getInternal().getScoreboard());

            this.manager = Bukkit.getScoreboardManager();
            if(manager == null) {
                throw new IllegalStateException("Scoreboard manager is null!");
            }

            this.board = manager.getNewScoreboard();
        }

        void init() {

            Objective obj = board.registerNewObjective(objectiveId, Criteria.DUMMY, "");
            Adapter.INSTANCE.get().setObjectiveName(obj, title.resolveFor(player));
            obj.setDisplaySlot(DisplaySlot.SIDEBAR);

            updateNumberFormat();
            for(int i = 0 ; i < 15 ; i++) {
                updateLine(i);
                updateNumberFormat(i);
            }

            player.getInternal().setScoreboard(board);
        }

        void clear() {
            Scoreboard prev = originalBoard.get();
            if(prev == null) {
                prev = manager.getMainScoreboard();
            }

            player.getInternal().setScoreboard(prev);
        }

        void updateTitle() {
            Objective obj = board.getObjective(objectiveId);
            if(obj == null) {
                throw new IllegalStateException("Attempt to update scoreboard before initialization!");
            }

            Adapter.INSTANCE.get().setObjectiveName(obj, title.resolveFor(player));
        }

        void updateLine(int line) {
            Objective obj = board.getObjective(objectiveId);
            if(obj == null) {
                throw new IllegalStateException("Attempt to update scoreboard before initialization!");
            }

            String hexIndex = Integer.toHexString(line);
            String teamName = objectiveId.substring(0, 14) + hexIndex;
            String playerName = '\u00A7' + hexIndex;

            Team team = board.getTeam(teamName);
            if(team == null) {
                team = board.registerNewTeam(teamName);
                team.addEntry(playerName);
            }

            if(entries[line] == null) {
                board.resetScores(playerName);
            } else {
                Adapter.INSTANCE.get().setTeamPrefix(team, entries[line].resolveFor(player));
                obj.getScore(playerName).setScore(line);
            }
        }

        void updateNumberFormat() {
            Objective obj = board.getObjective(objectiveId);
            if(obj == null) {
                throw new IllegalStateException("Attempt to update scoreboard before initialization!");
            }
            if(numberFormat == null) return;

            Adapter.INSTANCE.get().setNumberFormat(obj, numberFormat.type, numberFormat.argument == null ? null : numberFormat.argument.resolveFor(player));
        }


        void updateNumberFormat(int line) {
            Objective obj = board.getObjective(objectiveId);
            if(obj == null) {
                throw new IllegalStateException("Attempt to update scoreboard before initialization!");
            }
            if(entries[line] == null) return;

            String hexIndex = Integer.toHexString(line);
            String playerName = '\u00A7' + hexIndex;

            NumberFormat og = lineFormats[line];
            if(og == null) {
                Adapter.INSTANCE.get().setNumberFormat(obj, NumberFormatType.DEFAULT, null, playerName);
            } else {
                Adapter.INSTANCE.get().setNumberFormat(obj, og.type, og.argument == null ? null : numberFormat.argument.resolveFor(player), playerName);
            }

        }
    }
}
