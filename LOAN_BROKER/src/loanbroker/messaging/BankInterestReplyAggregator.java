package loanbroker.messaging;

import mix.model.bank.BankInterestReply;
import mix.model.bank.BankInterestRequest;

public class BankInterestReplyAggregator {

    private int aggregatorId;
    private int expectedReplies;
    private int repliesReceived = 0;

    private BankInterestReply message;
    private BankInterestRequest interestRequest;

    public BankInterestReplyAggregator(BankInterestRequest interestRequest, int expectedReplies) {
        this.expectedReplies = expectedReplies;
        this.aggregatorId = interestRequest.hashCode();
        this.interestRequest = interestRequest;
        System.out.println("created aggregator: id = "+aggregatorId);
    }

    public BankInterestReply processReceivedMessage(BankInterestReply reply){
        repliesReceived++;
        System.out.println("Reply "+repliesReceived+" out of "+expectedReplies);
        if(message == null){
            message = reply;
        }else if(reply.getInterest() < message.getInterest()){
            message = reply;
        }

        if(repliesReceived == expectedReplies){
            return message;
        }

        return null;
    }

    public int getAggregatorId(){return aggregatorId;}

    public BankInterestRequest getInterestRequest() {
        return interestRequest;
    }
}
