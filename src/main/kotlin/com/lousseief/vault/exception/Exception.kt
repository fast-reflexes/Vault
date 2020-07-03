package com.lousseief.vault.exception

interface Cause {
    val name: String
}

class AuthenticationException(message: AuthenticationExceptionCause, val e: Throwable? = null): Exception(message.explanation) {

    enum class AuthenticationExceptionCause(val explanation: String): Cause {
        UNAUTHORIZED("The password was incorrect"),
        EMPTY_PASSWORDS_NOT_ALLOWED("The password must contain at least one character")
    }
}
class EncryptionException(message: EncryptionExceptionCause, val e: Throwable? = null): Exception(message.explanation) {

    enum class EncryptionExceptionCause(val explanation: String): Cause {
        UNAUTHORIZED("Bad password")
    }
}

class DecryptionException(message: DecryptionExceptionCause, val e: Throwable? = null): Exception(message.explanation) {

    enum class DecryptionExceptionCause(val explanation: String): Cause {
        UNAUTHORIZED("Bad password")
    }
}

class UserException(message: String): Exception(message)
class FileException(message: FileExceptionCause, val e: Throwable? = null): Exception(message.explanation) {

    enum class FileExceptionCause(val explanation: String): Cause {
        NOT_FOUND("User doesn't exist"),
        READ_ERROR("There was an error while reading the user data from disk"),
        CORRUPT_FILE("The file doesn't seem to be a legitimate .vault file"),
        WRITE_ERROR("There was an error while saving changes to disk, please try again")
    }
}
class InternalException(message: InternalExceptionCause, val e: Throwable? = null): Exception(message.explanation) {

    enum class InternalExceptionCause(val explanation: String): Cause {
        FILE_EXISTS("The .vault file already exists and the overwrite flag was not set to true")
    }

}