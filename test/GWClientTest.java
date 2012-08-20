import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import kz.kase.next.gw.Gateway;
import kz.kase.ts.kase.proto.Protocol;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created with IntelliJ IDEA.
 * User: Vadim
 * Date: 8/20/12
 * Time: 9:52 AM
 * To change this template use File | Settings | File Templates.
 */
public class GWClientTest {

    public static final int PORT = 1580;
    private OutputStream clientOut;

    public void startGW() throws IOException {

        new Gateway(PORT);
    }

    public void startClient() throws IOException {

        Socket clientSocket = new Socket("localhost", PORT);
        new Reader(clientSocket.getInputStream()).start();
        clientOut = clientSocket.getOutputStream();
    }

    public OutputStream getClientOut() {
        return clientOut;
    }


    private class Reader implements Runnable {

        private final InputStream in;

        public Reader(InputStream in) {
            this.in = in;
        }

        @Override
        public void run() {

            while (!Thread.currentThread().isInterrupted()) {

                try {
                    Protocol.ServerAction action = Protocol.ServerAction.parseDelimitedFrom(in);
                    if (action.getAction() == Protocol.ServerAction.Action.USER_RESPONSE) {
                        Protocol.UserResponse response = Protocol.UserResponse.parseFrom(action.getData());

                        System.out.printf("Got response on operation: %d, %s. ErCode: %d. " +
                                "(ver: %d)\n",
                                response.getRef(),
                                response.getStatus(),
                                response.getErCode(),
                                response.getChangeVersion());


                    } else if (action.getAction() == Protocol.ServerAction.Action.UPDATES) {

                        Protocol.Changes changes = Protocol.Changes.parseFrom(action.getData());

                        System.out.printf("Got %d changes (ver: %d):\n",
                                changes.getChangesCount(),
                                changes.getVersion());
                        System.out.println("--------------------------------");

                        for (int i = 0; i < changes.getChangesCount(); i++) {
                            Protocol.Change change = changes.getChanges(i);
                            System.out.printf("%s, %s (id: %d)\n",
                                    change.getObjType(),
                                    change.getType(),
                                    change.getId());

                            if (change.getType() == Protocol.Change.Type.CREATE ||
                                    change.getType() == Protocol.Change.Type.UPDATE) {
                                Message mes = Factory.build(change.getObjType(), change.getData());
                                System.out.print(mes);
                            }

                            if (i < changes.getChangesCount() - 1) {
                                System.out.println();
                            }
                        }
                        System.out.println("--------------------------------");
                        System.out.println();

                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }

        public Future start() {
            return Executors.newSingleThreadExecutor().submit(this);
        }
    }

    public static class Factory {

        public static Message build(Protocol.ObjectType type, ByteString data)
                throws InvalidProtocolBufferException {
            switch (type) {
                case ACCOUNT:
                    return Protocol.Account.parseFrom(data);

                case INSTRUMENT:
                    return Protocol.Instrument.parseFrom(data);
                case LIMIT_ORDER:
                    return Protocol.Order.parseFrom(data);
                case DEAL:
                    return Protocol.Deal.parseFrom(data);
                case USER:
                    return Protocol.User.parseFrom(data);
                case RIGHT:
                    return Protocol.Right.parseFrom(data);
            }

            return null;
        }

    }

    public static void sendMessage(OutputStream out, Protocol.UserAction.Action action, Message message)
            throws IOException {
        Protocol.UserAction.newBuilder()
                .setAction(action)
                .setBody(message.toByteString())
                .build()
                .writeDelimitedTo(out);
    }


    public static void main(String[] args)
            throws InterruptedException, ExecutionException, IOException {

        GWClientTest test = new GWClientTest();

        System.out.println("Starting gw...");
        test.startGW();
        System.out.println("Started");

        Thread.sleep(1000);

        System.out.println("Starting client...");
        test.startClient();
        System.out.println("Started");

        long ref = 0L;
        long id = 1L;

        //-------------

        long instrId = id;
        System.out.println();
        System.out.printf("Sending instr create (ref: %d)\n", ref);
        sendMessage(test.getClientOut(), Protocol.UserAction.Action.CREATE_INSTRUMENT,
                Protocol.CreateInstrument.newBuilder()
                        .setName("I000001")
                        .setRef(ref)
                        .build());
        id++;
        ref++;

        //-------------

        long userId1 = id;
        System.out.println();
        System.out.printf("Sending user #1 create (ref: %d)\n", ref);
        sendMessage(test.getClientOut(), Protocol.UserAction.Action.CREATE_USER,
                Protocol.CreateUser.newBuilder()
                        .setName("U000001")
                        .setRef(ref)
                        .setType(Protocol.UserType.TRADER)
                        .build());
        id++;
        ref++;

        long accId1 = id;
        System.out.println();
        System.out.printf("Sending account #1 create (ref: %d)\n", ref);
        sendMessage(test.getClientOut(), Protocol.UserAction.Action.CREATE_ACCOUNT,
                Protocol.CreateAccount.newBuilder()
                        .setName("A000001")
                        .setRef(ref)
                        .build());
        id++;
        ref++;


        System.out.println();
        System.out.printf("Sending position #1 set (ref: %d)\n", ref);
        sendMessage(test.getClientOut(), Protocol.UserAction.Action.SET_POSITION,
                Protocol.SetPosition.newBuilder()
                        .setInstrId(instrId)
                        .setAccId(accId1)
                        .setPosition(200)
                        .setRef(ref)
                        .build());
        ref++;


        System.out.println();
        System.out.printf("Sending add right #1 (ref: %d)\n", ref);
        sendMessage(test.getClientOut(), Protocol.UserAction.Action.ADD_RIGHT,
                Protocol.AddRight.newBuilder()
                        .setRef(ref)
                        .setToUser(userId1)
                        .setToObject(accId1)
                        .setType(Protocol.Right.Type.READ_WRITE)
                        .build());
        id++;
        ref++;


        //-------------

        long userId2 = id;
        System.out.println();
        System.out.printf("Sending user #2 create (ref: %d)\n", ref);
        sendMessage(test.getClientOut(), Protocol.UserAction.Action.CREATE_USER,
                Protocol.CreateUser.newBuilder()
                        .setName("U000002")
                        .setRef(ref)
                        .setType(Protocol.UserType.TRADER)
                        .build());
        id++;
        ref++;


        long accId2 = id;
        System.out.println();
        System.out.printf("Sending account #2 create (ref: %d)\n", ref);
        sendMessage(test.getClientOut(), Protocol.UserAction.Action.CREATE_ACCOUNT,
                Protocol.CreateAccount.newBuilder()
                        .setName("A000002")
                        .setRef(ref)
                        .build());
        ref++;


        System.out.println();
        System.out.printf("Sending position #2 set (ref: %d)\n", ref);
        sendMessage(test.getClientOut(), Protocol.UserAction.Action.SET_MONEY,
                Protocol.SetMoney.newBuilder()
                        .setAccId(accId2)
                        .setPosition(100000)
                        .setRef(ref)
                        .build());
        ref++;


        System.out.println();
        System.out.printf("Sending add right #2 (ref: %d)\n", ref);
        sendMessage(test.getClientOut(), Protocol.UserAction.Action.ADD_RIGHT,
                Protocol.AddRight.newBuilder()
                        .setRef(ref)
                        .setToUser(userId2)
                        .setToObject(accId2)
                        .setType(Protocol.Right.Type.READ_WRITE)
                        .build());
        ref++;

        //-------------

        while(true) {
        Thread.sleep(5000);
        System.out.println();
        System.out.printf("Sending order #1 (ref: %d)\n", ref);
        sendMessage(test.getClientOut(), Protocol.UserAction.Action.CREATE_ORDER,
                Protocol.CreateOrder.newBuilder()
                        .setRef(ref)
                        .setInstrId(instrId)
                        .setUserId(userId1)
                        .setAccountId(accId1)
                        .setDir(Protocol.Dir.SELL)
                        .setPrice(30.0)
                        .setQty(200)
                        .build());
        ref++;
        }

/*        System.out.println();
        System.out.printf("Sending order #2 (ref: %d)\n", ref);
        sendMessage(test.getClientOut(), Protocol.UserAction.Action.CREATE_ORDER,
                Protocol.CreateOrder.newBuilder()
                        .setRef(ref)
                        .setInstrId(instrId)
                        .setUserId(userId2)
                        .setAccountId(accId2)
                        .setDir(Protocol.Dir.BUY)
                        .setPrice(30.1)
                        .setQty(150)
                        .build());
        ref++;

        System.out.println();
        System.out.printf("Sending order #3 (ref: %d)\n", ref);
        sendMessage(test.getClientOut(), Protocol.UserAction.Action.CREATE_ORDER,
                Protocol.CreateOrder.newBuilder()
                        .setRef(ref)
                        .setInstrId(instrId)
                        .setUserId(userId2)
                        .setAccountId(accId2)
                        .setDir(Protocol.Dir.BUY)
                        .setPrice(30.09)
                        .setQty(50)
                        .build());
        ref++;



        Thread.sleep(30000);
        System.exit(0);
  */
    }


}
