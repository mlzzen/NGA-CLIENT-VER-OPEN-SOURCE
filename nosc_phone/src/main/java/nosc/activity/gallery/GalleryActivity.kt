package nosc.activity.gallery

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.core.content.FileProvider
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.SimpleOnPageChangeListener
import com.tbruyelle.rxpermissions2.RxPermissions
import gov.anzong.androidnga.BuildConfig
import gov.anzong.androidnga.R
import gov.anzong.androidnga.activity.BaseActivity
import io.reactivex.disposables.Disposable
import nosc.utils.DeviceUtils
import nosc.utils.uxUtils.ToastUtils
import java.io.File

class GalleryActivity : BaseActivity() {
    private val mGalleryUrls: List<String> by lazy {
        intent.getStringArrayExtra(KEY_GALLERY_URLS)?.asList() ?: emptyList()
    }
    private val viewModel:GalleryViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_zoom)
        if(mGalleryUrls.isEmpty()){
            finish()
        }else{
            viewModel.setImgList(mGalleryUrls)
        }
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeButtonEnabled(true)
        }

        findViewById<ViewPager>(R.id.gallery).apply {
            adapter = GalleryAdapter(viewModel.images)
            addOnPageChangeListener(object : SimpleOnPageChangeListener() {
                override fun onPageSelected(position: Int) {
                    viewModel.currPageIndex.value = position
                    title = (position + 1).toString() + " / " + adapter?.count
                }
            })
            currentItem = intent.getIntExtra(KEY_GALLERY_INDEX,0).coerceIn(0,mGalleryUrls.size)
            title = (currentItem + 1).toString() + " / " + adapter?.count
        }
    }

    private fun saveBitmap(
        index:Int,
        then:suspend (GalleryViewModel.GalleryImageState)->Unit = {}
    ): Disposable? {
        return RxPermissions(this)
            .request(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            .subscribe { granted: Boolean ->
                if (granted) { // Always true pre-M
                    viewModel.saveImage(index,then)
                } else {
                    ToastUtils.warn("未授予存储权限，无法保存")
                }
            }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_image_zoom, menu)
        return true
    }

    private fun share(file: File) {
        try {
            if (DeviceUtils.isGreaterEqual_7_0()) {
                val contentUri = FileProvider.getUriForFile(
                    this,
                    BuildConfig.APPLICATION_ID, file
                )
                val intent = Intent(Intent.ACTION_SEND)
                intent.putExtra(Intent.EXTRA_STREAM, contentUri)
                intent.type = "image/jpeg"
                val text = resources.getString(R.string.share)
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                startActivity(Intent.createChooser(intent, text))
            } else {
                val intent = Intent(Intent.ACTION_SEND)
                intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file))
                intent.type = "image/jpeg"
                val text = resources.getString(R.string.share)
                startActivity(Intent.createChooser(intent, text))
            }
        } catch (e: ActivityNotFoundException) {
            ToastUtils.error("分享失败")
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_share -> saveBitmap(viewModel.currPageIndex.value){
                it.file?.let { share(it) }
            }
            R.id.menu_download -> saveBitmap(viewModel.currPageIndex.value){
                ToastUtils.success(getString(R.string.file_saved)+it.file?.path)
            }
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    companion object {
        const val KEY_GALLERY_URLS = "keyGalleryUrl"
        const val KEY_GALLERY_INDEX = "keyGalleryCurIndex"
    }
}