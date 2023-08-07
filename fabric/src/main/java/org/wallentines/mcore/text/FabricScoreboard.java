package org.wallentines.mcore.text;

import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundSetDisplayObjectivePacket;
import net.minecraft.network.protocol.game.ClientboundSetObjectivePacket;
import net.minecraft.network.protocol.game.ClientboundSetScorePacket;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import org.wallentines.mcore.Player;
import org.wallentines.mcore.util.ConversionUtil;

import java.util.Random;

public class FabricScoreboard extends CustomScoreboard {

    private final String objectiveId;
    private final TeamBuilder[] teams;

    public FabricScoreboard(Component title) {
        super(title);

        this.teams = new TeamBuilder[15];
        this.objectiveId = generateRandomId();

        for(int i = 0 ; i < 15 ; i++) {
            String hexIndex = Integer.toHexString(i);
            teams[i] = new TeamBuilder(objectiveId.substring(0, 14) + hexIndex);
            teams[i].addMember("§" + hexIndex);
        }

    }

    @Override
    protected void updateTitle(Player player) {

        ServerPlayer spl = ConversionUtil.validate(player);
        spl.connection.send(updateObjectivePacket(objectiveId, WrappedComponent.resolved(title, player), false));
    }

    @Override
    protected void sendToPlayer(Player player) {

        ServerPlayer spl = ConversionUtil.validate(player);
        spl.connection.send(updateObjectivePacket(objectiveId, WrappedComponent.resolved(title, player), true));

        for(int i = 0 ; i < 15 ; i++) {
            teams[i].prefix(WrappedComponent.resolved(entries[i], player));
            spl.connection.send(teams[i].addPacket());
        }

        spl.connection.send(displayObjectivePacket(objectiveId));

    }

    @Override
    protected void clearForPlayer(Player player) {

        ServerPlayer spl = ConversionUtil.validate(player);

        for(int i = 0 ; i < 15 ; i++) {
            spl.connection.send(teams[i].removePacket());
        }

        spl.connection.send(removeObjectivePacket(objectiveId));
    }

    @Override
    protected void updateLine(int line, Player player) {

        ServerPlayer spl = ConversionUtil.validate(player);
        String playerName = teams[line].getMembers().iterator().next();

        int score = line;

        if(entries[line] == null) {
            teams[line].prefix(null);
            score = 0;
        } else {
            teams[line].prefix(WrappedComponent.resolved(entries[line], player));
        }

        spl.connection.send(teams[line].updatePacket());
        spl.connection.send(scorePacket(objectiveId, playerName, score));
    }


    private static ClientboundSetObjectivePacket updateObjectivePacket(String id, net.minecraft.network.chat.Component title, boolean add) {

        FriendlyByteBuf fakeBuf = new FriendlyByteBuf(Unpooled.buffer());
        fakeBuf.writeUtf(id); // Objective ID
        fakeBuf.writeByte(add ? 0 : 2); // Action (0 = ADD, 2 = UPDATE)

        fakeBuf.writeComponent(title);
        fakeBuf.writeEnum(ObjectiveCriteria.RenderType.INTEGER);

        return new ClientboundSetObjectivePacket(fakeBuf);
    }

    private static ClientboundSetObjectivePacket removeObjectivePacket(String id) {

        FriendlyByteBuf fakeBuf = new FriendlyByteBuf(Unpooled.buffer());
        fakeBuf.writeUtf(id); // Objective ID
        fakeBuf.writeByte(1); // Action (1 = REMOVE)

        return new ClientboundSetObjectivePacket(fakeBuf);
    }

    private static ClientboundSetDisplayObjectivePacket displayObjectivePacket(String id) {

        FriendlyByteBuf fakeBuf = new FriendlyByteBuf(Unpooled.buffer());
        fakeBuf.writeByte(1); // Position (1 = SIDEBAR)
        fakeBuf.writeUtf(id == null ? "" : id); // Objective ID

        return new ClientboundSetDisplayObjectivePacket(fakeBuf);
    }

    private ClientboundSetScorePacket scorePacket(String objectiveId, String user, int score) {

        ServerScoreboard.Method method = ServerScoreboard.Method.CHANGE;
        if(score == 0) {
            method = ServerScoreboard.Method.REMOVE;
        }
        return new ClientboundSetScorePacket(method, objectiveId, user, score);
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
