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
import java.util.List;

import wiz.project.ircbot.IRCBOT;
import wiz.project.jan.JanPai;
import wiz.project.janbot.game.exception.InvalidInputException;
import wiz.project.janbot.game.exception.InvalidStateException;
import wiz.project.janbot.game.exception.JanException;



/**
 * ゲーム管理
 */
public final class GameMaster {
    
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
            throw new NullPointerException("Player name is empty.");
        }
        if (!_playerNameList.contains(playerName)) {
            throw new IllegalArgumentException("Player is not entry : " + playerName);
        }
        
        synchronized (_STATUS_LOCK) {
            if (!_status.isIdle()) {
                throw new InvalidStateException("--- Already started ---");
            }
        }
        
        // TODO ツモ切り
        // コントローラでやる予定
        
        
        
        // 以降はダミー処理
        final String activePlayer = _playerNameList.get(_activePlayerIndex++);
        if (_activePlayerIndex >= _playerNameList.size()) {
            _activePlayerIndex = 0;
        }
        final List<String> list = Arrays.asList(activePlayer + " のターン！", "(色々未実装なので、このトーク画面で", " 「jan d」とだけ発言してください)");
        IRCBOT.getInstance().talk(activePlayer, list);
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
        
        // TODO 確認後に消したい
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
            throw new NullPointerException("Player name list is empty.");
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
        
        // TODO 席決め
        // TODO 山積み
        // TODO 配牌
        // 全部コントローラでやる予定
        
        synchronized (_STATUS_LOCK) {
            _status = GameStatus.IDLE;
        }
        
        // TODO 第一ツモ
        // コントローラでやる予定
        
        
        
        // 以降はダミー処理
        final String activePlayer = _playerNameList.get(_activePlayerIndex++);
        if (_activePlayerIndex >= _playerNameList.size()) {
            _activePlayerIndex = 0;
        }
        final List<String> list = Arrays.asList(activePlayer + " のターン！", "(色々未実装なので、このトーク画面で", " 「jan d」とだけ発言してください)");
        IRCBOT.getInstance().talk(activePlayer, list);
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
            throw new NullPointerException("Player name is empty.");
        }
        if (!_playerNameList.contains(playerName)) {
            throw new IllegalArgumentException("Player is not entry : " + playerName);
        }
        
        // TODO 内部状態によって表示内容を変えたい
        final List<String> messageList = Arrays.asList("ダミー実装につき何もできません");
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
        IRCBOT.getInstance().println("ex.) jan entry Mr.A Mr.B Mr.C");
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
     * 自分自身のインスタンス
     */
    private static final GameMaster INSTANCE = new GameMaster();
    
    
    
    /**
     * ロックオブジェクト (ゲームの状態)
     */
    private final Object _STATUS_LOCK = new Object();
    
    
    
    /**
     * ゲームの状態
     */
    private GameStatus _status = GameStatus.CLOSE;
    
    /**
     * 参加プレイヤーリスト
     */
    private final List<String> _playerNameList = Collections.synchronizedList(new ArrayList<String>());
    
    
    
    /**
     * ダミー処理用のインデックス (後で消す)
     */
    private int _activePlayerIndex = 0;
    
}

