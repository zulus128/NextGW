package kz.kase.next.gw.handlers.in;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.RingBuffer;
import kz.kase.next.gw.EventContainer;
import kz.kase.ts.kase.proto.Protocol;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created with IntelliJ IDEA.
 * User: Vadim
 * Date: 8/14/12
 * Time: 4:53 PM
 * To change this template use File | Settings | File Templates.
 */
public class ServerHandler implements EventHandler<EventContainer>, Runnable {

    public static final int SERVER_PORT = 1570;
    Socket socket;
    private OutputStream streamOut;
    private RingBuffer<EventContainer> outBuffer;

    public void setOutBuffer(RingBuffer<EventContainer> outBuffer) {
        this.outBuffer = outBuffer;
    }

    public Future start() throws IOException {

        socket = new Socket("localhost", SERVER_PORT);
        streamOut = socket.getOutputStream();
        return Executors.newSingleThreadExecutor().submit(this);
    }

    @Override
    public void run () {

        while(true) {

            try {

                Protocol.ServerAction action = Protocol.ServerAction.parseDelimitedFrom(socket.getInputStream());

                if (action.getAction() == Protocol.ServerAction.Action.USER_RESPONSE) {
                    Protocol.UserResponse response = Protocol.UserResponse.parseFrom(action.getData());

                    System.out.printf("+++GW! Got response on operation: %d, %s. ErCode: %d. " +
                            "(ver: %d)\n",
                            response.getRef(),
                            response.getStatus(),
                            response.getErCode(),
                            response.getChangeVersion());


                } else if (action.getAction() == Protocol.ServerAction.Action.UPDATES) {

                    Protocol.Changes changes = Protocol.Changes.parseFrom(action.getData());

                    System.out.printf("+++GW! Got %d changes (ver: %d):\n",
                            changes.getChangesCount(),
                            changes.getVersion());
                    System.out.println("--------------------------------");
                }


                    publish(action);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void onEvent(final EventContainer ev, final long seq, final boolean endOfBatch)
            throws Exception {

//        Object obj = ev.getObj();
//        Message action = (Message)obj;
//        action.writeDelimitedTo(streamOut);

        Protocol.UserAction req = Protocol.UserAction.parseFrom(ev.getRawData().getBytes());
        req.writeDelimitedTo(streamOut);
    }

    private void publish(Object o) {

        long sequence = outBuffer.next();
        EventContainer event = outBuffer.get(sequence);
        event.setObj(o);
        outBuffer.publish(sequence);
    }
}
