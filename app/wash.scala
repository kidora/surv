package controllers

import play._
import play.mvc._
import com.mongodb.casbah.Imports._
import scala.util.parsing.json._
//import com.mongodb.util.JSON

object Wash extends Controller {

    import views.Application._

    val _mongoConn = MongoConnection()

    def admin(mode: String="") = mode match {
      case "term" => {
        val q = MongoDBObject.empty
        val o = MongoDBObject("_id" -> 0, "ip" -> 1, "term_name" -> 1, "term_add" -> 1)
        val x = for{
          t <- _mongoConn("wash")("term").find(q,o).sort(MongoDBObject("ip"->1)).toList
          Some(ip: String) = t.getAs[String]("ip")
          Some(term_name: String) = t.getAs[String]("term_name")
          Some(term_add: String) = t.getAs[String]("term_add")
        } yield (ip, term_name, term_add)
        html.washadmin(mode,x)
      }
      case "cont" => {
        val q = MongoDBObject.empty
        val o = MongoDBObject("_id" -> 0, "cont_ttl" -> 1, "content" -> 0)
        val x = for{
          t <- _mongoConn("wash")("cont").find().toList
          Some(ttl: String) = t.getAs[String]("cont_ttl")
        } yield (ttl, "", "")
        html.washadmin(mode,x)
      }
      case "sche" => {
        val q = MongoDBObject.empty
        val o = MongoDBObject("_id" -> 0, "cont_ttl" -> 1, "content" -> 0)
        val x = for{
          t <- _mongoConn("wash")("cont").find().toList
          Some(ttl: String) = t.getAs[String]("cont_ttl")
        } yield (ttl, "", "")
        html.washadmin(mode,x)
      }
      case _ => {
        val x = Nil
        html.washadmin(mode,x)
      }
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
      Redirect("/surv/washadmin?mode=term")
    }

    def edit_term = html.washterm(
      List(
        (params.get("ip"), params.get("term_name"), params.get("term_add"))
      )
    )

    def cont_entry = {
      val ttl  = params.get("cont_ttl")
      val cont = params.get("cont")
      if (!ttl.isEmpty) {
        val q = MongoDBObject("cont_ttl" -> ttl)
        _mongoConn("wash")("cont").remove(q)
        val doc = MongoDBObject(
              "cont_ttl" -> ttl,
              "cont" -> cont
        )
        _mongoConn("wash")("cont").save( doc )
      }
      Redirect("/surv/washadmin?mode=cont")
    }
    def cont_edit = {
      val ttl = params.get("cont_ttl")
      val q = MongoDBObject("cont_ttl" -> ttl)
      val o = MongoDBObject("_id" -> 0, "cont_ttl" -> 1, "cont" -> 1)
      val x = for{
        t <- _mongoConn("wash")("cont").find(q,o).toList
          Some(ttl: String) = t.getAs[String]("cont_ttl")
          Some(cont: String) = t.getAs[String]("cont")
        } yield (ttl, cont)
      html.washcont(x)
    }
    def cont_edit_new = html.washcont(List(("","")))
    
