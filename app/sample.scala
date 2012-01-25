package controllers

import play._
import play.mvc._
import com.mongodb.casbah.gridfs.Imports._
import com.mongodb.casbah.Imports._
import com.mongodb.util.JSON
import scala.collection.JavaConverters._

object Sample extends Controller {

    import views.Application._

    val _mongoConn = MongoConnection()
    //    val _mongodb = com.mongodb.casbah.Imports.MongoDB()
    
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
      
    def map = {
      html.map("33.586685","130.401")
    }
      
    def coupon = {
      "your IP: " + Http.Request.current().remoteAddress
    }

    def entry_form = {
      val ip  = Http.Request.current().remoteAddress
      //      val q = MongoDBObject("ip" -> ip)
      val q = MongoDBObject.empty
      val o = MongoDBObject("_id" -> 0, "ip" -> 1, "term_name" -> 1, "term_add" -> 1)
      val u = _mongoConn("wash")("term").findOne(q,o).get
           u("ip").toString
      html.term(List((u("ip").toString, u("term_name").toString, u("term_add").toString)))
      //_mongoConn("wash")("term").findOne(q,o).foreach { x => x("ip") }
      //      t.getAs[String]("ip").toString      
      /*val x = for {
        t <- _mongoConn("wash")("term").findOne(q,o).toList
        Some(term_ip: String) = t.getAs[String]("ip")
        Some(term_name: String) = t.getAs[String]("term_name")
        Some(term_add: String) = t.getAs[String]("term_add")
      } yield (term_ip, term_name, term_add)
        html.term(x)*/
    }
        
    def edit_term = {
      val ip = params.get("ip")
      val term_name = params.get("term_name")
      val term_add = params.get("term_add")
      html.term(List((params.get("ip"), params.get("term_name"), params.get("term_add"))))
    }

  def entry = {
      val ra        = Http.Request.current().remoteAddress
      val ip        = params.get("term_ip")
      val term_name = params.get("term_name")
      val term_add  = params.get("term_add")
      val latlng    = ""
      if (!ip.isEmpty) {
        val q = MongoDBObject("ip" -> ip)
        _mongoConn("wash")("term").remove(q)
        val doc = MongoDBObject(
              "ip" -> ip,
              "term_name" -> term_name,
              "term_add" -> term_add,
              "latlng" -> latlng
        )
        _mongoConn("wash")("term").save( doc )
      }
      /*      if (ip.isEmpty) {
        val doc = MongoDBObject(
              "ip" -> ip,
              "term_name" -> term_name,
              "term_add" -> term_add,
              "latlng" -> latlng
        )
        _mongoConn("wash")("term").save( doc )
      } else {
        val q = MongoDBObject("ip" -> ip)
        val doc = MongoDBObject(
              "term_name" -> term_name,
              "term_add" -> term_add,
              "latlng" -> latlng
        )
        _mongoConn("wash")("term").update(q, doc)
        } */
      Redirect("/surv/admin?mode=term")
    }
        
}