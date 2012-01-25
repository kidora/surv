package controllers

import play._
import play.libs._
import play.mvc._
import com.mongodb.casbah.gridfs.Imports._
import com.mongodb.casbah.Imports._
import com.mongodb.util.JSON
import java.io.File
import java.io.FileInputStream
import scala.collection.JavaConverters._

object Testgfs extends Controller {

    val _mongoConn = MongoConnection()
    
    def gridfstest = {
      //      Http.Request.current().getBase
      // Play.applicationPath.getPath
      val gridfs = GridFS(_mongoConn("wash")) // creates a GridFS handle on ``fs``
      val logo = new FileInputStream(Play.applicationPath.getPath+"/public/images/title.png")
      gridfs(logo) { fh =>
        fh.filename = "title.png"
        fh.contentType = "image/png"
      }
        gridfs.findOne("title.png").get
    }

    def getpath = Play.applicationPath.getPath
      
    def getgfs = {
      val gridfs = GridFS(_mongoConn("wash")) // creates a GridFS handle on ``fs``
      val f = new File("a.png")
      gridfs.findOne("title.png").get.writeTo(f)
      f
    }
}