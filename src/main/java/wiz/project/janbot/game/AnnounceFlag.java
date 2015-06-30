/**
 * AnnounceFlag.java
 * 
 * @Author
 *   D-freak
 */

package wiz.project.janbot.game;



/**
 * 実況フラグ
 */
public enum AnnounceFlag {
    
    /**
     * ゲーム開始
     */
    GAME_START,
    
    /**
     * ロン和了
     */
    COMPLETE_RON,
    
    /**
     * ツモ和了
     */
    COMPLETE_TSUMO,
    
    /**
     * 流局
     */
    GAME_OVER,
    
    /**
     * 打牌
     */
    DISCARD,
    
    /**
     * 鳴き確認
     */
    CONFIRM_CALL,
    
    /**
     * 誰も鳴けない
     */
    NOBODY_CALL,
    
    /**
     * ロン可能
     */
    CALLABLE_RON,
    
    /**
     * チー可能
     */
    CALLABLE_CHI,
    
    /**
     * ポン可能
     */
    CALLABLE_PON,
    
    /**
     * 大明カン可能
     */
    CALLABLE_KAN,
    
    /**
     * プレイヤーのターンが回ってきた
     */
    PLAYER_TURN,
    
    /**
     * 手牌 (オープン)
     */
    HAND_OPEN,
    
    /**
     * 手牌 (トーク)
     */
    HAND_TALK,
    
    /**
     * ツモ牌
     */
    ACTIVE_TSUMO,
    
    /**
     * 直前の捨て牌
     */
    ACTIVE_DISCARD,
    
    /**
     * 副露直後か
     */
    AFTER_CALL,
    
    /**
     * 場情報 (オープン)
     */
    FIELD_OPEN,
    
    /**
     * 場情報 (トーク)
     */
    FIELD_TALK,
    
    /**
     * 捨て牌情報
     */
    RIVER_SINGLE,
    
    /**
     * 全捨て牌情報
     */
    RIVER_ALL,
    
    /**
     * 裏ドラ
     */
    URA_DORA,
    
    /**
     * 指定牌の残り枚数
     */
    OUTS,
    
    /**
     * 指定牌の残り枚数(確認メッセージ用)
     */
    CONFIRM_OUTS,
    
    /**
     * 点数
     */
    SCORE,
    
    /**
     * 中国麻雀の実装済みの役
     */
    RELEASED_CHM_YAKU;
    
    
    
    /**
     * 副露可能か
     * 
     * @return 判定結果。
     */
    public boolean isCallable() {
        switch (this) {
        case CALLABLE_RON:
        case CALLABLE_CHI:
        case CALLABLE_PON:
        case CALLABLE_KAN:
            return true;
        default:
            return false;
        }
    }
    
    /**
     * 局が終了したか
     * 
     * @return 判定結果。
     */
    public boolean isRoundEnd() {
        switch (this) {
        case COMPLETE_RON:
        case COMPLETE_TSUMO:
        case GAME_OVER:
            return true;
        default:
            return false;
        }
    }
    
}

