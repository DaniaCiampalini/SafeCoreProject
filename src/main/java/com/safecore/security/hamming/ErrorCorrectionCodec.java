package com.safecore.security.hamming;

/**
 * Astrazione per meccanismi di error detection/correction.
 * Permette future estensioni (CRC, Reed-Solomon, ecc.).
 */
public interface ErrorCorrectionCodec {

    byte[] encode(byte[] data);

    byte[] decode(byte[] encodedData);
}
