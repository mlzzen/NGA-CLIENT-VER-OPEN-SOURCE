package nosc.api.bean

class CategoryBean {
    /**
     * id : wow
     * name : 魔兽世界
     * sub : [{"name":"魔兽世界","content":[{"fid":7,"name":"艾泽拉斯议事厅","info":"魔兽主讨论区","nameS":"议事厅","bit":1},{"fid":230,"name":"艾泽拉斯风纪委员会","info":"曝光违背公认准则的行为","nameS":"风纪委员会"},{"fid":310,"name":"前瞻资讯","info":"新版本与高阶讨论","infoS":"高阶讨论"},{"fid":624,"name":"经典旧世","info":"怀旧服讨论"},{"fid":323,"name":"国服以外"},{"fid":586,"name":"争霸艾泽拉斯","info":"新版本信息汇集"}]},{"name":"职业讨论区","content":[{"fid":390,"name":"五晨寺","info":"武僧"},{"fid":320,"name":"黑锋要塞","info":"死亡骑士"},{"fid":181,"name":"铁血沙场","info":"战士"},{"fid":182,"name":"魔法圣堂","info":"法师"},{"fid":183,"name":"信仰神殿","info":"牧师"},{"fid":185,"name":"风暴祭坛","info":"萨满"},{"fid":186,"name":"翡翠梦境","info":"德鲁伊"},{"fid":187,"name":"猎手大厅","info":"猎人"},{"fid":184,"name":"圣光之力","info":"圣骑士"},{"fid":188,"name":"恶魔深渊","info":"术士"},{"fid":189,"name":"暗影裂口","info":"盗贼"},{"fid":477,"name":"伊利达雷","info":"恶魔猎手"}]},{"name":"冒险心得","content":[{"fid":310,"name":"前瞻资讯","info":"新版本与高阶讨论","infoS":"高阶讨论"},{"fid":463,"name":"要塞讨论"},{"fid":327,"name":"任务/成就","icon":327},{"fid":218,"name":"副本专区","infoL":"魔兽世界副本"},{"fid":388,"name":"幻化讨论","infoL":"魔兽世界装备幻化"},{"fid":411,"name":"宠物讨论","infoL":"魔兽世界宠物"},{"fid":191,"name":"地精商会","infoL":"魔兽世界生产/商业"},{"fid":272,"name":"竞技场/战场","infoL":"PvP讨论"},{"fid":213,"name":"战争档案","info":"魔兽世界战报"},{"fid":255,"name":"公会管理","infoL":"公会管理经验交流"},{"fid":306,"name":"人员招募","infoL":"公会人员招募"},{"fid":200,"name":"插件研究","infoL":"魔兽世界插件研究"},{"fid":240,"name":"魔兽世界大脚","info":"官方合作辅助工具"}]},{"name":"历史背景 资料整理","content":[{"fid":254,"name":"镶金玫瑰","info":"剧情讨论 历史研究"},{"fid":124,"name":"壁画洞窟","info":"原创艺术 同人作品"},{"fid":102,"name":"作家协会","info":"游戏文学作品发布"},{"fid":264,"name":"卡拉赞剧院","info":"影音制作讨论"}]}]
     */
    var _id: String? = null
    var name: String? = null
    var groups: List<Group>? = null

    class Group {
        /**
         * name : 魔兽世界
         * content : [{"fid":7,"name":"艾泽拉斯议事厅","info":"魔兽主讨论区","nameS":"议事厅","bit":1},{"fid":230,"name":"艾泽拉斯风纪委员会","info":"曝光违背公认准则的行为","nameS":"风纪委员会"},{"fid":310,"name":"前瞻资讯","info":"新版本与高阶讨论","infoS":"高阶讨论"},{"fid":624,"name":"经典旧世","info":"怀旧服讨论"},{"fid":323,"name":"国服以外"},{"fid":586,"name":"争霸艾泽拉斯","info":"新版本信息汇集"}]
         */
        var name: String? = null
        var forums: List<Forum>? = null

        class Forum {
            /**
             * fid : 7
             * name : 艾泽拉斯议事厅
             * info : 魔兽主讨论区
             * nameS : 议事厅
             * bit : 1
             * infoS : 高阶讨论
             */
            var fid = 0
            var name: String? = null
            var info: String? = null
            var nameS: String? = null
            var infoS: String? = null
            var stid = 0
            var head: String? = null
            override fun toString(): String {
                return "ContentBean{" +
                        "fid=" + fid +
                        ", name='" + name + '\'' +
                        ", info='" + info + '\'' +
                        ", nameS='" + nameS + '\'' +
                        ", infoS='" + infoS + '\'' +
                        ", stid=" + stid +
                        ", head='" + head + '\'' +
                        '}'
            }
        }
    }
}