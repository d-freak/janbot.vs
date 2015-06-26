/**
 * CallInfo.java
 * 
 * @Author
 *   D-freak
 */

package wiz.project.janbot.game;

import wiz.project.jan.JanPai;



/**
 * 鳴き情報
 */
final class CallInfo {
    
    // TODO 手抜き実装を直す
    
    /**
     * コンストラクタ (鳴き確認をパス)
     */
    public CallInfo(final String playerName) {
        _playerName = playerName;
    }
    
    /**
     * コンストラクタ
     */
    public CallInfo(final String playerName, final CallType type) {
        _playerName = playerName;
        _callType = type;
    }
    
    /**
     * コンストラクタ
     */
    public CallInfo(final String playerName, final CallType type, final JanPai pai) {
        _playerName = playerName;
        _callType = type;
        _targetPai = pai;
    }
    
    
    
    public CallType getCallType() {
        return _callType;
    }
    
    public String getPlayerName() {
        return _playerName;
    }
    
    public JanPai getTargetPai() {
        return _targetPai;
    }
    
    
    
    /**
     * プレイヤー名
     */
    private String _playerName = "";
    
    /**
     * 鳴きタイプ (nullならパス)
     */
    private CallType _callType = null;
    
    /**
     * 鳴き対象牌 (ポンなど入力必須のものでなければnull)
     */
    private JanPai _targetPai = JanPai.HAKU;
    
}

