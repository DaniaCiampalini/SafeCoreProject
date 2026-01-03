package com.safecore.security.hamming;

import java.util.ArrayList;
import java.util.List;

/**
 * Codice di Hamming (7,4).
 * L'ho implementato per i backup: se un file su disco ha un bit "girato" (corrotto),
 * Hamming lo rileva e lo corregge al volo durante l'importazione.
 */
public class Hamming74Codec implements ErrorCorrectionCodec {

    @Override
    public byte[] encode(byte[] data) {
        List<Byte> encoded = new ArrayList<>();

        for (byte b : data) {
            int high = (b >> 4) & 0x0F;
            int low  = b & 0x0F;

            encoded.add((byte) encodeNibble(high));
            encoded.add((byte) encodeNibble(low));
        }

        return toByteArray(encoded);
    }

    @Override
    public byte[] decode(byte[] encodedData) {
        List<Byte> decoded = new ArrayList<>();

        for (int i = 0; i < encodedData.length; i += 2) {
            int high = decodeNibble(encodedData[i]);
            int low  = decodeNibble(encodedData[i + 1]);

            decoded.add((byte) ((high << 4) | low));
        }

        return toByteArray(decoded);
    }

    // --- Hamming core logic ---

    private int encodeNibble(int data) {
        int d1 = (data >> 3) & 1;
        int d2 = (data >> 2) & 1;
        int d3 = (data >> 1) & 1;
        int d4 = data & 1;

        int p1 = d1 ^ d2 ^ d4;
        int p2 = d1 ^ d3 ^ d4;
        int p3 = d2 ^ d3 ^ d4;

        return (p1 << 6) | (p2 << 5) | (d1 << 4)
                | (p3 << 3) | (d2 << 2) | (d3 << 1) | d4;
    }

    private int decodeNibble(byte b) {
        int p1 = (b >> 6) & 1;
        int p2 = (b >> 5) & 1;
        int d1 = (b >> 4) & 1;
        int p3 = (b >> 3) & 1;
        int d2 = (b >> 2) & 1;
        int d3 = (b >> 1) & 1;
        int d4 = b & 1;

        int c1 = p1 ^ d1 ^ d2 ^ d4;
        int c2 = p2 ^ d1 ^ d3 ^ d4;
        int c3 = p3 ^ d2 ^ d3 ^ d4;

        int errorPos = (c3 << 2) | (c2 << 1) | c1;

        if (errorPos != 0) {
            b ^= 1 << (7 - errorPos);
        }

        return ((b >> 4) & 1) << 3 |
                ((b >> 2) & 1) << 2 |
                ((b >> 1) & 1) << 1 |
                (b & 1);
    }

    // Metodo utility per convertire le liste in array (usato dai backup)
    private byte[] toByteArray(List<Byte> list) {
        byte[] arr = new byte[list.size()];
        for (int i = 0; i < list.size(); i++) {
            arr[i] = list.get(i);
        }
        return arr;
    }
}
