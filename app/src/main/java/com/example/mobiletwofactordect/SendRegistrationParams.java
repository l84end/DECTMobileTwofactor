package com.example.mobiletwofactordect;

import org.json.JSONException;
import org.json.JSONObject;

public class SendRegistrationParams {

    public static JSONObject getInfoToSend(String user, String secretCode) {
        String firebaseId = GetTokenForApp.getFirebaseId(); // Získání Firebase ID pomocí třídy GetTokenForApp
        JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put("user", user);
            jsonObject.put("secretCode", secretCode);
            jsonObject.put("firebaseId", firebaseId);

        } catch (JSONException e) {
            e.printStackTrace();
            jsonObject = new JSONObject();
        }

        return jsonObject;
    }


    private void sendRegistration(String matchingKey, String publicKey, String firebaseId, String login) {
        // Vytvoření instance třídy JSONObject pro uložení dat
        JSONObject requestData = new JSONObject();
        try {
            // Přidání parametrů do objektu JSON
            requestData.put("matchingKey", matchingKey);
            requestData.put("publicKey", publicKey);
            requestData.put("firebaseId", firebaseId);
            requestData.put("login", login);

            // Zde můžete provést odeslání dat na server, například pomocí knihovny Retrofit nebo Volley
            // Po odeslání dat můžete zpracovat odpověď serveru
        } catch (JSONException e) {
            e.printStackTrace();
            // Chyba při vytváření objektu JSON, můžete zde přidat kód pro zpracování chyby
        }
    }
}
