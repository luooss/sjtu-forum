package xyz.ls.sjtuforum.cache;

import xyz.ls.sjtuforum.dto.TagDTO;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class TagCache {
    public static List<TagDTO> get() {
        List<TagDTO> tagDTOS = new ArrayList<>();
        TagDTO news = new TagDTO();
        news.setCategoryName("交大动态");
        news.setTags(Arrays.asList("公告", "新闻"));
        tagDTOS.add(news);

        TagDTO learning = new TagDTO();
        learning.setCategoryName("学在交大");
        learning.setTags(Arrays.asList("考试信息", "科创竞赛", "本科生教务", "研究生教务", "学习研讨"));
        tagDTOS.add(learning);

        TagDTO living = new TagDTO();
        living.setCategoryName("校园生活");
        living.setTags(Arrays.asList("讲座报告", "校园服务", "失物招领", "校园网络", "二手交易", "医疗健康", "青年之声"));
        tagDTOS.add(living);

        TagDTO square = new TagDTO();
        square.setCategoryName("水源广场");
        square.setTags(Arrays.asList("亦可赛艇", "深水区", "谈笑风生"));
        tagDTOS.add(square);

        TagDTO talk = new TagDTO();
        talk.setCategoryName("人生经验");
        talk.setTags(Arrays.asList("人在江湖", "学习进阶", "逢考必过", "境外求索", "职场生涯"));
        tagDTOS.add(talk);

        TagDTO art = new TagDTO();
        art.setCategoryName("文化艺术");
        art.setTags(Arrays.asList("演出活动", "影视品论", "文学交流", "音乐之声", "泛二次元", "宠物花草", "游戏竞技", "摄影天地", "美妆时尚"));
        tagDTOS.add(art);

        TagDTO sports = new TagDTO();
        sports.setCategoryName("体育运动");
        sports.setTags(Arrays.asList("篮球", "足球", "网球", "羽毛球", "赛艇", "健身", "跑步", "其他运动"));
        tagDTOS.add(sports);

        TagDTO tech = new TagDTO();
        tech.setCategoryName("数码科技");
        tech.setTags(Arrays.asList("极客时间", "硬件产品", "软件应用"));
        tagDTOS.add(tech);

        TagDTO info = new TagDTO();
        info.setCategoryName("社会信息");
        info.setTags(Arrays.asList("租房信息", "实习兼职", "招生信息", "求职就业", "广而告之"));
        tagDTOS.add(info);
        return tagDTOS;
    }

    public static String filterInvalid(String tags) {
        String[] split = StringUtils.split(tags, ",");
        List<TagDTO> tagDTOS = get();

        List<String> tagList = tagDTOS.stream().flatMap(tag -> tag.getTags().stream()).collect(Collectors.toList());
        String invalid = Arrays.stream(split).filter(t -> StringUtils.isBlank(t) || !tagList.contains(t))
                .collect(Collectors.joining(","));
        return invalid;
    }

    public static void main(String[] args) {
        int i = (5 - 1) >>> 1;
        System.out.println(i);
    }

    public List<String> getHots() {
        return null;
    }
}
