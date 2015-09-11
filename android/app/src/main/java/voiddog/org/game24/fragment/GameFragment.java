package voiddog.org.game24.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;

import java.util.Random;
import java.util.Stack;
import java.util.Timer;
import java.util.TimerTask;

import de.greenrobot.event.EventBus;
import voiddog.org.game24.R;
import voiddog.org.game24.data.GameMode;
import voiddog.org.game24.data.OperationEnum;
import voiddog.org.game24.data.OptionData;
import voiddog.org.game24.event.GameClearEvent;
import voiddog.org.game24.event.AskGameExitEvent;
import voiddog.org.game24.event.GameOverEvent;
import voiddog.org.game24.event.MarginItemFinishEvent;
import voiddog.org.game24.fragment.dialog.SystemNotifyDialogFragment;
import voiddog.org.game24.fragment.dialog.SystemNotifyDialogFragment_;
import voiddog.org.game24.ui.DragGroupView;
import voiddog.org.game24.ui.NumberItem;
import voiddog.org.game24.ui.TitleBar;
import voiddog.org.game24.util.CalculateAnswerHelper;
import voiddog.org.game24.util.SizeUtil;
import voiddog.org.game24.util.UIHandler;

/**
 * 游戏逻辑主题fragment
 * Created by Dog on 2015/9/8.
 */
@EFragment(R.layout.fragment_game)
public class GameFragment extends Fragment{
    //分离的距离，默认100，初始化为屏幕宽度的1/4
    private int mSeparationDis = 100;
    //游戏时间2分钟
    private final int GAME_TIME = 2*60;

    @ViewById
    DragGroupView game_view;
    @ViewById
    Button rcb_back, rcb_plus, rcb_sub, rcb_mul, rcb_div, rcb_no_anw;
    @ViewById
    TitleBar title_bar;
    @FragmentArg
    GameMode mGameMode = GameMode.Nervous;
    @FragmentArg
    int roundId = 1;

    //操作保存栈，供撤销用
    Stack<OptionData> mOperationStack = new Stack<>();
    //两个操作数
    NumberItem firstNumber, secondNumber;
    //操作
    OperationEnum mOperation = null;
    //view tag
    int viewTag = 1;
    //计时器
    Timer mTimer;
    //剩余时间
    int mLeftTime = 0;
    //是否有解
    boolean hasAnswer = true;
    //剩余球体数目
    int leftNumber = 4;
    //是否游戏结束
    boolean isGameOver = false;
    //游戏提示dialog
    SystemNotifyDialogFragment notifyDialog;

