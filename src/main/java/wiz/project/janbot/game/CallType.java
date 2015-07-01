/**
 * CallType.java
 * 
 * @Author
 *   D-freak
 */

package wiz.project.janbot.game;



/**
 * 鳴きタイプ
 */
public enum CallType {
    
    /**
     * ロン
     */
    RON,
    
    /**
     * チー
     */
    CHI,
    
    /**
     * ポン
     */
    PON,
    
    /**
     * 大明カン
     */
    KAN_LIGHT,
    
    /**
     * 加カン
     */
    KAN_ADD,
    
    /**
     * 暗カン
     */
    KAN_DARK;
    
    
    
    /**
     * 優先度を取得
     * 
     * @return 優先度。
     */
    public int getPriority() {
        switch (this) {
        case RON:
            return 3;
        case PON:
        case KAN_LIGHT:
            return 2;
        case CHI:
            return 1;
        default:
            return 0;
        }
    }
    
}

