import kz.kase.next.gw.Gateway;
import kz.kase.ts.kase.proto.Protocol;
import quickfix.field.*;
import quickfix.fix50.*;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created with IntelliJ IDEA.
 * User: Vadim
 * Date: 8/28/12
 * Time: 2:36 PM
 * To change this template use File | Settings | File Templates.
 */

public class GWClientTestFix {

    public static final int PORT = 1580;
//    private OutputStream clientOut;

    private PrintWriter pw;

    public void startGW() throws IOException {

        new Gateway(PORT);
    }
    public void startClient() throws IOException {

        Socket clientSocket = new Socket("localhost", PORT);
        new Reader(clientSocket.getInputStream()).start();
//        clientOut = clientSocket.getOutputStream();
        pw = new PrintWriter(clientSocket.getOutputStream(), true);
    }

    public PrintWriter getClientOut() {
        return pw;
    }

//    public OutputStream getClientOut() {
//        return clientOut;
//    }


    private class Reader implements Runnable {

        private final InputStream in;

        public Reader(InputStream in) {
            this.in = in;
        }

        @Override
        public void run() {

            while (!Thread.currentThread().isInterrupted()) {

            }
        }

        public Future start() {
            return Executors.newSingleThreadExecutor().submit(this);
        }
    }

     public static void main(String[] args)
            throws InterruptedException, ExecutionException, IOException {

        GWClientTestFix test = new GWClientTestFix();

        System.out.println("Starting gw...");
        test.startGW();
        System.out.println("gw Started");

        Thread.sleep(1000);

        System.out.println("Starting client...");
        test.startClient();
        System.out.println("client Started");

        long ref = 0L;

        System.out.println();
        System.out.printf("Sending instr create (ref: %d)\n", ref);


         ClOrdID order_id = new ClOrdID();
         order_id.setValue(String.valueOf(ref));

         OrderQty quantity = new OrderQty();
         quantity.setValue(new Double(79955));

         OrdType type = new OrdType(OrdType.LIMIT);

         NewOrderSingle newOrderSingle = new NewOrderSingle(order_id, new Side(Side.SELL), new TransactTime(), type );
         newOrderSingle.set(new Account("0000000111"));
         newOrderSingle.set(new Symbol("USD"));
         newOrderSingle.set(quantity);
//         newOrderSingle.set(new Currency("United State Dollar"));
         Message msg =  (Message)newOrderSingle;

         System.out.println("FIX Message from client: " + msg);
         test.getClientOut().println(msg);

//         System.out.println("FIX Message: "+newOrderSingle);


         ref++;
    }
}
