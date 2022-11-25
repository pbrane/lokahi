package org.opennms.horizon.inventory.mapper.cloud;

import org.springframework.stereotype.Component;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.spec.SecretKeySpec;
import javax.persistence.AttributeConverter;
import java.security.InvalidKeyException;
import java.security.Key;
import java.util.Base64;

@Component
public class EncryptAttributeConverter implements AttributeConverter<String, String> {
    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";

    // todo: IMPORTANT change this. generate random and externalize
    private static final String SECRET = "secret-key-12345";


    private final Key key;
    private final Cipher cipher;

    public EncryptAttributeConverter() throws Exception {
        this.key = new SecretKeySpec(SECRET.getBytes(), ALGORITHM);
        this.cipher = Cipher.getInstance(TRANSFORMATION);
    }

    @Override
    public String convertToDatabaseColumn(String value) {
        try {
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return Base64.getEncoder().encodeToString(cipher.doFinal(value.getBytes()));
        } catch (IllegalBlockSizeException | BadPaddingException | InvalidKeyException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public String convertToEntityAttribute(String value) {
        try {
            cipher.init(Cipher.DECRYPT_MODE, key);
            return new String(cipher.doFinal(Base64.getDecoder().decode(value)));
        } catch (InvalidKeyException | BadPaddingException | IllegalBlockSizeException e) {
            throw new IllegalStateException(e);
        }
    }
}
