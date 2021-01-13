package starup;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Map;

import util.DESUtil;
import util.MD5Util;
import util.RSAUtil;

/**
 * �������
 *
 */
public class ServerBootstrap {

	public static void main(String[] args) throws Exception {

		// A��B��RSA��Կ��Կ��
		Map<Integer, String> keyMapA = RSAUtil.genKeyPair();
		Map<Integer, String> keyMapB = RSAUtil.genKeyPair();

		/**
		 * A
		 */
		System.out.println("��A��������ɵġ���Կ��Ϊ:" + keyMapA.get(0));
		System.out.println("��A��������ɵġ�˽Կ��Ϊ:" + keyMapA.get(1));

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
		System.out.println("��A�����B�Ĺ�ԿΪ:" + keyMapB.get(0));
		System.out.println("============================================================");
		String desCipher = RSAUtil.encrypt(desKey, keyMapB.get(0));
		System.out.println("��A��ʹ��B�Ĺ�Կ���ܺ��DES��ԿΪ:" + desCipher);

		// A��ȡ�ļ�����MD5����ժҪA
		String md5AClient = MD5Util.getFileMD5String(filePath);
		System.out.println("��A����MD5���ɵġ�ժҪA��Ϊ:" + md5AClient);

		// ��A��˽Կ��ժҪA����ǩ��
		String sigA = RSAUtil.sign(md5AClient, keyMapA.get(1));
		System.out.println("��A����RSA��˽Կ��ժҪA���ɵġ�ǩ����Ϊ:" + sigA);

		// A��DES�Գ���Կ���ļ����м���
		String destFile = "E:\\exp1\\tmpA\\miwen.txt";
		String fileCipher = DESUtil.encryptFile(desKey, file.getAbsolutePath(), destFile);
		System.out.println("��A����des��Կ���ļ����м���:" + fileCipher);

		// A����ǩ�����͡����ܺ��DES��Կ��д���ļ�
		String packageSigA = "E:\\exp1\\tmpA\\PackSigA.txt";
		String packageDesCipher = "E:\\exp1\\tmpA\\PackDesCipher.txt";

		// true��ʾ������ԭ�������ݣ����Ǽӵ��ļ��ĺ��档��Ҫ����ԭ�������ݣ�ֱ��ʡ����������ͺ� 
		//fwriter = new FileWriter(packageA, true);
		FileWriter fwriter = new FileWriter(packageSigA);
		fwriter.write(sigA);
		fwriter.flush();
		fwriter.close();
		fwriter = new FileWriter(packageDesCipher);
		fwriter.write(desCipher);
		fwriter.flush();
		fwriter.close();
		
		
		/**
		 * B
		 */
		System.out.println("------------------------------------------------------------------------------------------------");
		System.out.println("��B��������ɵĹ�ԿΪ:" + keyMapB.get(0));
		System.out.println("��B��������ɵ�˽ԿΪ:" + keyMapB.get(1));
		System.out.println("��B�����A�Ĺ�ԿΪ:" + keyMapA.get(0));
		System.out
				.println("======================================================================================");
		
		File fileDES = new File(packageDesCipher);
		File fileSig = new File(packageSigA);
		File fileCiph = new File(destFile);
		
		// ʹ��B��˽Կ������DES��Կ
		FileReader reader = new FileReader(fileDES);// ����һ��fileReader����������ʼ��BufferedReader
		BufferedReader bReader = new BufferedReader(reader);// newһ��BufferedReader���󣬽��ļ����ݶ�ȡ������
		String tempDesKey = bReader.readLine();// ��Ϊ��Կֻ��һ�У���Ϊ�ַ���
		bReader.close();
		String desKeyB = RSAUtil.decrypt(tempDesKey, keyMapB.get(1));// ʹ��B��˽Կ����
		System.out.println("��B����˽Կ���ܵõ���DES��ԿΪ:" + desKeyB);
		
		// B���ļ��ж�ȡA��ǩ��
		reader = new FileReader(fileSig);
		bReader = new BufferedReader(reader);
		String BsigA = bReader.readLine();// ��Ϊǩ��ֻ��һ�У���Ϊ�ַ���
		bReader.close();
		System.out.println("��B���õ���ǩ��Ϊ:" + BsigA);
		
		// ���ܺ��ļ�·��
		String destFile2 = "E:\\exp1\\tmpB\\mingwen.txt";
		// ʹ�ý��ܵõ���DES��Կ�������ļ�
		if (desKeyB == null) {
			throw new Error("DES��ԿΪ��");
		} else {
			DESUtil.decryptFile(desKeyB, fileCiph.getAbsolutePath(), destFile);// �����ļ�
			System.out.println("��B�����ܵ��ļ�Ϊ:" + destFile2);

			// B��ȡ���ܵ��ļ�����MD5����ժҪB
			String md5BClient = MD5Util.getFileMD5String(destFile2);
			System.out.println("��B����MD5�Խ��ܵ��ļ����ɡ�ժҪB��:" + md5BClient);

			// ������ �����ܵ��ļ�����ժҪ����ͬ�� ��RSA��Կ����ǩ�����ɵ� ժҪ�Ƚϣ����������˼ά��
			// �����ܵ��ļ�����ժҪ �� ��ȡ��A��ǩ�� �� A�Ĺ�Կ ��֤�Ƿ�ƥ�䣨�������˼ά��
			boolean check = RSAUtil.verify(md5BClient, BsigA, keyMapA.get(0));
			System.out.println("��B����֤�Ƿ�ɹ�:" + check);
		}
		
	}

}
