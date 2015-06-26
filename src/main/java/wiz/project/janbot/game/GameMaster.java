/**
 * GameMaster.java
 * 
 * @Author
 *   D-freak
 */

package wiz.project.janbot.game;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import wiz.project.ircbot.IRCBOT;
import wiz.project.jan.JanPai;
import wiz.project.janbot.game.exception.InvalidInputException;
import wiz.project.janbot.game.exception.InvalidStateException;
import wiz.project.janbot.game.exception.JanException;



/**
 * ゲーム管理
 */
public final class GameMaster implements Observer {
    
    /**
     * コンストラクタを自分自身に限定許可
     */
    private GameMaster() {
    }
    
    
    
    /**
     * インスタンスを取得
     * 
     * @return インスタンス。
     */
    public static GameMaster getInstance() {
        return INSTANCE;
    }
    
    
    
    /**
     * 内部状態を初期化
     */
    public void clear() {
        _playerNameList.clear();
        _callBuf.clear();
        
        synchronized (_JAN_INFO_LOCK) {
            _janInfo = new JanInfo();
        }
        synchronized (_STATUS_LOCK) {
            _status = GameStatus.CLOSE;
        }
    }
    
    /**
     * ゲームの状態を取得
     * 
     * @return ゲームの状態。
     */
    public GameStatus getStatus() {
        synchronized (_STATUS_LOCK) {
            return _status;
        }
    }
    
    /**
     * ツモ和了処理
     * 
     * @param playerName プレイヤー名。
     * @throws JanException ゲーム処理エラー。
     */
    public void onCompleteTsumo(final String playerName) throws JanException {
        if (playerName == null) {
            throw new NullPointerException("Player name is null.");
        }
        if (playerName.isEmpty()) {
            throw new IllegalArgumentException("Player name is empty.");
        }
        
        synchronized (_STATUS_LOCK) {
            if (!_status.isIdleDiscard()) {
                // 入力待機状態ではない場合、コマンド実行を無視
                return;
            }
        }
        
        synchronized (_JAN_INFO_LOCK) {
            if (!playerName.equals(_janInfo.getActivePlayer().getName())) {
                // アクティブ状態ではない場合、コマンド実行を無視
                return;
            }
            
            final JanController controller = createJanController();
            controller.completeTsumo(_janInfo);
        }
    }
    
    /**
     * 鳴き処理 (パス)
     * 
     * @param playerName プレイヤー名。
     * @throws JanException ゲーム処理例外。
     */
    public void onCall(final String playerName) throws JanException {
        if (playerName == null) {
            throw new NullPointerException("Player name is null.");
        }
        if (playerName.isEmpty()) {
            throw new IllegalArgumentException("Player name is empty.");
        }
        if (!_playerNameList.contains(playerName)) {
            throw new IllegalArgumentException("Player is not entry : " + playerName);
        }
        
        onCall(new CallInfo(playerName));
    }
    
    /**
     * 鳴き処理 (牌指定無し)
     * 
     * @param playerName プレイヤー名。
     * @param type 鳴きの種類。
     * @throws JanException ゲーム処理例外。
     */
    public void onCall(final String playerName, final CallType type) throws JanException {
        if (playerName == null) {
            throw new NullPointerException("Player name is null.");
        }
        if (playerName.isEmpty()) {
            throw new IllegalArgumentException("Player name is empty.");
        }
        if (!_playerNameList.contains(playerName)) {
            throw new IllegalArgumentException("Player is not entry : " + playerName);
        }
        if (type == null) {
            throw new NullPointerException("Call type is null.");
        }
        
        onCall(new CallInfo(playerName, type));
    }
    
    /**
     * 鳴き処理 (牌指定あり)
     * 
     * @param playerName プレイヤー名。
     * @param type 鳴きの種類。
     * @param target 対象牌。
     * @throws JanException ゲーム処理例外。
     */
    public void onCall(final String playerName, final CallType type, final String target) throws JanException {
        if (playerName == null) {
            throw new NullPointerException("Player name is null.");
        }
        if (playerName.isEmpty()) {
            throw new IllegalArgumentException("Player name is empty.");
        }
        if (!_playerNameList.contains(playerName)) {
            throw new IllegalArgumentException("Player is not entry : " + playerName);
        }
        if (type == null) {
            throw new NullPointerException("Call type is null.");
        }
        if (target == null) {
            throw new NullPointerException("Target janpai is null.");
        }
        if (target.isEmpty()) {
            throw new IllegalArgumentException("Target janpai is empty.");
        }
        
        onCall(new CallInfo(playerName, type, convertStringToJanPai(target)));
    }
    
