package util;

import org.apache.commons.codec.binary.Base64;
//import org.apache.tomcat.util.codec.binary.Base64;
import javax.crypto.Cipher;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;
import java.util.Map;

public class RSAUtil {
	// private
	public static Map<Integer, String> keyMap = new HashMap<Integer, String>(); // ���ڷ�װ��������Ĺ�Կ��˽Կ

	public static void main(String[] args) throws Exception {
		// ���ɹ�Կ��˽Կ
		genKeyPair();
		// �����ַ���
		String message = "df723820";
		System.out.println("������ɵĹ�ԿΪ:" + keyMap.get(0));
		System.out.println("������ɵ�˽ԿΪ:" + keyMap.get(1));
		String messageEn = encrypt(message, keyMap.get(0));
		System.out.println(message + "\t���ܺ���ַ���Ϊ:" + messageEn);

		File file = new File("E:\\exp1\\tmpRSA\\RSA.txt");
		FileWriter fwriter = null;
		fwriter = new FileWriter(file);
		fwriter.write(messageEn);
		fwriter.flush();
		fwriter.close();

		FileReader reader = new FileReader(file);// ����һ��fileReader����������ʼ��BufferedReader
		BufferedReader bReader = new BufferedReader(reader);// newһ��BufferedReader���󣬽��ļ����ݶ�ȡ������
		String tempDesKey = bReader.readLine();// ��Ϊ��Կֻ��һ�У���Ϊ�ַ���
		bReader.close();
		System.out.println("��ȡ����"+tempDesKey);
		String messageDECODE = RSAUtil.decrypt(tempDesKey, keyMap.get(1));// ʹ��B��˽Կ����
		System.out.println("��ԭ����ַ���Ϊ:" + messageDECODE);
		
//		String messageDe = decrypt(messageEn, keyMap.get(1));
//		System.out.println("��ԭ����ַ���Ϊ:" + messageDe);

	}

	/**
	 * ���������Կ��
	 * 
	 * @throws NoSuchAlgorithmException
	 */
	public static Map<Integer, String> genKeyPair() throws NoSuchAlgorithmException {
		// KeyPairGenerator���������ɹ�Կ��˽Կ�ԣ�����RSA�㷨���ɶ���
		KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA");
		// ��ʼ����Կ������������Կ��СΪ96-1024λ
		keyPairGen.initialize(1024, new SecureRandom());
		// ����һ����Կ�ԣ�������keyPair��
		KeyPair keyPair = keyPairGen.generateKeyPair();
		RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate(); // �õ�˽Կ
		RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic(); // �õ���Կ
		String publicKeyString = new String(Base64.encodeBase64(publicKey.getEncoded()));
		// �õ�˽Կ�ַ���
		String privateKeyString = new String(Base64.encodeBase64((privateKey.getEncoded())));
		keyMap.put(0, publicKeyString); // 0��ʾ��Կ
		keyMap.put(1, privateKeyString); // 1��ʾ˽Կ

		// ����Կ��˽Կ���浽Map
		Map<Integer, String> map = new HashMap<Integer, String>(); // ���ڷ�װ��������Ĺ�Կ��˽Կ
		map.put(0, publicKeyString); // 0��ʾ��Կ
		map.put(1, privateKeyString); // 1��ʾ˽Կ
		return map;

	}

	/**
	 * RSA��Կ����
	 * 
	 * @param str       �����ַ���
	 * @param publicKey ��Կ
	 * @return ����
	 * @throws Exception ���ܹ����е��쳣��Ϣ
	 */
	public static String encrypt(String str, String publicKey) throws Exception {
		// base64����Ĺ�Կ
		byte[] decoded = Base64.decodeBase64(publicKey);
		RSAPublicKey pubKey = (RSAPublicKey) KeyFactory.getInstance("RSA")
				.generatePublic(new X509EncodedKeySpec(decoded));
		// RSA����
		Cipher cipher = Cipher.getInstance("RSA");
		cipher.init(Cipher.ENCRYPT_MODE, pubKey);
		String outStr = Base64.encodeBase64String(cipher.doFinal(str.getBytes("UTF-8")));
		return outStr;
	}

