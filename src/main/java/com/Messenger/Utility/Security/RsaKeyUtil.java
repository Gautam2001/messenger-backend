package com.Messenger.Utility.Security;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class RsaKeyUtil {
	
	private static final String SECRET_PATH = "/etc/secrets/"; //for render

	public static PublicKey loadPublicKey() throws Exception {
        String keyPem = loadKey("public_key.pem");
        keyPem = keyPem.replace("-----BEGIN PUBLIC KEY-----", "")
                       .replace("-----END PUBLIC KEY-----", "")
                       .replaceAll("\\s", "");

        byte[] keyBytes = Base64.getDecoder().decode(keyPem);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        return KeyFactory.getInstance("RSA").generatePublic(spec);
    }

    private static String loadKey(String fileName) throws IOException {
        File file = new File(SECRET_PATH + fileName);
        if (file.exists()) {
            return Files.readString(file.toPath(), StandardCharsets.UTF_8);
        } else {
            try (InputStream is = RsaKeyUtil.class.getResourceAsStream("/keys/" + fileName)) {
                if (is == null) {
                    throw new IOException("Key file not found: " + fileName);
                }
                return new String(is.readAllBytes(), StandardCharsets.UTF_8);
            }
        }
    }
}