    /**
     * 打牌処理 (ツモ切り)
     * 
     * @param playerName プレイヤー名。
     * @throws JanException ゲーム処理例外。
     */
    public void onDiscard(final String playerName) throws JanException {
        if (playerName == null) {
            throw new NullPointerException("Player name is null.");
        }
        if (playerName.isEmpty()) {
            throw new IllegalArgumentException("Player name is empty.");
        }
        if (!_playerNameList.contains(playerName)) {
            throw new IllegalArgumentException("Player is not entry : " + playerName);
        }
        
        synchronized (_STATUS_LOCK) {
            if (!_status.isIdleDiscard()) {
                // 入力待機状態ではない場合、コマンド実行を無視
                return;
            }
        }
        
        synchronized (_JAN_INFO_LOCK) {
            if (!playerName.equals(_janInfo.getActivePlayer().getName())) {
                // アクティブ状態ではない場合、コマンド実行を無視
                return;
            }
            
            final JanController controller = createJanController();
            controller.discard(_janInfo);
        }
    }
    
    /**
     * 打牌処理 (手出し)
     * 
     * @param playerName プレイヤー名。
     * @param target 牌の指定。
     * @throws JanException ゲーム処理例外。
     */
    public void onDiscard(final String playerName, final String target) throws JanException {
        if (playerName == null) {
            throw new NullPointerException("Player name is null.");
        }
        if (playerName.isEmpty()) {
            throw new IllegalArgumentException("Player name is empty.");
        }
        if (target == null) {
            throw new NullPointerException("Discard target is null.");
        }
        if (target.isEmpty()) {
            throw new IllegalArgumentException("Discard target is empty.");
        }
        if (!_playerNameList.contains(playerName)) {
            throw new IllegalArgumentException("Player is not entry : " + playerName);
        }
        
        synchronized (_STATUS_LOCK) {
            if (!_status.isIdleDiscard()) {
                // 入力待機状態ではない場合、コマンド実行を無視
                return;
            }
        }
        
        synchronized (_JAN_INFO_LOCK) {
            if (!playerName.equals(_janInfo.getActivePlayer().getName())) {
                // アクティブ状態ではない場合、コマンド実行を無視
                return;
            }
            
            final JanController controller = createJanController();
            final JanPai pai = convertStringToJanPai(target);
            controller.discard(_janInfo, pai);
        }
    }
    
    /**
     * ゲーム終了処理
     */
    public void onEnd() {
        synchronized (_STATUS_LOCK) {
            if (_status.isClose()) {
                return;
            }
        }
        
        // TODO プレイヤーに確認後に消したい
        clear();
        IRCBOT.getInstance().println("--- 終了 ---");
    }
    
    /**
     * 参加プレイヤー登録処理
     * 
     * @param playerNameList プレイヤー名のリスト。
     * @throws JanException ゲーム処理例外。
     */
    public void onEntry(final List<String> playerNameList) throws JanException {
        if (playerNameList == null) {
            throw new NullPointerException("Player name list is null.");
        }
        if (playerNameList.isEmpty()) {
            throw new IllegalArgumentException("Player name list is empty.");
        }
        if (playerNameList.size() == 1) {
            throw new IllegalArgumentException("ぼっち");
        }
        
        synchronized (_STATUS_LOCK) {
            if (_status.isClose()) {
                throw new InvalidStateException("--- Not started ---");
            }
            if (!_status.isEntryable()) {
                throw new InvalidStateException("--- Already started ---");
            }
        }
        
        for (final String playerName : playerNameList) {
            if (!IRCBOT.getInstance().exists(playerName)) {
                // 存在しないプレイヤーが指定された
                throw new InvalidInputException("Player is not found : " + playerName);
            }
        }
        _playerNameList.addAll(playerNameList);
        
        synchronized (_STATUS_LOCK) {
            _status = GameStatus.IDLE_DISCARD;
        }
        
        synchronized (_JAN_INFO_LOCK) {
            _janInfo.addObserver(this);
            _janInfo.addObserver(new OpenAnnouncer());
            _janInfo.addObserver(new TalkAnnouncer());
            
            final JanController controller = createJanController();
            controller.startGame(_janInfo, playerNameList);
            
            controller.startRound(_janInfo);
        }
    }
    
