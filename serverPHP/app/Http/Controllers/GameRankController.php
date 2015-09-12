<?php

namespace App\Http\Controllers;

use App\RelaxRank;
use App\NervousRank;
use App\User;
use Illuminate\Support\Facades\Input;

class GameRankController extends Controller{

    /**
     * 获取休闲模式的排名列表
     * @param $pageId 页号
     * @param $pageSize 页大小
     */
    public function getRelaxRankList(){
        $pageId = Input::get('pageId', 0);
        $pageSize = Input::get('pageSize', 20);
        return RelaxRank::getRankList($pageId, $pageSize);
    }

    /**
     * 获取紧张模式的排名列表
     * @param $pageId 页号
     * @param $pageSize 页大小
     */
    public function getNervousRankList(){
        $pageId = Input::get('pageId', 0);
        $pageSize = Input::get('pageSize', 20);
        return NervousRank::getRankList($pageId, $pageSize);
    }

    /**
     * 保存分数到休闲列表
     */
    public function saveMarkToRelaxRankList(){
        $userId = Input::get('user_id');
        $mark = Input::get('mark');
        if($userId == null || $mark == null){
            return ['code'=>'-1', 'msg'=>'参数不合法', 'data'=>null];
        }
        $user = User::find($userId);
        if($user == null){
            return ['code'=>'-1', 'msg'=>'用户不存在', 'data'=>null];
        }
        $rank = RelaxRank::firstOrNew(['uid'=>$userId, 'name'=>$user->name]);
        if($rank->mark < $mark){
            $rank->mark = $mark;
            $rank->save();
        }
        return ['code'=>'-1', 'msg'=>'保存成功', 'data'=>null];
    }

    /**
     * 保存数据到紧张列表
     */
    public function saveMarkToNervousRankList(){
        $userId = Input::get('user_id');
        $mark = Input::get('mark');
        if($userId == null || $mark == null){
            return ['code'=>'-1', 'msg'=>'参数不合法', 'data'=>null];
        }
        $user = User::find($userId);
        if($user == null){
            return ['code'=>'-1', 'msg'=>'用户不存在', 'data'=>null];
        }
        $rank = NervousRank::firstOrNew(['uid'=>$userId, 'name'=>$user->name]);
        if($rank->mark < $mark){
            $rank->mark = $mark;
            $rank->save();
        }
        return ['code'=>'-1', 'msg'=>'保存成功', 'data'=>null];
    }
}