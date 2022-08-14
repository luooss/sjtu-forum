package xyz.ls.sjtuforum.controller;

import xyz.ls.sjtuforum.dto.CommentDTO;
import xyz.ls.sjtuforum.dto.QuestionDTO;
import xyz.ls.sjtuforum.enums.CommentTypeEnum;
import xyz.ls.sjtuforum.exception.SFErrorCode;
import xyz.ls.sjtuforum.exception.SFException;
import xyz.ls.sjtuforum.service.CommentService;
import xyz.ls.sjtuforum.service.SubjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 * Created by codedrinker on 2019/5/21.
 */
@Controller
public class SubjectController {

    @Autowired
    private SubjectService questionService;

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
        QuestionDTO questionDTO = questionService.getById(questionId);
        List<QuestionDTO> relatedQuestions = questionService.selectRelated(questionDTO);
        List<CommentDTO> comments = commentService.listByTargetId(questionId, CommentTypeEnum.QUESTION);
        // 累加阅读数
        questionService.incView(questionId);
        model.addAttribute("question", questionDTO);
        model.addAttribute("comments", comments);
        model.addAttribute("relatedQuestions", relatedQuestions);
        return "question";
    }
}