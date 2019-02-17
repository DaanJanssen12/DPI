package Bank;
import mix.model.bank.BankInterestReply;
import mix.model.bank.BankInterestRequest;
import mix.model.loan.LoanReply;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

public class LoanBrokerAppGateway {
    private MessageSenderGateway sender;
    private MessageReceiverGateway receiver;
    private BankSerializer serializer;

    public LoanBrokerAppGateway(String senderChannel, String receiverChannel) {
        serializer = new BankSerializer();
        receiver = new MessageReceiverGateway(senderChannel);
        sender = new MessageSenderGateway(receiverChannel);
        receiver.setListener(message -> {
            try {
                String body = ((TextMessage)message).getText();
                BankInterestRequest request = serializer.requestFromString(body);
                onBankRequestArrived(request, null);
            } catch (JMSException e) {
                e.printStackTrace();
            }
        });
    }

    public void sendBankReply(BankInterestReply reply){
        Message message = sender.createTextMessage(serializer.replyToString(reply));
        sender.send(message);
    }

    public void onBankRequestArrived(BankInterestRequest request, BankInterestReply reply){
        throw new NotImplementedException();
    }
}
