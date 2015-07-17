package com.ixhong.common.yeepay.transfer;

import com.cfca.util.pki.PKIException;
import com.cfca.util.pki.Parser;
import com.cfca.util.pki.cert.X509Cert;
import com.cfca.util.pki.cipher.JCrypto;
import com.cfca.util.pki.cipher.Session;
import com.cfca.util.pki.crl.X509CRL;
import com.cfca.util.pki.extension.CRLDistributionPointsExt;
import com.cfca.util.pki.extension.DistributionPointsExt;
import com.cfca.util.pki.pkcs.P7B;
import com.cfca.util.pki.pkcs.PKCS12;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.util.Hashtable;

/**
 * Created by shenhongxi on 15/6/15.
 */
public class CertUtils {

    public CertUtils() {
    }

    public static X509Cert getCert(byte[] pfxData, String pfxPWD) throws PKIException {
        PKCS12 p12 = new PKCS12();
        p12.load(pfxData);
        p12.decrypt(pfxPWD.toCharArray());
        return p12.getCertificate();
    }

    public static X509Cert getCert(String pfxPath, String pfxPWD) throws PKIException {
        PKCS12 p12 = new PKCS12();
        p12.load(pfxPath);
        p12.decrypt(pfxPWD.toCharArray());
        return p12.getCertificate();
    }

    public static void changePfxPWD(String oldPfxPath, String oldPfxPWD, String newPfxPath, String newPfxPWD) throws PKIException {
        PKCS12 p12 = new PKCS12();
        p12.load(oldPfxPath);
        p12.decrypt(oldPfxPWD.toCharArray());
        p12.generatePfxFile(p12.getPrivateKey(), p12.getCerts(), newPfxPWD.toCharArray(), newPfxPath);
    }

    public static X509Cert generateCert(byte[] certData) throws PKIException {
        X509Cert cert = new X509Cert(certData);
        return cert;
    }

    public static X509Cert generateCert(String certPath) throws PKIException {
        FileInputStream fis = null;

        try {
            fis = new FileInputStream(certPath);
        } catch (FileNotFoundException var3) {
            throw new PKIException("850900", "证书文件不存在 " + var3.getMessage());
        }

        X509Cert cert = new X509Cert(fis);
        return cert;
    }

    public static X509Cert[] parseP7b(byte[] data) throws PKIException {
        P7B p7b = new P7B();
        return p7b.parseP7b(data);
    }

    public static X509Cert[] parseP7b(String p7bPath) throws PKIException {
        P7B p7b = new P7B();
        return p7b.parseP7b(p7bPath);
    }

    public static boolean verifyCert(X509Cert userCert, X509Cert[] caCerts, String crlPath, Session session) throws PKIException {
        verifyCertDate(userCert);
        return verifyCertSign(userCert, caCerts, session)?(crlPath != null?verifyCertByCRLOutLine(userCert, crlPath, caCerts, session):verifyCertByCRLOnLine(userCert)):false;
    }

    public static boolean verifyCertSign(X509Cert userCert, X509Cert[] caCerts, Session session) throws PKIException {
        if(caCerts.length < 1) {
            throw new PKIException("850903", "证书链为空");
        } else if(caCerts.length == 1) {
            return userCert.verify(caCerts[0].getPublicKey(), session);
        } else {
            verifyCertChain(caCerts, userCert.getIssuer(), session);
            boolean userCertSignVerify = userCert.verify(caCerts[0].getPublicKey(), session);
            return userCertSignVerify;
        }
    }

    public static boolean verifyCertDate(X509Cert userCert) throws PKIException {
        X509Certificate cert = Parser.convertX509Cert(userCert);

        try {
            cert.checkValidity();
            return true;
        } catch (CertificateNotYetValidException var3) {
            throw new PKIException("850901", "证书未生效");
        } catch (CertificateExpiredException var4) {
            throw new PKIException("850902", "证书已过期");
        }
    }

