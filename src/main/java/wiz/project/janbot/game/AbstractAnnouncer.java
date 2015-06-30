/**
 * AbstractAnnouncer.java
 * 
 * @Author
 *   D-freak
 */

package wiz.project.janbot.game;

import java.util.Collections;
import java.util.List;

import wiz.project.jan.Hand;
import wiz.project.jan.JanPai;
import wiz.project.jan.MenTsu;
import wiz.project.jan.MenTsuType;
import wiz.project.jan.Wind;



/**
 * ゲーム実況者の抽象クラス
 */
abstract class AbstractAnnouncer implements Announcer {
    
    /**
     * コンストラクタ
     */
    public AbstractAnnouncer() {
    }
    
    
    
    /**
     * 副露された雀牌を文字列に変換
     * 
     * @param pai 副露された雀牌。
     * @return 変換結果。
     */
    protected final String convertCalledJanPaiToString(final JanPai pai) {
        if (pai == null) {
            throw new NullPointerException("Source janpai is null.");
        }
        
        final StringBuilder buf = new StringBuilder();
        buf.append(COLOR_FLAG).append("14");  // 灰色
        buf.append(pai);
        buf.append(COLOR_FLAG);
        return buf.toString();
    }
    
    /**
     * 副露情報を文字列に変換
     * 
     * @param callableList 可能な鳴きリスト。
     * @param discard 捨て牌。
     * @return 変換結果。
     */
    protected final String convertCallInfoToString(final List<CallType> callableList, final JanPai discard) {
        if (callableList == null) {
            throw new NullPointerException("Callable list is null.");
        }
        if (callableList.isEmpty()) {
            throw new IllegalArgumentException("Callable list is empty.");
        }
        if (discard == null) {
            throw new NullPointerException("Discard janpai is null.");
        }
        
        final StringBuilder buf = new StringBuilder();
        buf.append(convertJanPaiToString(discard)).append(" <- ");
        if (callableList.contains(CallType.RON)) {
            buf.append("ロン可能です：  ");
        }
        else {
            buf.append("鳴けそうです：  ");
        }
        if (callableList.contains(CallType.RON)) {
            buf.append("[ロン]");
        }
        if (callableList.contains(CallType.CHI)) {
            buf.append("[チー]");
        }
        if (callableList.contains(CallType.PON)) {
            buf.append("[ポン]");
        }
        if (callableList.contains(CallType.KAN_LIGHT)) {
            buf.append("[カン]");
        }
        return buf.toString();
    }
    
    /**
     * 場情報を文字列に変換
     * 
     * @param info ゲーム情報。
     * @return 変換結果。
     */
    protected final String convertFieldToString(final JanInfo info) {
        final StringBuilder buf = new StringBuilder();
        buf.append("場風：").append(info.getFieldWind()).append("   ");
        buf.append("残り枚数：").append(info.getRemainCount());
        return buf.toString();
    }
    
    /**
     * 場情報を文字列に変換
     * 
     * @param info ゲーム情報。
     * @param playerWind 対象プレイヤーの風。
     * @return 変換結果。
     */
    protected final String convertFieldToString(final JanInfo info, final Wind playerWind) {
        final StringBuilder buf = new StringBuilder();
        buf.append("場風：").append(info.getFieldWind()).append("   ");
        buf.append("自風：").append(playerWind).append("   ");
        buf.append("残り枚数：").append(info.getRemainCount());
        return buf.toString();
    }
    
    /**
     * 副露牌を文字列に変換
     * 
     * @param hand 手牌。
     * @return 変換結果。
     */
    protected final String convertFixedMenTsuToString(final Hand hand) {
        if (hand == null) {
            throw new NullPointerException("Source hand is null.");
        }
        
        final StringBuilder buf = new StringBuilder();
        if (hand.getFixedMenTsuCount() == 0) {
            return buf.toString();
        }
        
        buf.append(" ");
        final List<MenTsu> fixedMenTsuList = hand.getFixedMenTsuList();
        Collections.reverse(fixedMenTsuList);
        for (final MenTsu fixedMenTsu : fixedMenTsuList) {
            buf.append(" ");
            final List<JanPai> sourceList = fixedMenTsu.getSource();
            if (fixedMenTsu.getMenTsuType() == MenTsuType.KAN_DARK) {
                buf.append(convertKanDarkToString(sourceList.get(0)));
            }
            else {
                buf.append(convertJanPaiToString(sourceList));
            }
        }
        return buf.toString();
    }
    
    /**
     * 手牌を文字列に変換
     * 
     * @param info ゲーム情報。
     * @param param 実況パラメータ。
     * @return 変換結果。
     */
    protected final String convertHandToString(final JanInfo info, final AnnounceParam param) {
        if (info == null) {
            throw new NullPointerException("Source info is null.");
        }
        if (param == null) {
            throw new NullPointerException("Source parameter is null.");
        }
        
        final Hand hand = info.getHand(param.getPlayer().getName());
        final StringBuilder buf = new StringBuilder();
        buf.append(convertMenzenHandToString(hand));
        if (param.hasFlag(AnnounceFlag.ACTIVE_TSUMO)) {
            buf.append(" ").append(convertJanPaiToString(info.getActiveTsumo()));
        }
        else if (param.hasFlag(AnnounceFlag.ACTIVE_DISCARD)) {
            buf.append(" ").append(convertJanPaiToString(info.getActiveDiscard()));
        }
        buf.append(convertFixedMenTsuToString(hand));
        return buf.toString();
    }
    