    /**
     * ヘルプ表示処理 (オープン)
     */
    public void onHelpOpen() {
        // TODO 内部状態によって表示内容を変えたい
        final List<String> messageList = Arrays.asList("s：開始   e：強制終了");
        IRCBOT.getInstance().println(messageList);
    }
    
    /**
     * ヘルプ表示処理 (トーク)
     * 
     * @param playerName プレイヤー名。
     */
    public void onHelpTalk(final String playerName) {
        if (playerName == null) {
            throw new NullPointerException("Player name is null.");
        }
        if (playerName.isEmpty()) {
            throw new IllegalArgumentException("Player name is empty.");
        }
        if (!_playerNameList.contains(playerName)) {
            throw new IllegalArgumentException("Player is not entry : " + playerName);
        }
        
        // TODO 内部状態によって表示内容を変えたい
        final List<String> messageList = Arrays.asList("適当にどうぞ");
        IRCBOT.getInstance().talk(playerName, messageList);
    }
    
    /**
     * ゲーム開始処理
     * 
     * @throws JanException ゲーム処理例外。
     */
    public void onStart() throws JanException {
        synchronized (_STATUS_LOCK) {
            if (!_status.isClose()) {
                throw new InvalidStateException("--- Already started ---");
            }
            _status = GameStatus.PLAYER_ENTRY;
        }
        
        IRCBOT.getInstance().println("--- 参加プレイヤーを登録してください ---");
        IRCBOT.getInstance().println("----- IRCで現在使用しているニックネームで登録すること");
        IRCBOT.getInstance().println("----- 区切り文字には半角スペースを使用すること");
        IRCBOT.getInstance().println("ex.) jan entry  Mr.A  Mr.B  Mr.C  Mr.D");
    }
    
    /**
     * 状態更新時の処理
     * 
     * @param target 監視対象オブジェクト。
     * @param p 更新通知パラメータ。
     */
    public void update(final Observable target, final Object p) {
        if (!(target instanceof JanInfo)) {
            return;
        }
        if (!(p instanceof AnnounceParam)) {
            return;
        }
        
        final AnnounceParam param = (AnnounceParam)p;
        
        if (param.hasFlag(AnnounceFlag.CONFIRM_CALL)) {
            // 鳴き確認状態に遷移
            
            // TODO ちゃんと書けたら解禁
            // 全員の鳴き確認結果が出揃うまで待つにはどうすれば良いか...
            
//          _status = GameStatus.IDLE_CALL;
        }
        else if (param.hasFlag(AnnounceFlag.COMPLETE_RON) ||
                 param.hasFlag(AnnounceFlag.COMPLETE_TSUMO) ||
                 param.hasFlag(AnnounceFlag.GAME_OVER)) {
            // TODO 次局開始確認
            
            // 直接次局に行くと _janInfo に対するデッドロックになる気がするので注意
            // ユーザ操作(jan next とか)を待って次局に行くようにすれば回避できるので最初はそれ
        }
    }
    
    
    
