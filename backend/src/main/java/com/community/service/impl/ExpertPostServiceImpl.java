package com.community.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.community.common.BizException;
import com.community.common.ResultCode;
import com.community.common.SecurityUser;
import com.community.dto.AppExpertContentBlockDTO;
import com.community.dto.AppExpertPostCreateDTO;
import com.community.dto.AppExpertPostPageQueryDTO;
import com.community.entity.CmsSensitiveWord;
import com.community.entity.KbCategory;
import com.community.entity.KbEntry;
import com.community.entity.KbEntryTag;
import com.community.entity.QaTag;
import com.community.entity.User;
import com.community.entity.UserPrivacySetting;
import com.community.mapper.CmsSensitiveWordMapper;
import com.community.mapper.ExpertPostMapper;
import com.community.mapper.KbCategoryMapper;
import com.community.mapper.KbEntryTagMapper;
import com.community.mapper.QaTagMapper;
import com.community.mapper.UserMapper;
import com.community.mapper.UserPrivacySettingMapper;
import com.community.service.EsSearchService;
import com.community.service.ExpertPostService;
import com.community.service.RecommendationBehaviorService;
import com.community.vo.AppExpertContentBlockVO;
import com.community.vo.AppExpertPostDetailVO;
import com.community.vo.AppExpertPostItemVO;
import com.community.vo.AppKbCategoryVO;
import com.community.vo.KbTagSimpleVO;
import com.community.vo.SearchKbDoc;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ExpertPostServiceImpl implements ExpertPostService {
    private static final String SOURCE_EXPERT_POST = "expert_post";

    private final ExpertPostMapper expertPostMapper;
    private final UserMapper userMapper;
    private final CmsSensitiveWordMapper sensitiveWordMapper;
    private final KbEntryTagMapper kbEntryTagMapper;
    private final KbCategoryMapper kbCategoryMapper;
    private final QaTagMapper qaTagMapper;
    private final ObjectMapper objectMapper;
    private final EsSearchService esSearchService;
    private final RecommendationBehaviorService recommendationBehaviorService;
    private final UserPrivacySettingMapper userPrivacySettingMapper;

    @Override
    public List<AppKbCategoryVO> categories() {
        List<KbCategory> rows = kbCategoryMapper.selectList(new LambdaQueryWrapper<KbCategory>()
            .eq(KbCategory::getStatus, 1)
            .orderByAsc(KbCategory::getSort)
            .orderByAsc(KbCategory::getId));
        if (rows == null || rows.isEmpty()) {
            return List.of();
        }
        return rows.stream().map(x -> {
            AppKbCategoryVO vo = new AppKbCategoryVO();
            vo.setId(x.getId());
            vo.setParentId(x.getParentId());
            vo.setName(x.getName());
            return vo;
        }).toList();
    }

    @Override
    @Transactional
    public Long create(AppExpertPostCreateDTO dto) {
        Long userId = requireExpertUserId();
        validateCategoryRequired(dto.getCategoryId());

        List<AppExpertContentBlockVO> blocks = normalizeContentBlocks(dto.getContentBlocks(), dto.getContent(), dto.getImageUrls());
        String mergedText = mergeTextBlocks(blocks);
        validateNoSensitiveWords("科普文章", dto.getTitle(), dto.getSummary(), mergedText);

        List<String> tagNames = normalizeTagNames(dto.getTagNames());
        validateNoSensitiveWords("标签", tagNames.toArray(String[]::new));

        KbEntry entry = new KbEntry();
        entry.setCategoryId(dto.getCategoryId());
        entry.setTitle(dto.getTitle().trim());
        entry.setSummary(trimOrNull(dto.getSummary()));
        entry.setContent(mergedText);
        entry.setContentRef(buildContentRef(dto.getCoverImage(), blocks));
        entry.setSource(SOURCE_EXPERT_POST);
        entry.setAuthorUserId(userId);
        entry.setStatus(1);
        entry.setViewCount(0);
        entry.setLikeCount(0);
        entry.setFavoriteCount(0);
        LocalDateTime now = LocalDateTime.now();
        entry.setCreatedAt(now);
        entry.setUpdatedAt(now);
        expertPostMapper.insert(entry);

        bindTags(entry.getId(), List.of(), tagNames, now);
        indexKbForEs(entry);
        return entry.getId();
    }

    @Override
    @Transactional
    public void update(Long id, AppExpertPostCreateDTO dto) {
        Long userId = requireExpertUserId();
        KbEntry exists = expertPostMapper.selectById(id);
        if (exists == null || !SOURCE_EXPERT_POST.equals(exists.getSource())) {
            throw new BizException(ResultCode.BAD_REQUEST, "科普文章不存在");
        }
        if (!userId.equals(exists.getAuthorUserId())) {
            throw new BizException(ResultCode.FORBIDDEN, "无权修改该科普文章");
        }

        validateCategoryRequired(dto.getCategoryId());
        List<AppExpertContentBlockVO> blocks = normalizeContentBlocks(dto.getContentBlocks(), dto.getContent(), dto.getImageUrls());
        String mergedText = mergeTextBlocks(blocks);
        validateNoSensitiveWords("科普文章", dto.getTitle(), dto.getSummary(), mergedText);

        List<String> tagNames = normalizeTagNames(dto.getTagNames());
        validateNoSensitiveWords("标签", tagNames.toArray(String[]::new));

        exists.setCategoryId(dto.getCategoryId());
        exists.setTitle(dto.getTitle().trim());
        exists.setSummary(trimOrNull(dto.getSummary()));
        exists.setContent(mergedText);
        exists.setContentRef(buildContentRef(dto.getCoverImage(), blocks));
        exists.setUpdatedAt(LocalDateTime.now());
        expertPostMapper.updateById(exists);

        List<KbTagSimpleVO> oldTags = kbEntryTagMapper.selectTagsByEntryId(id);
        List<Long> oldTagIds = oldTags == null ? List.of() : oldTags.stream().map(KbTagSimpleVO::getId).toList();
        bindTags(id, oldTagIds, tagNames, LocalDateTime.now());
        indexKbForEs(exists);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Long userId = requireExpertUserId();
        KbEntry exists = expertPostMapper.selectById(id);
        if (exists == null || !SOURCE_EXPERT_POST.equals(exists.getSource())) {
            throw new BizException(ResultCode.BAD_REQUEST, "科普文章不存在");
        }
        if (!userId.equals(exists.getAuthorUserId())) {
            throw new BizException(ResultCode.FORBIDDEN, "无权删除该科普文章");
        }

        List<KbTagSimpleVO> oldTags = kbEntryTagMapper.selectTagsByEntryId(id);
        if (oldTags != null) {
            for (KbTagSimpleVO old : oldTags) {
                adjustTagUseCount(old.getId(), -1);
            }
        }
        kbEntryTagMapper.delete(new LambdaQueryWrapper<KbEntryTag>().eq(KbEntryTag::getEntryId, id));
        expertPostMapper.deleteById(id);
        esSearchService.syncKbById(id);
    }

    @Override
    public PageInfo<AppExpertPostItemVO> page(AppExpertPostPageQueryDTO query) {
        int page = resolvePage(query);
        int pageSize = resolvePageSize(query);
        Long userId = currentUserIdOrNull();
        boolean personalized = shouldPersonalize(query, userId);
        PageHelper.startPage(page, pageSize);
        List<AppExpertPostItemVO> rows = expertPostMapper.selectPublishedPage(
            query == null ? null : query.getKeyword(),
            query == null ? null : query.getSortBy(),
            query == null ? null : query.getCategoryId(),
            userId,
            personalized
        );
        rows.forEach(this::fillRefAndTags);
        return new PageInfo<>(rows);
    }

    @Override
    public PageInfo<AppExpertPostItemVO> myPage(AppExpertPostPageQueryDTO query) {
        Long userId = requireExpertUserId();
        int page = resolvePage(query);
        int pageSize = resolvePageSize(query);
        PageHelper.startPage(page, pageSize);
        List<AppExpertPostItemVO> rows = expertPostMapper.selectMyPage(
            userId,
            query == null ? null : query.getKeyword(),
            query == null ? null : query.getStatus()
        );
        rows.forEach(this::fillRefAndTags);
        return new PageInfo<>(rows);
    }

    @Override
    @Transactional
    public AppExpertPostDetailVO detail(Long id) {
        AppExpertPostDetailVO vo = expertPostMapper.selectPublishedDetail(id);
        if (vo == null) {
            throw new BizException(ResultCode.BAD_REQUEST, "科普文章不存在或已下线");
        }
        Long currentUserId = currentUserIdOrNull();
        if (currentUserId == null || !currentUserId.equals(vo.getAuthorUserId())) {
            expertPostMapper.increaseViewCount(id);
            vo.setViewCount((vo.getViewCount() == null ? 0 : vo.getViewCount()) + 1);
            if (currentUserId != null) {
                recommendationBehaviorService.recordKbView(currentUserId, id, vo.getCategoryId());
            }
        }
        fillRefAndTags(vo);
        return vo;
    }

    private void fillRefAndTags(AppExpertPostItemVO vo) {
        Map<String, Object> ref = parseContentRef(vo.getContentRefRaw());
        vo.setCoverImage(asString(ref.get("coverImage")));
        vo.setImageUrls(asStringList(ref.get("imageUrls")));
        List<KbTagSimpleVO> tags = kbEntryTagMapper.selectTagsByEntryId(vo.getId());
        vo.setTagNames(tags == null ? List.of() : tags.stream().map(KbTagSimpleVO::getName).toList());
    }

    private void fillRefAndTags(AppExpertPostDetailVO vo) {
        Map<String, Object> ref = parseContentRef(vo.getContentRefRaw());
        vo.setCoverImage(asString(ref.get("coverImage")));
        vo.setImageUrls(asStringList(ref.get("imageUrls")));
        vo.setContentBlocks(asBlockList(ref.get("contentBlocks")));
        List<KbTagSimpleVO> tags = kbEntryTagMapper.selectTagsByEntryId(vo.getId());
        vo.setTagNames(tags == null ? List.of() : tags.stream().map(KbTagSimpleVO::getName).toList());
    }

    private void validateCategoryRequired(Long categoryId) {
        if (categoryId == null) {
            throw new BizException(ResultCode.BAD_REQUEST, "请先选择科普分类");
        }
        KbCategory category = kbCategoryMapper.selectById(categoryId);
        if (category == null || category.getStatus() == null || category.getStatus() == 0) {
            throw new BizException(ResultCode.BAD_REQUEST, "科普分类不存在或已禁用");
        }
    }

    private List<AppExpertContentBlockVO> normalizeContentBlocks(List<AppExpertContentBlockDTO> contentBlocks,
                                                                 String content,
                                                                 List<String> imageUrls) {
        List<AppExpertContentBlockVO> result = new ArrayList<>();
        if (contentBlocks != null) {
            for (AppExpertContentBlockDTO item : contentBlocks) {
                if (item == null || !StringUtils.hasText(item.getType())) {
                    continue;
                }
                String type = item.getType().trim().toLowerCase();
                if ("text".equals(type)) {
                    if (!StringUtils.hasText(item.getText())) {
                        continue;
                    }
                    AppExpertContentBlockVO block = new AppExpertContentBlockVO();
                    block.setType("text");
                    block.setText(item.getText().trim());
                    result.add(block);
                } else if ("image".equals(type)) {
                    if (!StringUtils.hasText(item.getUrl())) {
                        continue;
                    }
                    AppExpertContentBlockVO block = new AppExpertContentBlockVO();
                    block.setType("image");
                    block.setUrl(item.getUrl().trim());
                    result.add(block);
                }
            }
        }

        if (result.isEmpty()) {
            if (StringUtils.hasText(content)) {
                AppExpertContentBlockVO text = new AppExpertContentBlockVO();
                text.setType("text");
                text.setText(content.trim());
                result.add(text);
            }
            if (imageUrls != null) {
                for (String url : imageUrls) {
                    if (!StringUtils.hasText(url)) {
                        continue;
                    }
                    AppExpertContentBlockVO image = new AppExpertContentBlockVO();
                    image.setType("image");
                    image.setUrl(url.trim());
                    result.add(image);
                }
            }
        }

        if (result.isEmpty()) {
            throw new BizException(ResultCode.BAD_REQUEST, "正文内容不能为空");
        }
        return result;
    }

    private String mergeTextBlocks(List<AppExpertContentBlockVO> blocks) {
        StringBuilder sb = new StringBuilder();
        for (AppExpertContentBlockVO block : blocks) {
            if (!"text".equals(block.getType()) || !StringUtils.hasText(block.getText())) {
                continue;
            }
            if (!sb.isEmpty()) {
                sb.append('\n');
            }
            sb.append(block.getText().trim());
        }
        return sb.toString();
    }

    private List<String> normalizeTagNames(List<String> tagNames) {
        if (tagNames == null || tagNames.isEmpty()) {
            return List.of();
        }
        Set<String> dedup = new LinkedHashSet<>();
        for (String raw : tagNames) {
            if (!StringUtils.hasText(raw)) {
                continue;
            }
            String tag = raw.trim();
            if (tag.startsWith("#")) {
                tag = tag.substring(1).trim();
            }
            if (!StringUtils.hasText(tag)) {
                continue;
            }
            dedup.add(tag);
            if (dedup.size() >= 10) {
                break;
            }
        }
        return dedup.stream().toList();
    }

    private void bindTags(Long entryId, List<Long> oldTagIds, List<String> newTagNames, LocalDateTime now) {
        kbEntryTagMapper.delete(new LambdaQueryWrapper<KbEntryTag>().eq(KbEntryTag::getEntryId, entryId));
        for (Long oldId : oldTagIds) {
            adjustTagUseCount(oldId, -1);
        }
        if (newTagNames == null || newTagNames.isEmpty()) {
            return;
        }
        for (String name : newTagNames) {
            QaTag tag = qaTagMapper.selectOne(new LambdaQueryWrapper<QaTag>()
                .eq(QaTag::getName, name)
                .last("LIMIT 1"));
            if (tag == null) {
                tag = new QaTag();
                tag.setName(name);
                tag.setStatus(1);
                tag.setSource(2);
                tag.setUseCount(0);
                tag.setCreatedAt(now);
                qaTagMapper.insert(tag);
            }
            KbEntryTag rel = new KbEntryTag();
            rel.setEntryId(entryId);
            rel.setTagId(tag.getId());
            rel.setCreatedAt(now);
            kbEntryTagMapper.insert(rel);
            adjustTagUseCount(tag.getId(), 1);
        }
    }

    private void adjustTagUseCount(Long tagId, int delta) {
        if (tagId == null || delta == 0) {
            return;
        }
        QaTag tag = qaTagMapper.selectById(tagId);
        if (tag == null) {
            return;
        }
        int current = tag.getUseCount() == null ? 0 : tag.getUseCount();
        int next = Math.max(0, current + delta);
        tag.setUseCount(next);
        qaTagMapper.updateById(tag);
    }

    private String buildContentRef(String coverImage, List<AppExpertContentBlockVO> blocks) {
        try {
            Map<String, Object> ref = new HashMap<>();
            if (StringUtils.hasText(coverImage)) {
                ref.put("coverImage", coverImage.trim());
            }
            List<String> imageUrls = blocks.stream()
                .filter(x -> "image".equals(x.getType()) && StringUtils.hasText(x.getUrl()))
                .map(x -> x.getUrl().trim())
                .toList();
            if (!imageUrls.isEmpty()) {
                ref.put("imageUrls", imageUrls);
            }
            if (!blocks.isEmpty()) {
                ref.put("contentBlocks", blocks);
            }
            if (ref.isEmpty()) {
                return null;
            }
            return objectMapper.writeValueAsString(ref);
        } catch (Exception e) {
            throw new BizException(ResultCode.SERVER_ERROR, "科普文章内容处理失败");
        }
    }

    private Map<String, Object> parseContentRef(String raw) {
        if (!StringUtils.hasText(raw)) {
            return Map.of();
        }
        try {
            return objectMapper.readValue(raw, new TypeReference<>() {
            });
        } catch (Exception ignored) {
            return Map.of();
        }
    }

    private String asString(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private List<String> asStringList(Object value) {
        if (!(value instanceof List<?> list)) {
            return List.of();
        }
        return list.stream()
            .filter(Objects::nonNull)
            .map(String::valueOf)
            .toList();
    }

    private List<AppExpertContentBlockVO> asBlockList(Object value) {
        if (!(value instanceof List<?> list) || list.isEmpty()) {
            return List.of();
        }
        List<AppExpertContentBlockVO> result = new ArrayList<>();
        for (Object item : list) {
            if (!(item instanceof Map<?, ?> map)) {
                continue;
            }
            AppExpertContentBlockVO block = new AppExpertContentBlockVO();
            block.setType(asString(map.get("type")));
            block.setText(asString(map.get("text")));
            block.setUrl(asString(map.get("url")));
            if (!StringUtils.hasText(block.getType())) {
                continue;
            }
            result.add(block);
        }
        return result;
    }

    private int resolvePage(AppExpertPostPageQueryDTO query) {
        if (query == null || query.getPage() == null || query.getPage() <= 0) {
            return 1;
        }
        return query.getPage();
    }

    private int resolvePageSize(AppExpertPostPageQueryDTO query) {
        if (query == null || query.getPageSize() == null || query.getPageSize() <= 0) {
            return 10;
        }
        return Math.min(query.getPageSize(), 50);
    }

    private boolean shouldPersonalize(AppExpertPostPageQueryDTO query, Long userId) {
        if (userId == null || query == null) {
            return false;
        }
        if (!isPersonalizedRecommendEnabled(userId)) {
            return false;
        }
        if (StringUtils.hasText(query.getKeyword())) {
            return false;
        }
        String sortBy = StringUtils.hasText(query.getSortBy()) ? query.getSortBy().trim().toLowerCase() : "hot";
        return !"latest".equals(sortBy);
    }

    private boolean isPersonalizedRecommendEnabled(Long userId) {
        UserPrivacySetting setting = userPrivacySettingMapper.selectOne(new LambdaQueryWrapper<UserPrivacySetting>()
            .eq(UserPrivacySetting::getUserId, userId)
            .last("LIMIT 1"));
        if (setting == null || setting.getPersonalizedRecommend() == null) {
            return true;
        }
        return setting.getPersonalizedRecommend() == 1;
    }

    private Long currentUserIdOrNull() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof SecurityUser securityUser)) {
            return null;
        }
        return securityUser.getId();
    }

    private Long requireExpertUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof SecurityUser securityUser)) {
            throw new BizException(ResultCode.UNAUTHORIZED, "请先登录");
        }
        User user = userMapper.selectById(securityUser.getId());
        if (user == null) {
            throw new BizException(ResultCode.BAD_REQUEST, "用户不存在");
        }
        if (user.getExpertStatus() == null || user.getExpertStatus() != 3) {
            throw new BizException(ResultCode.FORBIDDEN, "仅认证专家可发布科普文章");
        }
        return user.getId();
    }

    private String trimOrNull(String text) {
        return StringUtils.hasText(text) ? text.trim() : null;
    }

    private void validateNoSensitiveWords(String bizName, String... texts) {
        if (texts == null || texts.length == 0) {
            return;
        }
        StringBuilder allText = new StringBuilder();
        for (String text : texts) {
            if (StringUtils.hasText(text)) {
                allText.append(text).append(' ');
            }
        }
        if (allText.isEmpty()) {
            return;
        }
        List<CmsSensitiveWord> words = sensitiveWordMapper.selectList(new LambdaQueryWrapper<CmsSensitiveWord>()
            .eq(CmsSensitiveWord::getEnabled, 1));
        if (words == null || words.isEmpty()) {
            return;
        }
        String target = allText.toString().toLowerCase();
        for (CmsSensitiveWord item : words) {
            if (item == null || !StringUtils.hasText(item.getWord())) {
                continue;
            }
            String w = item.getWord().toLowerCase();
            if (target.contains(w)) {
                throw new BizException(ResultCode.BAD_REQUEST, bizName + "包含敏感词：" + item.getWord());
            }
        }
    }

    private void indexKbForEs(KbEntry entry) {
        if (esSearchService == null || !esSearchService.isEnabled() || entry == null || entry.getId() == null) {
            return;
        }
        esSearchService.syncKbById(entry.getId());
    }
}
