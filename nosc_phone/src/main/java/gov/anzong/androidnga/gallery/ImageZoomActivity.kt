package gov.anzong.androidnga.gallery

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.SimpleOnPageChangeListener
import com.tbruyelle.rxpermissions2.RxPermissions
import gov.anzong.androidnga.BuildConfig
import gov.anzong.androidnga.R
import gov.anzong.androidnga.activity.BaseActivity
import gov.anzong.androidnga.gallery.SaveImageTask.DownloadResult
import nosc.api.callbacks.OnSimpleHttpCallBack
import nosc.utils.DeviceUtils
import nosc.utils.uxUtils.ToastUtils
import java.io.File

//import com.justwen.androidnga.cloud.CloudServerManager;
/**
 * 显示图片
 * Created by Elrond on 2015/11/18.
 */
class ImageZoomActivity : BaseActivity() {
    private val mGalleryUrls: List<String> by lazy {
        intent.getStringArrayExtra(KEY_GALLERY_URLS)?.asList() ?: emptyList()
    }
    private var mPageIndex = 0
    private var mProgressBar: ProgressBar? = null
    private var mViewPager: ViewPager? = null
    private val mSaveImageTask: SaveImageTask by lazy{ SaveImageTask() }
    private val mDownloadResults: MutableMap<String,DownloadResult> = mutableMapOf()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_zoom)
        receiveIntent()
        initBottomView()
        initGallery()
        initActionBar()
    }

    private fun initActionBar() {
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeButtonEnabled(true)
        }
    }

    private fun initGallery() {
        mViewPager = findViewById<View>(R.id.gallery) as ViewPager
        val adapter = GalleryAdapter(this, mGalleryUrls)
        mViewPager!!.adapter = adapter
        mViewPager!!.currentItem = mPageIndex
        mViewPager!!.addOnPageChangeListener(object : SimpleOnPageChangeListener() {
            override fun onPageSelected(position: Int) {
                mPageIndex = position
                title = (position + 1).toString() + " / " + mGalleryUrls.size
            }
        })
    }

    private fun receiveIntent() {
        if(mGalleryUrls.isEmpty()){
            finish()
        } else{
            mPageIndex = intent.getIntExtra(KEY_GALLERY_INDEX,0).coerceIn(0,mGalleryUrls.size)
        }
    }

    private fun initBottomView() {
        mProgressBar = findViewById<View>(R.id.progress) as ProgressBar
        mProgressBar!!.visibility = View.VISIBLE
        title = (mPageIndex + 1).toString() + " / " + mGalleryUrls.size
    }

    private fun saveBitmap(callBack: OnSimpleHttpCallBack<DownloadResult>, urls: List<String>) {
        RxPermissions(this)
            .request(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            .subscribe { granted: Boolean ->
                if (granted) { // Always true pre-M
                    mSaveImageTask.execute(callBack, urls)
                } else {
                    // Oups permission denied
                }
            }
    }

    private fun saveBitmap(urls: List<String>) {
        saveBitmap({ data: DownloadResult ->
            for (aUrl in mGalleryUrls) {
                if (aUrl == data.url) {
                    mDownloadResults[aUrl] = data
                    break
                }
            }
        }, urls)
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
            R.id.menu_share -> mDownloadResults[mGalleryUrls[mPageIndex]]?.file?.let { share(it) }
            R.id.menu_download_all -> showDownloadAllDialog()
            R.id.menu_download -> saveBitmap(listOf( mGalleryUrls[mPageIndex]))
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    private fun showDownloadAllDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("是否要下载全部图片 ？")
            .setPositiveButton(android.R.string.ok) { _: DialogInterface?, _: Int ->
                saveBitmap(mGalleryUrls)
            }
            .setNegativeButton(android.R.string.cancel, null).create().show()
    }

    fun hideLoading() {
        mProgressBar?.visibility = View.GONE
    }

    companion object {
        const val KEY_GALLERY_URLS = "keyGalleryUrl"
        const val KEY_GALLERY_INDEX = "keyGalleryCurIndex"
    }
}