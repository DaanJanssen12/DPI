package loanbroker.messaging.Bank;

import loanbroker.messaging.BankInterestReplyAggregator;
import loanbroker.messaging.MessageReceiverGateway;
import loanbroker.messaging.MessageSenderGateway;
import mix.model.bank.BankInterestReply;
import mix.model.bank.BankInterestRequest;
import mix.model.loan.LoanReply;
import mix.model.loan.LoanRequest;
import net.sourceforge.jeval.EvaluationException;
import net.sourceforge.jeval.Evaluator;
import org.apache.camel.RecipientList;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import java.util.*;

public class BankAppGateway {
    private MessageReceiverGateway receiver;
    private BankSerializer serializer;

    private Evaluator evaluator;
    private Map<MessageSenderGateway, String> recipientList;
    private List<BankInterestReplyAggregator> aggregators;

    public BankAppGateway(String receiverChannel) {
        serializer = new BankSerializer();
        evaluator = new Evaluator();
        aggregators = new ArrayList<>();
        receiver = new MessageReceiverGateway(receiverChannel);
        recipientList = new HashMap<>();
        receiver.setListener(message -> {
            try {
                String body = ((TextMessage)message).getText();
                BankInterestReply reply = new BankSerializer().replyFromString(body);
                int messageId = message.getIntProperty("messageId");
                if(aggregators.stream().anyMatch(f -> f.getAggregatorId() == messageId)){
                    BankInterestReplyAggregator aggregator = aggregators.stream().filter(f -> f.getAggregatorId() == messageId).findFirst().get();
                    BankInterestReply replyToSend = aggregator.processReceivedMessage(reply);
                    if(replyToSend != null) {
                        aggregators.remove(aggregator);
                        onBankReplyArrived(aggregator.getInterestRequest(), reply);
                    }
                }else{
                    onBankReplyArrived(null, reply);
                }
            } catch (JMSException e) {
                e.printStackTrace();
            }
        });
    }

    public void addRecipient(String channel, String condition){
        recipientList.put(new MessageSenderGateway(channel), condition);
    }

    public void sendBankRequest(BankInterestRequest request) throws EvaluationException {
        if(recipientList != null && recipientList.size() > 0){
            evaluator.putVariable("amount", Integer.toString(request.getAmount()));
            evaluator.putVariable("time", Integer.toString(request.getTime()));
            int requests = 0;
            for (Map.Entry<MessageSenderGateway, String> recipient : recipientList.entrySet()) {
                String condition = recipient.getValue();
                MessageSenderGateway sender = recipient.getKey();
                if(evaluator.evaluate(condition).equals("1.0")){
                    try {
                        Message message = sender.createTextMessage(serializer.requestToString(request));
                        message.setIntProperty("messageId", request.hashCode());
                        sender.send(message);
                        requests++;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            System.out.println("Amount of messages: "+requests);
            if(requests != 0) aggregators.add(new BankInterestReplyAggregator(request, requests));
        }
    }

    public void onBankReplyArrived(BankInterestRequest request, BankInterestReply reply){
        throw new NotImplementedException();
    }
}
