package org.wallentines.mcore.messenger;

import io.netty.buffer.ByteBuf;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.ShortBufferException;
import javax.crypto.spec.IvParameterSpec;
import java.security.GeneralSecurityException;

public class BufCipher {


    private final Cipher dCipher;
    private final Cipher eCipher;

    public BufCipher(SecretKey key) throws GeneralSecurityException  {

        this.dCipher = Cipher.getInstance("AES/CFB8/NoPadding");
        this.dCipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(key.getEncoded()));

        this.eCipher = Cipher.getInstance("AES/CFB8/NoPadding");
        this.eCipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(key.getEncoded()));

    }

    public int getDecryptedLength(int length) {
        return dCipher.getOutputSize(length);
    }

    public int getEncryptedLength(int length) {
        return eCipher.getOutputSize(length);
    }

    public void decrypt(ByteBuf in, ByteBuf out) throws ShortBufferException {
        cipher(dCipher, in, out);
    }

    public void encrypt(ByteBuf in, ByteBuf out) throws ShortBufferException {
        cipher(eCipher, in, out);
    }

    private void cipher(Cipher cipher, ByteBuf buffer, ByteBuf out) {

        int inputLength = buffer.readableBytes();

        byte[] input = new byte[inputLength];
        buffer.readBytes(input);

        byte[] output = new byte[cipher.getOutputSize(inputLength)];

        try {

            cipher.update(input, 0, inputLength, output, 0);

        } catch (ShortBufferException ex) {
            throw new IllegalStateException("Not enough room for ciphered data!");
        }

        out.writeBytes(output);
    }

}
