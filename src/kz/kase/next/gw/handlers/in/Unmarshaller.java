package kz.kase.next.gw.handlers.in;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.lmax.disruptor.EventHandler;
import kz.kase.next.gw.EventContainer;
import kz.kase.ts.kase.proto.Protocol.*;
import static kz.kase.ts.kase.proto.Protocol.UserAction.Action;

/**
 * Created with IntelliJ IDEA.
 * User: Vadim
 * Date: 8/14/12
 * Time: 4:48 PM
 * To change this template use File | Settings | File Templates.
 */

public class Unmarshaller implements EventHandler<EventContainer> {

    @Override
    public void onEvent(EventContainer event, long sequence, boolean endOfBatch)
            throws Exception {

        if (event.getRawData() != null) {
            byte[] bytes = event.getRawData().getBytes();

            UserAction req = UserAction.parseFrom(bytes);
            Object obj = build(req.getBody(), req.getAction());
            if (obj != null) {
                event.setObj(obj);
            }
        }
    }


    public static Object build(ByteString bytes, Action action)
            throws InvalidProtocolBufferException {

        if (action == Action.CREATE_ORDER) {
            return CreateOrder.parseFrom(bytes);

        } else if (action == Action.REMOVE_ORDER) {
            return RemoveOrder.parseFrom(bytes);

        } else if (action == Action.CREATE_USER) {
            return CreateUser.parseFrom(bytes);

        } else if (action == Action.CREATE_INSTRUMENT) {
            return CreateInstrument.parseFrom(bytes);

        } else if (action == Action.ADD_RIGHT) {
            return AddRight.parseFrom(bytes);

        } else if (action == Action.REMOVE_RIGHT) {
            return RemoveRight.parseFrom(bytes);

        } else if (action == Action.CREATE_ACCOUNT) {
            return CreateAccount.parseFrom(bytes);

        } else if (action == Action.SET_POSITION) {
            return SetPosition.parseFrom(bytes);

        } else if (action == Action.SET_MONEY) {
            return SetMoney.parseFrom(bytes);


        } /*else if (action == Action.START_TEST) {
            return new StatObject(START);
        } else if (action == Action.END_TEST) {
            return new StatObject(FINISH);

        }         */


        return null;
    }


}