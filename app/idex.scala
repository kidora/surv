package controllers

import play._
import play.mvc._

object Idex extends Controller {

    import views.Application._
    
    def index = html.idexmenu()
    
    def shaken = html.idexshaken()
    
    def flow = html.idexflow()
    
    def price = html.idexprice()
}