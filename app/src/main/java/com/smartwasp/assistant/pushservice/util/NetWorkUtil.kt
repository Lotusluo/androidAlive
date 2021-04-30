package com.smartwasp.assistant.pushservice.util

import android.content.Context
import android.net.wifi.WifiManager
import android.os.Looper
import androidx.core.text.isDigitsOnly
import com.smartwasp.assistant.pushservice.App
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.io.*
import java.net.Inet6Address
import java.net.InetAddress
import java.net.NetworkInterface
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSession
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager


/**
 * Created by luotao on 2020/12/26 15:30
 * E-Mail Address：gtkrockets@163.com
 */
object NetWorkUtil {
    /**
     * 是否有联网
     * @param context 上下文
     * @return 判断是否有分配的内网IP
     */
    fun getIpV4Address(context:Context): String? {
        var ipAddress:String? = null
        val nis = NetworkInterface.getNetworkInterfaces()
        var ia: InetAddress? = null
        while (nis.hasMoreElements()) {
            val ni = nis.nextElement() as NetworkInterface
            val ias = ni.inetAddresses
            while (ias.hasMoreElements()) {
                ia = ias.nextElement()
                if (ia is Inet6Address) {
                    // 局域网内就算存在IPv6地址，也一定会分配ipV4地址，使用ipV4地址判断即可
                    continue
                }
                val ip = ia.hostAddress
                if(ip != "127.0.0.1"){
                    ipAddress = ia.hostAddress
                    break
                }
            }
        }
        return ipAddress
    }

    /**
     * 是否联网
     * @param context 上下文
     */
    fun isInternetConnected(context: Context):Boolean{
        val ipAddress = getIpV4Address(context)
        return !ipAddress.isNullOrEmpty() && ipAddress != "127.0.0.1"
    }

    /**
     * 判断是否是良好的网络环境
     * @param context 上下文
     * @阿里DNS 223.6.6.6
     */
    fun isGoodInternet(context: Context):Boolean{
        if(Looper.myLooper() == Looper.getMainLooper())
            throw RuntimeException("Looper.myLooper() == Looper.getMainLooper()")
        val runtime = Runtime.getRuntime()
        var ipProcess: Process? = null
        try{
            ipProcess = runtime.exec("ping -c 3 -i 0.2 223.6.6.6")
            val input: InputStream = ipProcess.inputStream
            val readIn = BufferedReader(InputStreamReader(input))
            var content = ""
            while (true){
                val tempString = readIn.readLine() ?: break
                content += tempString
            }
            val exitValue = ipProcess.waitFor()

            if(exitValue == 0){
                return true
            }else{
                //根据正则表达式获取 0% packet loss 中的0部分并转换未数值
                val pattern = Pattern.compile("\\d+(?=% packet loss)")
                val matcher = pattern.matcher(content)
                if(matcher.find()){
                    val badNumber = matcher.group(0)
                    if(badNumber.isDigitsOnly()){
                        return badNumber.toInt() <= 10
                    }
                }
            }
        }
        catch (e:IOException){}
        catch (e:InterruptedException){}
        finally {
            ipProcess?.destroy();
            runtime.gc();
        }
        return false
    }

    /**
     * 将okhttp3库设置https请求信任
     */
    fun getOkHttpsSSLOkHttpClient(): OkHttpClient.Builder{
        val trustManager: X509TrustManager = object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {}
            override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {}
            override fun getAcceptedIssuers(): Array<X509Certificate?> {
                return arrayOfNulls(0)
            }
        }
        val sslContext = SSLContext.getInstance("SSL")
        sslContext.init(null, arrayOf<TrustManager>(trustManager), SecureRandom())
        val sslSocketFactory = sslContext.socketFactory
        return OkHttpClient.Builder()
                .sslSocketFactory(sslSocketFactory, trustManager)
                .hostnameVerifier { _: String?, _: SSLSession? -> true }
    }

    /**
     * 为retrofit配置okhttps
     * @param interceptor 自定义拦截器
     */
    fun getOkHttpsSSLOkHttpClientForRetrofit(interceptor:Interceptor? = null):OkHttpClient{
        val okHttpsClientBuilder = getOkHttpsSSLOkHttpClient()
        val loggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY //这里可以选择拦截级别

        return okHttpsClientBuilder.run {
            cache(Cache(File("${App._thiz.getExternalFilesDir(null)}/okhttp_cache/"),50 * 1024 * 1024))
            connectTimeout(10, TimeUnit.SECONDS)
            readTimeout(10, TimeUnit.SECONDS)
            writeTimeout(10, TimeUnit.SECONDS)
            addInterceptor(interceptor)
            addInterceptor(loggingInterceptor)
            retryOnConnectionFailure(true)
            build()
        }
    }

    fun getCurrentGateway(context: Context): String {
        val wm = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val dhcpInfo = wm.dhcpInfo
        return ipToString(dhcpInfo.gateway)
    }

    private fun ipToString(ip: Int): String {
        val sb = StringBuffer()
        sb.append(ip and 0xFF)
        sb.append('.')
        sb.append(ip.shr(8) and 0xFF)
        sb.append('.')
        sb.append(ip.shr(16) and 0xFF)
        sb.append('.')
        sb.append(ip.shr(24) and 0xFF)
        return sb.toString()
    }

}