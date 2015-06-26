/**
 * GameStatus.java
 * 
 * @Author
 *   D-freak
 */

package wiz.project.janbot.game;



/**
 * ゲームの状態
 */
public enum GameStatus {
    
    /**
     * エントリー受付
     */
    PLAYER_ENTRY,
    
    /**
     * ユーザ入力遮断
     */
    BUSY,
    
    /**
     * ユーザ入力待機 (打牌待ち)
     */
    IDLE_DISCARD,
    
    /**
     * ユーザ入力待機 (鳴き確認)
     */
    IDLE_CALL,
    
    /**
     * 局が終了
     */
    END_ROUND,
    
    /**
     * 未開始
     */
    CLOSE;
    
    
    
    /**
     * 未開始か
     * 
     * @return 判定結果。
     */
    public boolean isClose() {
        return this == CLOSE;
    }
    
    /**
     * 入力可能か
     * 
     * @return 判定結果。
     */
    public boolean isIdle() {
        switch (this) {
        case IDLE_DISCARD:
        case IDLE_CALL:
            return true;
        default:
            return false;
        }
    }
    
    /**
     * 鳴き入力可能か
     * 
     * @return 判定結果。
     */
    public boolean isIdleCall() {
        return this == IDLE_CALL;
    }
    
    /**
     * 打牌入力可能か
     * 
     * @return 判定結果。
     */
    public boolean isIdleDiscard() {
        return this == IDLE_DISCARD;
    }
    
    /**
     * エントリー可能か
     * 
     * @return 判定結果。
     */
    public boolean isEntryable() {
        return this == PLAYER_ENTRY;
    }
    
}