    public static boolean verifyCertByCRLOutLine(X509Cert userCert, String crlPath, X509Cert[] caCerts, Session session) throws PKIException {
        File file = new File(crlPath);
        if(file.isDirectory()) {
            CRLDistributionPointsExt fis1 = userCert.getCRLDistributionPoints();
            DistributionPointsExt crl2 = fis1.getDistributionPoint(0);
            String verifySignature2 = crl2.getDistributionPointNameByFullName(0);
            int cnIndex = verifySignature2.indexOf("CN=");
            int conIndex = verifySignature2.indexOf(",");
            if(cnIndex != -1 && conIndex != -1) {
                verifySignature2 = verifySignature2.substring(cnIndex + 3, conIndex);
            }

            String issuer = userCert.getIssuer();
            cnIndex = issuer.indexOf("O=");
            conIndex = issuer.indexOf(",");
            if(cnIndex != -1 && conIndex != -1) {
                issuer = issuer.substring(cnIndex + 2, conIndex);
            }

            boolean revoke = false;
            String crlFilePath = crlPath + File.separator + issuer + File.separator + verifySignature2 + ".crl";
            FileInputStream inStream = null;

            try {
                inStream = new FileInputStream(crlFilePath);
            } catch (Exception var17) {
                throw new PKIException("850920", "读CRL文件失败 " + var17.getMessage(), var17);
            }

            X509CRL crl1 = new X509CRL(inStream);

            try {
                inStream.close();
            } catch (IOException var16) {
                throw new PKIException("850920", "读CRL文件失败 " + var16.getMessage(), var16);
            }

            verifyCertChain(caCerts, crl1.getIssuer(), session);
            boolean verifySignature1 = crl1.verify(caCerts[0].getPublicKey(), session);
            if(!verifySignature1) {
                throw new PKIException("850921", "验证CRL签名失败");
            } else {
                if(!crl1.isRevoke(userCert.getSerialNumber())) {
                    revoke = true;
                }

                return revoke;
            }
        } else {
            FileInputStream fis = null;

            try {
                fis = new FileInputStream(file);
            } catch (FileNotFoundException var18) {
                throw new PKIException("850906", "CRL文件不存在", var18);
            }

            X509CRL crl = new X509CRL(fis);
            verifyCertChain(caCerts, crl.getIssuer(), session);
            boolean verifySignature = crl.verify(caCerts[0].getPublicKey(), session);
            if(!verifySignature) {
                throw new PKIException("850921", "验证CRL签名失败");
            } else {
                return !crl.isRevoke(userCert);
            }
        }
    }

    public static boolean verifyCertByCRLOutLine(X509Cert userCert, byte[] crlData, X509Cert[] caCerts, Session session) throws PKIException {
        X509CRL crl = new X509CRL(crlData);
        verifyCertChain(caCerts, crl.getIssuer(), session);
        boolean verifySignature = crl.verify(caCerts[0].getPublicKey(), session);
        if(!verifySignature) {
            throw new PKIException("850921", "验证CRL签名失败");
        } else {
            return !crl.isRevoke(userCert);
        }
    }

    public static boolean verifyCertByCRLOnLine(X509Cert userCert) throws PKIException {
        CRLDistributionPointsExt crlDistExt = userCert.getCRLDistributionPoints();
        int crlDistCount = crlDistExt.getDistributionPointCount();
        String crl = null;

        int ldapIndex;
        String port;
        for(ldapIndex = 0; ldapIndex < crlDistCount; ++ldapIndex) {
            DistributionPointsExt ip = crlDistExt.getDistributionPoint(ldapIndex);
            port = ip.getDistributionPointNameByFullName(0);
            if(port.indexOf("ldap://") != -1) {
                crl = port;
            }
        }

        if(crl == null) {
            throw new PKIException("850904", "用户证书中没有CRL的目录信息");
        } else {
            ldapIndex = crl.indexOf("ldap://");
            crl = crl.substring(ldapIndex + 7, crl.length());
            ldapIndex = crl.indexOf(":");
            String var13 = crl.substring(0, ldapIndex);
            crl = crl.substring(ldapIndex + 1, crl.length());
            ldapIndex = crl.indexOf("/");
            port = crl.substring(0, ldapIndex);
            crl = crl.substring(ldapIndex + 1, crl.length());
            ldapIndex = crl.indexOf("?");
            String dn = crl.substring(0, ldapIndex);
            String cn = crl.substring(crl.indexOf("=") + 1, crl.indexOf(","));
            boolean revoke = false;
            X509CRL x509Crl = null;

            try {
                x509Crl = getCRLFromLDAP(var13, port, dn, cn);
            } catch (Exception var12) {
                throw new PKIException("850905", "从目录服务器下载CRL失败", var12);
            }

            revoke = x509Crl.isRevoke(userCert);
            if(revoke) {
                return false;
            } else {
                return true;
            }
        }
    }

