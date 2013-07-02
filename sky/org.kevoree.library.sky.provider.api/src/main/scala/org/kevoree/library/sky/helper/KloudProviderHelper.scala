package org.kevoree.library.sky.helper

import org.kevoree.ContainerRoot
import org.kevoree.framework.KevoreeXmiHelper
import java.net.{URLConnection, URL}
import java.io._
import org.kevoree.library.sky.api.helper.KloudModelHelper
import scala.collection.JavaConversions._
import org.kevoree.log.Log

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 01/11/12
 * Time: 18:11
 *
 * @author Erwan Daubert
 * @version 1.0
 */
object KloudProviderHelper {

  def pullModel(urlPath: String): ContainerRoot = {
    try {
      val url: URL = new URL(urlPath)
      val conn: URLConnection = url.openConnection
      conn.setConnectTimeout(2000)
      val inputStream: InputStream = conn.getInputStream
      KevoreeXmiHelper.instance$.loadStream(inputStream)
    }
    catch {
      case e: IOException => {
        null
      }
    }
  }

  def sendModel(model: ContainerRoot, urlPath: String): Boolean = {
    Log.debug("send model on {}", urlPath)
    try {
      val outStream: ByteArrayOutputStream = new ByteArrayOutputStream
      KevoreeXmiHelper.instance$.saveStream(outStream, model)
      outStream.flush()
      val url: URL = new URL(urlPath)
      val conn: URLConnection = url.openConnection
      conn.setConnectTimeout(3000)
      conn.setDoOutput(true)
      val wr: OutputStreamWriter = new OutputStreamWriter(conn.getOutputStream)
      wr.write(outStream.toString)
      wr.flush()
      val rd: BufferedReader = new BufferedReader(new InputStreamReader(conn.getInputStream))
      var line: String = rd.readLine
      while (line != null) {
        line = rd.readLine
      }
      wr.close()
      rd.close()
      true
    }
    catch {
      case e: Exception => {
        false
      }
    }
  }

  def getMasterIP_PORT(masterProp: String): java.util.List[String] = {
    val result = new java.util.ArrayList[String]()
    masterProp.split(",").foreach(ips => {
      val vals = ips.split("=")
      if (vals.size == 2) {
        result.add(vals(1))
      }
    })
    result
  }

  def selectIaaSNodeAsAMaster(model: ContainerRoot) {
    val iaasNodes = model.getNodes.filter(n => KloudModelHelper.isIaaSNode(model, n))

  }

}
