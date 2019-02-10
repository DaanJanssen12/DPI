package loanbroker;

import javafx.beans.property.MapProperty;
import mix.messaging.requestreply.RequestReply;
import mix.model.bank.BankInterestReply;
import mix.model.bank.BankInterestRequest;
import mix.model.loan.LoanReply;
import mix.model.loan.LoanRequest;
import org.apache.activemq.ActiveMQConnectionFactory;

import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.Serializable;
import java.util.Map;
import java.util.Properties;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
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
	private static final long serialVersionUID = 1L;
	private static JPanel contentPane;
	private static DefaultListModel<JListLine> listModel = new DefaultListModel<JListLine>();
	private static JList<JListLine> list;

	private static Connection connection;
	private static Session session;
	private static Destination receiverDestination;
	private static Destination senderDestination;
	private static MessageProducer producer;
	private static MessageConsumer consumer;

	private static Destination otherReceiverDestination;
	private static Destination otherSenderDestination;
	private static MessageProducer otherProducer;
	private static MessageConsumer otherConsumer;

	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					LoanBrokerFrame frame = new LoanBrokerFrame();
					frame.setVisible(true);
					subscribe();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}


	private static void subscribe(){
		try {
			Properties props = new Properties();
			props.setProperty(Context.INITIAL_CONTEXT_FACTORY,
					"org.apache.activemq.jndi.ActiveMQInitialContextFactory");
			props.setProperty(Context.PROVIDER_URL, "tcp://localhost:61616");

			// connect to the Destination called “myFirstChannel”
			// queue or topic: “queue.myFirstDestination” or “topic.myFirstDestination”
			props.put(("queue.LoanRequests"), " LoanRequests");
			props.put(("queue.BankInterestReplies"), "BankInterestReplies");

			Context jndiContext = new InitialContext(props);
			ActiveMQConnectionFactory connectionFactory = (ActiveMQConnectionFactory) jndiContext
					.lookup("ConnectionFactory");
			connectionFactory.setTrustAllPackages(true);
			connection = connectionFactory.createConnection();
			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

			// connect to the receiver destination
			receiverDestination = (Destination) jndiContext.lookup("LoanRequests");
			consumer = session.createConsumer(receiverDestination);

			otherReceiverDestination = (Destination) jndiContext.lookup("BankInterestReplies");
			otherConsumer = session.createConsumer(otherReceiverDestination);

			connection.start(); // this is needed to start receiving messages

		} catch (NamingException | JMSException e) {
			e.printStackTrace();
		}


		try {
			consumer.setMessageListener(msg -> {
                try {
                    Object obj = ((ObjectMessage) msg).getObject();
                    LoanRequest request = (LoanRequest) obj;

					BankInterestRequest interestRequest = new BankInterestRequest(request.getAmount(), request.getTime());
					JListLine listLine = new JListLine(request);
					listLine.setBankRequest(interestRequest);
                    listModel.addElement(listLine);


					Properties props = new Properties();
					props.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.apache.activemq.jndi.ActiveMQInitialContextFactory");
					props.setProperty(Context.PROVIDER_URL, "tcp://localhost:61616");

					// connect to the Destination called “myFirstChannel”
					// queue or topic: “queue.myFirstDestination” or “topic.myFirstDestination”
					props.put(("queue.BankInterestRequests"), "BankInterestRequests");

					Context jndiContext = new InitialContext(props);
					ConnectionFactory connectionFactory = (ConnectionFactory) jndiContext
							.lookup("ConnectionFactory");
					connection = connectionFactory.createConnection();
					session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

					// connect to the sender destination
					senderDestination = (Destination) jndiContext.lookup("BankInterestRequests");
					producer = session.createProducer(senderDestination);

					// create a text message
					Message newMessage = session.createObjectMessage(interestRequest);
					// send the message
					producer.send(newMessage);

                } catch (JMSException e) {
                    e.printStackTrace();
                } catch (NamingException e) {
					e.printStackTrace();
				}

			});



		} catch (JMSException e) {
			e.printStackTrace();
		}

		try {
			otherConsumer.setMessageListener(msg -> {
				try {
					Object obj = ((ObjectMessage) msg).getObject();
					BankInterestReply reply = (BankInterestReply) obj;
					System.out.println("received reply: "+reply);
					for (int i = 0; i < listModel.size(); i++) {
						JListLine listLine = listModel.get(i);
						if(listLine.getBankRequest().hashCode() == msg.getIntProperty("requestId")){
							listLine.setBankReply(reply);
							LoanReply loanReply = new LoanReply(reply.getInterest(), reply.getQuoteId());
							MessageProducer messageProducer = createMessageProducer(("queue.LoanReplies"), "LoanReplies");
							// create a text message
							Message newMessage = session.createObjectMessage(loanReply);
							newMessage.setIntProperty("loanRequestId", listLine.getLoanRequest().hashCode());
							// send the message
							messageProducer.send(newMessage);
						}
					}
					list.repaint();


				} catch (JMSException e) {
					e.printStackTrace();
				}

			});



		} catch (JMSException e) {
			e.printStackTrace();
		}
	}

	public static MessageProducer createMessageProducer(String queueOrTopicName, String lookupName){
        try {
            Properties props = new Properties();
            props.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.apache.activemq.jndi.ActiveMQInitialContextFactory");
            props.setProperty(Context.PROVIDER_URL, "tcp://localhost:61616");

            props.put(queueOrTopicName, lookupName);

            Context jndiContext = new InitialContext(props);
            ConnectionFactory connectionFactory = (ConnectionFactory) jndiContext
                    .lookup("ConnectionFactory");
            connection = connectionFactory.createConnection();
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            // connect to the sender destination
            Destination destination = (Destination) jndiContext.lookup(lookupName);
            MessageProducer messageProducer = session.createProducer(destination);

            return messageProducer;

        } catch (JMSException e) {
            e.printStackTrace();
            return null;
        } catch (NamingException e) {
            e.printStackTrace();
            return null;
        }
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
