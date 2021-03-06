/**
 * GameMasterTest.java
 * 
 * @Author
 *   D-freak
 */

package wiz.project.janbot.game;

import static org.junit.Assert.*;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import wiz.project.janbot.game.exception.JanException;



/**
 * GameMasterのテスト
 */
public final class GameMasterTest {
    
    /**
     * コンストラクタ
     */
    public GameMasterTest() {
    }
    
    
    
    /**
     * メソッド開始時の処理
     */
    @Before
    public void setUp() {
        GameMaster.getInstance().clear();
    }
    
    
    
    /**
     * getInstance() のテスト
     * 
     * @type 正常系。
     */
    @Test
    public void testGetInstance_Normal() {
        Assert.assertNotNull(GameMaster.getInstance());
    }
    
    /**
     * getStatus() のテスト
     * 
     * @type 正常系。
     * @note 初期状態。
     */
    @Test
    public void testGetStatus_Normal() {
        final GameStatus result = GameMaster.getInstance().getStatus();
        Assert.assertEquals(GameStatus.CLOSE, result);
    }
    
    /**
     * onCall() のテスト
     * 
     * @type 正常系。
     */
    @Test
    public void testOnCall_Normal_Pon() throws JanException {
        final GameStatus status = GameStatus.IDLE_CALL;
        final String playerName = TEST_PLAYER_NAME;
        final CallType type = CallType.PON;
        
        setStatus(status);
        entry(playerName);
        
        GameMaster.getInstance().onCall(playerName, type);
    }
    
    /**
     * onDiscard() のテスト
     * 
     * @type 正常系。
     */
    @Test
    public void testOnDiscard_Normal() throws JanException {
        // TODO いいテスト方法が無いだろうか...
        
        final String playerName = TEST_PLAYER_NAME;
        
        GameMaster.getInstance().onStart();
        GameMaster.getInstance().onEntry(TEST_PLAYER_NAME_LIST);
        
        try {
            GameMaster.getInstance().onDiscard(playerName);
            Assert.fail("Expect: IllegalArgumentException");
        }
        catch (final IllegalArgumentException e) {}
    }
    
    /**
     * onDiscard() のテスト
     * 
     * @type 異常系。
     * @note ゲーム未開始。
     */
    @Test
    public void testOnDiscard_Error_GameNotStared() throws JanException {
        final String playerName = TEST_PLAYER_NAME;
        
        try {
            GameMaster.getInstance().onDiscard(playerName);
            Assert.fail("Expect: IllegalArgumentException");
        }
        catch (final IllegalArgumentException e) {}
    }
    
    /**
     * onEnd() のテスト
     * 
     * @type 異常系。
     * @note ゲーム未開始。
     */
    @Test
    public void testOnEnd_Error_GameNotStared() {
        GameMaster.getInstance().onEnd();
    }
    
    /**
     * onEntry() のテスト
     * 
     * @type 正常系。
     */
    @Test
    public void testOnEntry_Normal() throws JanException {
        final List<String> playerNameList = TEST_PLAYER_NAME_LIST;
        
        GameMaster.getInstance().onStart();
        
        GameMaster.getInstance().onEntry(playerNameList);
    }
    
    /**
     * onHelpOpen() のテスト
     */
    @Test
    public void testOnHelpOpen() {
        fail("まだ実装されていません");
    }
    
    /**
     * onHelpTalk() のテスト
     */
    @Test
    public void testOnHelpTalk() {
        fail("まだ実装されていません");
    }
    
    /**
     * onStart() のテスト
     */
    @Test
    public void testOnStart() {
        fail("まだ実装されていません");
    }
    
    
    
    /**
     * プレイヤーエントリ
     * 
     * @param playerName プレイヤー名。
     */
    public void entry(final String playerName) {
        try {
            final Field field = GameMaster.getInstance().getClass().getDeclaredField("_playerNameList");
            field.setAccessible(true);
            final List<String> playerNameList = (List<String>)field.get(GameMaster.getInstance());
            playerNameList.add(playerName);
        }
        catch (final SecurityException | IllegalAccessException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * フィールドに値を設定
     * 
     * @param value 設定値。
     */
    public void setStatus(final GameStatus value) {
        try {
            final Field field = GameMaster.getInstance().getClass().getDeclaredField("_status");
            field.setAccessible(true);
            field.set(GameMaster.getInstance(), value);
        }
        catch (final SecurityException | IllegalAccessException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }
    
    
    
    /**
     * テスト用プレイヤー名
     */
    private static final String TEST_PLAYER_NAME = "testerA";
    
    /**
     * テスト用プレイヤー名リスト
     */
    private static final List<String> TEST_PLAYER_NAME_LIST =
        Collections.unmodifiableList(Arrays.asList(TEST_PLAYER_NAME, "testerB", "testerC", "testerD"));
    
}

