/**
 * TalkAnnouncer.java
 * 
 * @Author
 *   D-freak
 */

package wiz.project.janbot.game;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

import wiz.project.ircbot.IRCBOT;
import wiz.project.jan.Wind;



/**
 * 個別実況
 */
public class TalkAnnouncer extends AbstractAnnouncer {
    
    /**
     * コンストラクタ
     */
    public TalkAnnouncer() {
    }
    
    
    
    /**
     * 状況更新時の処理
     * 
     * @param target 監視対象。
     * @param p 更新パラメータ。
     */
    public void update(final Observable target, final Object p) {
        if (!(target instanceof JanInfo)) {
            return;
        }
        if (!(p instanceof AnnounceParam)) {
            return;
        }
        
        final JanInfo info = (JanInfo)target;
        final AnnounceParam param = (AnnounceParam)p;
        final Player player = param.getPlayer();
        final Wind playerWind = info.getWind(player.getName());
        
        final List<String> messageList = new ArrayList<>();
        if (param.hasFlag(AnnounceFlag.FIELD_TALK)) {
            messageList.add(convertFieldToString(info, playerWind));
        }
        if (param.hasFlag(AnnounceFlag.HAND_TALK)) {
            messageList.add(convertHandToString(info, param));
        }
        
        // TODO 鳴き確認メッセージを出す
        
        if (param.hasFlag(AnnounceFlag.COMPLETE_RON)) {
            messageList.add("---- ロン和了 ----");
        }
        else if (param.hasFlag(AnnounceFlag.COMPLETE_TSUMO)) {
            messageList.add("---- ツモ和了 ----");
        }
        else if (param.hasFlag(AnnounceFlag.GAME_OVER)) {
            messageList.add("---- 流局 ----");
        }
        
        IRCBOT.getInstance().talk(player.getName(), messageList);
    }
    
}
