package sp.phone.util

import android.graphics.*
import android.graphics.Bitmap.CompressFormat
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.TypedValue
import android.widget.ImageView
import coil.load
import coil.transform.CircleCropTransformation
import gov.anzong.androidnga.R
import gov.anzong.androidnga.app
import nosc.utils.ContextUtils
import org.apache.commons.io.FilenameUtils
import sp.phone.common.PhoneConfiguration.avatarSize
import java.io.ByteArrayOutputStream
import java.io.InputStream
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.sqrt

object ImageUtils {
    //final static int max_avatar_width = 200;
    private const val max_avatar_height = 255
    val sDefaultAvatar: Drawable by lazy {
        BitmapDrawable(
            app.resources,
            BitmapFactory.decodeResource(app.resources, R.drawable.default_avatar)
        )
    }

    private fun zoomImageByWidth(bitmap: Bitmap?, bookWidth: Int): Bitmap? {
        if (bitmap == null) return null
        val width = bitmap.width
        val height = bitmap.height
        val newWidth = bookWidth
        val newHeight = (height * newWidth / width).toFloat()
        if (newWidth < 2 || newHeight < 1.01f) return null
        val scaleWidth = 1f * newWidth / width
        val scaleHeight = newHeight / height
        val matrix = Matrix()
        matrix.postScale(scaleWidth, scaleHeight)
        return Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true)
    }

    fun zoomImageByHeight(bitmap: Bitmap?, bookHeight: Int): Bitmap? {
        if (bitmap == null) return null
        val width = bitmap.width
        val height = bitmap.height
        val newWidth = (width * bookHeight / height).toFloat()
        if (newWidth < 2 || bookHeight < 1.01f) return null
        val scaleWidth = 1f * newWidth / width
        val scaleHeight = 1f * bookHeight / height
        val matrix = Matrix()
        matrix.postScale(scaleWidth, scaleHeight)
        return Bitmap.createBitmap(
            bitmap, 0, 0, width, height,
            matrix, true
        )
    }

    @JvmStatic
    fun newImage(oldImage: String?, userId: String): String? {
        val extension = FilenameUtils.getExtension(oldImage)
        val path = FilenameUtils.getPath(oldImage)
        val newName: String = if (extension != null) {
            if (path == null || "" == path) {
                return null
            } else if (extension.length == 3) {
                HttpUtil.PATH_AVATAR + "/" + userId + "." + extension
            } else if (extension.length >= 4
                && "?" == extension.substring(3, 4)
            ) {
                (HttpUtil.PATH_AVATAR + "/" + userId + "."
                        + extension.substring(0, 3))
            } else {
                HttpUtil.PATH_AVATAR + "/" + userId + ".jpg"
            }
        } else {
            HttpUtil.PATH_AVATAR + "/" + userId + ".jpg"
        }
        return newName
    }

    private fun computeSampleSize(
        options: BitmapFactory.Options,
        minSideLength: Int, maxNumOfPixels: Int
    ): Int {
        val initialSize = computeInitialSampleSize(
            options, minSideLength,
            maxNumOfPixels
        )
        var roundedSize: Int
        if (initialSize <= 8) {
            roundedSize = 1
            while (roundedSize < initialSize) {
                roundedSize = roundedSize shl 1
            }
        } else {
            roundedSize = (initialSize + 7) / 8 * 8
        }
        return roundedSize
    }

    private fun computeInitialSampleSize(
        options: BitmapFactory.Options,
        minSideLength: Int, maxNumOfPixels: Int
    ): Int {
        val w = options.outWidth.toDouble()
        val h = options.outHeight.toDouble()
        val lowerBound = if (maxNumOfPixels == -1) 1 else ceil(
            sqrt(w * h / maxNumOfPixels)
        ).toInt()
        val upperBound = if (minSideLength == -1) 128 else floor(w / minSideLength).coerceAtMost(
            floor(h / minSideLength)
        ).toInt()
        if (upperBound < lowerBound) {
            // return the larger one when there is no overlapping zone.
            return lowerBound
        }
        return if (maxNumOfPixels == -1 && minSideLength == -1) {
            1
        } else if (minSideLength == -1) {
            lowerBound
        } else {
            upperBound
        }
    }

    @JvmOverloads
    @JvmStatic
    fun loadAvatarFromSdcard(avatarPath: String?, maxHeight: Int = max_avatar_height): Bitmap? {
        val opts = BitmapFactory.Options()
        opts.inJustDecodeBounds = true
        val avatarWidth = avatarSize
        val minSideLength = avatarWidth.coerceAtMost(maxHeight)
        opts.inSampleSize = computeSampleSize(
            opts, minSideLength,
            avatarWidth * maxHeight
        )
        opts.inJustDecodeBounds = false
        opts.inInputShareable = true
        opts.inPurgeable = true
        var bitmap = BitmapFactory.decodeFile(avatarPath, opts)
        if (bitmap != null && bitmap.width != avatarWidth) {
            val tmp = bitmap
            bitmap = zoomImageByWidth(tmp, avatarWidth)
            tmp.recycle()
        }
        return bitmap
    }

    @JvmStatic
    fun fitImageToUpload(`is`: InputStream?, is2: InputStream): ByteArray? {
        if (`is` == null) return null
        if (`is` === is2) return null
        val opts = BitmapFactory.Options()
        opts.inJustDecodeBounds = true
        val minSideLength = 512
        opts.inSampleSize = computeSampleSize(
            opts, minSideLength,
            1024 * 1024
        )
        opts.inJustDecodeBounds = false
        opts.inInputShareable = true
        opts.inPurgeable = true
        val bitmap = BitmapFactory.decodeStream(is2, null, opts)
        val stream = ByteArrayOutputStream()
        bitmap!!.compress(CompressFormat.PNG, 100, stream)
        return stream.toByteArray()
    }

    @JvmStatic
    fun fitNGAImageToUpload(`is`: InputStream?, opts: BitmapFactory.Options): ByteArray? {
        if (`is` == null) return null
        var width = opts.outWidth
        var height = opts.outHeight
        if (height > 255) {
            if (width <= 180) {
                width = (255 * width / height)
                height = 255
            } else {
                if ((height / width).toFloat() > (255 / 180).toFloat()) {
                    width = (255 * width / height)
                    height = 255
                } else if ((height / width).toFloat() < (255 / 180).toFloat()) {
                    height = (180 * height / width)
                    width = 180
                } else {
                    height = 255
                    width = 180
                }
            }
        } else {
            if (width > 180) {
                height = (180 * height / width)
                width = 180
            }
        }
        var widthchuli = 1
        var heightchuli = 1
        widthchuli = if (opts.outWidth % width == 0) {
            opts.outWidth / width
        } else {
            opts.outWidth / width + 1
        }
        heightchuli = if (opts.outHeight % height == 0) {
            opts.outHeight / height
        } else {
            opts.outHeight / height + 1
        }
        opts.inSampleSize = Math.max(widthchuli, heightchuli)
        opts.inJustDecodeBounds = false
        opts.inPurgeable = true
        var bitmap = BitmapFactory.decodeStream(`is`, null, opts)
        if (opts.outHeight > 255 || opts.outWidth > 180) bitmap = Bitmap.createScaledBitmap(
            bitmap!!, width, height,
            true
        )
        val stream = ByteArrayOutputStream()
        bitmap!!.compress(CompressFormat.PNG, 100, stream)
        return stream.toByteArray()
    }

    fun recycleImageView(avatarIV: ImageView) {
        val drawable = avatarIV.drawable
        if (drawable is BitmapDrawable) {
            val bitmap = drawable.bitmap
            bitmap?.recycle()
        }
    }

    @JvmStatic
    fun loadRoundCornerAvatar(
        imageView: ImageView,
        url: String?,
    ) {
        imageView.load(
            url
        ){
            transformations(CircleCropTransformation())
            placeholder(sDefaultAvatar)
            error(sDefaultAvatar)
            crossfade(false)
        }
    }

    @JvmStatic
    fun loadAvatar(imageView: ImageView, url: String) {
        imageView.load(
            url
        ){
            placeholder(sDefaultAvatar)
            error(sDefaultAvatar)
            crossfade(false)
        }
    }
}