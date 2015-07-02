/**
 * MessageListener.java
 * 
 * @Author
 *   D-freak
 */

package wiz.project.janbot;

import java.util.Arrays;
import java.util.List;

import org.pircbotx.PircBotX;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.events.PrivateMessageEvent;

import wiz.project.ircbot.IRCBOT;
import wiz.project.janbot.game.CallType;
import wiz.project.janbot.game.GameMaster;
import wiz.project.janbot.game.exception.BoneheadException;
import wiz.project.janbot.game.exception.JanException;



/**
 * メッセージ受付
 * 
 * @param <T> PircBoxT、またはその継承クラス。
 */
final class MessageListener<T extends PircBotX> extends ListenerAdapter<T> {
    
    /**
     * コンストラクタ
     */
    public MessageListener() {
    }
    
    
    
    /**
     * メッセージ受信時の処理
     * 
     * @param event イベント情報。
     * @throws InterruptedException 処理に失敗。
     */
    @Override
    public void onMessage(final MessageEvent<T> event) throws Exception {
        if (event == null) {
            throw new NullPointerException("Event information is null.");
        }
        
        // メッセージ解析
        final String playerName = event.getUser().getNick();
        try {
            final String message = event.getMessage().replaceAll(" +", " ");  // 連続する半角スペースを削除
            if (!message.startsWith(COMMAND_PREFIX)) {
                // 「jan hoge」以外全て無視
                return;
            }
            
            final List<String> commandList = Arrays.asList(message.substring(COMMAND_PREFIX.length()).split(" "));  // 半角スペースで分解
            onCommandOpen(playerName, commandList);
        }
        catch (final BoneheadException e) {
            IRCBOT.getInstance().println("(  ´∀｀) ＜ チョンボ");
        }
        catch (final JanException e) {
            IRCBOT.getInstance().println("(  ´∀｀) ＜ " + e.getMessage());
        }
        catch (final Throwable e) {
            IRCBOT.getInstance().println("(  ´∀｀) ＜ " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * トーク受信時の処理
     * 
     * @param event イベント情報。
     * @throws Exception 処理に失敗。
     */
    @Override
    public void onPrivateMessage(final PrivateMessageEvent<T> event) throws Exception {
        if (event == null) {
            throw new NullPointerException("Event information is null.");
        }
        
        // メッセージ解析
        final String playerName = event.getUser().getNick();
        try {
            final String message = event.getMessage().replaceAll(" +", " ");  // 連続する半角スペースを削除
            if (!message.startsWith(COMMAND_PREFIX)) {
                // 「jan hoge」以外全て無視
                return;
            }
            
            final List<String> commandList = Arrays.asList(message.substring(COMMAND_PREFIX.length()).split(" "));  // 半角スペースで分解
            onCommandTalk(playerName, commandList);
        }
        catch (final BoneheadException e) {
            IRCBOT.getInstance().talk(playerName, "(  ´∀｀) ＜ チョンボ");
        }
        catch (final JanException e) {
            IRCBOT.getInstance().talk(playerName, "(  ´∀｀) ＜ " + e.getMessage());
        }
        catch (final Throwable e) {
            IRCBOT.getInstance().talk(playerName, "(  ´∀｀) ＜ " + e.getMessage());
            throw e;
        }
    }
    
    
    
    /**
     * コマンド処理 (オープン)
     * 
     * @param playerName 発言者のニックネーム。
     * @param commandList コマンドリスト。先頭要素はコマンド種類、以降はパラメータ。
     * @throws JanException 麻雀ゲーム中の例外。
     */
    private void onCommandOpen(final String playerName, final List<String> commandList) throws JanException {
        final String command = commandList.get(0);
        switch (command) {
        case "--close":
            if (commandList.size() == 1) {
                IRCBOT.getInstance().println("(  ；∀；)");
                IRCBOT.getInstance().disconnect();
            }
            break;
        case "s":
            if (commandList.size() == 1) {
                GameMaster.getInstance().onStart();
            }
            break;
        case "e":
            if (commandList.size() == 1) {
                GameMaster.getInstance().onEnd();
            }
            break;
        case "entry":
            GameMaster.getInstance().onEntry(commandList.subList(1, commandList.size()));
            break;
        case "h":
        case "help":
            if (commandList.size() == 1) {
                GameMaster.getInstance().onHelpOpen();
            }
            break;
        default:
            // 不明なコマンドは全て無視
            break;
        }
    }
    
    /**
     * コマンド処理 (トーク)
     * 
     * @param playerName 発言者のニックネーム。
     * @param commandList コマンドリスト。先頭要素はコマンド種類、以降はパラメータ。
     * @throws JanException 麻雀ゲーム中の例外。
     */
    private void onCommandTalk(final String playerName, final List<String> commandList) throws JanException {
        final String command = commandList.get(0);
        switch (command) {
        case "d":
            if (!GameMaster.getInstance().getStatus().isIdleCall()) {
                switch (commandList.size()) {
                case 1:
                    GameMaster.getInstance().onDiscard(playerName);
                    break;
                case 2:
                    GameMaster.getInstance().onDiscard(playerName, commandList.get(1));
                    break;
                default:
                    // 不正な指定を無視
                    break;
                }
            }
            else {
                if (commandList.size() == 1) {
                    GameMaster.getInstance().onCall(playerName);
                }
            }
            break;
        case "tsumo":
            if (commandList.size() == 1) {
                GameMaster.getInstance().onCompleteTsumo(playerName);
            }
            break;
        case "ron":
            if (commandList.size() == 1) {
                GameMaster.getInstance().onCall(playerName, CallType.RON);
            }
            break;
        case "hu":
            if (commandList.size() == 1) {
                if (!GameMaster.getInstance().getStatus().isIdleCall()) {
                    GameMaster.getInstance().onCompleteTsumo(playerName);
                }
                else {
                    GameMaster.getInstance().onCall(playerName, CallType.RON);
                }
            }
        case "chi":
            if (commandList.size() == 2) {
                GameMaster.getInstance().onCall(playerName, CallType.CHI, commandList.get(1));
            }
            break;
        case "pon":
            if (commandList.size() == 1) {
                GameMaster.getInstance().onCall(playerName, CallType.PON);
            }
            break;
        case "kan":
            if (commandList.size() == 2) {
                if (!GameMaster.getInstance().getStatus().isIdleCall()) {
                    // この時点では手牌を確認できないので、加槓も KAN_DARK ルートから入る
                    GameMaster.getInstance().onCall(playerName, CallType.KAN_DARK, commandList.get(1));
                }
                else {
                    GameMaster.getInstance().onCall(playerName, CallType.KAN_LIGHT, commandList.get(1));
                }
            }
            break;
        case "h":
        case "help":
            if (commandList.size() == 1) {
                GameMaster.getInstance().onHelpTalk(playerName);
            }
            break;
        case "debug":
            if (commandList.size() == 3) {
                GameMaster.getInstance().onDebugInfo(playerName);
            }
            break;
        default:
            // 不明なコマンドは全て無視
            break;
        }
    }
    
    
    
    /**
     * BOTコマンドのプレフィックス
     */
    private static final String COMMAND_PREFIX = "jan ";
    
}

