package com.memorias.app.domain.result

sealed class DomainResult<out T> {

    data class Success<T>(val data: T) : DomainResult<T>()

    sealed class Error : DomainResult<Nothing>() {
        data class Permission(val permission: String) : Error()
        data class NotFound(val message: String) : Error()
        data class Network(val message: String, val code: Int? = null) : Error()
        data class Auth(val message: String) : Error()
        data class Storage(val message: String) : Error()
        data class Database(val message: String) : Error()
        data class Unknown(val throwable: Throwable) : Error()
    }

    val isSuccess: Boolean get() = this is Success
    val isError: Boolean get() = this is Error

    fun getOrNull(): T? = (this as? Success)?.data

    fun getOrThrow(): T = when (this) {
        is Success -> data
        is Error   -> error("DomainResult.Error: $this")
    }

    inline fun <R> map(transform: (T) -> R): DomainResult<R> = when (this) {
        is Success -> Success(transform(data))
        is Error   -> this
    }

    inline fun onSuccess(action: (T) -> Unit): DomainResult<T> {
        if (this is Success) action(data)
        return this
    }

    inline fun onError(action: (Error) -> Unit): DomainResult<T> {
        if (this is Error) action(this)
        return this
    }
}

suspend fun <T> runDomain(block: suspend () -> T): DomainResult<T> = try {
    DomainResult.Success(block())
} catch (e: SecurityException) {
    DomainResult.Error.Permission(e.message ?: "Permiso denegado")
} catch (e: android.database.sqlite.SQLiteException) {
    DomainResult.Error.Database(e.message ?: "Error de base de datos")
} catch (e: Exception) {
    DomainResult.Error.Unknown(e)
}
