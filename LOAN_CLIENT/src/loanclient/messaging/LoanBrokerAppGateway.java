package loanclient.messaging;

import mix.model.loan.LoanReply;
import mix.model.loan.LoanRequest;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import javax.jms.Message;
import javax.jms.MessageListener;

public class LoanBrokerAppGateway {
    private MessageSenderGateway sender;
    private MessageReceiverGateway receiver;
    private LoanSerializer serializer;

    public LoanBrokerAppGateway(String senderChannel, String receiverChannel) {
        serializer = new LoanSerializer();
        receiver = new MessageReceiverGateway(senderChannel);
        sender = new MessageSenderGateway(receiverChannel);
    }

    public void applyForLoan(LoanRequest request){
        Message message = sender.createTextMessage(serializer.requestToString(request));
        sender.send(message);
    }

    public void onLoanReplyArrived(LoanRequest request, LoanReply reply){
        throw new NotImplementedException();
    }

    public void setMessageListener(MessageListener listener){
        receiver.setListener(listener);
    }
}
