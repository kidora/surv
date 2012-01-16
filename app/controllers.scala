package controllers

import play._
import play.mvc._
import com.mongodb.casbah.Imports._
import com.mongodb.util.JSON
import scala.collection.JavaConverters._

object Application extends Controller {

    import views.Application._

    val _mongoConn = MongoConnection()

    def index = html.index("アンケート")

    // ログイン画面表示
    def login = html.login("Login")

    // ログイン認証
    def auth = Redirect("/surv/admin")

    // アンケート回答送信
    def entry = html.thanks()

    // アンケート作成キャンセル
    def cancel = Redirect("/surv/admin")

    // 管理画面
    def admin(mode: String = "survey") = {
      val uid = 1
      val q = MongoDBObject("uid" -> uid)
      val o = MongoDBObject("_id" -> 0, "uid" -> 1, "survey_id" -> 1, "caption" -> 1)
      // val s = _mongoConn("surv")("survey").find(q,o).toList
      val x = for{
        t <- _mongoConn("surv")("survey").find(q,o).sort(MongoDBObject("survey_id"->1)).toList
        Some(sid: Int) = t.getAs[Int]("survey_id")
        Some(cap: String) = t.getAs[String]("caption")
      } yield (sid.toString, cap)
      html.admin(mode,uid,x)
    }

    def test(no: String) = no match {
      case "1" => "佐藤イチロウ"
      case "2" => "鈴木ジロー"
      case "3" => "高橋秀樹"
      case "4" => "田中光一"
      case "5" => "渡辺陽一"
      case _ => "社員番号が登録されていません。"
    }

    def create(uid: String, sid: String) = {
      html.create("create", uid, sid)
    }

    def load = {
      val uid = params.get("uid").toInt
      val sid = params.get("sid").toInt
      val q = MongoDBObject("uid" -> uid, "survey_id" -> sid)
      _mongoConn("surv")("survey").findOne(q) match {
        case Some(d: DBObject) => Json(d)
      }
    }

    def save = {
      val _id = params.get("_id")
      val uid = params.get("uid").toInt
      val sid = params.get("sid").toInt
      val cap = params.get("caption")
      val cap_desc = params.get("caption_desc")
      val fjson = JSON.parse(params.get("fjson")).asInstanceOf[DBObject]
      if (_id.isEmpty) {
          val doc = MongoDBObject(
              "uid" -> uid,
              "survey_id" -> sid,
              "caption" -> cap,
              "caption_desc" -> cap_desc,
              "fjson" -> fjson
          )
          _mongoConn("surv")("survey").save( doc )
      } else {
          val q = MongoDBObject("uid" -> uid, "survey_id" -> sid)
          val doc = MongoDBObject(
              "uid" -> uid,
              "survey_id" -> sid,
              "caption" -> cap,
              "caption_desc" -> cap_desc,
              "fjson" -> fjson
          )
          _mongoConn("surv")("survey").update(q, doc)
      }
      Redirect("/surv/admin")
    }

    // Summary page
    def sum = html.summary()
}
