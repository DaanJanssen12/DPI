package loanbroker.messaging;

import mix.model.bank.BankInterestReply;

import java.util.UUID;

public class BankInterestReplyAggregator {

    private int aggregatorId;
    private int expectedReplies;
    private int repliesReceived = 0;

    private BankInterestReply message;

    public BankInterestReplyAggregator(int aggregatorId, int expectedReplies) {
        this.expectedReplies = expectedReplies;
        this.aggregatorId = aggregatorId;
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
}
