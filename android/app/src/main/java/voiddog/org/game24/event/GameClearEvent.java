package voiddog.org.game24.event;

import voiddog.org.game24.data.GameMode;

/**
 * 游戏解除事件
 * Created by Dog on 2015/9/11.
 */
public class GameClearEvent {
    public GameMode gameMode;   //游戏模式
    public int score;           //分数

    public GameClearEvent(GameMode gameMode, int score){
        this.gameMode = gameMode;
        this.score = score;
    }
}