    /**
     * 文字列を牌に変換
     * 
     * @param source 変換元。
     * @return 変換結果。
     * @throws InvalidInputException 不正な入力。
     */
    private JanPai convertStringToJanPai(final String source) throws InvalidInputException {
        switch (source) {
        case "1m":
            return JanPai.MAN_1;
        case "2m":
            return JanPai.MAN_2;
        case "3m":
            return JanPai.MAN_3;
        case "4m":
            return JanPai.MAN_4;
        case "5m":
            return JanPai.MAN_5;
        case "6m":
            return JanPai.MAN_6;
        case "7m":
            return JanPai.MAN_7;
        case "8m":
            return JanPai.MAN_8;
        case "9m":
            return JanPai.MAN_9;
        case "1p":
            return JanPai.PIN_1;
        case "2p":
            return JanPai.PIN_2;
        case "3p":
            return JanPai.PIN_3;
        case "4p":
            return JanPai.PIN_4;
        case "5p":
            return JanPai.PIN_5;
        case "6p":
            return JanPai.PIN_6;
        case "7p":
            return JanPai.PIN_7;
        case "8p":
            return JanPai.PIN_8;
        case "9p":
            return JanPai.PIN_9;
        case "1s":
            return JanPai.SOU_1;
        case "2s":
            return JanPai.SOU_2;
        case "3s":
            return JanPai.SOU_3;
        case "4s":
            return JanPai.SOU_4;
        case "5s":
            return JanPai.SOU_5;
        case "6s":
            return JanPai.SOU_6;
        case "7s":
            return JanPai.SOU_7;
        case "8s":
            return JanPai.SOU_8;
        case "9s":
            return JanPai.SOU_9;
        case "東":
        case "ton":
        case "dong":
            return JanPai.TON;
        case "南":
        case "nan":
            return JanPai.NAN;
        case "西":
        case "sha":
        case "sya":
        case "xi":
            return JanPai.SHA;
        case "北":
        case "pei":
        case "pe":
        case "bei":
            return JanPai.PEI;
        case "白":
        case "haku":
        case "bai":
            return JanPai.HAKU;
        case "發":
        case "hatu":
        case "hatsu":
        case "fa":
            return JanPai.HATU;
        case "中":
        case "chun":
        case "ch":
        case "zhong":
            return JanPai.CHUN;
        default:
            throw new InvalidInputException("Invalid jan pai - " + source);
        }
    }
    
    /**
     * 麻雀コントローラを生成
     * 
     * @return 麻雀コントローラ。
     */
    private JanController createJanController() {
        return new VSChmJanController();
    }
    
    /**
     * 鳴き処理
     * 
     * @param info 鳴き情報。
     * @throws JanException ゲーム処理例外。
     */
    private void onCall(final CallInfo info) throws JanException {
        synchronized (_STATUS_LOCK) {
            if (!_status.isIdleCall()) {
                // 入力待機状態ではない場合、コマンド実行を無視
                return;
            }
        }
        
        final String playerName = info.getPlayerName();
        if (_callBuf.containsKey(playerName)) {
            // 一度入力した内容は覆せない
            return;
        }
        
        _callBuf.put(playerName, info);
        
        if (!_callBuf.keySet().containsAll(_playerNameList)) {
            // 全員の入力が終わるまでは先の処理に進まない
            return;
        }
        
        // 全員の入力が終わったので、最も優先度の高い処理を判定
        // TODO 優先度判定
        
        // 鳴き待機状態を解除
        _status = GameStatus.IDLE_DISCARD;
        _callBuf.clear();
        
        // 最も優先度の高かった処理を実行
//        synchronized (_JAN_INFO_LOCK) {
//            final JanController controller = createJanController();
//            controller.hoge(_janInfo);
//        }
        
        // TODO ↑ここのコントローラ内の処理でエラーになったらどう復旧する？
    }
    
    
    
    /**
     * 自分自身のインスタンス
     */
    private static final GameMaster INSTANCE = new GameMaster();
    
    
    
    /**
     * ロックオブジェクト (ゲームの状態)
     */
    private final Object _STATUS_LOCK = new Object();
    
    /**
     * ロックオブジェクト (麻雀ゲーム情報)
     */
    private final Object _JAN_INFO_LOCK = new Object();
    
    
    
    /**
     * ゲームの状態
     */
    private GameStatus _status = GameStatus.CLOSE;
    
    /**
     * 参加プレイヤーリスト
     */
    private final List<String> _playerNameList = Collections.synchronizedList(new ArrayList<String>());
    
    /**
     * 麻雀ゲーム情報
     */
    private JanInfo _janInfo = new JanInfo();
    
    /**
     * 確認結果バッファ
     */
    private final Map<String, CallInfo> _callBuf = Collections.synchronizedMap(new HashMap<String, CallInfo>());
    
}