	/**
	 * RSA˽Կ����
	 * 
	 * @param str        �����ַ���
	 * @param privateKey ˽Կ
	 * @return ����
	 * @throws Exception ���ܹ����е��쳣��Ϣ
	 */
	public static String decrypt(String str, String privateKey) throws Exception {
		// 64λ������ܺ���ַ���
		byte[] inputByte = Base64.decodeBase64(str.getBytes("UTF-8"));
		// base64�����˽Կ
		byte[] decoded = Base64.decodeBase64(privateKey);
		RSAPrivateKey priKey = (RSAPrivateKey) KeyFactory.getInstance("RSA")
				.generatePrivate(new PKCS8EncodedKeySpec(decoded));
		// RSA����
		Cipher cipher = Cipher.getInstance("RSA");
		cipher.init(Cipher.DECRYPT_MODE, priKey);
		String outStr = new String(cipher.doFinal(inputByte));
		return outStr;
	}

	/**
	 * RSA˽Կǩ��
	 * 
	 * ����ʹ�ù�Կ���м��ܣ�Ȼ��ʹ��˽Կ���ܡ������Ϸ�����Ҳ�У�˽Կ���ܣ���Կ���ܣ���
	 * ���ⲻ��ȫ�Ҵ������(����java.security)Ҳ��֧�֡�Ȼ�������ַ�ʽ�ڹ���APIʱ�Ƚ����á� ʹ��˽Կ����Ϣ����ǩ����Ȼ��ʹ�ù�Կ������֤ǩ����
	 * ���ֻ��ƿ���ȷ����Ϣȷʵ���Ź�Կ������(˽Կ������)��ʹ�ô��������Ϣ���ᱻ�۸ġ������ȿ�ǩ��������
	 * 
	 * @param plainText  ��ǩ����ժҪ�ַ���
	 * @param privateKey RSA˽Կ�ַ���
	 * @return ǩ���ַ���
	 * @throws Exception ǩ�������е��쳣��Ϣ
	 */
	public static String sign(String plainText, String privateKey) throws Exception {
		Signature privateSignature = Signature.getInstance("SHA256withRSA");
		// base64�����˽Կ String->RSAPrivateKey
		byte[] decoded = Base64.decodeBase64(privateKey);
		RSAPrivateKey priKey = (RSAPrivateKey) KeyFactory.getInstance("RSA")
				.generatePrivate(new PKCS8EncodedKeySpec(decoded));

		privateSignature.initSign(priKey);
		privateSignature.update(plainText.getBytes("UTF-8"));

		byte[] signature = privateSignature.sign();
		String outStr = Base64.encodeBase64String(signature);

//	    return Base64.getEncoder().encodeToString(signature);
		return outStr;
	}

	/**
	 * RSA��֤ǩ��
	 * 
	 * @param plainText ժҪ�ַ���
	 * @param signature ��ժҪ��ǩ��
	 * @param publicKey ��Կ
	 * @return ����ֵ���Ƿ�ƥ��
	 * @throws Exception ��֤�����е��쳣��Ϣ
	 */
	public static boolean verify(String plainText, String signature, String publicKey) throws Exception {
		Signature publicSignature = Signature.getInstance("SHA256withRSA");
		// base64����Ĺ�Կ String->RSAPublicKey
		byte[] decoded = Base64.decodeBase64(publicKey);
		RSAPublicKey pubKey = (RSAPublicKey) KeyFactory.getInstance("RSA")
				.generatePublic(new X509EncodedKeySpec(decoded));

		publicSignature.initVerify(pubKey);
		publicSignature.update(plainText.getBytes("UTF-8"));

//	    byte[] signatureBytes = Base64.getDecoder().decode(signature);

		// 64λ������ܺ��ǩ�� String->byte[]
		byte[] signatureBytes = Base64.decodeBase64(signature.getBytes("UTF-8"));

		return publicSignature.verify(signatureBytes);
	}

}
