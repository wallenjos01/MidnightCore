package org.wallentines.mcore.messenger;

import io.netty.buffer.ByteBuf;
import org.wallentines.mdcfg.serializer.Serializer;
import org.wallentines.midnightlib.event.EventHandler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CompositeMessenger implements Messenger {

    private final List<Messenger> messengers;

    public CompositeMessenger(MessengerModule module, Collection<String> messengers) {

        this.messengers = new ArrayList<>(messengers.size());
        for(String s : messengers) {
            this.messengers.add(module.getMessenger(s));
        }
    }

    @Override
    public void subscribe(Object listener, String channel, EventHandler<Message> handler) {
        for(Messenger m : messengers) m.subscribe(listener, channel, handler);
    }

    @Override
    public void unsubscribe(Object listener, String channel) {
        for(Messenger m : messengers) m.unsubscribe(listener, channel);
    }

    @Override
    public void publish(String channel, ByteBuf message) {
        for(Messenger m : messengers) m.publish(channel, message);
    }

    @Override
    public void queue(String channel, ByteBuf message) {
        for(Messenger m : messengers) m.queue(channel, message);
    }

    public static final MessengerType TYPE = (module, params) -> new CompositeMessenger(module, params.getList("messengers", Serializer.STRING));

}
