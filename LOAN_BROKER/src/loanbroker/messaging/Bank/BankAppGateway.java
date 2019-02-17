package loanbroker.messaging.Bank;

import loanbroker.messaging.MessageReceiverGateway;
import loanbroker.messaging.MessageSenderGateway;
import mix.model.bank.BankInterestReply;
import mix.model.bank.BankInterestRequest;
import mix.model.loan.LoanReply;
import mix.model.loan.LoanRequest;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

public class BankAppGateway {
    private MessageSenderGateway sender;
    private MessageReceiverGateway receiver;
    private BankSerializer serializer;

    public BankAppGateway(String senderChannel, String receiverChannel) {
        serializer = new BankSerializer();
        receiver = new MessageReceiverGateway(senderChannel);
        sender = new MessageSenderGateway(receiverChannel);
        receiver.setListener(message -> {
            try {
                String body = ((TextMessage)message).getText();
                BankInterestReply reply = new BankSerializer().replyFromString(body);
                onBankReplyArrived(null, reply);
            } catch (JMSException e) {
                e.printStackTrace();
            }
        });
    }

    public void sendBankRequest(BankInterestRequest request){
        Message message = sender.createTextMessage(serializer.requestToString(request));
        sender.send(message);
    }

    public void onBankReplyArrived(BankInterestRequest request, BankInterestReply reply){
        throw new NotImplementedException();
    }

    public void setMessageListener(MessageListener listener){
        receiver.setListener(listener);
    }
}
