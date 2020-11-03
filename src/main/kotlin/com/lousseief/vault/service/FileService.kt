package com.lousseief.vault.service

import com.lousseief.vault.crypto.Conversion
import com.lousseief.vault.exception.FileException
import com.lousseief.vault.exception.InternalException
import com.lousseief.vault.model.Profile
import java.io.File
import java.io.IOException

object FileService {

    val FILE_SUFFIX = ".vault"

    fun userExists(user: String): Boolean {
        // why can't you put the dot in the expression template instead of in the SUFFIX?'
        return File("../").list().contains(user + FILE_SUFFIX) && File("../$user$FILE_SUFFIX").isFile
    }

    fun readFile(user: String): Profile {
        if(!userExists(user))
            throw FileException(FileException.FileExceptionCause.NOT_FOUND,
                IOException("User doesn't exist (no .vault file was found)")
            )
        try {
            val userFile = File("../" + user + FILE_SUFFIX)
            val fileBytes = userFile.readBytes()
            val fileText = Conversion.bytesToUTF8(fileBytes) // content is Base64 but with line endings
            val parts = fileText.split("\n")
            assert(parts.size == 6, { "Expected .vault file to contain 6 parts but was ${parts.size}" })
            return Profile(user, parts[0], parts[1], parts[2], parts[3], parts[4], parts[5])
        }
        catch(e: AssertionError) {
            throw FileException(FileException.FileExceptionCause.CORRUPT_FILE, e)
        }
        catch(e: Exception) {
            throw FileException(FileException.FileExceptionCause.READ_ERROR, e)
        }

    }

    fun writeFile(user: Profile, overwrite: Boolean) {
        try {
            assert(overwrite || !userExists(user.name))
            val userFile = File("../" + user.name + FILE_SUFFIX)
            userFile.writeBytes(Conversion.UTF8ToBytes(user.toString()))
        }
        catch(e: AssertionError) {
            throw InternalException(InternalException.InternalExceptionCause.FILE_EXISTS, e)
        }
        catch(e: Exception) {
            throw FileException(FileException.FileExceptionCause.WRITE_ERROR, e)
        }
    }


}