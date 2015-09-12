<?php

/*
|--------------------------------------------------------------------------
| Application Routes
|--------------------------------------------------------------------------
|
| Here is where you can register all of the routes for an application.
| It's a breeze. Simply tell Laravel the URIs it should respond to
| and give it the controller to call when that URI is requested.
|
*/

Route::get('/', function () {
    return view('welcome');
});

Route::get('get_csrf_token', function(){
    return ['code'=>'0', 'msg'=>'success', 'data'=>csrf_token()];
});

Route::post('relax_rank_list', 'GameRankController@getRelaxRankList');

Route::post('register', 'UserController@registerNewUser');

Route::post('upload_relax_mark', 'GameRankController@saveMarkToRelaxRankList');

Route::post('upload_nervous_mark', 'GameRankController@saveMarkToNervousRankList');