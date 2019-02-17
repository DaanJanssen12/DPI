package loanclient.messaging;

import mix.model.bank.BankInterestRequest;
import mix.model.loan.LoanReply;
import mix.model.loan.LoanRequest;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;
import java.util.UUID;

public class LoanBrokerAppGateway {
    private MessageSenderGateway sender;
    private MessageReceiverGateway receiver;
    private LoanSerializer serializer;

    public LoanBrokerAppGateway(String senderChannel, String receiverChannel) {
        serializer = new LoanSerializer();
        receiver = new MessageReceiverGateway(senderChannel);
        sender = new MessageSenderGateway(receiverChannel);
        receiver.setListener(msg -> {
            try {
                String body = ((TextMessage)msg).getText();
                LoanReply reply = serializer.replyFromString(body);
                onLoanReplyArrived(null, reply);
            } catch (JMSException e) {
                e.printStackTrace();
            }
        });
    }

    public void applyForLoan(LoanRequest request) throws JMSException {
        Message message = sender.createTextMessage(serializer.requestToString(request));
        message.setJMSCorrelationID(UUID.randomUUID().toString());
        sender.send(message);
    }

    public void onLoanReplyArrived(LoanRequest request, LoanReply reply){
        throw new NotImplementedException();
    }
}
