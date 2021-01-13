package service;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;

import util.DESUtil;
import util.MD5Util;
import util.RSAUtil;

/**
 * 文件传输Client端<br>
 * 功能说明：
 */
public class FileTransferClient extends Socket {

	private static final String SERVER_IP = "127.0.0.1"; // 服务端IP
	private static final int SERVER_PORT = 8899; // 服务端端口
	public static Map<Integer, String> mapA = null;
	

	private Socket client;

	private FileInputStream fis;

	private DataOutputStream dos;

	static {
		//A生成公私钥匙
		try {
			mapA = RSAUtil.genKeyPair();
		} catch (NoSuchAlgorithmException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		}
	}
	
	/**
	 * 构造函数<br/>
	 * 与服务器建立连接
	 * 
	 * @throws Exception
	 */
	public FileTransferClient() throws Exception {
		super(SERVER_IP, SERVER_PORT);
		this.client = this;
		System.out.println("Cliect[port:" + client.getLocalPort() + "] 成功连接服务端");
	}

	/**
	 * 向服务端传输文件
	 * 
	 * @throws Exception
	 */
	public void sendFile(String filePath) throws Exception {
		try {
			File file = new File(filePath);
			if (file.exists()) {
				fis = new FileInputStream(file);
				dos = new DataOutputStream(client.getOutputStream());

				// 文件名和长度
				dos.writeUTF(file.getName());
				dos.flush();
				dos.writeLong(file.length());
				dos.flush();

				// 开始传输文件
				System.out.println("======== 开始传输文件 ========");
				System.out.println("文件名:" + file.getName() + "\t文件路径:" + file.getAbsolutePath() + "\t文件大小:"
						+ file.length() + "字节");
				byte[] bytes = new byte[1024];
				int length = 0;
				long progress = 0;
				while ((length = fis.read(bytes, 0, bytes.length)) != -1) {
					dos.write(bytes, 0, length);
					dos.flush();
					progress += length;
					System.out.print("| " + (100 * progress / file.length()) + "% |");
				}
				System.out.println();
				System.out.println("======== 文件传输成功 ========");
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (fis != null)
				fis.close();
			if (dos != null)
				dos.close();
			client.close();
		}
	}

	/**
	 * 入口
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		try {

			System.out.println("【A】随机生成的【公钥】为:" + mapA.get(0));
			System.out.println("【A】随机生成的【私钥】为:" + mapA.get(1));

			// A生成DES对称密钥
			String desKey = "923533706";
			System.out.println("【A】的【DES对称密钥】为:" + desKey);

			// 指定传输的文件
			String filePath = "E:\\exp1\\tmpA\\mingwen.txt";
			File file = new File(filePath);
			if (file.exists()) {
				System.out.println("【A】将传输的文件为:" + file.getName() + "\t文件路径为:" + file.getAbsolutePath() + "\t文件大小为:"
						+ file.length() + "字节");
			} else {
				System.out.println("该文件不存在");
			}

			// A使用B的公钥加密DES密钥
			System.out.println("【A】获得B的公钥为:" + FileTransferServer.mapB.get(0));
			System.out.println("============================================================");
			String desCipher = RSAUtil.encrypt(desKey, FileTransferServer.mapB.get(0));
			System.out.println("【A】使用B的公钥加密后的DES密钥为:" + desCipher);

			// A读取文件并用MD5生成摘要A
			String md5AClient = MD5Util.getFileMD5String(filePath);
			System.out.println("【A】用MD5生成的【摘要A】为:" + md5AClient);

			// 用A的私钥对摘要A生成签名
			String sigA = RSAUtil.sign(md5AClient, mapA.get(1));
			System.out.println("【A】用RSA的私钥对摘要A生成的【签名】为:" + sigA);

			// A用DES对称密钥对文件进行加密
			String destFile = "E:\\exp1\\tmpA\\miwen.txt";
			String fileCipher = DESUtil.encryptFile(desKey, file.getAbsolutePath(), destFile);
			System.out.println("【A】用des密钥对文件进行加密:" + fileCipher);

			/*
			 * //测试验证签名 boolean check = RSAUtil.verify(md5AClient, sigA, mapA.get(0));
			 * System.out.println("【A】签名验证是否匹配:"+check);
			 */

			// 将【签名】和【加密后的DES密钥】写入文件
			String packageSigA = "E:\\exp1\\tmpA\\PackSigA.txt";
			String packageDesCipher = "E:\\exp1\\tmpA\\PackDesCipher.txt";

			
			// true表示不覆盖原来的内容，而是加到文件的后面。若要覆盖原来的内容，直接省略这个参数就好 fwriter = new
			// FileWriter(packageA, true);
			FileWriter fwriter1 = new FileWriter(packageSigA);
			fwriter1.write(sigA);
			fwriter1.flush();
			fwriter1.close();
			FileWriter fwriter2 = new FileWriter(packageDesCipher);
			fwriter2.write(desCipher);
			fwriter2.flush();
			fwriter2.close();
			
			
			// 启动客户端连接
			FileTransferClient client1 = new FileTransferClient();
			FileTransferClient client2 = new FileTransferClient();
			FileTransferClient client3 = new FileTransferClient();
			// 开始传输文件
			client1.sendFile(packageDesCipher); // 加密的DES密钥
			// 三个线程之间一定要暂停，不会改变B服务端接收文件的顺序，但是会改变B对文件名的判定。
			// （因为线程是异步的，B的if还没判断完毕，下个线程就改变了变量）
			Thread.sleep(3000);
			client2.sendFile(packageSigA); // 签名
			Thread.sleep(3000);
			client3.sendFile(fileCipher); // 加密的文件

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}