    public static boolean verifyCertByCRLOnLineMirror(String mirrorLdapIp, String port, X509Cert userCert) throws PKIException {
        CRLDistributionPointsExt crlDistExt = userCert.getCRLDistributionPoints();
        int crlDistCount = crlDistExt.getDistributionPointCount();
        String crl = null;

        int ldapIndex;
        String cn;
        for(ldapIndex = 0; ldapIndex < crlDistCount; ++ldapIndex) {
            DistributionPointsExt dn = crlDistExt.getDistributionPoint(ldapIndex);
            cn = dn.getDistributionPointNameByFullName(0);
            if(cn.indexOf("ldap://") != -1) {
                crl = cn;
            }
        }

        if(crl == null) {
            throw new PKIException("850904", "用户证书中没有CRL的目录信息");
        } else {
            ldapIndex = crl.indexOf("ldap://");
            crl = crl.substring(ldapIndex + 7, crl.length());
            ldapIndex = crl.indexOf(":");
            crl = crl.substring(ldapIndex + 1, crl.length());
            ldapIndex = crl.indexOf("/");
            crl = crl.substring(ldapIndex + 1, crl.length());
            ldapIndex = crl.indexOf("?");
            String var13 = crl.substring(0, ldapIndex);
            cn = crl.substring(crl.indexOf("=") + 1, crl.indexOf(","));
            boolean revoke = false;
            X509CRL x509Crl = null;

            try {
                x509Crl = getCRLFromLDAP(mirrorLdapIp, port, var13, cn);
            } catch (Exception var12) {
                throw new PKIException("850905", "从目录服务器下载CRL失败", var12);
            }

            revoke = x509Crl.isRevoke(userCert);
            if(revoke) {
                return false;
            } else {
                return true;
            }
        }
    }

    private static X509CRL getCRLFromLDAP(String ip, String port, String dn, String cn) throws Exception {
        InitialDirContext ctx = null;
        Hashtable env = new Hashtable();
        env.put("java.naming.factory.initial", "com.sun.jndi.ldap.LdapCtxFactory");
        env.put("java.naming.provider.url", "ldap://" + ip + ":" + port);
        env.put("java.naming.ldap.attributes.binary", "certificateRevocationList");

        try {
            ctx = new InitialDirContext(env);
        } catch (NamingException var15) {
            throw var15;
        }

        SearchControls tConstraints = new SearchControls();
        tConstraints.setSearchScope(2);
        NamingEnumeration tResults = null;
        String filter = "(&(objectclass=cRLDistributionPoint)(cn=" + cn + "))";
        String[] attrs = new String[]{"certificateRevocationList;binary"};
        X509CRL crl = null;

        try {
            tResults = ctx.search(dn, filter, attrs, tConstraints);
            if(tResults != null && tResults.hasMore()) {
                while(tResults.hasMore()) {
                    SearchResult ex = (SearchResult)tResults.next();
                    Attributes allAttrs = ex.getAttributes();
                    Attribute attCRL = allAttrs.get("certificateRevocationList;binary");
                    byte[] bCRL = (byte[])attCRL.get(0);
                    crl = new X509CRL(bCRL);
                }
            }

            ctx.close();
            return crl;
        } catch (Exception var16) {
            throw var16;
        }
    }

    private static void verifyCertChain(X509Cert[] caCerts, String issuer, Session session) throws PKIException {
        if(caCerts.length == 1) {
            if(!caCerts[0].getSubject().equals(issuer)) {
                throw new PKIException("850907", "CA证书链不正确");
            }
        } else {
            X509Cert tempCert = null;

            int j;
            for(j = 0; j < caCerts.length; ++j) {
                for(int j1 = caCerts.length - 1; j1 > j; --j1) {
                    if(caCerts[j1].getIssuer().equals(caCerts[j1 - 1].getSubject())) {
                        tempCert = caCerts[j1 - 1];
                        caCerts[j1 - 1] = caCerts[j1];
                        caCerts[j1] = tempCert;
                    }
                }
            }

            if(!caCerts[0].getSubject().equals(issuer)) {
                throw new PKIException("850907", "CA证书链不正确");
            }

            for(j = 0; j < caCerts.length; ++j) {
                if(j != caCerts.length - 1) {
                    if(!caCerts[j].getIssuer().equals(caCerts[j + 1].getSubject())) {
                        throw new PKIException("850907", "CA证书链不正确");
                    }

                    if(!caCerts[j].verify(caCerts[j + 1].getPublicKey(), session)) {
                        throw new PKIException("850908", "验证CA证书链中的证书签名失败");
                    }
                } else {
                    if(!caCerts[j].getSubject().equals(caCerts[j].getIssuer())) {
                        throw new PKIException("850907", "CA证书链不正确");
                    }

                    if(!caCerts[j].verify(caCerts[j].getPublicKey(), session)) {
                        throw new PKIException("850907", "CA证书链不正确");
                    }
                }
            }
        }

    }

    public static void main(String[] args) {
        try {
            JCrypto ex = JCrypto.getInstance();
            ex.initialize("JSOFT_LIB", (Object)null);
            Session session = ex.openSession("JSOFT_LIB");
            changePfxPWD("c:/superChange.pfx", "1", "c:/superChange.pfx", "111");
            boolean a = true;
        } catch (Exception var4) {
            System.out.println(var4);
        }

    }
}
