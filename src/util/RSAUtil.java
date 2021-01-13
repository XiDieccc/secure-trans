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
	public static Map<Integer, String> keyMap = new HashMap<Integer, String>(); // 用于封装随机产生的公钥与私钥

	public static void main(String[] args) throws Exception {
		// 生成公钥和私钥
		genKeyPair();
		// 加密字符串
		String message = "df723820";
		System.out.println("随机生成的公钥为:" + keyMap.get(0));
		System.out.println("随机生成的私钥为:" + keyMap.get(1));
		String messageEn = encrypt(message, keyMap.get(0));
		System.out.println(message + "\t加密后的字符串为:" + messageEn);

		File file = new File("E:\\exp1\\tmpRSA\\RSA.txt");
		FileWriter fwriter = null;
		fwriter = new FileWriter(file);
		fwriter.write(messageEn);
		fwriter.flush();
		fwriter.close();

		FileReader reader = new FileReader(file);// 定义一个fileReader对象，用来初始化BufferedReader
		BufferedReader bReader = new BufferedReader(reader);// new一个BufferedReader对象，将文件内容读取到缓存
		String tempDesKey = bReader.readLine();// 因为密钥只有一行，且为字符串
		bReader.close();
		System.out.println("读取到的"+tempDesKey);
		String messageDECODE = RSAUtil.decrypt(tempDesKey, keyMap.get(1));// 使用B的私钥解密
		System.out.println("还原后的字符串为:" + messageDECODE);
		
//		String messageDe = decrypt(messageEn, keyMap.get(1));
//		System.out.println("还原后的字符串为:" + messageDe);

	}

	/**
	 * 随机生成密钥对
	 * 
	 * @throws NoSuchAlgorithmException
	 */
	public static Map<Integer, String> genKeyPair() throws NoSuchAlgorithmException {
		// KeyPairGenerator类用于生成公钥和私钥对，基于RSA算法生成对象
		KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA");
		// 初始化密钥对生成器，密钥大小为96-1024位
		keyPairGen.initialize(1024, new SecureRandom());
		// 生成一个密钥对，保存在keyPair中
		KeyPair keyPair = keyPairGen.generateKeyPair();
		RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate(); // 得到私钥
		RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic(); // 得到公钥
		String publicKeyString = new String(Base64.encodeBase64(publicKey.getEncoded()));
		// 得到私钥字符串
		String privateKeyString = new String(Base64.encodeBase64((privateKey.getEncoded())));
		keyMap.put(0, publicKeyString); // 0表示公钥
		keyMap.put(1, privateKeyString); // 1表示私钥

		// 将公钥和私钥保存到Map
		Map<Integer, String> map = new HashMap<Integer, String>(); // 用于封装随机产生的公钥与私钥
		map.put(0, publicKeyString); // 0表示公钥
		map.put(1, privateKeyString); // 1表示私钥
		return map;

	}

	/**
	 * RSA公钥加密
	 * 
	 * @param str       加密字符串
	 * @param publicKey 公钥
	 * @return 密文
	 * @throws Exception 加密过程中的异常信息
	 */
	public static String encrypt(String str, String publicKey) throws Exception {
		// base64编码的公钥
		byte[] decoded = Base64.decodeBase64(publicKey);
		RSAPublicKey pubKey = (RSAPublicKey) KeyFactory.getInstance("RSA")
				.generatePublic(new X509EncodedKeySpec(decoded));
		// RSA加密
		Cipher cipher = Cipher.getInstance("RSA");
		cipher.init(Cipher.ENCRYPT_MODE, pubKey);
		String outStr = Base64.encodeBase64String(cipher.doFinal(str.getBytes("UTF-8")));
		return outStr;
	}

	/**
	 * RSA私钥解密
	 * 
	 * @param str        加密字符串
	 * @param privateKey 私钥
	 * @return 铭文
	 * @throws Exception 解密过程中的异常信息
	 */
	public static String decrypt(String str, String privateKey) throws Exception {
		// 64位解码加密后的字符串
		byte[] inputByte = Base64.decodeBase64(str.getBytes("UTF-8"));
		// base64编码的私钥
		byte[] decoded = Base64.decodeBase64(privateKey);
		RSAPrivateKey priKey = (RSAPrivateKey) KeyFactory.getInstance("RSA")
				.generatePrivate(new PKCS8EncodedKeySpec(decoded));
		// RSA解密
		Cipher cipher = Cipher.getInstance("RSA");
		cipher.init(Cipher.DECRYPT_MODE, priKey);
		String outStr = new String(cipher.doFinal(inputByte));
		return outStr;
	}

	/**
	 * RSA私钥签名
	 * 
	 * 我们使用公钥进行加密，然后使用私钥解密。理论上反过来也行（私钥加密，公钥解密），
	 * 但这不安全且大多数库(包括java.security)也不支持。然而，这种方式在构建API时比较有用。 使用私钥对消息进行签名，然后使用公钥进行验证签名。
	 * 这种机制可以确保消息确实来着公钥创建者(私钥持有者)，使得传输过程消息不会被篡改。下面先看签名方法：
	 * 
	 * @param plainText  待签名的摘要字符串
	 * @param privateKey RSA私钥字符串
	 * @return 签名字符串
	 * @throws Exception 签名过程中的异常信息
	 */
	public static String sign(String plainText, String privateKey) throws Exception {
		Signature privateSignature = Signature.getInstance("SHA256withRSA");
		// base64编码的私钥 String->RSAPrivateKey
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
	 * RSA验证签名
	 * 
	 * @param plainText 摘要字符串
	 * @param signature 该摘要的签名
	 * @param publicKey 公钥
	 * @return 布尔值，是否匹配
	 * @throws Exception 验证过程中的异常信息
	 */
	public static boolean verify(String plainText, String signature, String publicKey) throws Exception {
		Signature publicSignature = Signature.getInstance("SHA256withRSA");
		// base64编码的公钥 String->RSAPublicKey
		byte[] decoded = Base64.decodeBase64(publicKey);
		RSAPublicKey pubKey = (RSAPublicKey) KeyFactory.getInstance("RSA")
				.generatePublic(new X509EncodedKeySpec(decoded));

		publicSignature.initVerify(pubKey);
		publicSignature.update(plainText.getBytes("UTF-8"));

//	    byte[] signatureBytes = Base64.getDecoder().decode(signature);

		// 64位解码加密后的签名 String->byte[]
		byte[] signatureBytes = Base64.decodeBase64(signature.getBytes("UTF-8"));

		return publicSignature.verify(signatureBytes);
	}

}
