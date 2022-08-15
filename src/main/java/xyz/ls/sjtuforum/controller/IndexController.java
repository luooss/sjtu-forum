package xyz.ls.sjtuforum.controller;

import xyz.ls.sjtuforum.cache.TagCache;
import xyz.ls.sjtuforum.dto.PaginationDTO;
import xyz.ls.sjtuforum.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;


@Controller
public class IndexController {

    @Autowired
    private PostService questionService;

    @Autowired
    private TagCache hotTagCache;

    @GetMapping("/")
    public String index(Model model,
            @RequestParam(name = "page", defaultValue = "1") Integer page,
            @RequestParam(name = "size", defaultValue = "10") Integer size,
            @RequestParam(name = "search", required = false) String search,
            @RequestParam(name = "tag", required = false) String tag,
            @RequestParam(name = "sort", required = false) String sort) {
        PaginationDTO pagination = questionService.list(search, tag, sort, page, size);
        List<String> tags = hotTagCache.getHots();
        model.addAttribute("pagination", pagination);
        model.addAttribute("search", search);
        model.addAttribute("tag", tag);
        model.addAttribute("tags", tags);
        model.addAttribute("sort", sort);
        return "index";
    }
}
