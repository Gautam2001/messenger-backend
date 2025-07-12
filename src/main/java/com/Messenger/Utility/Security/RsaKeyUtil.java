package com.Messenger.Utility.Security;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class RsaKeyUtil {

	public static PublicKey loadPublicKey() throws Exception {
		InputStream is = RsaKeyUtil.class.getResourceAsStream("/keys/public_key.pem");
		String publicKeyPem = new String(is.readAllBytes(), StandardCharsets.UTF_8)
				.replaceAll("-----BEGIN PUBLIC KEY-----", "").replaceAll("-----END PUBLIC KEY-----", "")
				.replaceAll("\\s", "");

		byte[] keyBytes = Base64.getDecoder().decode(publicKeyPem);
		X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
		return KeyFactory.getInstance("RSA").generatePublic(spec);
	}
}
