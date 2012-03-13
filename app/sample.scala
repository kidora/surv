package controllers

import play._
import play.mvc._
import com.mongodb.casbah.gridfs.Imports._
import com.mongodb.casbah.Imports._
import com.mongodb.util.JSON
import scala.collection.JavaConverters._
// Open CSV
import au.com.bytecode.opencsv.CSVReader
import java.io.FileReader
import java.io.File

object Sample extends Controller {

    import views.Application._

    val _mongoConn = MongoConnection()
    //    val _mongodb = com.mongodb.casbah.Imports.MongoDB()
    
    def samples(mode: String = "menu") = mode match {
      case "reversi" => html.reversi()
      case _ => html.samples()
    }
    
    def reversi = html.reversi()
    
    // Google Maps APIs sample
    def geolocation = html.geoloc()
    def gcgeoloc = html.gcgeocoder()
    def geotest = {
      val address = "福岡県福岡市中央区"
      // val rs = scala.io.Source.fromURL("http://maps.google.com/maps/api/geocode/json?address="+address+"&language=ja&sensor=false").mkString
      //      scala.util.parsing.json.JSON.parseFull(rs)
      val rs = scala.xml.XML.loadString(scala.io.Source.fromURL("http://maps.google.com/maps/api/geocode/xml?address="+address+"&language=ja&sensor=false").mkString)
      (rs \\ "location" \ "lng").text
    }
    
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
    
    def vflick = html.vflick()

    def wktest = html.webkit()
    
    def jqm = html.jqm()
    
    def jqm_entry = {
        val attr0   = params.get("attr0")
        val name    = params.get("name")
        val address = params.get("address")
        val tel     = params.get("tel")
        val email   = params.get("email")
        val comment = params.get("comment")
        val rs = scala.xml.XML.loadString(scala.io.Source.fromURL("http://maps.google.com/maps/api/geocode/xml?address="+address+"&language=ja&sensor=false").mkString)
        val lat     = (rs \\ "location" \ "lat").text
        val lng     = (rs \\ "location" \ "lng").text
        val doc = MongoDBObject(
        "attr0" -> attr0,
              "name" -> name,
              "address" -> address,
              "tel" -> tel,
              "email" -> email,
        "comment" -> comment,
              "lat" -> lat,
              "lng" -> lng
        )
        _mongoConn("idex")("pos").save( doc )
        Redirect("/surv/bizsupo#mappage")
    }

    def jqmap = {
        val lat = params.get("lat")
        val lng = params.get("lng")
        if ( lat==null || lat=="" || lng==null || lng=="" ) {
          "usage :?lat=xxx&lng=yyy"
        } else {
          val latlng = lat+","+lng
          // JSON
          val rs = JSON.parse(scala.io.Source.fromURL("http://maps.google.com/maps/api/geocode/json?latlng="+latlng+"&language=ja&sensor=false").mkString).asInstanceOf[DBObject]
          val res = JSON.parse(rs.as[BasicDBList]("results").toList.head.toString).asInstanceOf[DBObject]
          val rs2 = JSON.parse(res.as[BasicDBObject]("geometry").toString).asInstanceOf[DBObject]
          val rs3 = JSON.parse(rs2.get("viewport").toString).asInstanceOf[DBObject]
          val n_e = JSON.parse(rs3.get("northeast").toString).asInstanceOf[DBObject]
          val s_w = JSON.parse(rs3.get("southwest").toString).asInstanceOf[DBObject]
          // "ne: lat="+n_e.get("lat").toString+" lng="+n_e.get("lng").toString+" - sw: lat="+
          // s_w.get("lat").toString+" lng="+s_w.get("lng").toString
          
          // XML
          // val rs = scala.xml.XML.loadString(scala.io.Source.fromURL("http://maps.google.com/maps/api/geocode/xml?latlng="+latlng+"&language=ja&sensor=false").mkString)
          // rs
          
          val nelat = n_e.get("lat")
          val nelng = n_e.get("lng")
          val swlat = s_w.get("lat")
          val swlng = s_w.get("lng")
          "southweat lat:"+ swlat +" lng:"+ swlng +" northeast lat:"+ nelat +" lng:"+nelng
        }
    }

