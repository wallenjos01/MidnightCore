package org.wallentines.mcore.messenger;

import io.netty.buffer.ByteBuf;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.ShortBufferException;
import javax.crypto.spec.IvParameterSpec;
import java.security.GeneralSecurityException;

public class BufCipher {

    private final Cipher cipher;
    private final SecretKey key;

    public BufCipher(SecretKey key) throws GeneralSecurityException  {

        this.key = key;
        this.cipher = Cipher.getInstance("AES/CFB8/NoPadding");
    }

    public int getOutputLength(int length) {
        try {
            cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(key.getEncoded()));
            return cipher.getOutputSize(length);
        } catch(GeneralSecurityException ex) {
            throw new RuntimeException("Unable to determine output length!", ex);
        }
    }

    public void decrypt(ByteBuf in, ByteBuf out) {
        cipher(Cipher.DECRYPT_MODE, in, out);
    }

    public void encrypt(ByteBuf in, ByteBuf out) {
        cipher(Cipher.ENCRYPT_MODE, in, out);
    }

    private void cipher(int mode, ByteBuf buffer, ByteBuf out)  {

        int inputLength = buffer.readableBytes();

        try {
            cipher.init(mode, key, new IvParameterSpec(key.getEncoded()));
        } catch(GeneralSecurityException ex) {
            throw new RuntimeException("An error occurred while ciphering data!", ex);
        }

        byte[] input = new byte[inputLength];
        buffer.readBytes(input);

        byte[] output = new byte[cipher.getOutputSize(inputLength)];

        try {
            cipher.update(input, 0, inputLength, output, 0);
        } catch (ShortBufferException ex) {
            throw new RuntimeException("Not enough room for ciphered data!", ex);
        }


        out.writeBytes(output);
    }

}
