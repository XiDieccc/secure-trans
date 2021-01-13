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
 * �ļ�����Client��<br>
 * ����˵����
 */
public class FileTransferClient extends Socket {

	private static final String SERVER_IP = "127.0.0.1"; // �����IP
	private static final int SERVER_PORT = 8899; // ����˶˿�
	public static Map<Integer, String> mapA = null;
	

	private Socket client;

	private FileInputStream fis;

	private DataOutputStream dos;

	static {
		//A���ɹ�˽Կ��
		try {
			mapA = RSAUtil.genKeyPair();
		} catch (NoSuchAlgorithmException e) {
			// TODO �Զ����ɵ� catch ��
			e.printStackTrace();
		}
	}
	
	/**
	 * ���캯��<br/>
	 * ���������������
	 * 
	 * @throws Exception
	 */
	public FileTransferClient() throws Exception {
		super(SERVER_IP, SERVER_PORT);
		this.client = this;
		System.out.println("Cliect[port:" + client.getLocalPort() + "] �ɹ����ӷ����");
	}

	/**
	 * �����˴����ļ�
	 * 
	 * @throws Exception
	 */
	public void sendFile(String filePath) throws Exception {
		try {
			File file = new File(filePath);
			if (file.exists()) {
				fis = new FileInputStream(file);
				dos = new DataOutputStream(client.getOutputStream());

				// �ļ����ͳ���
				dos.writeUTF(file.getName());
				dos.flush();
				dos.writeLong(file.length());
				dos.flush();

				// ��ʼ�����ļ�
				System.out.println("======== ��ʼ�����ļ� ========");
				System.out.println("�ļ���:" + file.getName() + "\t�ļ�·��:" + file.getAbsolutePath() + "\t�ļ���С:"
						+ file.length() + "�ֽ�");
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
				System.out.println("======== �ļ�����ɹ� ========");
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
	 * ���
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		try {

			System.out.println("��A��������ɵġ���Կ��Ϊ:" + mapA.get(0));
			System.out.println("��A��������ɵġ�˽Կ��Ϊ:" + mapA.get(1));

			// A����DES�Գ���Կ
			String desKey = "923533706";
			System.out.println("��A���ġ�DES�Գ���Կ��Ϊ:" + desKey);

			// ָ��������ļ�
			String filePath = "E:\\exp1\\tmpA\\mingwen.txt";
			File file = new File(filePath);
			if (file.exists()) {
				System.out.println("��A����������ļ�Ϊ:" + file.getName() + "\t�ļ�·��Ϊ:" + file.getAbsolutePath() + "\t�ļ���СΪ:"
						+ file.length() + "�ֽ�");
			} else {
				System.out.println("���ļ�������");
			}

			// Aʹ��B�Ĺ�Կ����DES��Կ
			System.out.println("��A�����B�Ĺ�ԿΪ:" + FileTransferServer.mapB.get(0));
			System.out.println("============================================================");
			String desCipher = RSAUtil.encrypt(desKey, FileTransferServer.mapB.get(0));
			System.out.println("��A��ʹ��B�Ĺ�Կ���ܺ��DES��ԿΪ:" + desCipher);

			// A��ȡ�ļ�����MD5����ժҪA
			String md5AClient = MD5Util.getFileMD5String(filePath);
			System.out.println("��A����MD5���ɵġ�ժҪA��Ϊ:" + md5AClient);

			// ��A��˽Կ��ժҪA����ǩ��
			String sigA = RSAUtil.sign(md5AClient, mapA.get(1));
			System.out.println("��A����RSA��˽Կ��ժҪA���ɵġ�ǩ����Ϊ:" + sigA);

			// A��DES�Գ���Կ���ļ����м���
			String destFile = "E:\\exp1\\tmpA\\miwen.txt";
			String fileCipher = DESUtil.encryptFile(desKey, file.getAbsolutePath(), destFile);
			System.out.println("��A����des��Կ���ļ����м���:" + fileCipher);

			/*
			 * //������֤ǩ�� boolean check = RSAUtil.verify(md5AClient, sigA, mapA.get(0));
			 * System.out.println("��A��ǩ����֤�Ƿ�ƥ��:"+check);
			 */

			// ����ǩ�����͡����ܺ��DES��Կ��д���ļ�
			String packageSigA = "E:\\exp1\\tmpA\\PackSigA.txt";
			String packageDesCipher = "E:\\exp1\\tmpA\\PackDesCipher.txt";

			
			// true��ʾ������ԭ�������ݣ����Ǽӵ��ļ��ĺ��档��Ҫ����ԭ�������ݣ�ֱ��ʡ����������ͺ� fwriter = new
			// FileWriter(packageA, true);
			FileWriter fwriter1 = new FileWriter(packageSigA);
			fwriter1.write(sigA);
			fwriter1.flush();
			fwriter1.close();
			FileWriter fwriter2 = new FileWriter(packageDesCipher);
			fwriter2.write(desCipher);
			fwriter2.flush();
			fwriter2.close();
			
			
			// �����ͻ�������
			FileTransferClient client1 = new FileTransferClient();
			FileTransferClient client2 = new FileTransferClient();
			FileTransferClient client3 = new FileTransferClient();
			// ��ʼ�����ļ�
			client1.sendFile(packageDesCipher); // ���ܵ�DES��Կ
			// �����߳�֮��һ��Ҫ��ͣ������ı�B����˽����ļ���˳�򣬵��ǻ�ı�B���ļ������ж���
			// ����Ϊ�߳����첽�ģ�B��if��û�ж���ϣ��¸��߳̾͸ı��˱�����
			Thread.sleep(3000);
			client2.sendFile(packageSigA); // ǩ��
			Thread.sleep(3000);
			client3.sendFile(fileCipher); // ���ܵ��ļ�

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}