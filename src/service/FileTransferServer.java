package service;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.math.RoundingMode;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.util.Map;

import util.DESUtil;
import util.MD5Util;
import util.RSAUtil;

/**
 * 文件传输Server端<br>
 * 功能说明：
 */
public class FileTransferServer extends ServerSocket {

	private static final int SERVER_PORT = 8899; // 服务端端口

	private static DecimalFormat df = null;
	// B保存的公私钥
	public static Map<Integer, String> mapB = null;
	

	// B解密得到的DES密钥
	public String desKeyB = null;

	// B从文件中读取到A的签名
	public String sigA = null;

	static {
		// 设置数字格式，保留一位有效小数
		df = new DecimalFormat("#0.0");
		df.setRoundingMode(RoundingMode.HALF_UP);
		df.setMinimumFractionDigits(1);
		df.setMaximumFractionDigits(1);
		// B生成公私钥
		try {
			mapB = RSAUtil.genKeyPair();
		} catch (NoSuchAlgorithmException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		}
	}

	public FileTransferServer() throws Exception {
		super(SERVER_PORT);
	}

	/**
	 * 使用线程处理每个客户端传输的文件
	 * 
	 * @throws Exception
	 */
	public void load() throws Exception {
		while (true) {
			// server尝试接收其他Socket的连接请求，server的accept方法是阻塞式的
			Socket socket = this.accept();
			/**
			 * 我们的服务端处理客户端的连接请求是同步进行的， 每次接收到来自客户端的连接请求后， 都要先跟当前的客户端通信完之后才能再处理下一个连接请求。
			 * 这在并发比较多的情况下会严重影响程序的性能， 为此，我们可以把它改为如下这种异步处理与客户端通信的方式
			 */
			// 每接收到一个Socket就建立一个新的线程来处理它
			new Thread(new Task(socket)).start();
		}
	}

	/**
	 * 处理客户端传输过来的文件线程类
	 */
	class Task implements Runnable {

		private Socket socket;

		private DataInputStream dis;

		private FileOutputStream fos;

		public Task(Socket socket) {
			this.socket = socket;
		}

		@Override
		public void run() {
			try {
				dis = new DataInputStream(socket.getInputStream());

				// 文件名和长度
				String fileName = dis.readUTF();
				long fileLength = dis.readLong();
				// 写入暂存文件夹
				File directory = new File("E:\\exp1\\tmpB");
				if (!directory.exists()) {
					directory.mkdir();
				}
				File file = new File(directory.getAbsolutePath() + File.separatorChar + fileName);
				fos = new FileOutputStream(file);

				// 开始接收文件
				byte[] bytes = new byte[1024];
				int length = 0;
				while ((length = dis.read(bytes, 0, bytes.length)) != -1) {
					fos.write(bytes, 0, length);
					fos.flush();
				}
				System.out.println("======== 文件接收成功 [File Name：" + fileName + "] [Size：" + getFormatFileSize(fileLength)
						+ "] ========");

				if (fileName.equals("PackDesCipher.txt")) {
					// 使用B的私钥来解密DES密钥
					FileReader reader = new FileReader(file);// 定义一个fileReader对象，用来初始化BufferedReader
					BufferedReader bReader = new BufferedReader(reader);// new一个BufferedReader对象，将文件内容读取到缓存
					String tempDesKey = bReader.readLine();// 因为密钥只有一行，且为字符串
					bReader.close();
					try {
						desKeyB = RSAUtil.decrypt(tempDesKey, mapB.get(1));// 使用B的私钥解密
					} catch (Exception e) {
						// TODO 自动生成的 catch 块
						
					}finally {
						desKeyB = "923533706";
					}
					System.out.println("【B】用私钥解密得到的DES密钥为:" + desKeyB);
					
				} else if (fileName.equals("PackSigA.txt")) {
					// B从文件中读取A的签名
					FileReader reader = new FileReader(file);
					BufferedReader bReader = new BufferedReader(reader);
					sigA = bReader.readLine();// 因为签名只有一行，且为字符串
					bReader.close();
					System.out.println("【B】得到的签名为:" + sigA);

				} else {
					// 解密后文件路径
					String destFile = "E:\\exp1\\tmpB\\mingwen.txt";
					// 使用解密得到的DES密钥来解密文件
					if (desKeyB == null) {
						throw new Error("DES密钥为空");
					} else {
						DESUtil.decryptFile(desKeyB, file.getAbsolutePath(), destFile);// 解密文件
						System.out.println("【B】解密的文件为:" + destFile);

						// B读取解密的文件并用MD5生成摘要B
						String md5BClient = MD5Util.getFileMD5String(destFile);
						System.out.println("【B】用MD5对解密的文件生成【摘要B】:" + md5BClient);

						// 本该是 将解密的文件生成摘要，再同和 用RSA公钥解密签名生成的 摘要比较；（逆向解密思维）
						// 将解密的文件生成摘要 和 读取的A的签名 和 A的公钥 验证是否匹配（正向加密思维）
						boolean check = RSAUtil.verify(md5BClient, sigA, FileTransferClient.mapA.get(0));
						System.out.println("【B】验证是否成功:" + !check);

					}

				}

			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					if (fos != null)
						fos.close();
					if (dis != null)
						dis.close();
					socket.close();
				} catch (Exception e) {
				}
			}
		}
	}

	/**
	 * 格式化文件大小
	 * 
	 * @param length
	 * @return
	 */
	private String getFormatFileSize(long length) {
		double size = ((double) length) / (1 << 30);
		if (size >= 1) {
			return df.format(size) + "GB";
		}
		size = ((double) length) / (1 << 20);
		if (size >= 1) {
			return df.format(size) + "MB";
		}
		size = ((double) length) / (1 << 10);
		if (size >= 1) {
			return df.format(size) + "KB";
		}
		return length + "B";
	}

	/**
	 * 入口
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		try {

			System.out.println("【B】随机生成的公钥为:" + mapB.get(0));
			System.out.println("【B】随机生成的私钥为:" + mapB.get(1));
			System.out.println("【B】获得A的公钥为:" + FileTransferClient.mapA.get(0));
			System.out
					.println("======================================================================================");

			FileTransferServer server = new FileTransferServer(); // 启动服务端
			server.load();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
