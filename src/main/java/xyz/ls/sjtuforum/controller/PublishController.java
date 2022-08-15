package xyz.ls.sjtuforum.controller;

import xyz.ls.sjtuforum.cache.PostRateLimiter;
import xyz.ls.sjtuforum.cache.TagCache;
import xyz.ls.sjtuforum.dto.PostDTO;
import xyz.ls.sjtuforum.model.Post;
import xyz.ls.sjtuforum.model.User;
import xyz.ls.sjtuforum.service.PostService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;

@Controller
@Slf4j
public class PublishController {

    @Autowired
    private PostService postService;

    @Autowired
    private PostRateLimiter postRateLimiter;

    @GetMapping("/publish/{id}")
    public String edit(@PathVariable(name = "id") Long id,
            Model model) {
        PostDTO post = postService.getById(id);
        model.addAttribute("title", post.getTitle());
        model.addAttribute("description", post.getDescription());
        model.addAttribute("tag", post.getTag());
        model.addAttribute("id", post.getId());
        model.addAttribute("tags", TagCache.get());
        return "publish";
    }

    @GetMapping("/publish")
    public String publish(Model model) {
        model.addAttribute("tags", TagCache.get());
        return "publish";
    }

    @PostMapping("/publish")
    public String doPublish(
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "tag", required = false) String tag,
            @RequestParam(value = "id", required = false) Long id,
            HttpServletRequest request,
            Model model) {
        model.addAttribute("title", title);
        model.addAttribute("description", description);
        model.addAttribute("tag", tag);
        model.addAttribute("tags", TagCache.get());

        if (StringUtils.isBlank(title)) {
            model.addAttribute("error", "标题不能为空");
            return "publish";
        }

        if (StringUtils.length(title) > 50) {
            model.addAttribute("error", "标题最多 50 个字符");
            return "publish";
        }
        if (StringUtils.isBlank(description)) {
            model.addAttribute("error", "帖子内容不能为空");
            return "publish";
        }
        if (StringUtils.isBlank(tag)) {
            model.addAttribute("error", "标签不能为空");
            return "publish";
        }

        String invalid = TagCache.filterInvalid(tag);
        if (StringUtils.isNotBlank(invalid)) {
            model.addAttribute("error", "输入非法标签:" + invalid);
            return "publish";
        }

        User user = (User) request.getSession().getAttribute("user");
        if (user == null) {
            model.addAttribute("error", "用户未登录");
            return "publish";
        }

        if (user.getDisable() != null && user.getDisable() == 1) {
            model.addAttribute("error", "操作被禁用");
            return "publish";
        }

        if (postRateLimiter.reachLimit(user.getId())) {
            model.addAttribute("error", "操作太快");
            return "publish";
        }

        Post post = new Post();
        post.setTitle(title);
        post.setDescription(description);
        post.setTag(tag);
        post.setCreator(user.getId());
        post.setId(id);
        postService.createOrUpdate(post);
        return "redirect:/";
    }
}