    @AfterViews
    void setupViews(){
        //延时1秒添加item
        UIHandler.sendEmptyMessageDelayed(0, 1000, new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                createAllRandomItem();
                return true;
            }
        });
        //设置头部
        setupTitle();
    }

    /**
     * 设置模式 简单模式（没有倒计时）, 休闲模式(有倒计时)
     */
    void setupTitle(){
        title_bar.setTitle(String.format("第%d局", roundId));
        if(mGameMode == GameMode.Nervous) {
            mTimer = new Timer(true);
            mLeftTime = GAME_TIME;
            title_bar.setRightText(String.format("剩余时间:%d秒", mLeftTime));
            mTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    mLeftTime--;
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            title_bar.setRightText(String.format("剩余时间:%d秒", mLeftTime));
                        }
                    });
                    if (mLeftTime == 0) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                gameOver();
                            }
                        });
                    }
                }
            }, 2000, 1000);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);

        mSeparationDis = SizeUtil.getScreenWidth(getActivity()) >> 2;
        notifyDialog = SystemNotifyDialogFragment_.builder()
                .build();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mTimer.cancel();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        game_view.startThread();
    }

    @Override
    public void onStop() {
        super.onStop();
        game_view.stopThread();
    }

    /**
     * 获取游戏关数
     * @return 游戏关数
     */
    public int getGameRound(){
        return roundId;
    }

    /**
     * 上一步
     */
    public void backStep(){
        if(isGameOver){
            return;
        }

        game_view.requestLayout();
        if(mOperationStack.size() <= 0){
            return;
        }

        OptionData data = mOperationStack.pop();
        NumberItem itemA = createView(data.valueA);
        itemA.setTag(data.tagA);
        NumberItem itemB = createView(data.valueB);
        itemB.setTag(data.tagB);
        game_view.addView(
                itemA,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        game_view.addView(
                itemB,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );

        NumberItem numberItem = (NumberItem) game_view.findViewWithTag(data.viewTag);

        float marginX, marginY;
        marginX = numberItem == null ? 0 : numberItem.getX();
        marginY = numberItem == null ? 0 : numberItem.getY();

        itemA.setX(marginX);
        itemA.setY(marginY);
        itemA.playAppear();

        itemB.setX(marginX);
        itemB.setY(marginY);
        itemB.playAppear();

        if(numberItem != null){
            numberItem.playDisappearAndRemove();
        }

        //开始分离操作
        Random random = new Random();
        float itemAX, itemBX, itemAY, itemBY;
        float value = mSeparationDis*(random.nextFloat() + 0.5f);
        itemAX = marginX - value;
        itemBX = marginX + value;
        itemAX = Math.max(0, Math.min(game_view.getMeasuredWidth() - itemA.getSize(), itemAX));
        itemBX = Math.max(0, Math.min(game_view.getMeasuredWidth() - itemB.getSize(), itemBX));

        value = mSeparationDis*(random.nextFloat() + 0.5f);
        if(random.nextInt(2) > 0){
            value *= -1;
        }
        itemAY = marginY - value;
        itemBY = marginY + value;
        itemAY = Math.max(0, Math.min(game_view.getMeasuredHeight() - itemA.getSize(), itemAY));
        itemBY = Math.max(0, Math.min(game_view.getMeasuredHeight() - itemB.getSize(), itemBY));

        int marginColor = itemA.getColorByValue(data.marginValue);
        itemA.setColor(marginColor);
        itemB.setColor(marginColor);
        itemA.setSeparationPoint(itemAX, itemAY, itemA.getColorByValue(itemA.getValue()));
        itemB.setSeparationPoint(itemBX, itemBY, itemB.getColorByValue(itemB.getValue()));

        leftNumber++;
    }

    /**
     * 游戏结束
     */
    public void gameOver(){
        isGameOver = true;

        GameOverEvent event = new GameOverEvent(
                mGameMode,
                getScore()
        );
        EventBus.getDefault().post(event);

        game_view.stopThread();
        mTimer.cancel();
    }

    /**
     * 游戏成功
     */
    public void gameClear(){
        isGameOver = true;

        GameClearEvent event = new GameClearEvent(
                mGameMode,
                getScore()
        );
        EventBus.getDefault().post(event);

        game_view.stopThread();
        mTimer.cancel();
    }

    /**
     * 收到合并事件
     */
    public void onEvent(MarginItemFinishEvent event){
        if(event.currentItem.isNeedCreateNew()){

            int value = calculateValue(
                    event.currentItem.getValue(),
                    event.currentItem.getMarginItem().getValue(),
                    event.currentItem.getOperation()
            );

            NumberItem numberItem = createView(value);
            game_view.addView(numberItem,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            numberItem.setX(event.currentItem.getX() + event.currentItem.getSize() / 2.0f - numberItem.getSize() / 2.0f);
            numberItem.setY(event.currentItem.getY() + event.currentItem.getSize() / 2.0f - numberItem.getSize() / 2.0f);
            numberItem.playAppear();

            OptionData optionData = new OptionData();
            optionData.valueA = event.currentItem.getValue();
            optionData.valueB = event.currentItem.getMarginItem().getValue();
            optionData.tagA = (int) event.currentItem.getTag();
            optionData.tagB = (int) event.currentItem.getMarginItem().getTag();
            optionData.viewTag = (int) numberItem.getTag();
            mOperationStack.push(optionData);

            leftNumber--;

            if(leftNumber == 1
                    && numberItem.getValue() == 24){
                gameClear();
            }
        }
        event.currentItem.playDisappearAndRemove();
    }

    /**
     * 获取当前局的分数
     * @return 分数
     */
    int getScore(){
        return roundId;
    }

    @Click({R.id.rcb_plus, R.id.rcb_sub, R.id.rcb_mul, R.id.rcb_div})
    void onOperationButtonClick(View view){
        switch (view.getId()){
            case R.id.rcb_plus:{
                mOperation = OperationEnum.Add;
                break;
            }
            case R.id.rcb_sub:{
                mOperation = OperationEnum.Subtraction;
                break;
            }
            case R.id.rcb_mul:{
                mOperation = OperationEnum.Multiplication;
                break;
            }
            case R.id.rcb_div:{
                mOperation = OperationEnum.Division;
                break;
            }
        }
    }

    @Click({R.id.rcb_back, R.id.rcb_no_anw})
    void onExtraButtonClick(View view){
        if(isGameOver){
            return;
        }

        switch (view.getId()){
            case R.id.rcb_back:{
                EventBus.getDefault().post(new AskGameExitEvent());
                break;
            }
            case R.id.rcb_no_anw:{
                if(hasAnswer){
                    notifyDialog.setContent("有解的哦");
                    notifyDialog.show(
                            getActivity().getFragmentManager(), notifyDialog.getClass().getName()
                    );
                }
                else{
                    gameClear();
                }
                break;
            }
        }
    }

    int calculateValue(int valueA, int valueB, OperationEnum operation){
        int res = 0;
        switch (operation){
            case Add:{
                res = valueA + valueB;
                break;
            }
            case Multiplication:{
                res = valueA * valueB;
                break;
            }
            case Subtraction:{
                res = valueA - valueB;
                break;
            }
            case Division:{
                res = valueA / valueB;
                break;
            }
        }
        return res;
    }

    /**
     * 创建4个随机item
     */
    void createAllRandomItem(){
        Random random = new Random();
        int height = SizeUtil.getScreenHeight(getActivity());
        int width = SizeUtil.getScreenWidth(getActivity());

        int[] cards = new int[4];

        for (int i = 0; i < 4; i++) {
            NumberItem item = createItem();
            cards[i] = item.getValue();
            game_view.addView(
                    item,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            int x, y;
            if (i < 2) {
                y = random.nextInt(height >> 2);
            } else {
                y = random.nextInt(height >> 2) + (height >> 2);
            }
            if (i % 2 == 0) {
                x = random.nextInt(width >> 1);
            } else {
                x = random.nextInt(width >> 1) + (width >> 1);
            }
            if (x > width - (item.getSize() << 1)) {
                x = width - (item.getSize() << 1);
            }
            if (y > height - (item.getSize() << 1)) {
                y = height - (item.getSize() << 1);
            }
            item.setX(x);
            item.setY(y);
            item.playAppear();
        }

        hasAnswer = CalculateAnswerHelper.judgeAnswer(cards);
    }

    /**
     * 返回一个NumberItem 数字随机
     * @return number item
     */
    NumberItem createItem(){
        Random random = new Random();
        return createView(random.nextInt(10) + 1);
    }

    NumberItem createView(int value){
        NumberItem numberItem = new NumberItem(getActivity());
        numberItem.setValue(value);
        numberItem.setScaleType(ImageView.ScaleType.FIT_CENTER);
        numberItem.setImageResource(R.drawable.ball);
        numberItem.setToNormal();
        numberItem.setTag(viewTag++);

        numberItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NumberItem item = (NumberItem) v;
                if (item.getStatus() == NumberItem.Status.NORMAL) {
                    item.setToPadding();
                    if (firstNumber == null
                            || firstNumber == item
                            || mOperation == null
                            || firstNumber.getStatus() != NumberItem.Status.PADDING) {

                        if(firstNumber != item
                                && firstNumber != null){
                            firstNumber.setToNormal();
                        }

                        firstNumber = item;
                    } else if (secondNumber == null) {
                        if (mOperation == OperationEnum.Subtraction) {
                            if (firstNumber.getValue() < item.getValue()) {
                                notifyDialog.setContent("不能减哦");
                                notifyDialog.show(
                                        getActivity().getFragmentManager(), notifyDialog.getClass().getName()
                                );
                                initState();
                                return;
                            }
                        }
                        else if(mOperation == OperationEnum.Division){
                            if(item.getValue() == 0
                                    || firstNumber.getValue() % item.getValue() != 0){
                                notifyDialog.setContent("不能整除");
                                notifyDialog.show(
                                        getActivity().getFragmentManager(), notifyDialog.getClass().getName()
                                );
                                initState();
                                return;
                            }
                        }
                        secondNumber = item;
                        marginItem(firstNumber, secondNumber, mOperation);
                        firstNumber = secondNumber = null;
                        mOperation = null;
                    }
                }
                else if (item.getStatus() == NumberItem.Status.PADDING) {
                    item.setToNormal();
                    initState();
                }
            }
        });

        return numberItem;
    }

    /**
     * 转化为初始化状态
     */
    void initState(){
        if(firstNumber != null) {
            firstNumber.setToNormal();
            firstNumber = null;
        }
        if(secondNumber != null) {
            secondNumber.setToNormal();
            secondNumber = null;
        }
        mOperation = null;
    }

    /**
     * 合并item
     * @param itemA item A
     * @param itemB item B
     */
    void marginItem(NumberItem itemA, NumberItem itemB, OperationEnum operationEnum){
        float marginX, marginY;

        float oAx = itemA.getX() + itemA.getSize()/2.0f;
        float oAy = itemA.getY() + itemA.getSize()/2.0f;
        float oBx = itemB.getX() + itemB.getSize()/2.0f;
        float oBy = itemB.getY() + itemB.getSize()/2.0f;

        if(oAx < oBx){
            marginX = (oBx - oAx)*itemB.getValue()/(itemA.getValue() + itemB.getValue()) + oAx;
        }
        else{
            marginX = (oAx - oBx)*itemA.getValue()/(itemA.getValue() + itemB.getValue()) + oBx;
        }
        if(oAy < oBy){
            marginY = (oBy - oAy)*itemB.getValue()/(itemA.getValue() + itemB.getValue()) + oAy;
        }
        else{
            marginY = (oAy - oBy)*itemA.getValue()/(itemA.getValue() + itemB.getValue()) + oBy;
        }

        int value = calculateValue(
                itemA.getValue(),
                itemB.getValue(),
                operationEnum
        );
        int color = itemA.getColorByValue(value);

        itemA.setMarginPoint(marginX - itemA.getSize()/2.0f, marginY - itemA.getSize()/2.0f, color);
        itemA.setOperation(operationEnum, itemB);
        itemB.setMarginPoint(marginX - itemB.getSize()/2.0f, marginY - itemB.getSize()/2.0f, color);
        itemA.setNeedCreateNew(true);
        itemB.setNeedCreateNew(false);
    }
}