    /**
     * 雀牌を文字列に変換
     * 
     * @param pai 雀牌。
     * @return 変換結果。
     */
    protected final String convertJanPaiToString(final JanPai pai) {
        if (pai == null) {
            throw new NullPointerException("Source janpai is null.");
        }
        
        final StringBuilder buf = new StringBuilder();
        buf.append(COLOR_FLAG).append(getColorCode(pai));
        buf.append(pai);
        buf.append(COLOR_FLAG);
        return buf.toString();
    }
    
    /**
     * 雀牌を文字列に変換
     * 
     * @param paiList 雀牌リスト。
     * @return 変換結果。
     */
    protected final String convertJanPaiToString(final List<JanPai> paiList) {
        if (paiList == null) {
            throw new NullPointerException("Source janpai list is null.");
        }
        if (paiList.isEmpty()) {
            throw new IllegalArgumentException("Source janpai list is empty.");
        }
        
        final StringBuilder buf = new StringBuilder();
        buf.append(COLOR_FLAG).append(getColorCode(paiList.get(0)));
        for (final JanPai pai : paiList) {
            buf.append(pai);
        }
        buf.append(COLOR_FLAG);
        return buf.toString();
    }
    
    /**
     * 暗槓子を文字列に変換
     * 
     * @param pai 雀牌。
     * @return 変換結果。
     */
    protected final String convertKanDarkToString(final JanPai pai) {
        if (pai == null) {
            throw new NullPointerException("Source janpai is null.");
        }
        
        final StringBuilder buf = new StringBuilder();
        buf.append(COLOR_FLAG).append(getColorCode(pai));
        buf.append("[■]" + pai + pai + "[■]");
        buf.append(COLOR_FLAG);
        return buf.toString();
    }
    
    /**
     * 面前手牌を文字列に変換
     * 
     * @param hand 手牌。
     * @return 変換結果。
     */
    protected final String convertMenzenHandToString(final Hand hand) {
        if (hand == null) {
            throw new NullPointerException("Source hand is null.");
        }
        
        final StringBuilder buf = new StringBuilder();
        for (final JanPai pai : hand.getMenZenList()) {
            buf.append(convertJanPaiToString(pai));
        }
        return buf.toString();
    }
    
    /**
     * 捨て牌リストを文字列に変換
     * 
     * @param info ゲーム情報。
     * @param param 実況パラメータ。
     * @return 変換結果。
     */
    protected final String convertRiverToString(final JanInfo info, final AnnounceParam param) {
        if (info == null) {
            throw new NullPointerException("Source info is null.");
        }
        if (param == null) {
            throw new NullPointerException("Source parameter is null.");
        }
        
        final String playerName = param.getPlayer().getName();
        final Wind playerWind = info.getWind(playerName);
        final River river = info.getRiver(playerWind);
        final StringBuilder buf = new StringBuilder();
        int count = 1;
        int calledIndex = 0;
        buf.append(playerWind + "：" + playerName + "捨牌：");
        for (final JanPai pai : river.get()) {
            if (calledIndex < river.getCalledIndexList().size() && count == river.getCalledIndexList().get(calledIndex)) {
                buf.append(convertCalledJanPaiToString(pai));
                calledIndex++;
            }
            else {
                buf.append(convertJanPaiToString(pai));
            }
            if (count % 6 == 0) {
                buf.append("  ");
            }
            count++;
        }
        return buf.toString();
    }
    
    
    
    /**
     * 色コードを取得
     * 
     * @param pai 雀牌。
     * @return 対応する色コード。
     */
    private String getColorCode(final JanPai pai) {
        switch (pai) {
        case MAN_1:
        case MAN_2:
        case MAN_3:
        case MAN_4:
        case MAN_5:
        case MAN_6:
        case MAN_7:
        case MAN_8:
        case MAN_9:
        case CHUN:
            return "04";  // 赤
        case PIN_1:
        case PIN_2:
        case PIN_3:
        case PIN_4:
        case PIN_5:
        case PIN_6:
        case PIN_7:
        case PIN_8:
        case PIN_9:
            return "12";  // 青
        case SOU_1:
        case SOU_2:
        case SOU_3:
        case SOU_4:
        case SOU_5:
        case SOU_6:
        case SOU_7:
        case SOU_8:
        case SOU_9:
        case HATU:
            return "03";  // 緑
        case TON:
        case NAN:
        case SHA:
        case PEI:
            return "06";  // 紫
        default:
            return "01";  // 黒
        }
    }
    
    
    
    /**
     * 色付けフラグ
     */
    private static final char COLOR_FLAG = 3;
    
}

