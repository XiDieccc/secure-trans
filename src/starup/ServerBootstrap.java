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
 * 启动入口
 *
 */
public class ServerBootstrap {

	public static void main(String[] args) throws Exception {

		// A和B的RSA公钥密钥对
		Map<Integer, String> keyMapA = RSAUtil.genKeyPair();
		Map<Integer, String> keyMapB = RSAUtil.genKeyPair();

		/**
		 * A
		 */
		System.out.println("【A】随机生成的【公钥】为:" + keyMapA.get(0));
		System.out.println("【A】随机生成的【私钥】为:" + keyMapA.get(1));

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
		System.out.println("【A】获得B的公钥为:" + keyMapB.get(0));
		System.out.println("============================================================");
		String desCipher = RSAUtil.encrypt(desKey, keyMapB.get(0));
		System.out.println("【A】使用B的公钥加密后的DES密钥为:" + desCipher);

		// A读取文件并用MD5生成摘要A
		String md5AClient = MD5Util.getFileMD5String(filePath);
		System.out.println("【A】用MD5生成的【摘要A】为:" + md5AClient);

		// 用A的私钥对摘要A生成签名
		String sigA = RSAUtil.sign(md5AClient, keyMapA.get(1));
		System.out.println("【A】用RSA的私钥对摘要A生成的【签名】为:" + sigA);

		// A用DES对称密钥对文件进行加密
		String destFile = "E:\\exp1\\tmpA\\miwen.txt";
		String fileCipher = DESUtil.encryptFile(desKey, file.getAbsolutePath(), destFile);
		System.out.println("【A】用des密钥对文件进行加密:" + fileCipher);

		// A将【签名】和【加密后的DES密钥】写入文件
		String packageSigA = "E:\\exp1\\tmpA\\PackSigA.txt";
		String packageDesCipher = "E:\\exp1\\tmpA\\PackDesCipher.txt";

		// true表示不覆盖原来的内容，而是加到文件的后面。若要覆盖原来的内容，直接省略这个参数就好 
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
		System.out.println("【B】随机生成的公钥为:" + keyMapB.get(0));
		System.out.println("【B】随机生成的私钥为:" + keyMapB.get(1));
		System.out.println("【B】获得A的公钥为:" + keyMapA.get(0));
		System.out
				.println("======================================================================================");
		
		File fileDES = new File(packageDesCipher);
		File fileSig = new File(packageSigA);
		File fileCiph = new File(destFile);
		
		// 使用B的私钥来解密DES密钥
		FileReader reader = new FileReader(fileDES);// 定义一个fileReader对象，用来初始化BufferedReader
		BufferedReader bReader = new BufferedReader(reader);// new一个BufferedReader对象，将文件内容读取到缓存
		String tempDesKey = bReader.readLine();// 因为密钥只有一行，且为字符串
		bReader.close();
		String desKeyB = RSAUtil.decrypt(tempDesKey, keyMapB.get(1));// 使用B的私钥解密
		System.out.println("【B】用私钥解密得到的DES密钥为:" + desKeyB);
		
		// B从文件中读取A的签名
		reader = new FileReader(fileSig);
		bReader = new BufferedReader(reader);
		String BsigA = bReader.readLine();// 因为签名只有一行，且为字符串
		bReader.close();
		System.out.println("【B】得到的签名为:" + BsigA);
		
		// 解密后文件路径
		String destFile2 = "E:\\exp1\\tmpB\\mingwen.txt";
		// 使用解密得到的DES密钥来解密文件
		if (desKeyB == null) {
			throw new Error("DES密钥为空");
		} else {
			DESUtil.decryptFile(desKeyB, fileCiph.getAbsolutePath(), destFile);// 解密文件
			System.out.println("【B】解密的文件为:" + destFile2);

			// B读取解密的文件并用MD5生成摘要B
			String md5BClient = MD5Util.getFileMD5String(destFile2);
			System.out.println("【B】用MD5对解密的文件生成【摘要B】:" + md5BClient);

			// 本该是 将解密的文件生成摘要，再同和 用RSA公钥解密签名生成的 摘要比较；（逆向解密思维）
			// 将解密的文件生成摘要 和 读取的A的签名 和 A的公钥 验证是否匹配（正向加密思维）
			boolean check = RSAUtil.verify(md5BClient, BsigA, keyMapA.get(0));
			System.out.println("【B】验证是否成功:" + check);
		}
		
	}

}
