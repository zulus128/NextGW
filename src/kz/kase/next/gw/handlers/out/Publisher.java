package kz.kase.next.gw.handlers.out;

import com.google.protobuf.Message;
import com.lmax.disruptor.EventHandler;
import kz.kase.next.gw.EventContainer;
import kz.kase.next.gw.UserServer;
import kz.kase.ts.kase.proto.Protocol;

/**
 * Created with IntelliJ IDEA.
 * User: Vadim
 * Date: 8/14/12
 * Time: 5:29 PM
 * To change this template use File | Settings | File Templates.
 */
public class Publisher implements EventHandler<EventContainer> {

    private final UserServer server;

    public Publisher(UserServer server) {
        this.server = server;
    }

    @Override
    public void onEvent(EventContainer ev, long sequence, boolean endOfBatch) throws Exception {

//        if (ev.getObj() instanceof Public) {
//            Public pub = (Public) ev.getObj();
//
//            Message mes = pub.publish();
//            if (pub.getServerAction() != null && mes != null) {
//                server.sendToAllUsers(Protocol.ServerAction.newBuilder()
//                        .setAction(pub.getServerAction())
//                        .setData(mes.toByteString())
//                        .build());
//            }
//        }

    }
}
