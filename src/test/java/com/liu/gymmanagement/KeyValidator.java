package com.liu.gymmanagement;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import java.util.Base64;

public class KeyValidator {
    public static void main(String[] args) {
        String yourSecret = "wtmBCTlbAN+oW+6Y7oaniojxBWU8Bkvyy+YxrJwNSdIWzQ3V/Ep2eSZMaFscaj3KTP18zfK2EoX0znxWQerwDg==";
        try {
            byte[] decodedKey = Base64.getDecoder().decode(yourSecret);
            SecretKey key = Keys.hmacShaKeyFor(decodedKey);
            System.out.println("Key is valid and can be used with HS512");
        } catch (Exception e) {
            System.out.println("Invalid key: " + e.getMessage());
        }
    }
}