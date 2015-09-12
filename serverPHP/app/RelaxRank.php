<?php
/**
 * Created by PhpStorm.
 * User: Dog
 * Date: 2015/9/12
 * Time: 10:53
 */

namespace App;

use Illuminate\Database\Eloquent\Model;
use Illuminate\Support\Facades\DB;

class RelaxRank extends Model{

    protected $fillable = ['uid', 'name', 'mark'];

    /**
     * 根据页号 页数大小获取数据
     * @param $pageId 页号
     * @param $pageSize 页大小
     */
    public static function getRankList($pageId, $pageSize){
        $rankList = DB::table('relax_ranks')
            ->orderBy('mark', 'desc')
            ->skip($pageSize*$pageId)
            ->take($pageSize)
            ->get();
        return $rankList;
    }
}