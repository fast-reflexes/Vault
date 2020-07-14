package com.lousseief.vault.service

import com.lousseief.vault.exception.CryptoException
import java.security.SecureRandom

object CryptoService {

    private val randomSource = SecureRandom.getInstanceStrong()

    fun generatePassword(characterPool: String, length: Int): String {
        if(characterPool.toList().size != characterPool.toSet().size)
            throw CryptoException(CryptoException.CryptoExceptionCause.CHARACTER_POOL_CONTAINS_DUPLICATES)
            return String(
                (0 until length)
                    .map {
                        val e: Char = characterPool[randomSource.nextInt(Int.MAX_VALUE).rem(characterPool.length)]
                        e
                    }
                    .toCharArray()
            )
    }

}