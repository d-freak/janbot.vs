/**
 * OpenAnnouncer.java
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
 * オープン実況
 * 
 * 理論上オープンでいい内容は全てここに集約する。
 * ただし、IRCプレイでの利便性を考えて一部メッセージはトークに流す。
 */
public class OpenAnnouncer extends AbstractAnnouncer {
    
    /**
     * コンストラクタ
     */
    public OpenAnnouncer() {
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
        
        final List<String> openMessageList = new ArrayList<>();
        final List<String> talkMessageList = new ArrayList<>();
        if (param.hasFlag(AnnounceFlag.PLAYER_TURN)) {
            // TODO 暫定封鎖
            // jan i で確認できるようにした上で永久に潰すかもしれない
//          openMessageList.add(player.getName() + " のターン！");
        }
        if (param.hasFlag(AnnounceFlag.FIELD_OPEN)) {
            openMessageList.add(convertFieldToString(info));
        }
        if (param.hasFlag(AnnounceFlag.FIELD_TALK_ALL)) {
            for (final Wind wind : Wind.values()) {
                final String name = info.getPlayer(wind).getName();
                IRCBOT.getInstance().talk(name, convertFieldToString(info, wind));
            }
        }
        if (param.hasFlag(AnnounceFlag.RIVER_SINGLE)) {
            talkMessageList.add(convertRiverToString(info, param));
        }
        if (param.hasFlag(AnnounceFlag.HAND_OPEN)) {
            talkMessageList.add(convertHandToString(info, param.getPlayer(), param));
        }
        if (param.hasFlag(AnnounceFlag.HAND_TALK_ALL)) {
            for (final Wind wind : Wind.values()) {
                if (wind != Wind.TON) {
                    final Player to = info.getPlayer(wind);
                    IRCBOT.getInstance().talk(to.getName(), convertHandToString(info, to, param));
                }
            }
        }
        
        if (param.hasFlag(AnnounceFlag.COMPLETE_RON)) {
            openMessageList.add("---- ロン和了 ----");
            talkMessageList.add("---- ロン和了 ----");
        }
        else if (param.hasFlag(AnnounceFlag.COMPLETE_TSUMO)) {
            openMessageList.add("---- ツモ和了 ----");
            talkMessageList.add("---- ツモ和了 ----");
        }
        else if (param.hasFlag(AnnounceFlag.GAME_OVER)) {
            openMessageList.add("---- 流局 ----");
            talkMessageList.add("---- 流局 ----");
        }
        
        IRCBOT.getInstance().println(openMessageList);
        
        for (final Player receiver : info.getPlayerTable().values()) {
            if (receiver.getType() == PlayerType.HUMAN) {
                IRCBOT.getInstance().talk(receiver.getName(), talkMessageList);
            }
        }
        
        // TODO 点数表示未解禁
//      if (flagSet.contains(AnnounceFlag.SCORE)) {
//          printCompleteInfo(info, flagSet);
//      }
    }
    
    
    
    /**
     * 和了情報を出力
     * 
     * @param info ゲーム情報。
     * @param flagSet 実況フラグ。
     */
//    private void printCompleteInfo(final JanInfo info, final EnumSet<AnnounceFlag> flagSet) {
//        final ChmCompleteInfo completeInfo = info.getCompleteInfo();
//        if (completeInfo.getYakuList().isEmpty()) {
//            return;
//        }
//        Integer total = 0;
//        for (final ChmYaku yaku : completeInfo.getYakuList()) {
//            IRCBOT.getInstance().println(yaku.toString() + " : " + yaku.toStringUS() + String.valueOf(yaku.getPoint()) + "点");
//            total += yaku.getPoint();
//        }
//        if (flagSet.contains(AnnounceFlag.ACTIVE_TSUMO)) {
//            IRCBOT.getInstance().println("合計(" + total.toString() + "+8)a点");
//        }
//        else if (flagSet.contains(AnnounceFlag.ACTIVE_DISCARD)) {
//            IRCBOT.getInstance().println("合計" + total.toString() + "+8a点");
//        }
//    }
    
}

