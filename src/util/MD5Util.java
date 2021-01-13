package util;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
 

public class MD5Util {
 
    /** 16���Ƶ��ַ������� */
    private final static String[] hexDigitsStrings = { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d",
            "e", "f" };
    
    /** 16���Ƶ��ַ��� */
    private final static char [] hexDigitsChar = {'0', '1', '2', '3', '4', '5', '6', '7', '8',  
            '9', 'A', 'B', 'C', 'D', 'E', 'F'}; 
    
    /** 
     * MD5�����ַ���
     * 
     * @param source Դ�ַ��� 
     * 
     * @return ���ܺ���ַ��� 
     * 
     */  
    public static String getMD5(String source) {  
        String mdString = null;  
        if (source != null) {  
            try {  
                mdString = getMD5(source.getBytes("UTF-8"));  
            } catch (UnsupportedEncodingException e) {  
                e.printStackTrace();  
            }  
        }  
        return mdString;  
    }
    
    /** 
     * MD5������byte�����ʾ���ַ���
     * 
     * @param source Դ�ֽ����� 
     * 
     * @return ���ܺ���ַ��� 
     */  
    public static String getMD5(byte[] source) {  
        String s = null;  
 
        final int temp = 0xf;  
        final int arraySize = 32;  
        final int strLen = 16;  
        final int offset = 4;  
        try {  
            java.security.MessageDigest md = java.security.MessageDigest  
                    .getInstance("MD5");  
            md.update(source);  
            byte [] tmp = md.digest();  
            char [] str = new char[arraySize];  
            int k = 0;  
            for (int i = 0; i < strLen; i++) {  
                byte byte0 = tmp[i];  
                str[k++] = hexDigitsChar[byte0 >>> offset & temp];  
                str[k++] = hexDigitsChar[byte0 & temp];  
            }  
            s = new String(str);  
        } catch (Exception e) {  
            e.printStackTrace();  
        }  
        return s;  
    }
    
    /**
     * * ��ȡ�ļ���MD5ֵ
     * 
     * @param file
     *            Ŀ���ļ�
     * 
     * @return MD5�ַ���
     * @throws Exception 
     */
    public static String getFileMD5String(File file) throws Exception {
        String ret = "";
        FileInputStream in = null;
        FileChannel ch = null;
        try {
            in = new FileInputStream(file);
            ch = in.getChannel();
            ByteBuffer byteBuffer = ch.map(FileChannel.MapMode.READ_ONLY, 0,
                    file.length());
            MessageDigest messageDigest=MessageDigest.getInstance("MD5");
            messageDigest.update(byteBuffer);
            ret = byteArrayToHexString(messageDigest.digest());
        } catch (IOException e) {
            e.printStackTrace();
 
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (ch != null) {
                try {
                    ch.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return ret;
    }
 
    /**
     * * ��ȡ�ļ���MD5ֵ
     * 
     * @param fileName
     *            Ŀ���ļ�����������
     * 
     * @return MD5�ַ���
     * @throws Exception 
     */
    public static String getFileMD5String(String fileName) throws Exception {
        return getFileMD5String(new File(fileName));
    }
    
    /**
     * ����
     * 
     * @param source
     *            ��Ҫ���ܵ�ԭ�ַ���
     * @param encoding
     *            ָ����������
     * @param uppercase
     *            �Ƿ�תΪ��д�ַ���
     * @return
     */
    public static String MD5Encode(String source, String encoding, boolean uppercase) {
        String result = null;
        try {
            result = source;
            // ���MD5ժҪ����
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            // ʹ��ָ�����ֽ��������ժҪ��Ϣ
            messageDigest.update(result.getBytes(encoding));
            // messageDigest.digest()���16λ����
            result = byteArrayToHexString(messageDigest.digest());
 
        } catch (Exception e) {
            e.printStackTrace();
        }
        return uppercase ? result.toUpperCase() : result;
    }
    
    
    /**
     * ת���ֽ�����Ϊ16�����ַ���
     * 
     * @param bytes
     *            �ֽ�����
     * @return
     */
    private static String byteArrayToHexString(byte[] bytes) {
        StringBuilder stringBuilder = new StringBuilder();
        for (byte tem : bytes) {
            stringBuilder.append(byteToHexString(tem));
        }
        return stringBuilder.toString();
    }
    
    /**
     * * ���ֽ�������ָ�������������ת����16�����ַ���
     * 
     * @param bytes
     *            Ŀ���ֽ�����
     * 
     * @param start
     *            ��ʼλ�ã�������λ�ã�
     * 
     * @param end
     *            ����λ�ã���������λ�ã�
     * 
     * @return ת�����
     */
    public static String bytesToHex(byte bytes[], int start, int end) {
        StringBuilder sb = new StringBuilder();
        for (int i = start; i < start + end; i++) {
            sb.append(byteToHexString(bytes[i]));
        }
        return sb.toString();
    }
    
    /**
     * ת��byte��16����
     * 
     * @param b
     *            Ҫת����byte
     * @return 16���ƶ�Ӧ���ַ�
     */
    private static String byteToHexString(byte b) {
        int n = b;
        if (n < 0) {
            n = 256 + n;
        }
        int d1 = n / 16;
        int d2 = n % 16;
        return hexDigitsStrings[d1] + hexDigitsStrings[d2];
    }
    
    
    
    /**
     * * У����������MD5�Ƿ�һ��
     * 
     * @param pwd
     *            �����ַ���
     * 
     * @param md5
     *            ��׼MD5ֵ
     * 
     * @return ������
     */
    public static boolean checkPassword(String pwd, String md5) {
        return getMD5(pwd).equalsIgnoreCase(md5);
    }
    
    /**
     * * У����������MD5�Ƿ�һ��
     * 
     * @param pwd
     *            ���ַ������ʾ������
     * 
     * @param md5
     *            ��׼MD5ֵ
     * 
     * @return ������
     */
    public static boolean checkPassword(char[] pwd, String md5) {
        return checkPassword(new String(pwd), md5);
    }
    
    
    /**
     * * �����ļ���MD5ֵ
     * 
     * @param file
     *            Ŀ���ļ�
     * 
     * @param md5
     *            ��׼MD5ֵ
     * 
     * @return ������
     * @throws Exception 
     */
    public static boolean checkFileMD5(File file, String md5) throws Exception {
        return getFileMD5String(file).equalsIgnoreCase(md5);
    }
    
    /**
     * * �����ļ���MD5ֵ
     * 
     * @param fileName
     *            Ŀ���ļ�����������
     * 
     * @param md5
     *            ��׼MD5ֵ
     * 
     * @return ������
     * @throws Exception 
     */
    public static boolean checkFileMD5(String fileName, String md5) throws Exception {
        return checkFileMD5(new File(fileName), md5);
    }
}
