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
	// 启动服务端
	public JButton btnStartServer;
	// 启动客户端
	public JButton btnStartClient;
	// 选择文件
	public JButton btnFile;
	// 加密前的交换密钥
	public JTextPane textPaneServer;
	public JTextPane textPaneClient;
	// 加密后的文件信息
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
		lblNewLabel.setFont(new Font("微软雅黑", Font.BOLD, 24));
		add(lblNewLabel);

		JLabel lblNewLabel_1 = new JLabel("Client");
		lblNewLabel_1.setFont(new Font("微软雅黑", Font.BOLD, 24));
		lblNewLabel_1.setBounds(703, 6, 83, 46);
		add(lblNewLabel_1);

		JSeparator separator = new JSeparator();
		separator.setOrientation(SwingConstants.VERTICAL);
		separator.setBounds(490, 0, 1, 700);
		add(separator);

		btnStartServer = new JButton("\u542F\u52A8\u670D\u52A1\u7AEF");
		btnStartServer.setForeground(SystemColor.textHighlight);
		btnStartServer.setBackground(SystemColor.activeCaption);
		btnStartServer.setFont(new Font("微软雅黑", Font.PLAIN, 15));
		btnStartServer.setBounds(14, 65, 113, 27);
		add(btnStartServer);

		btnStartClient = new JButton("\u53D1\u8D77\u8BF7\u6C42");
		btnStartClient.setForeground(SystemColor.textHighlight);
		btnStartClient.setFont(new Font("微软雅黑", Font.PLAIN, 15));
		btnStartClient.setBackground(SystemColor.activeCaption);
		btnStartClient.setBounds(514, 66, 113, 27);
		add(btnStartClient);

		// 选择文件按钮
		btnFile = new JButton("\u9009\u62E9\u6587\u4EF6");
		btnFile.setForeground(SystemColor.textHighlight);
		btnFile.setFont(new Font("微软雅黑", Font.PLAIN, 15));
		btnFile.setBackground(SystemColor.activeCaption);
		btnFile.setBounds(514, 277, 113, 27);
		add(btnFile);

		textPaneServer = new JTextPane();
		textPaneServer.setBackground(SystemColor.info);
		textPaneServer.setFont(new Font("微软雅黑 Light", Font.PLAIN, 15));
		textPaneServer.setBounds(14, 390, 451, 270);
		add(textPaneServer);

		JLabel lblNewLabel_2 = new JLabel("\u6587\u4EF6\u63A5\u6536\u53CA\u89E3\u5BC6\u8BE6\u60C5\uFF1A");
		lblNewLabel_2.setForeground(SystemColor.textHighlight);
		lblNewLabel_2.setFont(new Font("微软雅黑 Light", Font.PLAIN, 15));
		lblNewLabel_2.setBounds(14, 339, 153, 38);
		add(lblNewLabel_2);

		textPaneClient = new JTextPane();
		textPaneClient.setBackground(SystemColor.info);
		textPaneClient.setFont(new Font("微软雅黑 Light", Font.PLAIN, 15));
		textPaneClient.setBounds(514, 390, 451, 270);
		add(textPaneClient);

		JLabel lblNewLabel_2_1 = new JLabel("\u6587\u4EF6\u4F20\u8F93\u53CA\u52A0\u5BC6\u8BE6\u60C5\uFF1A");
		lblNewLabel_2_1.setForeground(SystemColor.textHighlight);
		lblNewLabel_2_1.setFont(new Font("微软雅黑 Light", Font.PLAIN, 15));
		lblNewLabel_2_1.setBounds(513, 339, 153, 38);
		add(lblNewLabel_2_1);

		textSrver = new JTextPane();
		textSrver.setBackground(SystemColor.info);
		textSrver.setFont(new Font("微软雅黑 Light", Font.PLAIN, 15));
		textSrver.setBounds(14, 105, 462, 199);
		add(textSrver);

		textClient = new JTextPane();
		textClient.setBackground(SystemColor.info);
		textClient.setFont(new Font("微软雅黑 Light", Font.PLAIN, 15));
		textClient.setBounds(503, 105, 462, 159);
		add(textClient);

		serverStartListener();

	}

	/**
	 * 启动服务端，同时生成RSA公钥密钥
	 */
	public void serverStartListener() {
		btnStartServer.addActionListener(new ActionListener() {
	
			@Override
			public void actionPerformed(ActionEvent e) {

				try {
					
					//生成公私钥
					RSAUtil.genKeyPair();
					System.out.println("随机生成的公钥为:" + RSAUtil.keyMap.get(0));
					System.out.println("随机生成的私钥为:" + RSAUtil.keyMap.get(1));
					
					//启动服务端
					FileTransferServer server = new FileTransferServer();
					System.out.println("服务端启动");
					server.load();
					
				} catch (Exception e1) {
					// TODO 自动生成的 catch 块
					e1.printStackTrace();
				}
				
			}
		});
	}

	/**
	 * 
	 */

}
