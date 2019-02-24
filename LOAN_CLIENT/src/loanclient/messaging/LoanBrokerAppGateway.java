package loanclient.messaging;

import mix.model.bank.BankInterestRequest;
import mix.model.loan.LoanReply;
import mix.model.loan.LoanRequest;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class LoanBrokerAppGateway {
    private MessageSenderGateway sender;
    private MessageReceiverGateway receiver;
    private LoanSerializer serializer;
    private List<LoanRequest> loanRequests;

    public LoanBrokerAppGateway(String senderChannel, String receiverChannel) {
        serializer = new LoanSerializer();
        receiver = new MessageReceiverGateway(receiverChannel);
        sender = new MessageSenderGateway(senderChannel);
        loanRequests = new ArrayList<>();
        receiver.setListener(msg -> {
            try {
                String body = ((TextMessage)msg).getText();
                LoanReply reply = serializer.replyFromString(body);
                int messageId = msg.getIntProperty("messageId");
                LoanRequest request = new LoanRequest();
                for (LoanRequest loanRequest:loanRequests) {
                    if(new BankInterestRequest(loanRequest.getAmount(), loanRequest.getTime()).hashCode() == messageId){
                        request = loanRequest;
                    }
                }
                loanRequests.remove(request);
                onLoanReplyArrived(request, reply);
            } catch (JMSException e) {
                e.printStackTrace();
            }
        });
    }

    public void applyForLoan(LoanRequest request) throws JMSException {
        loanRequests.add(request);
        Message message = sender.createTextMessage(serializer.requestToString(request));
        sender.send(message);
    }

    public void onLoanReplyArrived(LoanRequest request, LoanReply reply){
        throw new NotImplementedException();
    }
}
