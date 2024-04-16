package com.example.mobiletwofactordect;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.os.Build;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import com.google.zxing.ResultPoint;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.BarcodeView;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.List;


public class RegisterPhone extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CAMERA = 1;
    private BarcodeView barcodeView;
    private TextView showQRInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_page);

        Button backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        showQRInfo = findViewById(R.id.showQRInfo);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUEST_CAMERA);
            } else {
                initQRCodeScanner();
            }
        } else {
            initQRCodeScanner();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CAMERA) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initQRCodeScanner();
            } else {
                Toast.makeText(this, "Camera permission is required", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    private void initQRCodeScanner() {
        barcodeView = findViewById(R.id.barcodeView);

        barcodeView.decodeSingle(new BarcodeCallback() {
            @Override
            public void barcodeResult(BarcodeResult result) {
                if(result.getText() != null) {
                    showQRInfo.setText("Scanned: " + result.getText());
                    parseQRParameters(result.getText());
                }
            }

            @Override
            public void possibleResultPoints(List<ResultPoint> resultPoints) {
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (barcodeView != null) {
            barcodeView.resume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (barcodeView != null) {
            barcodeView.pause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (barcodeView != null) {
            barcodeView.pause();
        }
    }

    private void parseQRParameters(String QRText) {
        try {
            JSONObject jsonObject = new JSONObject(QRText);

            String secretCode = jsonObject.getString("secretCode");
            String user = jsonObject.getString("user");

            JSONObject dataToSend = SendRegistrationParams.getInfoToSend(user, secretCode);

            // Zde můžete provést další operace s daty, například odeslat je na server
        } catch (JSONException e) {
            e.printStackTrace();
            // Chyba při analýze JSON, zobrazíme chybovou zprávu v konzoli
            System.out.println("Error parsing QR parameters: " + e.getMessage());
        } catch (UnrecoverableKeyException e) {
            throw new RuntimeException(e);
        } catch (CertificateException e) {
            throw new RuntimeException(e);
        } catch (KeyStoreException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
