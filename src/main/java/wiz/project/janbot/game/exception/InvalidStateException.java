/**
 * InvalidStateException.java
 * 
 * @Author
 *   D-freak
 */

package wiz.project.janbot.game.exception;



/**
 * 不正入力例外
 */
public class InvalidStateException extends JanException {
    
    /**
     * コンストラクタ
     */
    public InvalidStateException() {
        super("[InvalidStateException]");
    }
    
    /**
     * コンストラクタ
     * 
     * @param message 例外メッセージ。
     */
    public InvalidStateException(final String message) {
        super(message);
    }
    
    /**
     * コンストラクタ
     * 
     * @param e 例外オブジェクト。
     */
    public InvalidStateException(final Throwable e) {
        super(e);
    }
    
    /**
     * コンストラクタ
     * 
     * @param message 例外メッセージ。
     * @param e 例外オブジェクト。
     */
    public InvalidStateException(final String message, final Throwable e) {
        super(message, e);
    }
    
    
    
    /**
     * シリアルバージョン
     */
    private static final long serialVersionUID = 1L;
    
}

