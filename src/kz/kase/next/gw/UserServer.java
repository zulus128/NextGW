package kz.kase.next.gw;

import com.google.protobuf.CodedInputStream;
import com.google.protobuf.InvalidProtocolBufferException;
import com.lmax.disruptor.RingBuffer;
import kz.kase.ts.kase.proto.Protocol;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Vadim
 * Date: 8/14/12
 * Time: 5:45 PM
 * To change this template use File | Settings | File Templates.
 */
public class UserServer implements Runnable{

    private final ServerSocket serverSocket;
    private final RingBuffer<EventContainer> inBuffer;

    private final List<UserSession> sessions = new ArrayList<UserSession>();

    public UserServer(int port, RingBuffer<EventContainer> inBuffer) throws IOException {
        this.inBuffer = inBuffer;
        serverSocket = new ServerSocket(port);
    }

    public void start() {
        new Thread(this, "ServerSocket Thread").start();
    }

    @Override
    public void run() {
        while (true) {
            try {
                Socket s = serverSocket.accept();
                UserSession session = new UserSession(s, inBuffer);
                sessions.add(session);
                session.process();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendToAllUsers(Protocol.ServerAction action) {
        for (UserSession session : sessions) {
            try {
                session.send(action);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public static class UserSession implements Runnable {

        private final RingBuffer<EventContainer> inBuffer;
        private final Socket socket;

        public UserSession(Socket socket, RingBuffer<EventContainer> inBuffer) {
            this.socket = socket;
            this.inBuffer = inBuffer;
        }

        public void send(Protocol.ServerAction action) throws IOException {
            action.writeDelimitedTo(socket.getOutputStream());
        }

        public void process() {
            new Thread(this, "UserSession Thread").start();
        }

        @Override
        public void run() {
            //todo some gateway login stuff
            System.out.println("Starting user session");

            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                while (true) {
                    try {
                        String str;
                        if ((str = br.readLine()) != null) {
                            publish(str.getBytes());
                        }
                    } catch (SocketTimeoutException e) {
                        e.printStackTrace();
                    }
                }
//                InputStream in = socket.getInputStream();
//                while (true) {
//                    try {
//                        final int firstByte = in.read();
//                        if (firstByte == -1) {
//                            //todo close session?
//                            return;
//                        }
//
//                        final int size = CodedInputStream.readRawVarint32(firstByte, in);
//                        byte[] bytes = new byte[size];
//                        int read = in.read(bytes);
//
//                        publish(bytes);
//
//                    } catch (SocketTimeoutException e) {
//                        e.printStackTrace();
//                    }
//
//                }

            } catch (IOException e) {
                e.printStackTrace();
                //todo close session
            }
        }


        private void publish(byte[] bytes) throws InvalidProtocolBufferException {

            long sequence = inBuffer.next();
            EventContainer event = inBuffer.get(sequence);
            event.setTimestamp(System.nanoTime());
            event.setRawData(bytes);
            inBuffer.publish(sequence);
        }

//        private void publish(String str) {
//
//            long sequence = inBuffer.next();
//            EventContainer event = inBuffer.get(sequence);
//            event.setTimestamp(System.nanoTime());
//            event.setRawData(bytes);
//            inBuffer.publish(sequence);
//        }

    }

}
