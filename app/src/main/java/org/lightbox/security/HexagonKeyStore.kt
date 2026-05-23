package org.lightbox.security

import java.security.SecureRandom
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

object HexagonKeyStore {
    private const val KEY_ID = "lightbox_vdisk"

    fun generateAes256Key(): SecretKey {
        val fallback = ByteArray(32).also { SecureRandom().nextBytes(it) }
        val bytes = HexagonCrypto.generateKey(KEY_ID).getOrDefault(fallback)
        return SecretKeySpec(bytes.copyOf(32), "AES")
    }
}

object HexagonCrypto {
    fun generateKey(keyId: String): Result<ByteArray> =
        Result.failure(UnsupportedOperationException("Hexagon not available"))
    fun encrypt(keyId: String, plain: ByteArray): Result<ByteArray> =
        Result.failure(UnsupportedOperationException("Hexagon not available"))
    fun decrypt(keyId: String, cipher: ByteArray): Result<ByteArray> =
        Result.failure(UnsupportedOperationException("Hexagon not available"))
}