    def jqm_get = {
        val lat = params.get("lat")
        val lng = params.get("lng")
        val pnelat = params.get("nelat")
        val pnelng = params.get("nelng")
        val pswlat = params.get("swlat")
        val pswlng = params.get("swlng")
        val area = params.get("area")
        val keyword = params.get("keyword")
        var q = MongoDBObject.empty
        if ( lat==null || lat=="" || lng==null || lng=="" ) {
          if ( area != null && area != "" ) {
            q = MongoDBObject("address"->MongoDBObject("$regex"->("[*"+area+"*]")))
          }
          if ( keyword != null && keyword != "" ) {
            q = q ++ MongoDBObject("name"->MongoDBObject("$regex"->("[/*"+keyword+"*/]")))
          }
        } else if ( !(pnelat==null || pnelat=="" || pnelng==null || pnelng=="" &&  pswlat==null || pswlat=="" || pswlng==null || pswlng=="") ) {
          val nelat = pnelat.toString
          val nelng = pnelng.toString
          val swlat = pswlat.toString
          val swlng = pswlng.toString
            q = MongoDBObject("lat"->MongoDBObject("$lt"->nelat, "$gt"->swlat), "lng"->MongoDBObject("$lt"->nelng, "$gt"->swlng))
        } else {
          val latlng = lat+","+lng
          // JSON
          val rs = JSON.parse(scala.io.Source.fromURL("http://maps.google.com/maps/api/geocode/json?latlng="+latlng+"&language=ja&sensor=false").mkString).asInstanceOf[DBObject]
          val res = JSON.parse(rs.as[BasicDBList]("results").toList.head.toString).asInstanceOf[DBObject]
          val rs2 = JSON.parse(res.as[BasicDBObject]("geometry").toString).asInstanceOf[DBObject]
          val rs3 = JSON.parse(rs2.get("viewport").toString).asInstanceOf[DBObject]
          val n_e = JSON.parse(rs3.get("northeast").toString).asInstanceOf[DBObject]
          val s_w = JSON.parse(rs3.get("southwest").toString).asInstanceOf[DBObject]
          val nelat = n_e.get("lat").toString
          val nelng = n_e.get("lng").toString
          val swlat = s_w.get("lat").toString
          val swlng = s_w.get("lng").toString
            q = MongoDBObject("lat"->MongoDBObject("$lt"->nelat, "$gt"->swlat), "lng"->MongoDBObject("$lt"->nelng, "$gt"->swlng))
        }
        val o = MongoDBObject("_id" -> 0, "name" -> 1, "address" -> 1, "tel" -> 1, "email" -> 1, "lat" -> 1, "lng" -> 1)
        /* OK *
        val r = for(x <- _mongoConn("idex")("pos").find(q,o).toList) yield x
          JSON.serialize(r)
        * OK */
        // JSON.serialize(_mongoConn("idex")("pos").find(q,o).toList) /* これもOKそりゃそうだ */
        // 履歴の取得を追加
        val x = for {
          r <- _mongoConn("idex")("pos").find(q,o).toList
          Some(name: String) = r.getAs[String]("name")
          val hq = MongoDBObject("name" -> name)
          val ho = MongoDBObject("_id" -> 0, "userid" -> 1, "name" -> 1, "comment" -> 1, "timestamp" -> 1)
          val comments = MongoDBObject("comments" -> _mongoConn("idex")("history").find(hq,ho).toList)
          val s = r ++ comments
        } yield s
        JSON.serialize(x)
    }
    
    def bshist = {
        val user = params.get("user")
        val name    = params.get("name")
        val comment = params.get("comment")
        val tmstamp = params.get("timestamp")
        val doc = MongoDBObject(
              "userid" -> user,
              "name" -> name,
              "comment" -> comment,
              "timestamp" -> tmstamp
        )
        _mongoConn("idex")("history").save( doc )
        Redirect("/surv/bizsupo#mappage")
    }

    // 訪問履歴取得
    def bsget_hist = {
      val qa = MongoDBObject.empty
      val oa = MongoDBObject("_id" -> 0, "name" -> 1, "address" -> 1, "tel" -> 1, "email" -> 1, "lat" -> 1, "lng" -> 1)
      val x = for {
        r <- _mongoConn("idex")("pos").find(qa,oa).toList
        Some(name: String) = r.getAs[String]("name")
        val q = MongoDBObject("name" -> name)
        val o = MongoDBObject("_id" -> 0, "userid" -> 1, "name" -> 1, "comment" -> 1, "timestamp" -> 1)
        val comments = MongoDBObject("comments" -> _mongoConn("idex")("history").find(q,o).toList)
        val s = r ++ comments
      } yield s
        JSON.serialize(x)
    }

    // 営業支援ツール管理画面
    def bsadmin(msg: String="") = html.bsadmin(msg)
    
    import java.io.Closeable
    import java.io.Reader
    import java.io.FileInputStream
    import models._
    
class ScalaCSVReader(reader: Reader) extends Iterator[Array[String]] with Closeable {
    require(reader != null)
    private val csv = new CSVReader(reader)
    private var nextRow = csv.readNext
    override def hasNext = nextRow != null
    override def next = try { nextRow } finally { nextRow = csv.readNext }
    override def close { csv.close }
}

    def bsupcsv(file: File) = {
      // title
      // if (photo!=null) "nothing" else "exist"
      // photo.getName()
      // Play.applicationPath.getPath+"/public/images/"+photo.getName()
            // val saveTo = new File(Play.applicationPath.getPath+"/public/images/"+photo.getName());
            // photo.renameTo(saveTo);
            // saveTo
      // val logo = new FileInputStream(file)
      // logo
      // file.getPath()
      // val reader = new CSVReader(new FileReader(file.getPath()))
      // val res = Iterator.continually(reader.readNext).takeWhile(_ != null).foreach {r =>
        // r(0) + " : " + r(2) + "<br>"
      // }
        // res
        // Iterator.continually(reader.readNext).takeWhile(_ != null).map(_.toList)
      // for (row <- reader.readNext) println(row)
      val reader = new ScalaCSVReader(new FileReader(file.getPath()))
      val rows: List[Array[String]] = try { reader.toList } finally { reader.close }
      for (r <- rows) {
        val cat = r(1)
        val name = r(2)
        val address = r(3)
        val tel     = r(4)
        val email   = r(5)
        val comment = r(6)
        val ggc = JSON.parse(scala.io.Source.fromURL("http://maps.google.com/maps/api/geocode/json?address="+address++"&language=ja&sensor=false").mkString).asInstanceOf[DBObject]
        val res = JSON.parse(ggc.as[BasicDBList]("results").toList.head.toString).asInstanceOf[DBObject]
        val geo = JSON.parse(res.as[BasicDBObject]("geometry").toString).asInstanceOf[DBObject]
        val loc = JSON.parse(geo.get("location").toString).asInstanceOf[DBObject]
        val lat = loc.get("lat")
        val lng = loc.get("lng")
        val doc = MongoDBObject(
              "cat" -> cat,
              "name" -> name,
              "address" -> address,
              "tel" -> tel,
              "email" -> email,
              "comment" -> comment,
              "lat" -> lat,
              "lng" -> lng
        )
        _mongoConn("idex")("pos").save( doc )
      }
      bsadmin("位置情報の登録が完了しました。")
    }
}