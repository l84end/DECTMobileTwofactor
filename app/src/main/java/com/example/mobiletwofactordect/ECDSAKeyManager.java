package com.example.mobiletwofactordect;

import android.content.Context;
import android.util.Log;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.spec.ECGenParameterSpec;

public class ECDSAKeyManager {
    private static final String TAG = "KeyManager";
    private static final String KEYSTORE_PROVIDER = "AndroidKeyStore";
    private static final String KEY_ALGORITHM = "EC";
    private static final String CURVE_NAME = "secp256r1";
    private static final String KEY_ALIAS = "myKeyAlias";

    private Context context;

    public ECDSAKeyManager(Context context) {
        this.context = context;
        // Přidání poskytovatele Bouncy Castle do bezpečnostního poskytovatele Javy
        Security.addProvider(new BouncyCastleProvider());
    }

    public KeyPair generateKeyPair() {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(KEY_ALGORITHM, KEYSTORE_PROVIDER);

            // Nastavení parametrů pro generování klíčů
            ECGenParameterSpec ecGenParameterSpec = new ECGenParameterSpec(CURVE_NAME);
            keyPairGenerator.initialize(ecGenParameterSpec);

            // Generování klíčů
            return keyPairGenerator.generateKeyPair();
        } catch (NoSuchAlgorithmException | NoSuchProviderException |
                 InvalidAlgorithmParameterException e) {
            Log.e(TAG, "Error generating key pair: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public PublicKey getPublicKey() {
        try {
            KeyStore keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER);
            keyStore.load(null);

            // Načtení veřejného klíče z KeyStore
            return keyStore.getCertificate(KEY_ALIAS).getPublicKey();
        } catch (KeyStoreException | CertificateException | NoSuchAlgorithmException | IOException e) {
            Log.e(TAG, "Error loading public key: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public PrivateKey getPrivateKey() {
        try {
            KeyStore keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER);
            keyStore.load(null);

            // Načtení privátního klíče z KeyStore
            return (PrivateKey) keyStore.getKey(KEY_ALIAS, null);
        } catch (KeyStoreException | CertificateException | NoSuchAlgorithmException | IOException | UnrecoverableKeyException e) {
            Log.e(TAG, "Error loading private key: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
}
