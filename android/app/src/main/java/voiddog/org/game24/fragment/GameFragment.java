package voiddog.org.game24.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Stack;

import de.greenrobot.event.EventBus;
import voiddog.org.game24.R;
import voiddog.org.game24.data.OperationEnum;
import voiddog.org.game24.data.OptionData;
import voiddog.org.game24.event.MarginItemFinishEvent;
import voiddog.org.game24.ui.DragGroupView;
import voiddog.org.game24.ui.NumberItem;
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

    @ViewById
    DragGroupView game_view;

    //操作保存栈，供撤销用
    Stack<OptionData> mOperationStack = new Stack<>();
    //Number Item 列表
    final List<NumberItem> mNumberItemList = new ArrayList<>();
    //两个操作数
    NumberItem firstNumber, secondNumber;
    //操作
    OperationEnum mOperation = OperationEnum.Add;
    //view tag
    int viewTag = 1;

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
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);

        mSeparationDis = SizeUtil.getScreenWidth(getActivity()) >> 2;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
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
     * 上一步
     */
    public void backStep(){
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

        synchronized (mNumberItemList){
            mNumberItemList.add(itemA);
            mNumberItemList.add(itemB);
            if(numberItem != null) {
                mNumberItemList.remove(numberItem);
            }
        }

        //开始分离操作
        Random random = new Random();
        float itemAX, itemBX, itemAY, itemBY;
        float value = mSeparationDis*(random.nextFloat() + 0.5f);
        itemAX = marginX - value;
        itemBX = marginX + value;
        itemAX = Math.max(0, Math.min(SizeUtil.getScreenWidth(getActivity()) - itemA.getSize(), itemAX));
        itemBX = Math.max(0, Math.min(SizeUtil.getScreenWidth(getActivity()) - itemB.getSize(), itemBX));

        value = mSeparationDis*(random.nextFloat() + 0.5f);
        if(random.nextInt(2) > 0){
            value *= -1;
        }
        itemAY = marginY - value;
        itemBY = marginY + value;
        itemAY = Math.max(0, Math.min(SizeUtil.getScreenHeight(getActivity()) - itemA.getSize(), itemAY));
        itemBY = Math.max(0, Math.min(SizeUtil.getScreenHeight(getActivity()) - itemB.getSize(), itemBY));

        int marginColor = itemA.getColorByValue(data.marginValue);
        itemA.setColor(marginColor);
        itemB.setColor(marginColor);
        itemA.setSeparationPoint(itemAX, itemAY, itemA.getColorByValue(itemA.getValue()));
        itemB.setSeparationPoint(itemBX, itemBY, itemB.getColorByValue(itemB.getValue()));
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
            synchronized (mNumberItemList){
                mNumberItemList.add(numberItem);
            }

            OptionData optionData = new OptionData();
            optionData.valueA = event.currentItem.getValue();
            optionData.valueB = event.currentItem.getMarginItem().getValue();
            optionData.tagA = (int) event.currentItem.getTag();
            optionData.tagB = (int) event.currentItem.getMarginItem().getTag();
            optionData.viewTag = (int) numberItem.getTag();
            mOperationStack.push(optionData);
        }
        synchronized (mNumberItemList){
            mNumberItemList.remove(event.currentItem);
        }
        event.currentItem.playDisappearAndRemove();
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

        synchronized (mNumberItemList) {
            for (int i = 0; i < 4; i++) {
                NumberItem item = createItem();
                game_view.addView(
                        item,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
                mNumberItemList.add(item);
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
        }
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
                            || firstNumber.getStatus() != NumberItem.Status.PADDING) {
                        firstNumber = item;
                    } else if (secondNumber == null) {
                        if (mOperation == null) {
                            initState();
                        } else {
                            //特殊处理减法
                            if (mOperation == OperationEnum.Subtraction) {
                                if (firstNumber.getValue() < item.getValue()) {
                                    // TODO 减法不合法
                                    initState();
                                    return;
                                }
                            }
                            secondNumber = item;
                            marginItem(firstNumber, secondNumber, mOperation);
                            firstNumber = secondNumber = null;
                            // TODO mOperation = null;
                        }
                    }
                } else if (item.getStatus() == NumberItem.Status.PADDING) {
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
        // TODO mOperation = null;
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
