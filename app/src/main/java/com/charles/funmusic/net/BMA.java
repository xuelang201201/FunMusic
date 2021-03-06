package com.charles.funmusic.net;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class BMA {

    private static final String FOR_MATE = "json";
    static final String BASE = "http://tingapi.ting.baidu.com/v1/restserver/ting?from=android&version=5.6.5.6&format=" + FOR_MATE;

    /**
     * 轮播音乐封面
     *
     * @param num 数量
     */
    public static String focusPic(int num) {
        return BASE + "&method=" + "baidu.ting.plaza.getFocusPic" + "&num=" + num;
    }

    /**
     * 唱片专辑
     *
     * @author Sanron
     */
    public static class Album {

        /**
         * 推荐唱片
         *
         * @param offset 偏移量
         * @param limmit 获取数量
         */
        public static String recommendAlbum(int offset, int limmit) {
            return BASE + "&method=" + "baidu.ting.plaza.getRecommendAlbum" + "&offset=" + offset +
                    "&limit=" + limmit;
        }

        /**
         * 唱片信息
         *
         * @param albumid 唱片id
         */
        public static String albumInfo(String albumid) {
            return BASE + "&method=" + "baidu.ting.album.getAlbumInfo" + "&album_id=" + albumid;
        }
    }


    /**
     * 音乐场景
     *
     * @author Sanron
     */
    public static class Scene {

        /**
         * 推荐音乐场景(需要cuid，暂时关闭)
         * @return
         */
//		public static String sugestionScene(){
//			StringBuffer sb = new StringBuffer(BASE);
//			sb.append("&method=").append("baidu.ting.scene.getSugScene");
//			return sb.toString();
//		}

        /**
         * 固定场景
         */
        public static String constantScene() {
            return BASE + "&method=" + "baidu.ting.scene.getConstantScene";
        }

        /**
         * 所有场景类别
         */
        public static String sceneCategories() {
            return BASE + "&method=" + "baidu.ting.scene.getCategoryList";
        }

        /**
         * 场景类别下的所有场景
         *
         * @param categoreid 类别id
         */
        public static String categoryScenes(String categoreid) {
            return BASE + "&method=" + "baidu.ting.scene.getCategoryScene" +
                    "&category_id=" + categoreid;
        }
    }

    /**
     * 音乐标签
     *
     * @author Sanron
     */
    public static class Tag {
        /**
         * 所有音乐标签
         */
        public static String allSongTags() {
            return BASE + "&method=" + "baidu.ting.tag.getAllTag";
        }

        /**
         * 热门音乐标签
         *
         * @param num 数量
         */
        public static String hotSongTags(int num) {
            return BASE + "&method=" + "baidu.ting.tag.getHotTag" +
                    "&nums=" + num;
        }

        /**
         * 标签为tagname的歌曲
         *
         * @param tagname 标签名
         * @param limit   数量
         */
        public static String tagSongs(String tagname, int limit) {
            return BASE + "&method=" + "baidu.ting.tag.songlist" +
                    "&tagname=" + encode(tagname) +
                    "&limit=" + limit;
        }
    }

    public static class Song {

        /**
         * 歌曲基本信息
         *
         * @param songid 歌曲id
         */
        public static String songBaseInfo(String songid) {
            return BASE + "&method=" + "baidu.ting.song.baseInfos" +
                    "&song_id=" + songid;
        }

        /**
         * 编辑推荐歌曲
         *
         * @param num 数量
         */
        public static String recommendSong(int num) {
            return BASE + "&method=" + "baidu.ting.song.getEditorRecommend" +
                    "&num=" + num;
        }

        /**
         * 歌曲信息和下载地址
         */
        public static String songInfo(String songid) {
            StringBuilder sb = new StringBuilder(BASE);
            String str = "songid=" + songid + "&ts=" + System.currentTimeMillis();
            String e = AESTools.encrpty(str);
            sb.append("&method=").append("baidu.ting.song.getInfos")
                    .append("&").append(str)
                    .append("&e=").append(e);
            return sb.toString();
        }

        /**
         * 歌曲伴奏信息
         */
        public static String accompanyInfo(String songid) {
            StringBuilder sb = new StringBuilder(BASE);
            String str = "song_id=" + songid + "&ts=" + System.currentTimeMillis();
            String e = AESTools.encrpty(str);
            sb.append("&method=").append("baidu.ting.learn.down")
                    .append("&").append(str)
                    .append("&e=").append(e);
            return sb.toString();
        }

        /**
         * 相似歌曲
         */
        public static String recommendSongList(String songid, int num) {
            return BASE + "&method=" + "baidu.ting.song.getRecommandSongList" +
                    "&song_id=" + songid +
                    "&num=" + num;
        }
    }

    /**
     * 艺术家
     *
     * @author Sanron
     */
    public static class Artist {

        /**
         * 全部地区
         */
        public static final int AREA_ALL = 0;
        /**
         * 华语
         */
        public static final int AREA_CHINIESE = 6;
        /**
         * 欧美
         */
        public static final int AREA_EU = 3;
        /**
         * 韩国
         */
        public static final int AREA_KOREA = 7;
        /**
         * 日本
         */
        public static final int AREA_JAPAN = 60;
        /**
         * 其他
         */
        public static final int AREA_OTHER = 5;

        /**
         * 无选择
         */
        public static final int SEX_NONE = 0;
        /**
         * 男性
         */
        public static final int SEX_MALE = 1;
        /**
         * 女性
         */
        public static final int SEX_FEMALE = 2;
        /**
         * 组合
         */
        public static final int SEX_GROUP = 3;

        /**
         * 获取艺术家列表
         *
         * @param offset 偏移
         * @param limit  数量
         * @param area   地区：0不分,6华语,3欧美,7韩国,60日本,5其他
         * @param sex    性别：0不分,1男,2女,3组合
         * @param order  排序：1按热门，2按艺术家id
         * @param abc    艺术家名首字母：a-z,other其他
         */
        static String artistList(int offset, int limit, int area, int sex, int order, String abc) {
            StringBuilder sb = new StringBuilder(BASE);
            sb.append("&method=").append("baidu.ting.artist.getList");
            sb.append("&offset=").append(offset);
            sb.append("&limit=").append(limit);
            sb.append("&area=").append(area);
            sb.append("&sex=").append(sex);
            sb.append("&order=").append(order);//暂时不清楚order排序
            if (abc != null && !abc.trim().equals("")) {
                sb.append("&abc=").append(abc);
            }
            return sb.toString();
        }

        /**
         * 热门艺术家
         *
         * @param offset 偏移量
         * @param limit  获取数量
         */
        public static String hotArtist(int offset, int limit) {
            return artistList(offset, limit, 0, 0, 1, null);
        }

        /**
         * 艺术家歌曲
         *
         * @param tinguid  tinguid
         * @param artistid 艺术家id
         * @param offset   偏移量
         * @param limit    获取数量
         */
        public static String artistSongList(String tinguid, String artistid, int offset, int limit) {
            return BASE + "&method=" + "baidu.ting.artist.getSongList" +
                    "&order=2" +
                    "&tinguid=" + tinguid +
                    "&artistid=" + artistid +
                    "&offset=" + offset +
                    "&limits=" + limit;
        }

        /**
         * 艺术家信息
         *
         * @param tinguid  tinguid
         * @param artistid 艺术家id
         */
        public static String artistInfo(String tinguid, String artistid) {
            return BASE + "&method=" + "baidu.ting.artist.getinfo" + "&tinguid=" + tinguid +
                    "&artistid=" + artistid;
        }
    }

    /**
     * 音乐榜
     */
    public static class Billboard {

        /**
         * 所有音乐榜类别
         */
        public static String billCategory() {
            return BASE + "&method=" + "baidu.ting.billboard.billCategory" + "&kflag=1";
        }

        /**
         * 音乐榜歌曲
         *
         * @param type   类型
         * @param offset 偏移
         * @param size   获取数量
         */
        public static String billSongList(int type, int offset, int size) {
            return BASE + "&method=" + "baidu.ting.billboard.billList" + "&type=" + type +
                    "&offset=" + offset + "&size=" + size + "&fields=" +
                    encode("song_id,title,author,album_title,pic_big,pic_small,havehigh,all_rate,charge,has_mv_mobile,learn,song_source,korean_bb_song");
        }
    }

    /**
     * 歌单
     *
     * @author Sanron
     */
    public static class GeDan {

        /**
         * 歌单分类
         */
        public static String geDanCategory() {
            return BASE + "&method=" + "baidu.ting.diy.gedanCategory";
        }

        /**
         * 热门歌单
         */
        public static String hotGeDan(int num) {
            return BASE + "&method=" + "baidu.ting.diy.getHotGeDanAndOfficial" +
                    "&num=" + num;
        }

        /**
         * 歌单
         *
         * @param pageNo   页码
         * @param pageSize 每页数量
         */
        public static String geDan(int pageNo, int pageSize) {
            return BASE + "&method=" + "baidu.ting.diy.gedan" + "&page_size=" + pageSize +
                    "&page_no=" + pageNo;
        }


        /**
         * 包含标签的歌单
         *
         * @param tag      标签名
         * @param pageNo   页码
         * @param pageSize 每页数量
         */
        public static String geDanByTag(String tag, int pageNo, int pageSize) {
            return BASE + "&method=" + "baidu.ting.diy.search" + "&page_size=" + pageSize +
                    "&page_no=" + pageNo + "&query=" + encode(tag);
        }

        /**
         * 歌单信息和歌曲
         *
         * @param listid 歌单id
         */
        public static String geDanInfo(String listid) {
            return BASE + "&method=" + "baidu.ting.diy.gedanInfo" + "&listid=" + listid;
        }
    }

    /**
     * 电台
     *
     * @author Sanron
     */
    public static class Radio {

        /**
         * 录制电台
         *
         * @param pageNo   页数
         * @param pageSize 每页数量，也是返回数量
         */
        public static String recChannel(int pageNo, int pageSize) {
            return BASE + "&method=" + "baidu.ting.radio.getRecChannel" +
                    "&page_no=" + pageNo + "&page_size=" + pageSize;
        }

        /**
         * 推荐电台（注意返回的都是乐播节目)
         */
        public static String recommendRadioList(int num) {
            return BASE + "&method=" + "baidu.ting.radio.getRecommendRadioList" + "&num=" + num;
        }

        /**
         * 频道歌曲
         *
         * @param channelname 频道名,注意返回的json数据频道有num+1个，但是最后一个是空的
         */
        public static String channelSong(String channelname, int num) {
            return BASE + "&method=" + "baidu.ting.radio.getChannelSong" +
                    "&channelname=" + encode(channelname) + "&pn=0" + "&rn=" + num;
        }
    }

    /**
     * 乐播节目
     * 节目相当于一个专辑
     * 每一期相当于专辑里的每首歌
     *
     * @author Sanron
     */
    public static class Lebo {

        /**
         * 频道
         *
         * @param pageNo   页码(暂时无用)
         * @param pageSize 每页数量，也是返回数量(暂时无用)
         */
        public static String channelTag(int pageNo, int pageSize) {

            return BASE + "&method=" + "baidu.ting.lebo.getChannelTag" + "&page_no=" + pageNo +
                    "&page_size=0" + pageSize;
        }

        /**
         * 返回频道下的不同节目的几期
         * 包含几个节目，每个节目有一期或多期
         * 比如返回 	节目1第1期，节目1第2期，节目2第1期，节目3第6期
         *
         * @param tagId 频道id
         * @param num   数量
         */
        public static String channelSongList(String tagId, int num) {
            return BASE + "&method=" + "baidu.ting.lebo.channelSongList" + "&tag_id=" + tagId +
                    "&num=" + num;
        }

        /**
         * 节目信息
         *
         * @param albumid        节目id
         * @param lastestSongNum 返回最近几期
         */
        public static String albumInfo(String albumid, int lastestSongNum) {
            return BASE + "&method=" + "baidu.ting.lebo.albumInfo" +
                    "&album_id=" + albumid + "&num=" + lastestSongNum;
        }
    }

    /**
     * 搜索
     *
     * @author Sanron
     */
    public static class Search {

        /**
         * 热门关键字
         */
        public static String hotWord() {
            return BASE + "&method=" + "baidu.ting.search.hot";
        }

        /**
         * 搜索建议
         */
        public static String searchSugestion(String query) {
            return BASE + "&method=" + "baidu.ting.search.catalogSug" + "&query=" + encode(query);
        }

        /**
         * 搜歌词
         *
         * @param songname 歌名
         * @param artist   艺术家
         */
        public static String searchLrcPic(String songname, String artist) {
            StringBuilder sb = new StringBuilder(BASE);
            String ts = Long.toString(System.currentTimeMillis());
            String query = encode(songname) + "$$" + encode(artist);
            String e = AESTools.encrpty("query=" + songname + "$$" + artist + "&ts=" + ts);
            sb.append("&method=").append("baidu.ting.search.lrcpic")
                    .append("&query=").append(query)
                    .append("&ts=").append(ts)
                    .append("&type=2")
                    .append("&e=").append(e);
            return sb.toString();
        }

        /**
         * 合并搜索结果，用于搜索建议中的歌曲
         */
        public static String searchMerge(String query, int pageNo, int pageSize) {
            return BASE + "&method=" + "baidu.ting.search.merge" +
                    "&query=" + encode(query) + "&page_no=" + pageNo +
                    "&page_size=" + pageSize + "&type=-1&data_source=0";
        }

        /**
         * 搜索伴奏
         *
         * @param query    关键词
         * @param pageNo   页码
         * @param pageSize 每页数量，也是返回数量
         */
        public static String searchAccompany(String query, int pageNo, int pageSize) {
            return BASE + "&method=" + "baidu.ting.learn.search" +
                    "&query=" + encode(query) + "&page_no=" + pageNo +
                    "&page_size=" + pageSize;
        }
    }

    static String encode(String str) {
        if (str == null) return "";

        try {
            return URLEncoder.encode(str, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return str;
    }
}