package loanbroker;

import jdk.nashorn.internal.ir.ExpressionStatement;
import loanbroker.messaging.Bank.BankAppGateway;
import loanbroker.messaging.Bank.BankSerializer;
import loanbroker.messaging.LoanClient.LoanClientAppGateway;
import loanbroker.messaging.LoanClient.LoanSerializer;
import mix.model.bank.BankInterestReply;
import mix.model.bank.BankInterestRequest;
import mix.model.loan.LoanReply;
import mix.model.loan.LoanRequest;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.jms.*;
import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;


public class LoanBrokerFrame extends JFrame {

	/**
	 * 
	 */
	private static LoanClientAppGateway loanClientAppGateway;
	private static BankAppGateway bankAppGateway;

	private static final long serialVersionUID = 1L;
	private static JPanel contentPane;
	private static DefaultListModel<JListLine> listModel = new DefaultListModel<JListLine>();
	private static JList<JListLine> list;

	public static void main(String[] args) {
		EventQueue.invokeLater(() -> {
            try {
                subscribe();
                LoanBrokerFrame frame = new LoanBrokerFrame();
                frame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
	}


	private static void subscribe(){
		loanClientAppGateway = new LoanClientAppGateway("LoanReplies", "LoanRequests"){
            @Override
            public void onLoanRequestArrived(LoanRequest request, LoanReply reply) {
                BankInterestRequest bankInterestRequest = new BankInterestRequest(request.getAmount(), request.getTime());
                JListLine listLine = new JListLine(request);
                listLine.setBankRequest(bankInterestRequest);
                listModel.addElement(listLine);

                bankAppGateway.sendBankRequest(bankInterestRequest);
            }
        };
		bankAppGateway = new BankAppGateway("BankInterestRequests", "BankInterestReplies"){
            @Override
            public void onBankReplyArrived(BankInterestRequest request, BankInterestReply reply) {
                for (int i = 0; i < listModel.size(); i++) {
                    JListLine listLine = listModel.get(i);
                    if(listLine.getBankReply() == null){
                        listLine.setBankReply(reply);
                        LoanReply loanReply = new LoanReply(reply.getInterest(), reply.getQuoteId());

                        loanClientAppGateway.sendLoanReply(loanReply);
                        break;
                    }
                }
                list.repaint();
            }
        };
	}

	/**
	 * Create the frame.
	 */
	public LoanBrokerFrame() {
		setTitle("Loan Broker");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		GridBagLayout gbl_contentPane = new GridBagLayout();
		gbl_contentPane.columnWidths = new int[]{46, 31, 86, 30, 89, 0};
		gbl_contentPane.rowHeights = new int[]{233, 23, 0};
		gbl_contentPane.columnWeights = new double[]{1.0, 0.0, 1.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_contentPane.rowWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
		contentPane.setLayout(gbl_contentPane);
		
		JScrollPane scrollPane = new JScrollPane();
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.gridwidth = 7;
		gbc_scrollPane.insets = new Insets(0, 0, 5, 5);
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 0;
		contentPane.add(scrollPane, gbc_scrollPane);
		
		list = new JList<JListLine>(listModel);
		scrollPane.setViewportView(list);		
	}
	
	 private static JListLine getRequestReply(LoanRequest request){
	     
	     for (int i = 0; i < listModel.getSize(); i++){
	    	 JListLine rr =listModel.get(i);
	    	 if (rr.getLoanRequest() == request){
	    		 return rr;
	    	 }
	     }
	     
	     return null;
	   }
	
	public static void add(LoanRequest loanRequest){
		listModel.addElement(new JListLine(loanRequest));		
	}

	public static void add(LoanRequest loanRequest,BankInterestRequest bankRequest){
		JListLine rr = getRequestReply(loanRequest);
		if (rr!= null && bankRequest != null){
			rr.setBankRequest(bankRequest);
            list.repaint();
		}		
	}
	
	public static void add(LoanRequest loanRequest, BankInterestReply bankReply){
		JListLine rr = getRequestReply(loanRequest);
		if (rr!= null && bankReply != null){
			rr.setBankReply(bankReply);
            list.repaint();
		}		
	}


}
