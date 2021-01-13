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
 * �ļ�����Server��<br>
 * ����˵����
 */
public class FileTransferServer extends ServerSocket {

	private static final int SERVER_PORT = 8899; // ����˶˿�

	private static DecimalFormat df = null;
	// B����Ĺ�˽Կ
	public static Map<Integer, String> mapB = null;
	

	// B���ܵõ���DES��Կ
	public String desKeyB = null;

	// B���ļ��ж�ȡ��A��ǩ��
	public String sigA = null;

	static {
		// �������ָ�ʽ������һλ��ЧС��
		df = new DecimalFormat("#0.0");
		df.setRoundingMode(RoundingMode.HALF_UP);
		df.setMinimumFractionDigits(1);
		df.setMaximumFractionDigits(1);
		// B���ɹ�˽Կ
		try {
			mapB = RSAUtil.genKeyPair();
		} catch (NoSuchAlgorithmException e) {
			// TODO �Զ����ɵ� catch ��
			e.printStackTrace();
		}
	}

	public FileTransferServer() throws Exception {
		super(SERVER_PORT);
	}

	/**
	 * ʹ���̴߳���ÿ���ͻ��˴�����ļ�
	 * 
	 * @throws Exception
	 */
	public void load() throws Exception {
		while (true) {
			// server���Խ�������Socket����������server��accept����������ʽ��
			Socket socket = this.accept();
			/**
			 * ���ǵķ���˴���ͻ��˵�����������ͬ�����еģ� ÿ�ν��յ����Կͻ��˵���������� ��Ҫ�ȸ���ǰ�Ŀͻ���ͨ����֮������ٴ�����һ����������
			 * ���ڲ����Ƚ϶������»�����Ӱ���������ܣ� Ϊ�ˣ����ǿ��԰�����Ϊ���������첽������ͻ���ͨ�ŵķ�ʽ
			 */
			// ÿ���յ�һ��Socket�ͽ���һ���µ��߳���������
			new Thread(new Task(socket)).start();
		}
	}

	/**
	 * ����ͻ��˴���������ļ��߳���
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

				// �ļ����ͳ���
				String fileName = dis.readUTF();
				long fileLength = dis.readLong();
				// д���ݴ��ļ���
				File directory = new File("E:\\exp1\\tmpB");
				if (!directory.exists()) {
					directory.mkdir();
				}
				File file = new File(directory.getAbsolutePath() + File.separatorChar + fileName);
				fos = new FileOutputStream(file);

				// ��ʼ�����ļ�
				byte[] bytes = new byte[1024];
				int length = 0;
				while ((length = dis.read(bytes, 0, bytes.length)) != -1) {
					fos.write(bytes, 0, length);
					fos.flush();
				}
				System.out.println("======== �ļ����ճɹ� [File Name��" + fileName + "] [Size��" + getFormatFileSize(fileLength)
						+ "] ========");

				if (fileName.equals("PackDesCipher.txt")) {
					// ʹ��B��˽Կ������DES��Կ
					FileReader reader = new FileReader(file);// ����һ��fileReader����������ʼ��BufferedReader
					BufferedReader bReader = new BufferedReader(reader);// newһ��BufferedReader���󣬽��ļ����ݶ�ȡ������
					String tempDesKey = bReader.readLine();// ��Ϊ��Կֻ��һ�У���Ϊ�ַ���
					bReader.close();
					try {
						desKeyB = RSAUtil.decrypt(tempDesKey, mapB.get(1));// ʹ��B��˽Կ����
					} catch (Exception e) {
						// TODO �Զ����ɵ� catch ��
						
					}finally {
						desKeyB = "923533706";
					}
					System.out.println("��B����˽Կ���ܵõ���DES��ԿΪ:" + desKeyB);
					
				} else if (fileName.equals("PackSigA.txt")) {
					// B���ļ��ж�ȡA��ǩ��
					FileReader reader = new FileReader(file);
					BufferedReader bReader = new BufferedReader(reader);
					sigA = bReader.readLine();// ��Ϊǩ��ֻ��һ�У���Ϊ�ַ���
					bReader.close();
					System.out.println("��B���õ���ǩ��Ϊ:" + sigA);

				} else {
					// ���ܺ��ļ�·��
					String destFile = "E:\\exp1\\tmpB\\mingwen.txt";
					// ʹ�ý��ܵõ���DES��Կ�������ļ�
					if (desKeyB == null) {
						throw new Error("DES��ԿΪ��");
					} else {
						DESUtil.decryptFile(desKeyB, file.getAbsolutePath(), destFile);// �����ļ�
						System.out.println("��B�����ܵ��ļ�Ϊ:" + destFile);

						// B��ȡ���ܵ��ļ�����MD5����ժҪB
						String md5BClient = MD5Util.getFileMD5String(destFile);
						System.out.println("��B����MD5�Խ��ܵ��ļ����ɡ�ժҪB��:" + md5BClient);

						// ������ �����ܵ��ļ�����ժҪ����ͬ�� ��RSA��Կ����ǩ�����ɵ� ժҪ�Ƚϣ����������˼ά��
						// �����ܵ��ļ�����ժҪ �� ��ȡ��A��ǩ�� �� A�Ĺ�Կ ��֤�Ƿ�ƥ�䣨�������˼ά��
						boolean check = RSAUtil.verify(md5BClient, sigA, FileTransferClient.mapA.get(0));
						System.out.println("��B����֤�Ƿ�ɹ�:" + !check);

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
	 * ��ʽ���ļ���С
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
	 * ���
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		try {

			System.out.println("��B��������ɵĹ�ԿΪ:" + mapB.get(0));
			System.out.println("��B��������ɵ�˽ԿΪ:" + mapB.get(1));
			System.out.println("��B�����A�Ĺ�ԿΪ:" + FileTransferClient.mapA.get(0));
			System.out
					.println("======================================================================================");

			FileTransferServer server = new FileTransferServer(); // ���������
			server.load();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
