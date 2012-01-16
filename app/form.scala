package controllers

import play._
import play.mvc._
import com.mongodb.casbah.Imports._
import com.mongodb.util.JSON
import scala.collection.JavaConverters._

object Form extends Controller {

    import views.Application._

    val _mongoConn = MongoConnection()
    
    def edit = html.edit()
    
    def demo = html.demo()
}