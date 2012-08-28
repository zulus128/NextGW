package kz.kase.next.gw;

/**
 * Created with IntelliJ IDEA.
 * User: Vadim
 * Date: 8/14/12
 * Time: 2:41 PM
 * To change this template use File | Settings | File Templates.
 */

import com.lmax.disruptor.EventFactory;

public final class EventContainer {

    public final static EventFactory<EventContainer> EVENT_FACTORY = new EventFactory<EventContainer>() {
        public EventContainer newInstance() {
            return new EventContainer();
        }
    };

    private RawData rawData;

    private Object obj;

    private long timestamp;



    public RawData getRawData() {
        return rawData;
    }

    public void setRawData(byte[] bytes) {
        if (bytes != null) {
            this.rawData = new RawData(bytes.length, bytes);
        }
    }

    public Object getObj() {
        return obj;
    }

    public void setObj(Object obj) {
        this.obj = obj;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }



    public static class RawData {

        private final int size;
        private final byte[] bytes;

        public RawData(int size, byte[] bytes) {
            this.size = size;
            this.bytes = bytes;
        }

        public int getSize() {
            return size;
        }

        public byte[] getBytes() {
            return bytes;
        }
    }
}
