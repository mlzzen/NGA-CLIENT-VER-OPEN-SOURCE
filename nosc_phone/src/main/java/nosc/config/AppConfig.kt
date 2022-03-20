package nosc.config

import androidx.lifecycle.ViewModel
import com.alibaba.fastjson.JSON
import gov.anzong.androidnga.app
import sp.phone.rxjava.RxBus
import java.io.File

/**
 * @author Yricky
 * @date 2021/12/29
 */
class AppConfig:ViewModel() {
    private val dir:File by lazy{ app.filesDir }
    private val settingsFile get() = File(dir,"settings.json")

    var settings:Config = try {
            JSON.parseObject(settingsFile.readText(),Config::class.java)
        } catch (e:Throwable){
            Config().also {
                settingsFile.writeText(JSON.toJSONString(it))
            }
        }
        set(value) {
            settingsFile.writeText(JSON.toJSONString(value))
            field = value
        }

    data class Config(
        val ngaDomain:String = "",
        val swipeBackEnable:Boolean = false
    )
    init {
        RxBus.getInstance()
    }
}