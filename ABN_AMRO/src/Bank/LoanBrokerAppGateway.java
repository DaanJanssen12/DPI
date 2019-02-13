package Bank;
import mix.model.bank.BankInterestReply;
import mix.model.bank.BankInterestRequest;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import javax.jms.Message;
import javax.jms.MessageListener;

public class LoanBrokerAppGateway {
    private MessageSenderGateway sender;
    private MessageReceiverGateway receiver;
    private BankSerializer serializer;

    public LoanBrokerAppGateway(String senderChannel, String receiverChannel) {
        serializer = new BankSerializer();
        receiver = new MessageReceiverGateway(senderChannel);
        sender = new MessageSenderGateway(receiverChannel);
    }

    public void sendBankReply(BankInterestReply reply){
        Message message = sender.createTextMessage(serializer.replyToString(reply));
        sender.send(message);
    }

    public void onBankRequestArrived(BankInterestRequest request, BankInterestReply reply){
        throw new NotImplementedException();
    }

    public void setMessageListener(MessageListener listener){
        receiver.setListener(listener);
    }
}
