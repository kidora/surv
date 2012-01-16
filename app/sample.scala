package controllers

import play._
import play.mvc._
// import com.mongodb.casbah.Imports._
// import com.mongodb.util.JSON
import scala.collection.JavaConverters._

object Sample extends Controller {

    import views.Application._

    //    val _mongoConn = MongoConnection()
    
    def samples(mode: String = "menu") = mode match {
      case "reversi" => html.reversi()
      case _ => html.samples()
    }
    
    def reversi = html.reversi()
    
    def geolocation = html.geoloc()
    
    def load = {
      val lat = params.get("lat").toDouble
      val lng = params.get("lng").toDouble
      if (lng < 130.401) {
        html.offloc()
      } else {
        html.onloc()
      }
    }
    
    def imode = html.imode()
    
    def igps = {
      val lat = params.get("lat")
      val lng = params.get("lon")
      html.imode(lat, lng)
    }
}