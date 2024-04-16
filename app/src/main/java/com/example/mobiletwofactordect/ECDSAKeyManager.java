package com.example.mobiletwofactordect;


import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import android.util.Base64;
import java.util.Enumeration;


public class ECDSAKeyManager {

    /*
    * Pár klíčů je uložený v Android KeyStore. Zajištění bezpečnosti klíču velice složitou na extrakci.
    * Třída generate funguje pro vytvoření jedinečného páru. Parametr alias je uživatelské jméno pro které bude pár vytvořen.
    * */
    public void generateKeyPair(String alias) {
        try {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance(
                    KeyProperties.KEY_ALGORITHM_EC, "AndroidKeyStore");
            kpg.initialize(new KeyGenParameterSpec.Builder(
                    alias,
                    KeyProperties.PURPOSE_SIGN | KeyProperties.PURPOSE_VERIFY)
                    .setDigests(KeyProperties.DIGEST_SHA256,
                            KeyProperties.DIGEST_SHA512)
                    .build());

            KeyPair kp = kpg.generateKeyPair();
        } catch (NoSuchAlgorithmException | NoSuchProviderException |
                 InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }
    }

    /*
    * V případě odtranění záznamu je možné pár klíču odstranit za pomocí této funkce. Parametr je uživatelské jméno.
     */
    public void deleteEntry(String alias) {
        try {
            KeyStore ks = KeyStore.getInstance("AndroidKeyStore");
            ks.load(null);
            ks.deleteEntry(alias);

            System.out.println("Záznam s aliasem '" + alias + "' byl úspěšně odstraněn.");
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
     * Výpis aliasů pro které existují klíče.
     */
    public void getAliases() {
        try {
            // Získání instance KeyStore
            KeyStore ks = KeyStore.getInstance("AndroidKeyStore");

            // Načtení KeyStore
            ks.load(null);

            // Získání seznamu aliasů
            Enumeration<String> aliases = ks.aliases();

            // Výpis aliasů
            System.out.println("Seznam aliasů v úložišti klíčů:");
            while (aliases.hasMoreElements()) {
                String alias = aliases.nextElement();
                System.out.println("uzivatel: " + alias);
            }
        } catch (KeyStoreException e) {
            e.printStackTrace();
            // Řešení výjimky KeyStoreException
        } catch (Exception e) {
            e.printStackTrace();
            // Řešení ostatních výjimek
        }
    }

    public String signMessage(String dataString, String alias) {
        try {
            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);

            PrivateKey privateKey = (PrivateKey) keyStore.getKey(alias, null);

            byte[] data = dataString.getBytes(StandardCharsets.UTF_8);

            Signature signature = Signature.getInstance("SHA256withECDSA");
            signature.initSign(privateKey);
            signature.update(data);

            byte[] signatureBytes = signature.sign();

            // Změna způsobu kódování pro Android
            return Base64.encodeToString(signatureBytes, Base64.DEFAULT);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
/*
    public String getPublicKeyPEM(String user) throws Exception {
        KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
        keyStore.load(null);

        PublicKey publicKey = keyStore.getCertificate(user).getPublicKey();
        byte[] publicKeyBytes = publicKey.getEncoded();

        // Zakódování veřejného klíče do Base64 a obalení PEM hlavičkami a patičkami
        String publicKeyBase64 = Base64.encodeToString(publicKeyBytes, Base64.DEFAULT);
        String publicKeyPEM = "-----BEGIN PUBLIC KEY-----\n" + publicKeyBase64 + "\n-----END PUBLIC KEY-----";
        System.out.println(publicKeyPEM);
        return publicKeyPEM;
    }


    public static void podpis() throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, SignatureException, KeyStoreException, CertificateException, IOException, UnrecoverableKeyException {
        // Vytvoření řetězce, který chceme podepsat
        String data = "Hello, world!";

        // Získání instance KeyStore
        KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
        keyStore.load(null);

        // Získání soukromého klíče podle aliasu
        PrivateKey privateKey = (PrivateKey) keyStore.getKey("vysetrovatel", null);

        // Podepsání zprávy
        byte[] signature = sign(data, privateKey);
        System.out.println("Podpis: " + bytesToHex(signature));

        // Ověření podpisu
        PublicKey publicKey = keyStore.getCertificate("vysetrovatel").getPublicKey();
        boolean isValid = verify(data, signature, publicKey);
        System.out.println("Tyvole tahle kokotina me stve: " + publicKey);
        System.out.println("Podpis je platný: " + isValid);
    }

    // Metoda pro podpis zprávy
    public static byte[] sign(String data, PrivateKey privateKey) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        Signature ecdsaSignature = Signature.getInstance("SHA256withECDSA");
        ecdsaSignature.initSign(privateKey);
        ecdsaSignature.update(data.getBytes());
        return ecdsaSignature.sign();
    }

    // Metoda pro ověření podpisu
    public static boolean verify(String data, byte[] signature, PublicKey publicKey) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        Signature ecdsaSignature = Signature.getInstance("SHA256withECDSA");
        ecdsaSignature.initVerify(publicKey);
        ecdsaSignature.update(data.getBytes());
        return ecdsaSignature.verify(signature);
    }

    // Pomocná metoda pro převod bytů na hexadecimální řetězec
    private static String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    public static void main(String[] args) {
        try {
            podpis();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException | InvalidKeyException |
                 SignatureException | KeyStoreException | CertificateException | IOException |
                 UnrecoverableKeyException e) {
            e.printStackTrace();
        }
    }*/

}