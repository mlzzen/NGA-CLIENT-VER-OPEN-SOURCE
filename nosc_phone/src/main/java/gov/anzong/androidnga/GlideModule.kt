package gov.anzong.androidnga

import com.bumptech.glide.module.AppGlideModule
import com.bumptech.glide.GlideBuilder
import android.app.ActivityManager
import android.content.Context
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.load.engine.cache.LruResourceCache
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.cache.DiskLruCacheFactory
import nosc.utils.ContextUtils

/**
 * @author Yricky
 * @date 2021/7/25 下午10:53
 */
@GlideModule
class GlideModule : AppGlideModule() {
    override fun applyOptions(context: Context, builder: GlideBuilder) {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryCacheSize = 1024 * 1024 * am.memoryClass / 3
        builder.setMemoryCache(LruResourceCache(memoryCacheSize.toLong()))
        var requestOptions = RequestOptions()
        requestOptions = requestOptions.format(DecodeFormat.PREFER_ARGB_8888)
        builder.setDefaultRequestOptions(requestOptions)
        val diskCacheSizeBytes = 1024 * 1024 * 32 // 32mb
        builder.setDiskCache(
            DiskLruCacheFactory(
                ContextUtils.getApplication().externalCacheDir.toString() + "/glideLruCache",
                diskCacheSizeBytes.toLong()
            )
        )
    }
}