/**
 * AnnounceParam.java
 * 
 * @Author
 *   D-freak
 */

package wiz.project.janbot.game;

import java.util.EnumSet;



/**
 * 実況パラメータ (immutable)
 */
final class AnnounceParam {
    
    // TODO 手抜き実装を直す
    
    /**
     * コンストラクタ
     */
    public AnnounceParam(final Player player) {
        _player = player;
    }
    
    /**
     * コンストラクタ
     */
    public AnnounceParam(final Player player, final AnnounceFlag flag) {
        _player = player;
        _flagSet = EnumSet.of(flag);
    }
    
    /**
     * コンストラクタ
     */
    public AnnounceParam(final Player player, final EnumSet<AnnounceFlag> flagSet) {
        _player = player;
        _flagSet = flagSet;
    }
    
    
    
    /**
     * 実況フラグ持っているか
     */
    public boolean hasFlag(final AnnounceFlag source) {
        return _flagSet.contains(source);
    }
    
    /**
     * プレイヤーを取得
     */
    public Player getPlayer() {
        return _player;
    }
    
    
    
    /**
     * トリガーとなったプレイヤー
     */
    private Player _player = null;
    
    /**
     * 実況フラグ
     */
    private EnumSet<AnnounceFlag> _flagSet = null;
    
}

