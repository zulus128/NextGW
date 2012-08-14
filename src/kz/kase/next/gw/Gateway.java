package kz.kase.next.gw;

import com.lmax.disruptor.MultiThreadedClaimStrategy;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.SingleThreadedClaimStrategy;
import com.lmax.disruptor.SleepingWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import kz.kase.next.gw.handlers.in.Unmarshaller;
import kz.kase.next.gw.handlers.out.Publisher;

import java.io.IOException;
import java.util.concurrent.Executors;

/**
 * Created with IntelliJ IDEA.
 * User: Vadim
 * Date: 8/14/12
 * Time: 12:14 PM
 * To change this template use File | Settings | File Templates.
 */
public class Gateway {

    public static final int IN_RING_SIZE = 1 << 17;
    public static final int OUT_RING_SIZE = 1 << 15;

    public Gateway(int port) throws IOException {

        int inThreads = 3;

        Unmarshaller unmarshaller = new Unmarshaller();

        Disruptor<EventContainer> inDisruptor = new Disruptor<EventContainer>(EventContainer.EVENT_FACTORY,
                Executors.newFixedThreadPool(inThreads),
                new MultiThreadedClaimStrategy(IN_RING_SIZE),
                new SleepingWaitStrategy());

//        inDisruptor.handleEventsWith(journaler).then(mainHandler);
        inDisruptor.handleEventsWith(unmarshaller).then(mainHandler);
        RingBuffer<EventContainer> inBuffer = inDisruptor.start();


        UserServer server = new UserServer(port, inBuffer);

//        statLogger = new StatLogger();
        Publisher publisher = new Publisher(server);

        int outThreads = 3;
        Disruptor<EventContainer> outDisruptor = new Disruptor<EventContainer>(EventContainer.EVENT_FACTORY,
                Executors.newFixedThreadPool(outThreads),
                new SingleThreadedClaimStrategy(OUT_RING_SIZE),
                new SleepingWaitStrategy());

//        outDisruptor.handleEventsWith(statLogger)
//                .then(publisher);
        outDisruptor.handleEventsWith(publisher);

        RingBuffer<EventContainer> outBuffer = outDisruptor.start();

        mainHandler.setOutBuffer(outBuffer);

        server.start();
    }


//    public StatLogger getStatLogger() {
//        return statLogger;
//    }

    public static void main(String[] args) throws IOException {
        int port = 1580;
        Gateway gw = new Gateway(port);
    }
}
