package nosc.api.retrofit

import io.reactivex.Observable
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.http.*

/**
 * Created by Justwen on 2017/10/10.
 */
interface Api {
    @GET("nuke.php")
    operator fun get(@QueryMap map: Map<String, String>): Observable<String>

    @GET
    operator fun get(@Url url: String): Observable<String>

    @GET
    suspend fun request(@Url url: String):String

    @POST
    fun post(@Url url: String): Observable<String>

    @FormUrlEncoded
    @POST("nuke.php")
    fun post(@FieldMap map: Map<String, String>): Observable<String>

    @FormUrlEncoded
    @POST("nuke.php")
    fun post(
        @QueryMap queryMap: Map<String, String>,
        @FieldMap fieldMap: Map<String, String>
    ): Observable<String>

    @POST
    fun uploadFile(@Url url: String, @Body body: MultipartBody): Observable<ResponseBody>

    @GET
    operator fun get(
        @Url url: String,
        @HeaderMap map: Map<String, String>
    ): Observable<String>
}