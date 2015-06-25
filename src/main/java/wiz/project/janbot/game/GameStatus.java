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
     * ユーザ入力待機
     */
    IDLE,
    
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
        return this == IDLE;
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

