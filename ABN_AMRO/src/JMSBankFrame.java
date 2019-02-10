import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Properties;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import mix.messaging.requestreply.RequestReply;
import mix.model.bank.BankInterestReply;
import mix.model.bank.BankInterestRequest;
import mix.model.loan.LoanRequest;
import org.apache.activemq.ActiveMQConnectionFactory;

public class JMSBankFrame extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JTextField tfReply;
	private static DefaultListModel<RequestReply<BankInterestRequest, BankInterestReply>> listModel = new DefaultListModel<RequestReply<BankInterestRequest, BankInterestReply>>();

	private static Connection connection;
	private static Session session;
	private static Destination receiverDestination;
	private static Destination senderDestination;
	private static MessageProducer producer;
	private static MessageConsumer consumer;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(() -> {
            try {
                JMSBankFrame frame = new JMSBankFrame();
                frame.setVisible(true);
                subscribe();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
	}

	public static void subscribe(){
		try {
			Properties props = new Properties();
			props.setProperty(Context.INITIAL_CONTEXT_FACTORY,
					"org.apache.activemq.jndi.ActiveMQInitialContextFactory");
			props.setProperty(Context.PROVIDER_URL, "tcp://localhost:61616");

			// connect to the Destination called “myFirstChannel”
			// queue or topic: “queue.myFirstDestination” or “topic.myFirstDestination”
			props.put(("queue.BankInterestRequests"), " BankInterestRequests");

			Context jndiContext = new InitialContext(props);
			ActiveMQConnectionFactory connectionFactory = (ActiveMQConnectionFactory) jndiContext
					.lookup("ConnectionFactory");
			connectionFactory.setTrustAllPackages(true);
			connection = connectionFactory.createConnection();
			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

			// connect to the receiver destination
			receiverDestination = (Destination) jndiContext.lookup("BankInterestRequests");
			consumer = session.createConsumer(receiverDestination);

			connection.start(); // this is needed to start receiving messages

		} catch (NamingException | JMSException e) {
			e.printStackTrace();
		}


		try {
			consumer.setMessageListener(msg -> {
				try {
					Object obj = ((ObjectMessage) msg).getObject();
					BankInterestRequest request = (BankInterestRequest) obj;
					listModel.addElement(new RequestReply<>(request, null));

				} catch (JMSException e) {
					e.printStackTrace();
				}

            });

		} catch (JMSException e) {
			e.printStackTrace();
		}


	}

	/**
	 * Create the frame.
	 */
	public JMSBankFrame() {
		setTitle("JMS Bank - ABN AMRO");
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
		gbc_scrollPane.gridwidth = 5;
		gbc_scrollPane.insets = new Insets(0, 0, 5, 5);
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 0;
		contentPane.add(scrollPane, gbc_scrollPane);
		
		JList<RequestReply<BankInterestRequest, BankInterestReply>> list = new JList<RequestReply<BankInterestRequest, BankInterestReply>>(listModel);
		scrollPane.setViewportView(list);
		
		JLabel lblNewLabel = new JLabel("type reply");
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel.insets = new Insets(0, 0, 0, 5);
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = 1;
		contentPane.add(lblNewLabel, gbc_lblNewLabel);
		
		tfReply = new JTextField();
		GridBagConstraints gbc_tfReply = new GridBagConstraints();
		gbc_tfReply.gridwidth = 2;
		gbc_tfReply.insets = new Insets(0, 0, 0, 5);
		gbc_tfReply.fill = GridBagConstraints.HORIZONTAL;
		gbc_tfReply.gridx = 1;
		gbc_tfReply.gridy = 1;
		contentPane.add(tfReply, gbc_tfReply);
		tfReply.setColumns(10);
		
		JButton btnSendReply = new JButton("send reply");
		btnSendReply.addActionListener(e -> {
            RequestReply<BankInterestRequest, BankInterestReply> rr = list.getSelectedValue();
            double interest = Double.parseDouble((tfReply.getText()));
            BankInterestReply reply = new BankInterestReply(interest,"ABN AMRO");
            if (rr!= null && reply != null){
                rr.setReply(reply);
                list.repaint();
                try{
                    Properties props = new Properties();
                    props.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.apache.activemq.jndi.ActiveMQInitialContextFactory");
                    props.setProperty(Context.PROVIDER_URL, "tcp://localhost:61616");

                    // connect to the Destination called “myFirstChannel”
                    // queue or topic: “queue.myFirstDestination” or “topic.myFirstDestination”
                    props.put(("queue.BankInterestReplies"), "BankInterestReplies");

                    Context jndiContext = new InitialContext(props);
                    ConnectionFactory connectionFactory = (ConnectionFactory) jndiContext
                            .lookup("ConnectionFactory");
                    connection = connectionFactory.createConnection();
                    session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

                    // connect to the sender destination
                    senderDestination = (Destination) jndiContext.lookup("BankInterestReplies");
                    producer = session.createProducer(senderDestination);

                    // create a text message
                    Message newMessage = session.createObjectMessage(reply);
                    newMessage.setIntProperty("requestId", rr.getRequest().hashCode());
                    // send the message
                    producer.send(newMessage);
                } catch (NamingException e1) {
                    e1.printStackTrace();
                } catch (JMSException e1) {
                    e1.printStackTrace();
                }
            }
        });
		GridBagConstraints gbc_btnSendReply = new GridBagConstraints();
		gbc_btnSendReply.anchor = GridBagConstraints.NORTHWEST;
		gbc_btnSendReply.gridx = 4;
		gbc_btnSendReply.gridy = 1;
		contentPane.add(btnSendReply, gbc_btnSendReply);
	}

}
