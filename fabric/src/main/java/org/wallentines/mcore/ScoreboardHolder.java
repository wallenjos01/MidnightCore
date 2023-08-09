package org.wallentines.mcore;

import net.minecraft.server.ServerScoreboard;
import net.minecraft.world.scores.Scoreboard;

public interface ScoreboardHolder {

    Scoreboard getScoreboard();
    void setScoreboard(ServerScoreboard scoreboard);

}
