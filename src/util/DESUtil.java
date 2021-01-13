package util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.Key;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.IvParameterSpec;

public class DESUtil {
	 
    /**
     * ƫ�Ʊ������̶�ռ8λ�ֽ�
     */
    private final static String IV_PARAMETER = "12345678";
    /**
     * ��Կ�㷨
     */
    private static final String ALGORITHM = "DES";
    /**
     * ����/�����㷨-����ģʽ-���ģʽ
     */
    private static final String CIPHER_ALGORITHM = "DES/CBC/PKCS5Padding";
    /**
     * Ĭ�ϱ���
     */
    private static final String CHARSET = "utf-8";
 
    /**
     * ����key
     *
     * @param password
     * @return
     * @throws Exception
     */
    private static Key generateKey(String password) throws Exception {
        DESKeySpec dks = new DESKeySpec(password.getBytes(CHARSET));
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(ALGORITHM);
        return keyFactory.generateSecret(dks);
    }
 
 
    /**
     * DES�����ַ���
     *
     * @param password �������룬���Ȳ��ܹ�С��8λ
     * @param data �������ַ���
     * @return ���ܺ�����
     */
    public static String encrypt(String password, String data) {
        if (password== null || password.length() < 8) {
            throw new RuntimeException("����ʧ�ܣ�key����С��8λ");
        }
        if (data == null)
            return null;
        try {
            Key secretKey = generateKey(password);
            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            IvParameterSpec iv = new IvParameterSpec(IV_PARAMETER.getBytes(CHARSET));
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, iv);
            byte[] bytes = cipher.doFinal(data.getBytes(CHARSET));
 
            //JDK1.8�����Ͽ�ֱ��ʹ��Base64��JDK1.7�����¿���ʹ��BASE64Encoder
            //Androidƽ̨����ʹ��android.util.Base64
            return new String(Base64.getEncoder().encode(bytes));
 
        } catch (Exception e) {
            e.printStackTrace();
            return data;
        }
    }
 
    /**
     * DES�����ַ���
     *
     * @param password �������룬���Ȳ��ܹ�С��8λ
     * @param data �������ַ���
     * @return ���ܺ�����
     */
    public static String decrypt(String password, String data) {
        if (password== null || password.length() < 8) {
            throw new RuntimeException("����ʧ�ܣ�key����С��8λ");
        }
        if (data == null)
            return null;
        try {
            Key secretKey = generateKey(password);
            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            IvParameterSpec iv = new IvParameterSpec(IV_PARAMETER.getBytes(CHARSET));
            cipher.init(Cipher.DECRYPT_MODE, secretKey, iv);
            return new String(cipher.doFinal(Base64.getDecoder().decode(data.getBytes(CHARSET))), CHARSET);
        } catch (Exception e) {
            e.printStackTrace();
            return data;
        }
    }
 
    /**
     * DES�����ļ�
     *
     * @param srcFile  �����ܵ��ļ�
     * @param destFile ���ܺ��ŵ��ļ�·��
     * @return ���ܺ���ļ�·��
     */
    public static String encryptFile(String password, String srcFile, String destFile) {
 
        if (password== null || password.length() < 8) {
            throw new RuntimeException("����ʧ�ܣ�key����С��8λ");
        }
        try {
            IvParameterSpec iv = new IvParameterSpec(IV_PARAMETER.getBytes(CHARSET));
            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, generateKey(password), iv);
            InputStream is = new FileInputStream(srcFile);
            OutputStream out = new FileOutputStream(destFile);
            CipherInputStream cis = new CipherInputStream(is, cipher);
            byte[] buffer = new byte[1024];
            int r;
            while ((r = cis.read(buffer)) > 0) {
                out.write(buffer, 0, r);
            }
            cis.close();
            is.close();
            out.close();
            return destFile;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }
 
    /**
     * DES�����ļ�
     *
     * @param srcFile  �Ѽ��ܵ��ļ�
     * @param destFile ���ܺ��ŵ��ļ�·��
     * @return ���ܺ���ļ�·��
     */
    public static String decryptFile(String password, String srcFile, String destFile) {
        if (password== null || password.length() < 8) {
            throw new RuntimeException("����ʧ�ܣ�key����С��8λ");
        }
        try {
            File file = new File(destFile);
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            IvParameterSpec iv = new IvParameterSpec(IV_PARAMETER.getBytes(CHARSET));
            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, generateKey(password), iv);
            InputStream is = new FileInputStream(srcFile);
            OutputStream out = new FileOutputStream(destFile);
            CipherOutputStream cos = new CipherOutputStream(out, cipher);
            byte[] buffer = new byte[1024];
            int r;
            while ((r = is.read(buffer)) >= 0) {
                cos.write(buffer, 0, r);
            }
            cos.close(); 
            is.close();
            out.close();
            return destFile;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }


    public static void main(String[] args) {
//    	String miwen = encrypt("zenmeyang", "nihaihaoma");
//    	String mingwen = decrypt("zenmeyang", miwen);
//    	System.out.println(miwen);
//    	System.out.println(mingwen);
    	
    	/**
    	 * ���ܺ���ļ�·�������� �� ���ܵ��ļ�����һ��������.txt .chm �ļ����Ҽ��ܺ��ļ��򲻿������ļ�����Ӱ�죬ϵͳ
    	 * ���ܱ��ϣ������ܺ���ܵõ����ļ�
    	 * ����дһ���������� ���� ѡȡ���ļ����ͣ�������Ӧ�������ļ���������ʽ
    	 */
    	String dest = "E:\\tmp\\miwen.chm";
//    	System.out.println(encryptFile("923533706", "C:\\Users\\szzs\\Desktop\\api\\jdk api 1.8_google.CHM", dest));
//    	System.out.println(decryptFile("923533706", dest, "E:\\tmp\\mingwen.chm"));
    	
    }
}