package me.m1dnightninja.midnightcore.spigot.text;

import me.m1dnightninja.midnightcore.api.player.MPlayer;
import me.m1dnightninja.midnightcore.api.text.MScoreboard;
import me.m1dnightninja.midnightcore.api.text.MComponent;
import me.m1dnightninja.midnightcore.spigot.player.SpigotPlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

public class SpigotScoreboard extends MScoreboard {

    private final Scoreboard internal;
    private final Team[] teams = new Team[15];
    private final Objective objective;
    private final boolean[] updated = new boolean[15];

    private final String[] lines = new String[15];

    private final ScoreboardManager manager;

    private String legacyName;

    public SpigotScoreboard(String id, MComponent title) {
        super(id, title);

        manager = Bukkit.getScoreboardManager();
        if(manager == null) {
            throw new IllegalStateException("Cannot create a scoreboard right now!");
        }

        legacyName = title.toLegacyText(false);

        internal = manager.getNewScoreboard();
        objective = internal.registerNewObjective(id, "dummy", legacyName, RenderType.INTEGER);
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        for(int i = 0 ; i < 15 ; i++) {

            if(id.length() > 15) {
                id = id.substring(0,14);
            }

            teams[i] = internal.registerNewTeam(id + i);
            if(teams[i] == null) {
                throw new IllegalStateException("Unable to create scoreboard teams!");
            }

            teams[i].addEntry("§" + Integer.toHexString(i));
        }

    }

    @Override
    public void setName(MComponent cmp) {
        super.setName(cmp);

        legacyName = cmp.toLegacyText(false);
        objective.setDisplayName(legacyName);

    }

    @Override
    public void setLine(int line, MComponent message) {

        if(line < 1 || line > 15) return;

        lines[line] = message == null ? null : message.toLegacyText(false);
        updated[line] = true;

    }

    @Override
    public void update() {

        for(int line = 0 ; line < 15 ; line ++) {
            if(!updated[line]) return;

            String message = lines[line];
            if (message == null) {

                teams[line].setPrefix("");
                internal.resetScores("§" + Integer.toHexString(line));

            } else {

                Score s = objective.getScore("§" + Integer.toHexString(line));
                s.setScore(line);

                teams[line].setPrefix(message);
            }
        }

    }

    @Override
    protected void onPlayerAdded(MPlayer u) {

        Player pl = ((SpigotPlayer) u).getSpigotPlayer();
        if(pl == null) return;

        pl.setScoreboard(internal);

    }

    @Override
    protected void onPlayerRemoved(MPlayer u) {

        Player pl = ((SpigotPlayer) u).getSpigotPlayer();
        if(pl == null) return;

        pl.setScoreboard(manager.getMainScoreboard());

    }
}
