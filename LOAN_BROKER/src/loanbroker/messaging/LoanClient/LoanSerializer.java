package loanbroker.messaging.LoanClient;

import com.owlike.genson.Genson;
import mix.model.loan.LoanReply;
import mix.model.loan.LoanRequest;

public class LoanSerializer {
    private Genson genson;

    public LoanSerializer() {
        this.genson = new Genson();
    }

    public String requestToString(LoanRequest request){
        return genson.serialize(request);
    }

    public LoanRequest requestFromString(String str){
        return genson.deserialize(str, LoanRequest.class);
    }

    public String replyToString(LoanReply reply){
        return genson.serialize(reply);
    }

    public LoanReply replyFromString(String str){
        return genson.deserialize(str, LoanReply.class);
    }
}
