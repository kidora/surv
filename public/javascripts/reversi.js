window.PLAYER_VS_PAYER = 0;
window.PLAYER_VS_CPU   = 1;
window.CPU_VS_PLAYER   = 2;

// Game System
var Reversi = (function() {

    /* 定数 */
    var CIRCUMFEFENCE = [[-1, -1], [0, -1], [1, -1], [-1, 0], [1, 0], [-1, 1], [0, 1], [1, 1]], // 8方向
	STONESIZE     = 40, // 石のサイズ
	NONE          = 0,  // マス目に石が置いてない状態
	WHITE         = 1,  // マス目に白石が置いてある状態
	BLACK         = 2;  // マス目い黒石が置いてある状態

    /* 変数 */
    var board,    // 盤面の状態を保持する配列(2次元配列にしてリバーシの盤面を用意する)
	boardColor, // 盤面の色を保持する変数
	playerTurn, // プレイヤーのターンかどうかのフラグ
	mode,     // PLAYER ve PLAYER, CPU vs PLAYER, PLAYER vs CPUのどのモードかを入れておく変数
	turn,     // 現在どちらのターンなのかを保持する。
	finish,   // ゲームが終了したかどうかのフラグ 
	blackCnt, // 現在の黒石の数を保持する変数 
	whiteCnt, // 現在の白石の数を保持する変数 
	ctx,      // canvasのコンテキスト 
	info;     // 対戦中の情報を表示させる領域  

    /* 変数の初期化メソッド */
    var initVar = function() {
	board      = []; 
	boardColor = (localStorage.boardColor) ? localStorage.boardColor : '#008800'; // ローカルストレージに既にデータが入っていたらそちらを使う
	info       = document.getElementById('reversiInfo'); 
	turn       = BLACK; // リバーシは黒から始まるゲームなので黒で初期化する
	finish     = false;
	blackCnt   = null;
	whiteCnt   = null;
	ctx        = null;
	playerTurn = true;
    };

    /* 盤面の初期化メソッド */
    var initBoard = function() {

	board = [];

	// boardの中身を石がない状態で初期化する
	for ( var y = 0; y < 8; y++ ) {
	    board[y] = [];
	    for ( var x = 0; x < 8; x++ ) {
		board[y][x] = NONE;
	    }
	}

	// 白と黒の石を真ん中にセットしリバーシの初期状態を作る
	board[3][3] = WHITE;
	board[4][4] = WHITE;
	board[3][4] = BLACK;
	board[4][3] = BLACK;
    };

    /* 石を書くメソッド */
    var drawStone = function(x, y, color) {
	ctx.beginPath();
	ctx.arc(x, y, 20, 0, Math.PI*2, false);
	ctx.fillStyle = color;
	ctx.fill();
    };

    /* 盤面を描画するメソッド */
    var drawBoard = function() {

	ctx.beginPath();

	// 画面をboardColorの色で埋める
	ctx.fillStyle = boardColor;
	ctx.fillRect(0, 0, 400, 400);
	
	// 線を描画する	
	ctx.strokeStyle = '#000000';
	ctx.lineWidth = 0.5;
	for ( var i = 0; i < 9; i++ ) {
	    ctx.moveTo(i * STONESIZE, 0);
	    ctx.lineTo(i * STONESIZE, 8 * STONESIZE);
	    ctx.moveTo(0, i * STONESIZE);
	    ctx.lineTo(8 * STONESIZE, i * STONESIZE);
	}
	ctx.stroke();
    };

    /* 盤面から石がはみ出たかどうかチェックするメソッド */
    var isOut = function (x, y) {
	return ( x < 0 || x > 7 || y < 0 || y > 7 );
    };

    /* 列単位で石を置けるかどうかの判定メソッド */
    var lineCheck = function(direction, x, y, stone) {

	var queue = '';

	// 盤面からはみでるまでループさせる
	while ( 1 ) {
	    // directionで指定した方向へ1マスづつ進む
	    x += direction[0];
	    y += direction[1];

	    // 盤面からはみでたらbreakで抜ける
	    if ( isOut(x, y) )
		break;

	    // queueに自分がタッチした位置から盤面をはみでるまでdirectionで指定した方向の盤面の状態を格納していく
	    queue += board[y][x];
	}

	// データを詰めたキューの中身を解析する
	// 自分と反対の色の石が連続して続いた後に自分の石の色が現れた場合はそこに石を置く事が可能なのでtrueを返す。置けない場合はfalseを返す。
	return ( queue.search(stone==WHITE ? /2+1/ : /1+2/) == 0 );
    };

    /* 石を置けるかどうかの判定メソッド */
    var isAbleToPut = function(x, y, stone) {

	// タッチされた場所が盤面の内側で石が置いてなければそこに石を置ける可能性がある
	if ( !isOut(x, y) && (board[y][x] == NONE) ) {
	    for (var i = 0; i < CIRCUMFEFENCE.length; i++) {
		var direction = CIRCUMFEFENCE[i];
		// 石を置きたい場所の周り八方向に対してlineCheck()を実行する
		// もし一方向でもlineCheck()がtrueを返してくれればそこには石を置けるという事がわかる
		if ( lineCheck(direction, x, y, stone) )
		    return true;
	    }
	}
	// 八方向ともlineCheck()がtrueを返してくれなければそこには石を置けない
	return false;
    };

    /* 自分のターンが続くかチェックするメソッド */
    var continueOwnTurn = function(stone) {

	for ( var y = 0; y < 8; y++ ) {
	    for ( var x = 0; x < 8; x++ ) {
		// 盤面の全てのマスに対してisAbleToPut()を実行する事によって置ける場所があるかどうかが分かる
		if( isAbleToPut(x, y, stone) )
		    return true;
	    }
	}
	// どのマスにも置けなければfalseを返す
	return false;
    };

    /* ひっくり返すメソッド */
    var turnover = function(x, y, stone) {
	// 盤面に石を置く
	board[y][x] = stone;

	// 置いた場所の周り八方向をチェックする
	for ( var k = 0; k < CIRCUMFEFENCE.length; k++ ) {
	    var direction = CIRCUMFEFENCE[k];

	    // 一方向づつひっくり返す石があるかチェックする
	    if ( lineCheck(direction, x, y, stone) ) {

		// ひっくり返す石があった場合ひっくり返す処理を実行する
		for ( var x_ = x, y_ = y;; ) {
		    // 石を置いた所から一個づつ盤面の外側に向かって進む
		    x_ += direction[0];
		    y_ += direction[1];
	    
		    // 盤面の外にでたり、自分の色とぶつかったらbreakで抜ける
		    if ( isOut(x_, y_) )
			break;
		    if ( stone == BLACK && board[y_][x_] == BLACK )
			break;
		    if ( stone == WHITE && board[y_][x_] == WHITE )
			break;

		    // 自分の石の色に置き換える 
		    board[y_][x_] = stone;
		}
	    }
	}
    };

    /* ゲームの描画メソッド */
    var drawGame = function() {

	blackCnt = 0;
	whiteCnt = 0;

	// 古い画面を一度画面を消去する
	ctx.clearRect(0, 0, ctx.canvas.width, ctx.canvas.height);

	// 盤面の描画
	drawBoard();

	// 配列に入っているデータを元に石を盤面に表示させる
	for ( var y = 0; y < 8; y++ ) {
	    for ( var x = 0; x < 8; x++ ) {
		if ( board[y][x] ) {
		    drawStone(x*STONESIZE + STONESIZE/2, y*STONESIZE + STONESIZE/2, (board[y][x]==BLACK ? '#000000' : '#FFFFFF') );
		    // 石のカウント
		    board[y][x] == BLACK ? blackCnt++ : whiteCnt++;
		}
	    }
	}

	// 情報表示エリアの色を変更し石の数を描画させる
	info.style.backgroundColor = (turn == BLACK) ? '#000000' : '#FFFFFF';
	info.innerHTML = ' BLACK:' + blackCnt + ' WHITE:' + whiteCnt;

    };

    /* 勝敗の判定メソッド */
    var judge = function() {
	// 白または黒の石の置き場が無くなった場合ゲームが終了する
	if ( !finish && (!continueOwnTurn(BLACK) && !continueOwnTurn(WHITE)) ) {
	    Ext.Msg.alert('判定', (whiteCnt == blackCnt ? '引き分けです' : whiteCnt > blackCnt ? '白の勝ちです' : '黒の勝ちです') );
	    finish = true;
	}

    };

    /* コンピュータの処理メソッド */
    var cpu = function() {

	// CPUのが処理してる間はplayerTurnをfalseにする
	playerTurn = false;

	// プレイヤーが石を置いた後ワンクッション空ける為にsetTimeoutを使用して1秒(1000ms)時間を空ける
	setTimeout(function() {

	    // CPUが置ける場所を格納する配列
	    var cpuCanPut = [];

	    // マス目を全てチェックしてCPUが置ける場所があったらcpuCanPut配列にpushで詰めていく
	    for ( var y = 0; y < 8; y++ ) {
		for ( var x = 0; x < 8; x++ ) {
		    if ( isAbleToPut(x, y, turn) ) 
			cpuCanPut.push({x:x, y:y});
		}
	    }

	    // CPUが置ける場所からランダムで石を置く場所を決める
	    var random = cpuCanPut[parseInt(Math.random() * cpuCanPut.length)];

	    // 置ける場所があったら置く
	    if ( random ) {
		board[random.y][random.x] = turn;
		turnover(random.x, random.y, turn);
	    }

	    // 相手が石を置ける状態なら相手のターンにする
	    if ( continueOwnTurn((turn==BLACK ? WHITE : BLACK)) ) {
		turn = (turn == BLACK) ? WHITE : BLACK;

	    // 相手が石を置けなければもう一度CPUのターンにする
	    } else {
		// ゲーム終了のフラグが経っていればCPUの処理は行わない
		if (!finish) cpu();
	    }

	    // 画面を描画し直す
	    drawGame();

	    // 判定
	    judge();

	    // CPUの処理が終わったのでplayerTurnをtrueにする
	    playerTurn = true;

	}, 1000);
    };

    return {

        /* メインの処理 */
        start: function( m ) {

	    // モードの設定
            mode = m; // どのモードで実行されたかmodeに記録しておく

            // 初期化
	    initVar();
            initBoard();

            var canavs = document.getElementById('canvas');
            ctx  = canvas.getContext('2d');

	    // ゲーム画面を初期状態で一度描画
            drawGame();

            // CPUが先行の場合はプレイヤーが石を置く前にここで一度CPUの処理を呼び出しておく
            if ( mode == CPU_VS_PLAYER ) {
                cpu();
            }

            // クリックした時の処理
            canavs.addEventListener('click', function(e) {

                // 排他処理(CPU戦の場合はtimerを使ってるので相手が石を置く前にタッチされてもreturnで返す)
                if ( mode != PLAYER_VS_PAYER) {
                    if ( !playerTurn ) return;
                }

		// タッチされた場所がリバーシ版のどこかを計算する
                var x = Math.floor(e.offsetX / STONESIZE);
                var y = Math.floor(e.offsetY / STONESIZE);

		// タッチされた場所に石を置けるようなら処理を行う
                if ( isAbleToPut(x, y, turn) ) {
		    // タッチされた場所に石を置く
                    board[y][x] = turn;
		    // 石をひっくり返す
                    turnover(x, y, turn);

		    // 相手が石を置ける状態なら相手のターンにする
                    if ( continueOwnTurn((turn==BLACK ? WHITE : BLACK)) ){
			turn = (turn == BLACK) ? WHITE : BLACK;

			// 対人戦じゃなければここでCPUのターン
			if( mode != PLAYER_VS_PAYER ) {
			    cpu();
			}
                    }

		    // 画面を描画し直す
		    drawGame();
		    // 勝敗の判定
		    judge();

		}
      
            });
        },

	/* 現在のモードを返すメソッド(リフレッシュボタンに使用) */
	getMode: function() {
	    return mode;
	},

        /* 盤面の色をセットする関数 */
        setBoardColor: function( color ) {
            localStorage.boardColor = boardColor = color;
            drawGame();
        },

    };
})();

