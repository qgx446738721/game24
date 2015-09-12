<?php
/**
 * Created by PhpStorm.
 * User: Dog
 * Date: 2015/9/12
 * Time: 12:13
 */

namespace App\Http\Controllers;

use App\User;
use Illuminate\Support\Facades\Input;

class UserController extends Controller{

    /**
     * 判断用户名是否合法
     */
    public static function registerNewUser(){
        $newUser = new User;
        $newUser->name = Input::get('user_name');

        if($newUser->name == null){
            return ['code'=>'-1', 'msg'=>'参数错误', 'data'=>null];
        }

        try{
            $newUser->save();
        }
        catch(\Exception $e){
            return ['code'=>'-1', 'msg'=>'用户名已存在', 'data'=>null];
        }
        return ['code'=>'0', 'msg'=>'注册成功', 'data'=>$newUser->toJson()];
    }
}