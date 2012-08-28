package kz.kase.next.gw.handlers.in;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.lmax.disruptor.EventHandler;
import kz.kase.next.gw.EventContainer;
import kz.kase.ts.kase.proto.Protocol;
import kz.kase.ts.kase.proto.Protocol.*;
import static kz.kase.ts.kase.proto.Protocol.UserAction.Action;

import quickfix.*;
import quickfix.field.ClOrdID;
import quickfix.field.MsgType;
import quickfix.field.OrderQty;
import quickfix.fix50.*;
import quickfix.fix50.Message;
import quickfix.fix50.MessageCracker;


/**
 * Created with IntelliJ IDEA.
 * User: Vadim
 * Date: 8/14/12
 * Time: 4:48 PM
 * To change this template use File | Settings | File Templates.
 */

public class Unmarshaller extends MessageCracker implements EventHandler<EventContainer> {

    private static final String NEW_ORDER_SINGLE = "35=D";

    @Override
    public void onEvent(EventContainer event, long sequence, boolean endOfBatch)
            throws Exception {

        if (event.getRawData() != null) {
            byte[] bytes = event.getRawData().getBytes();

            String s = new String(bytes);
            Object obj = build(s);
            if (obj != null) {
                event.setObj(obj);
            }

//            UserAction req = UserAction.parseFrom(bytes);
//            Object obj = build(req.getBody(), req.getAction());
//            if (obj != null) {
//                event.setObj(obj);
//            }
        }
    }

    @Override
    public void onMessage(NewOrderSingle message, quickfix.SessionID sessionID) throws quickfix.FieldNotFound, quickfix.UnsupportedMessageType, quickfix.IncorrectTagValue {
        super.onMessage(message, sessionID);    //To change body of overridden methods use File | Settings | File Templates.

        System.out.println("Message in Unmarshaller: " + message);

    }

    public Object build(String ms)
            throws InvalidProtocolBufferException {

        try {

            MsgType mt = Message.identifyType(ms);
            System.out.println("Message in Unmarshaller type: " + mt);
            Message msg = new Message();
            msg.fromString(ms, null, true);
//            quickfix.SessionID sid = new quickfix.SessionID("34434", "777777771", "7777772");
//            quickfix.Message msg = MessageUtils.parse(Session.lookupSession(null), ms);
            System.out.println("Message1 in Unmarshaller: " + msg);

            if(mt.toString() == NEW_ORDER_SINGLE){

                ClOrdID clOrdID = new ClOrdID();
                msg.getField(clOrdID);
                OrderQty quantity = new OrderQty();
                msg.setField(quantity);
                quickfix.field.Account acc = new quickfix.field.Account();
                msg.setField(acc);
                long instrId = 555;

                UserAction act =Protocol.CreateOrder.newBuilder()
                                        .setRef(Long.valueOf(clOrdID.getValue())
                                        .setInstrId(instrId)
                                        .setUserId(5)
                                        .setAccountId()
                                        .setDir(Protocol.Dir.SELL)
                                        .setPrice(30.0)
                                        .setQty(quantity.getValue())
                                        .build());
                return Protocol.UserAction.newBuilder()
                        .setAction(Protocol.UserAction.Action.CREATE_ORDER)
                        .setBody(act.toByteString())
                        .build();
            }


//            try {
//
//                crack(msg, sid);
//
//            } catch (quickfix.UnsupportedMessageType unsupportedMessageType) {
//                unsupportedMessageType.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//            } catch (quickfix.FieldNotFound fieldNotFound) {
//                fieldNotFound.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//            } catch (quickfix.IncorrectTagValue incorrectTagValue) {
//                incorrectTagValue.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//            }

        }   catch(InvalidMessage e){

            e.printStackTrace();
        }
             catch (MessageParseError messageParseError) {
            messageParseError.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        catch (FieldNotFound messageParseError) {
            messageParseError.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        return null;
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