(function($){

/* Format hh:mm:ss:sss from Date Object */
function formatTime(t) {
  var h = t.getHours();
  var m = t.getMinutes();
  var s = t.getSeconds();
  var ms = t.getMilliseconds();
  if(h<10){h="0"+h};
  if(m<10){m="0"+m};
  if(s<10){s="0"+s};
  if(ms<10){ms="00"+ms}else{if(ms<100){ms="0"+ms}};
  return h + ":" + m + ":" + s + "." + ms;
}

$(function() {
    // 現在地
    var latlng;
    //
    // 新規マーカー
    var nwmarker;

        function findLocation(location, marker) {
          console.log("findLocation");
          $('#map_canvas').gmap('search', {'location': location}, function(results, status) {
		        if ( status === 'OK' ) {
              console.log("Location found!");
			        $.each(results[0].address_components, function(i,v) {
				        if ( v.types[0] == "administrative_area_level_1" || 
					          v.types[0] == "administrative_area_level_2" ) {
					        $('#state'+marker.__gm_id).val(v.long_name);
				        } else if ( v.types[0] == "country") {
					        $('#country'+marker.__gm_id).val(v.long_name);
				        }
                console.log(
                  "findLocation:"+v.types[0]+":"+v.long_name
                );
			        });
			        marker.setTitle(results[0].formatted_address.replace("日本, ",""));
              console.log(results[0].formatted_address.replace("日本, ",""));
			        $('#address'+marker.__gm_id).val(results[0].formatted_address.replace("日本, ",""));
                $("#entry #address").val(results[0].formatted_address.replace("日本, ",""));
			        openDialog(marker);
		        }
	        });
          
        } // function findLocation

        function openDialog(marker) {
          console.log("openDialog"+marker.__gm_id);
          $.mobile.changePage('#entry', {});
        }

    /* Geolocation API function */
    var Geoloc = function() {
      var initDate,wid;
      var initSec = 4; // 位置情報誤差調整のための更新秒数
      var initlat,initlng,initacy;
      // Options for watchPositoin
      var _pos_opt = {
        enableHighAccuracy: true,
//        timeout : 10000,
        maximumAge: 0
      };
      // watchPosition success callback function
      function _pos_success(pos) {
			  var cds = pos.coords;
        console.log(
          "wp緯度: " + cds.latitude,
          "wp経度: " + cds.longitude,
          "wp正確性:" + cds.accuracy,
          "time: " + formatTime(new Date())
        );
		    // initSec秒間 位置情報誤差調整のため初期位置を上書き
		    var currTime = new Date().getTime();
		    if(currTime - initDate.getTime() < 1000 * initSec || !initlat){
          if (initacy) {
            // 正確性が上がっている場合のみ更新
            if(cds.accuracy < initacy) {
			        initlat = cds.latitude;
              initlng = cds.longitude;
              initacy = cds.accuracy;
              console.log(
                "現在地緯度更新: " + initlat,
                "現在地経度更新: " + initlng,
                "現在地正確性更新: " + initacy,
                "time: " + formatTime(new Date())
              );
            }
          } else {
			      initlat = cds.latitude;
            initlng = cds.longitude;
            initacy = cds.accuracy;
            console.log(
              "現在地緯度: " + initlat,
              "現在地経度: " + initlng,
              "現在地正確性: " + initacy,
                "time: " + formatTime(new Date())
            );
          }
			    return;
		    }
			  navigator.geolocation.clearWatch(wid);
        console.log(
          "clearWatch ",
          "time: " + formatTime(new Date())
        );
      }
      // watchPosition error callback function
      function _pos_error() {
			  navigator.geolocation.clearWatch(wid);
        console.log("位置情報の取得に失敗しました。");
      }
      return {
        init : function(){ // 初期化
          initDate = new Date();
          wid = navigator.geolocation.watchPosition(
            _pos_success,
            _pos_error,
            _pos_opt
          );
        },
        getInitLat : function(){ // 現在地取得(LatLng)
          return initlat;
        },
        getInitLng : function(){ // 現在地取得(LatLng)
          return initlng;
        }
      };
    }(); // Geoloc

    // 固定ツールバーのタップによる表示切替をしない
    $.mobile.fixedToolbars.setTouchToggleEnabled(false);
    // コンテンツ表示部の高さを設定
    $('[data-role=content]').height(
      $('.ui-mobile-viewport').height() - 
      (4 + $('[data-role=header]').height() 
      + $('[data-role=footer]').height())
    );
/*
		navigator.geolocation.getCurrentPosition(function(pos) {
			var cds = pos.coords;
      console.log(
        "緯度: " + cds.latitude,
        "経度: " + cds.longitude,
        "正確性:" + cds.accuracy
      );
      latlng = new google.maps.LatLng(cds.latitude,cds.longitude)
    }); // navigator.geolocation.getCurrentPosition
*/
    
    
    // 地点登録削除
    $("#rmnewmk").click(function() {
      nwmarker.setMap(null);
      nwMarker=null;
      $.mobile.changePage('#mappage');
    });
    
    // 履歴登録画面表示
    $("#hist").live("pagecreate", function() {
      $.getJSON('/surv/bsmark', {
        'lat': $("#lat").val(),
        'lng': $("#lng").val(),
        'nelat': $("#nelat").val(),
        'nelng': $("#nelng").val(),
        'swlat': $("#swlat").val(),
        'swlng': $("#swlng").val()
      }, function(json){
        $("#histories li").remove();
        for (var i in json) {
          console.log("name:"+json[i].name);
          $("#histories").append('<li data-theme="c" class="ui-btn ui-btn-icon-right ui-li-has-arrow ui-li ui-btn-up-c"><div class="ui-btn-inner ui-li"><div class="ui-btn-text"><a id="hist_name" class="ui-link-inherit" onClick="return false;">'+json[i].name+'</a></div><span class="ui-icon ui-icon-arrow-r ui-icon-shadow"></span></div></li>');
        }
      });
    });
    
    $("#hist_name").live('click', function() {
      $("#history #user").val($("#userid").val());
      $("#history #name").val(this.text);
      $("#history #comment").val("");
      $("#history #timestamp").val(new Date());
      $.mobile.changePage('#history');
    });
    
    // マーカー削除
    function delMarkers() {
      var mks = $("#map_canvas").gmap('get','markers');
      for (var m in mks) {
        mks[m].setMap(null);
      }
    }

  // 現在地のマーカー表示
  function dispCurrentMarker() {
    $('#map_canvas').gmap(
      'addMarker',
      {'position': latlng},
      function(map, marker) {
            $('#map_canvas').gmap('search', {
              'location': marker.getPosition()
            }, function(results, status) {
		          if ( status === 'OK' ) {
                console.log("Location found!");
			          $.each(results[0].address_components, function(i,v) {
				          if ( v.types[0] == "administrative_area_level_1" || 
					            v.types[0] == "administrative_area_level_2" ) {
					          $('#state'+marker.__gm_id).val(v.long_name);
				          } else if ( v.types[0] == "country") {
					          $('#country'+marker.__gm_id).val(v.long_name);
				          }
                  console.log(
                    "findLocation:"+v.types[0]+":"+v.long_name
                  );
			          });
			          marker.setTitle(results[0].formatted_address.replace("日本, ",""));
                console.log(results[0].formatted_address);
			          $('#address'+marker.__gm_id).val(results[0].formatted_address);
                $("#entry_sheet #address").val(results[0].formatted_address.replace("日本, ",""));
		          }
	          });
    }).click(function() {
      $('#map_canvas').gmap('openInfoWindow', {'content': '現在地'}, this);
    });
  }

  Geoloc.init();
  console.log("Geoloc.init() end");

      $("#map_canvas").gmap().bind('init', function(e, map) {
        console.log("gmap().bind.init start");
        $("#lat").val(Geoloc.getInitLat());
        $("#lng").val(Geoloc.getInitLng());
        latlng = new google.maps.LatLng(Geoloc.getInitLat(),Geoloc.getInitLng());
        $("#map_canvas").gmap('option', 'center', latlng);
        $("#map_canvas").gmap('option', 'zoom', 16);
        var bnds = map.getBounds();
        $("#nelat").val(bnds.getNorthEast().lat());
        $("#nelng").val(bnds.getNorthEast().lng());
        $("#swlat").val(bnds.getSouthWest().lat());
        $("#swlng").val(bnds.getSouthWest().lng());
        dispCurrentMarker();
        $(map).click(function(e) {
          $("#map_canvas").gmap('addMarker', {
            'position': e.latLng,
            'draggable': true,
            'bounds': false
          }, function(map, marker) {
            nwmarker = marker;
            $('#dialog').append('<form id="dialog'+marker.__gm_id+'" method="get" action="/" style="display:none;" data-rel="dialog"><p><label for="country">Country</label><input id="country'+marker.__gm_id+'" class="txt" name="country" value=""/></p><p><label for="state">State</label><input id="state'+marker.__gm_id+'" class="txt" name="state" value=""/></p><p><label for="address">Address</label><input id="address'+marker.__gm_id+'" class="txt" name="address" value=""/></p><p><label for="comment">Comment</label><textarea id="comment" class="txt" name="comment" cols="40" rows="5"></textarea></p></form>');
			      findLocation(marker.getPosition(), marker);
          }).dragend( function(event) {
			      findLocation(event.latLng, this);
		      }).click( function() {
			      openDialog(this);
		      });
        }); // map.click
      }); // gmap.bind.init

  $('#mappage').live('pageshow', function() {
    console.log("mappage.live.pageshow start");
    $("#map_canvas").gmap('option', 'center', latlng);
    $('#map_canvas').gmap('refresh');
    var bnds = $('#map_canvas').gmap('get','map').getBounds();
    $("#nelat").val(bnds.getNorthEast().lat());
    $("#nelng").val(bnds.getNorthEast().lng());
    $("#swlat").val(bnds.getSouthWest().lat());
    $("#swlng").val(bnds.getSouthWest().lng());
  });

    function dispMarkers(json) {
        for (var i in json) {
          console.log("name:"+json[i].name+"lat:"+json[i].lat+" lng:"+json[i].lng);
          $("#map_canvas").gmap('addMarker', {
            'position': new google.maps.LatLng(json[i].lat, json[i].lng),
            'bounds': true,
            'animation': google.maps.Animation.DROP,
            'icon': 'http://chart.apis.google.com/chart?chst=d_map_pin_letter&chld=|7FFF00|000000'
          }, function(map, marker) {
            var cmtwk = "<div style='width: 200px; height: 150px;'>"+json[i].name+"<hr>";
            for (var j in json[i].comments) {
              cmtwk = cmtwk + json[i].comments[j].comment + "<br>";
            }
            cmtwk = cmtwk + "</div>";
            $(marker).click(function(){
              $('#map_canvas').gmap('openInfoWindow', {'content': cmtwk}, this);
            });
          });
        }
    }

    // 検索
    $("#search").click(function() {
      delMarkers();
      dispCurrentMarker();
      $.getJSON('/surv/bsmark', {
        'area': $("#area").val(),
        'keyword': $("#keyword").val()
      }, function(json){
        dispMarkers(json);
        $.mobile.changePage('#mappage');
      });
    });

    // 現在地から検索
    $("#sfh").click(function() {
      delMarkers();
      $.getJSON('/surv/bsmark', {
        'lat': $("#lat").val(),
        'lng': $("#lng").val(),
        'nelat': $("#nelat").val(),
        'nelng': $("#nelng").val(),
        'swlat': $("#swlat").val(),
        'swlng': $("#swlng").val()
      }, function(json){
        dispMarkers(json);
        $("#map_canvas").gmap('option', 'center', latlng);
        $("#map_canvas").gmap('option', 'zoom', 16);
        $('#map_canvas').gmap('refresh');
        dispCurrentMarker();
      });
    });
});

})(jQuery);