    def sche_edit = {
      val ttl = params.get("cont_ttl")
      val q = MongoDBObject("cont_ttl" -> ttl)
      val o = MongoDBObject("_id" -> 0, "cont_ttl" -> 1, "st_dt" -> 1, "st_tm" -> 1,
        "en_dt" -> 1, "en_tm" -> 1, "tm" -> 1)
      val tmp = _mongoConn("wash")("schedule").findOne(q,o)
      var t = List((ttl,"","","","",List[String]()))
      if (!tmp.isEmpty) {
        val s = tmp.get
        val tm = JSON.parse(s("tm").toString) match {
          case Some(a: List[String]) => a
        }
        t = List((ttl,s("st_dt").toString,s("st_tm").toString,s("en_dt").toString,s("en_tm").toString,tm))
      }
      /*
      val t = for {
        s <- _mongoConn("wash")("schedule").find(q,o).toList
        Some(st_dt: String) = s.getAs[String]("st_dt")
        Some(st_tm: String) = s.getAs[String]("st_tm")
        Some(en_dt: String) = s.getAs[String]("en_dt")
        Some(en_tm: String) = s.getAs[String]("en_tm")
        tmp = s.getAs[String]("tm").toString
        val tm = JSON.parse(tmp) match {
          case Some(a: List[String]) => a
        }
        // tm = JSON.parse(t("tm").toString) match {
          // case Some(a: List[String]) => a
          // case _ => List()
          // }
      } yield (ttl, st_dt, st_tm, en_dt, en_tm, tm)
      */

      val cq = MongoDBObject.empty
      val co = MongoDBObject("_id" -> 0, "ip" -> 1, "term_name" -> 1, "term_add" -> 1)
      val x = for{
         c <- _mongoConn("wash")("term").find(cq,co).sort(MongoDBObject("ip"->1)).toList
          Some(ip: String) = c.getAs[String]("ip")
          Some(term_name: String) = c.getAs[String]("term_name")
          // Some(term_add: String) = t.getAs[String]("term_add")
      } yield (ip, term_name)
      // tm
      // Json(t("tm").toString)
      /*
      val x = for{
        t <- _mongoConn("wash")("schedule").find(q,o).toList
          Some(ttl: String) = t.getAs[String]("cont_ttl")
          Some(st_dt: String) = t.getAs[String]("st_dt")
          Some(st_tm: String) = t.getAs[String]("st_tm")
          Some(en_dt: String) = t.getAs[String]("en_dt")
          Some(en_tm: String) = t.getAs[String]("en_tm")
          Some(tm: BasicDBList) = t.getAs[BasicDBList]("tm")
        } yield (ttl, st_dt, st_tm, en_dt, en_tm, tm.toList)
        */
      html.washsche(t,x)
    }
    def sche_entry = {
      val ttl  = params.get("cont_ttl")
      val st_dt  = params.get("st_dt")
      val st_tm  = params.get("st_tm")
      val en_dt  = params.get("en_dt")
      val en_tm  = params.get("en_tm")
      val tm  = params.getAll("tm")
      //val tm = JSON.parse(params.getAll("tm")).asInstanceOf[DBObject]
      // toJson(tm)
      
      if (!ttl.isEmpty) {
        val q = MongoDBObject("cont_ttl" -> ttl)
        _mongoConn("wash")("schedule").remove(q)
        val doc = MongoDBObject(
              "cont_ttl" -> ttl,
              "st_dt" -> st_dt,
              "st_tm" -> st_tm,
              "en_dt" -> en_dt,
              "en_tm" -> en_tm,
              "tm" -> tm
        )
        _mongoConn("wash")("schedule").save( doc )
      }
      Redirect("/surv/washadmin?mode=sche")
      
    }
    
    def index(mode: String = "top") = mode match {
      case "top" => {
        val ra  = Http.Request.current().remoteAddress
        val ip  = params.get("ip")
        val q = MongoDBObject.empty
        val o = MongoDBObject("_id" -> 0, "cont_ttl" -> 1, "cont" -> 1)
        val x = for{
          t <- _mongoConn("wash")("cont").find().toList
          Some(ttl: String) = t.getAs[String]("cont_ttl")
        } yield (ttl)
        html.washclient(mode,ip,x,"")
      }
      case _ => {
        // val ttl = params.get("cont_ttl")
        val ip = params.get("ip")
        val q = MongoDBObject("cont_ttl" -> params.get("cont_ttl"))
        val o = MongoDBObject("_id" -> 0, "cont_ttl" -> 1, "cont" -> 1)
        val c = _mongoConn("wash")("cont").findOne(q,o).get
        html.washclient(mode,ip,List[(String)](),c("cont").toString)
      }
    }
    
    def demo = html.washdemo()
}