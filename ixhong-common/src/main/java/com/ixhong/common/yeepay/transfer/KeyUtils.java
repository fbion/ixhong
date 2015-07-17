package com.ixhong.common.yeepay.transfer;

import com.cfca.util.pki.PKIException;
import com.cfca.util.pki.cipher.JKey;
import com.cfca.util.pki.cipher.JKeyPair;
import com.cfca.util.pki.cipher.Mechanism;
import com.cfca.util.pki.cipher.Session;
import com.cfca.util.pki.encoders.Base64;
import com.cfca.util.pki.pkcs.PKCS12;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

/**
 * Created by shenhongxi on 15/6/15.
 */
public class KeyUtils {

    public static final String RC4 = "RC4";
    public static final String DES = "DES";
    public static final String DES3 = "DESede";

    public KeyUtils() {
    }

    public static JKey getPriKey(String pfxPath, String pfxPWD) throws PKIException {
        PKCS12 p12 = new PKCS12();
        FileInputStream fin = null;

        try {
            fin = new FileInputStream(pfxPath);
        } catch (FileNotFoundException var5) {
            throw new PKIException("850909", "PFX私钥证书不存在", var5);
        }

        p12.load(fin);
        p12.decrypt(pfxPWD.toCharArray());
        JKey prvKey = p12.getPrivateKey();
        return prvKey;
    }

    public static JKey getPriKey(byte[] pfxData, String pfxPWD) throws PKIException {
        PKCS12 p12 = new PKCS12();
        p12.load(pfxData);
        p12.decrypt(pfxPWD.toCharArray());
        JKey prvKey = p12.getPrivateKey();
        return prvKey;
    }

    public static JKeyPair genKeyPair(int keyLen, Session session) throws PKIException {
        Mechanism keyGen = new Mechanism("RSA");
        JKeyPair keyPair = session.generateKeyPair(keyGen, keyLen);
        return keyPair;
    }

    public static JKey generateKey(String keyType, Session session) throws PKIException {
        boolean keyLen = true;
        if(!keyType.equalsIgnoreCase("DES") && !keyType.equalsIgnoreCase("RC4") && !keyType.equalsIgnoreCase("DESede")) {
            throw new PKIException("850910", "不支持的密钥类型");
        } else {
            short keyLen1;
            if(keyType.equalsIgnoreCase("DES")) {
                keyLen1 = 64;
            } else if(keyType.equalsIgnoreCase("RC4")) {
                keyLen1 = 128;
            } else {
                keyLen1 = 192;
            }

            return session.generateKey(new Mechanism(keyType), keyLen1);
        }
    }

    public static void generateKeyFile(String keyType, String keyFile, Session session) throws PKIException {
        boolean keyLen = true;
        if(!keyType.equalsIgnoreCase("DES") && !keyType.equalsIgnoreCase("RC4") && !keyType.equalsIgnoreCase("DESede")) {
            throw new PKIException("850910", "不支持的密钥类型");
        } else {
            short keyLen1;
            if(keyType.equalsIgnoreCase("DES")) {
                keyLen1 = 64;
            } else if(keyType.equalsIgnoreCase("RC4")) {
                keyLen1 = 128;
            } else {
                keyLen1 = 192;
            }

            JKey key = session.generateKey(new Mechanism(keyType), keyLen1);

            try {
                FileOutputStream ex = new FileOutputStream(keyFile);
                ex.write(Base64.encode(key.getKey()));
                ex.flush();
                ex.close();
            } catch (Exception var6) {
                throw new PKIException("850925", "将对称密钥写入文件失败", var6);
            }
        }
    }

    public static void main(String[] args) throws Exception {
//        try {
//            JCrypto ex = JCrypto.getInstance();
//            ex.initialize("JSOFT_LIB", (Object)null);
//            Session session1 = ex.openSession("JSOFT_LIB");
//            JKey priKey = getPriKey("c:/test.pfx", "11111111");
//        } catch (PKIException var4) {
//            System.out.println("错误码:" + var4.getErrCode());
//            System.out.println("错误信息:" + var4.getErrDesc());
//            Exception session = var4.getHistory();
//        }

//        File file = new File("xhd-common/src/main/resources/10040003895.pfx");
//        InputStream is = new FileInputStream(file);
//        System.out.println(is);

    }
}
