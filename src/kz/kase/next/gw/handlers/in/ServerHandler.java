package kz.kase.next.gw.handlers.in;

import com.lmax.disruptor.EventHandler;
import kz.kase.next.gw.EventContainer;

/**
 * Created with IntelliJ IDEA.
 * User: Vadim
 * Date: 8/14/12
 * Time: 4:53 PM
 * To change this template use File | Settings | File Templates.
 */
public class ServerHandler implements EventHandler<EventContainer> {

    public void onEvent(final EventContainer ev, final long seq, final boolean endOfBatch)
            throws Exception {

        Object obj = ev.getObj();
    }
}
