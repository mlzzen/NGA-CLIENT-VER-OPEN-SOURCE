package nosc.api

/**
 * @author Yricky
 * @date 2022/9/8
 */
sealed class ApiResult<T>
class OK<T>(val result: T): ApiResult<T>()
class ERR<T>(val msg:String): ApiResult<T>()
