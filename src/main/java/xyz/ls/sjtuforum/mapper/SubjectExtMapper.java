package xyz.ls.sjtuforum.mapper;

import xyz.ls.sjtuforum.dto.QuestionQueryDTO;
import xyz.ls.sjtuforum.model.Subject;

import java.util.List;

public interface SubjectExtMapper {
    int incView(Subject record);

    int incCommentCount(Subject record);

    List<Subject> selectRelated(Subject question);

    Integer countBySearch(QuestionQueryDTO questionQueryDTO);

    List<Subject> selectBySearch(QuestionQueryDTO questionQueryDTO);

    List<Subject> selectSticky();
}