// Sencha Touch
Ext.setup({

    // ホームスクリーンブックマークした時のアイコン
    icon: '/surv/public/images/icon.png',
    // アイコンに光沢をつける
    glossOnIcon: true,
    // ホームスクリーンブックマークから起動した時のスタートアップ画像
    phoneStartupScreen: '/surv/public/images/phone_startup.png',

    onReady: function(){

        // パネル1 (TOP画面)
        var panel1 = new Ext.Panel({

            fullscreen: true,

            layout: {
                type: 'vbox',
                pack: 'center'
	    },

            items: [
                // TOP画像
		{html: '<img src="/surv/public/images/title.png"></img>', width:320, height:80},
                // ボタン1(1P vs CPU)
                new Ext.Button({cls:'startBtn1', text:'先攻', width:'120px', height:'50px', handler:function(){panel2.show('fade'); Reversi.start(PLAYER_VS_CPU); }}), //ボタンが押されたらパネル2を表示させる
                // ボタン2(CPU vs 1P)
                new Ext.Button({cls:'startBtn2', text:'後攻', width:'120px', height:'50px', handler:function(){panel2.show('fade'); Reversi.start(CPU_VS_PLAYER); }}), //ボタンが押されたらパネル2を表示させる
                // ボタン3(1P vs 2P)
                new Ext.Button({cls:'startBtn3', text:'対人戦',  width:'250px', height:'50px', handler:function(){panel2.show('fade'); Reversi.start(PLAYER_VS_PAYER); }})  //ボタンが押されたらパネル2を表示させる
            ]
        });

        // パネル2 (ゲーム画面、設定画面、アプリ説明画面を配置するタブパネル)
        var panel2 = new Ext.TabPanel({

            fullscreen: true,

            defaults: {
                iconMast: true,
                ui:       'plain'
            },

            // 画面上部にツールバーを追加
            dockedItems: new Ext.Toolbar({
                dock:  'top',
                title: 'Reversi',
                ui: 'light',
                items:[
                    // TOPボタンが押されたらpanel2を隠す
                    {text: 'TOP', handler:function(){panel2.hide();}},
                    {xtype: 'spacer'},
                    {iconMask: true, iconCls: 'refresh', handler:function(){Reversi.start(Reversi.getMode());}}
                ]
            }),

            // タブの設定
            tabBar: {
                dock:  'bottom',
                ui:    'light',
                layout: {pack:'center'}
            },

            // タブの切り替え方法(スライド)
            cardSwitchAnimation: {
                type: 'slide'
            },

            // 各ページの設定
            items: [
                {iconCls:'favorites', html:'<div style="width:100%; text-align:center;"><canvas id="canvas" width="320" height="320"></canvas><div id="reversiInfo" style="font-family: メイリオ;width:320px;height:50px; position:relative; top:-5px; background-color:#000000; margin-left:auto;margin-right:auto; padding: 13px 0 0 0; text-shadow:0 0 2px #9ff,0 0 4px #622,0 0 6px #3ff,0 0 8px #0ff,0 0 10px #9cc,0 0 12px #099,0 0 14px #066,0 0 16px #033; font-weight:bold;"></div></div>'},
                {
                    iconCls:'settings',
                    layout: {
                        type: 'vbox',
                        pick: 'center'
                    },
                    defaults: {
                        xtype: 'button',
                    },
		    html: '<div style="width:100%; text-align:center; padding: 50px 0 0 0; text-shadow:0 -1px 0 #fff, 1px 0 0 #fff, 0 1px 0 #fff, -1px 0 0 #fff, 0 -2px 0 #fff, 2px 0 0 #fff, 0 2px 0 #fff, -2px 0 0 #fff, 0 -3px 10px #333, 3px 0 10px #333, 0 3px 10px #333, -3px 0 10px #333; font-size:25px; font-weight: bold;">リバーシ盤の色設定</div>',
                    items:[
                        {id:'colorBtn1', text:'<span style="color:#008800">GREEN</span>',  width:250, height:30, handler:function(){ Reversi.setBoardColor('#008800'); }},
                        {id:'colorBtn2', text:'<span style="color:#808080">GRAY</span>',   width:250, height:30, handler:function(){ Reversi.setBoardColor('#808080'); }},
                        {id:'colorBtn3', text:'<span style="color:#FF00FF">PINK</span>',   width:250, height:30, handler:function(){ Reversi.setBoardColor('#FF00FF'); }},
                        {id:'colorBtn4', text:'<span style="color:#0000FF">BLUE</span>',   width:250, height:30, handler:function(){ Reversi.setBoardColor('#0000FF'); }},
                        {id:'colorBtn5', text:'<span style="color:#FF6600">ORANGE</span>', width:250, height:30, handler:function(){ Reversi.setBoardColor('#FF6600'); }}
                    ],
                },
		{iconCls:'info', scroll:'vertical', html:'<div style="width:100%; text-shadow:1px 1px #999; padding: 15px 15px 15px 15px; font-family: Comic Sans MS;">これはMdN発行の書籍「HTML5ではじめるアプリ制作の手引き」のサンプルアプリケーション用に作成されたJavaScriptアプリケーションです。<div style="height:100px;"><div style="float:left; width:50%; text-align:center; padding:70px 0 0 0;"><a href="http://b.hatena.ne.jp/add?url=' + encodeURIComponent(document.URL) + '&title=' + encodeURI('HTML5ではじめるアプリ制作の手引き「JavaScriptリバーシ」') + '"><img src="img/hatena.png"></a></div><div style="float:left; width:50%; text-align:center; padding:75px 0 0 0;"><a href="http://twitter.com/intent/tweet?text=' + encodeURI('HTML5ではじめるアプリ制作の手引き「JavaScriptリバーシ」') + '&url=' + encodeURIComponent(document.URL) + '"><img src="img/tweet.png"></a></div></div><div style="position:relative; top:70px; clear:both;">〜作った人〜<br>Toru Omura</div></div>'}
            ]

        });

        // オーバーレイ
        var overlay = new Ext.Panel({
            floating: true,
            modal:    true,
            centered: false,
            width:    260,
            height:   150,
            scroll:   'vertical',
            html:     '<div style="text-align:center">ブックマークメニューの<br>「ホーム画面に追加」<br>を選択後ホーム画面<br>からご利用ください。<br><span style="color:#FF0000">↓</span></div>',
            cls:      'htmlcontent',
	    centered: true,
            styleHtmlContent: true
        });

        // 初期状態ではパネル2を隠して、パネル1を表示する。
        panel2.hide();
        panel1.show();

        // iPhoneならフルスクリーンモードへの誘導
        if ( navigator.userAgent.indexOf('iPhone') != -1 ) {
            if ( !window.navigator.standalone )
		        overlay.show();
        // SP以外からアクセスされたら推奨環境メッセージを出す
        } else if ( (navigator.userAgent.indexOf('iPhone') == -1) && (navigator.userAgent.indexOf('Android') == -1) ) {
            Ext.Msg.alert('注意', '本アプリはiPhone/Androidケータイからの利用を推奨します。');
	}

    }
});
