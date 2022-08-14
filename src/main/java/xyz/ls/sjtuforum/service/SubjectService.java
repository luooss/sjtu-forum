package xyz.ls.sjtuforum.service;

import xyz.ls.sjtuforum.cache.SubjectCache;
import xyz.ls.sjtuforum.dto.PaginationDTO;
import xyz.ls.sjtuforum.dto.SubjectDTO;
import xyz.ls.sjtuforum.dto.SubjectQueryDTO;
import xyz.ls.sjtuforum.enums.SortEnum;
import xyz.ls.sjtuforum.exception.CustomizeErrorCode;
import xyz.ls.sjtuforum.exception.CustomizeException;
import xyz.ls.sjtuforum.mapper.SubjectExtMapper;
import xyz.ls.sjtuforum.mapper.SubjectMapper;
import xyz.ls.sjtuforum.mapper.UserMapper;
import xyz.ls.sjtuforum.model.Subject;
import xyz.ls.sjtuforum.model.SubjectExample;
import xyz.ls.sjtuforum.model.User;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.RowBounds;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SubjectService {

    @Autowired
    private SubjectMapper SubjectMapper;

    @Autowired
    private SubjectExtMapper SubjectExtMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private SubjectCache SubjectCache;

    public PaginationDTO list(String search, String tag, String sort, Integer page, Integer size) {

        if (StringUtils.isNotBlank(search)) {
            String[] tags = StringUtils.split(search, " ");
            search = Arrays
                    .stream(tags)
                    .filter(StringUtils::isNotBlank)
                    .map(t -> t.replace("+", "").replace("*", "").replace("?", ""))
                    .filter(StringUtils::isNotBlank)
                    .collect(Collectors.joining("|"));
        }

        PaginationDTO paginationDTO = new PaginationDTO();

        Integer totalPage;

        SubjectQueryDTO SubjectQueryDTO = new SubjectQueryDTO();
        SubjectQueryDTO.setSearch(search);
        if (StringUtils.isNotBlank(tag)) {
            tag = tag.replace("+", "").replace("*", "").replace("?", "");
            SubjectQueryDTO.setTag(tag);
        }

        for (SortEnum sortEnum : SortEnum.values()) {
            if (sortEnum.name().toLowerCase().equals(sort)) {
                SubjectQueryDTO.setSort(sort);

                if (sortEnum == SortEnum.HOT7) {
                    SubjectQueryDTO.setTime(System.currentTimeMillis() - 1000L * 60 * 60 * 24 * 7);
                }
                if (sortEnum == SortEnum.HOT30) {
                    SubjectQueryDTO.setTime(System.currentTimeMillis() - 1000L * 60 * 60 * 24 * 30);
                }
                break;
            }
        }

        Integer totalCount = SubjectExtMapper.countBySearch(SubjectQueryDTO);

        if (totalCount % size == 0) {
            totalPage = totalCount / size;
        } else {
            totalPage = totalCount / size + 1;
        }

        if (page < 1) {
            page = 1;
        }
        if (page > totalPage) {
            page = totalPage;
        }

        paginationDTO.setPagination(totalPage, page);
        Integer offset = page < 1 ? 0 : size * (page - 1);
        SubjectQueryDTO.setSize(size);
        SubjectQueryDTO.setPage(offset);
        List<Subject> Subjects = SubjectExtMapper.selectBySearch(SubjectQueryDTO);
        List<SubjectDTO> SubjectDTOList = new ArrayList<>();

        for (Subject Subject : Subjects) {
            User user = userMapper.selectByPrimaryKey(Subject.getCreator());
            SubjectDTO SubjectDTO = new SubjectDTO();
            BeanUtils.copyProperties(Subject, SubjectDTO);
            SubjectDTO.setDescription("");
            SubjectDTO.setUser(user);
            SubjectDTOList.add(SubjectDTO);
        }
        List<SubjectDTO> stickies = SubjectCache.getStickies();
        if (stickies != null && stickies.size() != 0) {
            SubjectDTOList.addAll(0, stickies);
        }
        paginationDTO.setData(SubjectDTOList);
        return paginationDTO;
    }

    public PaginationDTO list(Long userId, Integer page, Integer size) {
        PaginationDTO paginationDTO = new PaginationDTO();

        Integer totalPage;

        SubjectExample SubjectExample = new SubjectExample();
        SubjectExample.createCriteria()
                .andCreatorEqualTo(userId);
        Integer totalCount = (int) SubjectMapper.countByExample(SubjectExample);

        if (totalCount % size == 0) {
            totalPage = totalCount / size;
        } else {
            totalPage = totalCount / size + 1;
        }

        if (page < 1) {
            page = 1;
        }
        if (page > totalPage) {
            page = totalPage;
        }

        paginationDTO.setPagination(totalPage, page);

        // size*(page-1)
        Integer offset = size * (page - 1);
        SubjectExample example = new SubjectExample();
        example.createCriteria()
                .andCreatorEqualTo(userId);
        List<Subject> Subjects = SubjectMapper.selectByExampleWithRowbounds(example, new RowBounds(offset, size));
        List<SubjectDTO> SubjectDTOList = new ArrayList<>();

        for (Subject Subject : Subjects) {
            User user = userMapper.selectByPrimaryKey(Subject.getCreator());
            SubjectDTO SubjectDTO = new SubjectDTO();
            BeanUtils.copyProperties(Subject, SubjectDTO);
            SubjectDTO.setUser(user);
            SubjectDTOList.add(SubjectDTO);
        }

        paginationDTO.setData(SubjectDTOList);
        return paginationDTO;
    }

    public SubjectDTO getById(Long id) {
        Subject Subject = SubjectMapper.selectByPrimaryKey(id);
        if (Subject == null) {
            throw new CustomizeException(CustomizeErrorCode.Subject_NOT_FOUND);
        }
        SubjectDTO SubjectDTO = new SubjectDTO();
        BeanUtils.copyProperties(Subject, SubjectDTO);
        User user = userMapper.selectByPrimaryKey(Subject.getCreator());
        SubjectDTO.setUser(user);
        return SubjectDTO;
    }

    public void createOrUpdate(Subject Subject) {
        if (Subject.getId() == null) {
            // 创建
            Subject.setGmtCreate(System.currentTimeMillis());
            Subject.setGmtModified(Subject.getGmtCreate());
            Subject.setViewCount(0);
            Subject.setLikeCount(0);
            Subject.setCommentCount(0);
            Subject.setSticky(0);
            SubjectMapper.insert(Subject);
        } else {
            // 更新

            Subject dbSubject = SubjectMapper.selectByPrimaryKey(Subject.getId());
            if (dbSubject == null) {
                throw new CustomizeException(CustomizeErrorCode.Subject_NOT_FOUND);
            }

            if (dbSubject.getCreator().longValue() != Subject.getCreator().longValue()) {
                throw new CustomizeException(CustomizeErrorCode.INVALID_OPERATION);
            }

            Subject updateSubject = new Subject();
            updateSubject.setGmtModified(System.currentTimeMillis());
            updateSubject.setTitle(Subject.getTitle());
            updateSubject.setDescription(Subject.getDescription());
            updateSubject.setTag(Subject.getTag());
            SubjectExample example = new SubjectExample();
            example.createCriteria()
                    .andIdEqualTo(Subject.getId());
            int updated = SubjectMapper.updateByExampleSelective(updateSubject, example);
            if (updated != 1) {
                throw new CustomizeException(CustomizeErrorCode.Subject_NOT_FOUND);
            }
        }
    }

    public void incView(Long id) {
        Subject Subject = new Subject();
        Subject.setId(id);
        Subject.setViewCount(1);
        SubjectExtMapper.incView(Subject);
    }

    public List<SubjectDTO> selectRelated(SubjectDTO queryDTO) {
        if (StringUtils.isBlank(queryDTO.getTag())) {
            return new ArrayList<>();
        }
        String[] tags = StringUtils.split(queryDTO.getTag(), ",");
        String regexpTag = Arrays
                .stream(tags)
                .filter(StringUtils::isNotBlank)
                .map(t -> t.replace("+", "").replace("*", "").replace("?", ""))
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.joining("|"));
        Subject Subject = new Subject();
        Subject.setId(queryDTO.getId());
        Subject.setTag(regexpTag);

        List<Subject> Subjects = SubjectExtMapper.selectRelated(Subject);
        List<SubjectDTO> SubjectDTOS = Subjects.stream().map(q -> {
            SubjectDTO SubjectDTO = new SubjectDTO();
            BeanUtils.copyProperties(q, SubjectDTO);
            return SubjectDTO;
        }).collect(Collectors.toList());
        return SubjectDTOS;
    }
}
