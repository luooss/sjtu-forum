package xyz.ls.sjtuforum.controller;

import xyz.ls.sjtuforum.dto.CommentDTO;
import xyz.ls.sjtuforum.dto.PostDTO;
import xyz.ls.sjtuforum.enums.CommentTypeEnum;
import xyz.ls.sjtuforum.exception.SFErrorCode;
import xyz.ls.sjtuforum.exception.SFException;
import xyz.ls.sjtuforum.service.CommentService;
import xyz.ls.sjtuforum.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;


@Controller
public class PostController {

    @Autowired
    private PostService questionService;

    @Autowired
    private CommentService commentService;

    @GetMapping("/question/{id}")
    public String question(@PathVariable(name = "id") String id, Model model) {
        Long questionId = null;
        try {
            questionId = Long.parseLong(id);
        } catch (NumberFormatException e) {
            throw new SFException(SFErrorCode.INVALID_INPUT);
        }
        PostDTO postDTO = questionService.getById(questionId);
        List<PostDTO> relatedQuestions = questionService.selectRelated(postDTO);
        List<CommentDTO> comments = commentService.listByTargetId(questionId, CommentTypeEnum.SUBJECT);
        questionService.incView(questionId);
        model.addAttribute("question", postDTO);
        model.addAttribute("comments", comments);
        model.addAttribute("relatedQuestions", relatedQuestions);
        return "question";
    }
}
