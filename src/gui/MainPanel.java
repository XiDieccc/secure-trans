package gui;

import java.awt.Font;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;

import service.FileTransferServer;
import util.RSAUtil;

public class MainPanel extends JPanel {

	public static MainPanel instance = new MainPanel();
	// ���������
	public JButton btnStartServer;
	// �����ͻ���
	public JButton btnStartClient;
	// ѡ���ļ�
	public JButton btnFile;
	// ����ǰ�Ľ�����Կ
	public JTextPane textPaneServer;
	public JTextPane textPaneClient;
	// ���ܺ���ļ���Ϣ
	public JTextPane textSrver;
	public JTextPane textClient;

	/**
	 * Create the panel.
	 */
	public MainPanel() {
		setLayout(null);

		JLabel lblNewLabel = new JLabel("Server");
		lblNewLabel.setVerticalAlignment(SwingConstants.TOP);
		lblNewLabel.setBounds(187, 13, 102, 38);
		lblNewLabel.setBackground(SystemColor.activeCaption);
		lblNewLabel.setFont(new Font("΢���ź�", Font.BOLD, 24));
		add(lblNewLabel);

		JLabel lblNewLabel_1 = new JLabel("Client");
		lblNewLabel_1.setFont(new Font("΢���ź�", Font.BOLD, 24));
		lblNewLabel_1.setBounds(703, 6, 83, 46);
		add(lblNewLabel_1);

		JSeparator separator = new JSeparator();
		separator.setOrientation(SwingConstants.VERTICAL);
		separator.setBounds(490, 0, 1, 700);
		add(separator);

		btnStartServer = new JButton("\u542F\u52A8\u670D\u52A1\u7AEF");
		btnStartServer.setForeground(SystemColor.textHighlight);
		btnStartServer.setBackground(SystemColor.activeCaption);
		btnStartServer.setFont(new Font("΢���ź�", Font.PLAIN, 15));
		btnStartServer.setBounds(14, 65, 113, 27);
		add(btnStartServer);

		btnStartClient = new JButton("\u53D1\u8D77\u8BF7\u6C42");
		btnStartClient.setForeground(SystemColor.textHighlight);
		btnStartClient.setFont(new Font("΢���ź�", Font.PLAIN, 15));
		btnStartClient.setBackground(SystemColor.activeCaption);
		btnStartClient.setBounds(514, 66, 113, 27);
		add(btnStartClient);

		// ѡ���ļ���ť
		btnFile = new JButton("\u9009\u62E9\u6587\u4EF6");
		btnFile.setForeground(SystemColor.textHighlight);
		btnFile.setFont(new Font("΢���ź�", Font.PLAIN, 15));
		btnFile.setBackground(SystemColor.activeCaption);
		btnFile.setBounds(514, 277, 113, 27);
		add(btnFile);

		textPaneServer = new JTextPane();
		textPaneServer.setBackground(SystemColor.info);
		textPaneServer.setFont(new Font("΢���ź� Light", Font.PLAIN, 15));
		textPaneServer.setBounds(14, 390, 451, 270);
		add(textPaneServer);

		JLabel lblNewLabel_2 = new JLabel("\u6587\u4EF6\u63A5\u6536\u53CA\u89E3\u5BC6\u8BE6\u60C5\uFF1A");
		lblNewLabel_2.setForeground(SystemColor.textHighlight);
		lblNewLabel_2.setFont(new Font("΢���ź� Light", Font.PLAIN, 15));
		lblNewLabel_2.setBounds(14, 339, 153, 38);
		add(lblNewLabel_2);

		textPaneClient = new JTextPane();
		textPaneClient.setBackground(SystemColor.info);
		textPaneClient.setFont(new Font("΢���ź� Light", Font.PLAIN, 15));
		textPaneClient.setBounds(514, 390, 451, 270);
		add(textPaneClient);

		JLabel lblNewLabel_2_1 = new JLabel("\u6587\u4EF6\u4F20\u8F93\u53CA\u52A0\u5BC6\u8BE6\u60C5\uFF1A");
		lblNewLabel_2_1.setForeground(SystemColor.textHighlight);
		lblNewLabel_2_1.setFont(new Font("΢���ź� Light", Font.PLAIN, 15));
		lblNewLabel_2_1.setBounds(513, 339, 153, 38);
		add(lblNewLabel_2_1);

		textSrver = new JTextPane();
		textSrver.setBackground(SystemColor.info);
		textSrver.setFont(new Font("΢���ź� Light", Font.PLAIN, 15));
		textSrver.setBounds(14, 105, 462, 199);
		add(textSrver);

		textClient = new JTextPane();
		textClient.setBackground(SystemColor.info);
		textClient.setFont(new Font("΢���ź� Light", Font.PLAIN, 15));
		textClient.setBounds(503, 105, 462, 159);
		add(textClient);

		serverStartListener();

	}

	/**
	 * ��������ˣ�ͬʱ����RSA��Կ��Կ
	 */
	public void serverStartListener() {
		btnStartServer.addActionListener(new ActionListener() {
	
			@Override
			public void actionPerformed(ActionEvent e) {

				try {
					
					//���ɹ�˽Կ
					RSAUtil.genKeyPair();
					System.out.println("������ɵĹ�ԿΪ:" + RSAUtil.keyMap.get(0));
					System.out.println("������ɵ�˽ԿΪ:" + RSAUtil.keyMap.get(1));
					
					//���������
					FileTransferServer server = new FileTransferServer();
					System.out.println("���������");
					server.load();
					
				} catch (Exception e1) {
					// TODO �Զ����ɵ� catch ��
					e1.printStackTrace();
				}
				
			}
		});
	}

	/**
	 * 
	 */

}
