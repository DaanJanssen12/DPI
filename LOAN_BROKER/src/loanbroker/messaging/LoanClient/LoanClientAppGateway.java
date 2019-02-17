package loanbroker.messaging.LoanClient;

import loanbroker.messaging.Bank.BankSerializer;
import loanbroker.messaging.MessageReceiverGateway;
import loanbroker.messaging.MessageSenderGateway;
import mix.model.bank.BankInterestReply;
import mix.model.loan.LoanReply;
import mix.model.loan.LoanRequest;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

public class LoanClientAppGateway {
    private MessageSenderGateway sender;
    private MessageReceiverGateway receiver;
    private LoanSerializer serializer;

    public LoanClientAppGateway(String senderChannel, String receiverChannel) {
        serializer = new LoanSerializer();
        receiver = new MessageReceiverGateway(senderChannel);
        sender = new MessageSenderGateway(receiverChannel);
        receiver.setListener(message -> {
            try {
                String body = ((TextMessage)message).getText();
                LoanRequest request = serializer.requestFromString(body);
                onLoanRequestArrived(request, null);
            } catch (JMSException e) {
                e.printStackTrace();
            }
        });
    }

    public void sendLoanReply(LoanReply reply){
        Message message = sender.createTextMessage(serializer.replyToString(reply));
        sender.send(message);
    }

    public void onLoanRequestArrived(LoanRequest request, LoanReply reply){
        sendLoanReply(reply);
    }

    public void setMessageListener(MessageListener listener){
        receiver.setListener(listener);
    }
}
