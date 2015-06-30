/**
 * GameStatusParam.java
 * 
 * @Author
 *   D-freak
 */

package wiz.project.janbot.game;



/**
 * ゲーム状態通知パラメータ
 */
final class GameStatusParam {
    
    // TODO 手抜き実装を直す
    
    /**
     * コンストラクタ
     */
    public GameStatusParam(final Player player) {
        _player = player;
    }
    
    /**
     * コンストラクタ
     */
    public GameStatusParam(final Player player, final GameStatus status) {
        _player = player;
        _status = status;
    }
    
    
    
    /**
     * プレイヤーを取得
     */
    public Player getPlayer() {
        return _player;
    }
    
    /**
     * ゲーム状態を取得
     */
    public GameStatus getStatus() {
        return _status;
    }
    
    
    
    /**
     * トリガーとなったプレイヤー
     */
    private Player _player = null;
    
    /**
     * ゲーム状態
     */
    private GameStatus _status = null;
    
}

