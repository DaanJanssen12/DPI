package loanbroker.messaging.Bank;

import com.owlike.genson.Genson;
import mix.model.bank.BankInterestReply;
import mix.model.bank.BankInterestRequest;
import mix.model.loan.LoanReply;
import mix.model.loan.LoanRequest;

public class BankSerializer {
    private Genson genson;

    public BankSerializer() {
        this.genson = new Genson();
    }

    public String requestToString(BankInterestRequest request){
        return genson.serialize(request);
    }

    public BankInterestRequest requestFromString(String str){
        return genson.deserialize(str, BankInterestRequest.class);
    }

    public String replyToString(BankInterestReply reply){
        return genson.serialize(reply);
    }

    public BankInterestReply replyFromString(String str){
        return genson.deserialize(str, BankInterestReply.class);
    }
}
