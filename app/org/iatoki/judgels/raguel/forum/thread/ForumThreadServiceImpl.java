package org.iatoki.judgels.raguel.forum.thread;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.iatoki.judgels.play.Page;
import org.iatoki.judgels.raguel.forum.Forum;
import org.iatoki.judgels.raguel.forum.ForumServiceUtils;
import org.iatoki.judgels.raguel.user.item.UserItemStatus;
import org.iatoki.judgels.raguel.forum.ForumDao;
import org.iatoki.judgels.raguel.forum.ForumLastPostDao;
import org.iatoki.judgels.raguel.forum.module.ForumModuleDao;
import org.iatoki.judgels.raguel.forum.thread.post.ThreadPostDao;
import org.iatoki.judgels.raguel.user.item.UserItemDao;
import org.iatoki.judgels.raguel.forum.ForumModel;
import org.iatoki.judgels.raguel.forum.thread.post.ThreadPostModel_;
import org.iatoki.judgels.raguel.user.item.UserItemModel;
import org.iatoki.judgels.raguel.user.item.UserItemModel_;
import org.iatoki.judgels.raguel.forum.module.ForumModuleFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Singleton
public final class ForumThreadServiceImpl implements ForumThreadService {

    private final ForumDao forumDao;
    private final ForumLastPostDao forumLastPostDao;
    private final ForumModuleDao forumModuleDao;
    private final ForumModuleFactory forumModuleFactory;
    private final ForumThreadDao forumThreadDao;
    private final ThreadPostDao threadPostDao;
    private final UserItemDao userItemDao;

    @Inject
    public ForumThreadServiceImpl(ForumDao forumDao, ForumLastPostDao forumLastPostDao, ForumModuleDao forumModuleDao, ForumModuleFactory forumModuleFactory, ForumThreadDao forumThreadDao, ThreadPostDao threadPostDao, UserItemDao userItemDao) {
        this.forumDao = forumDao;
        this.forumLastPostDao = forumLastPostDao;
        this.forumModuleDao = forumModuleDao;
        this.forumModuleFactory = forumModuleFactory;
        this.forumThreadDao = forumThreadDao;
        this.threadPostDao = threadPostDao;
        this.userItemDao = userItemDao;
    }

    @Override
    public Page<ForumThread> getPageOfForumThreads(Forum forum, long pageIndex, long pageSize, String orderBy, String orderDir, String filterString) {
        long totalRowsCount = forumThreadDao.countByFiltersEq(filterString, ImmutableMap.of(ForumThreadModel_.forumJid, forum.getJid()));
        List<ForumThreadModel> forumThreadModels = forumThreadDao.findSortedByFiltersEq(orderBy, orderDir, filterString, ImmutableMap.of(ForumThreadModel_.forumJid, forum.getJid()), pageIndex * pageSize, pageSize);

        List<ForumThread> forumThreads = createForumThreadsFromModels(forum, forumThreadModels);

        return new Page<>(forumThreads, totalRowsCount, pageIndex, pageSize);
    }

    @Override
    public Page<ForumThreadWithStatistics> getPageOfForumThreadsWithStatistic(Forum forum, long pageIndex, long pageSize, String orderBy, String orderDir, String filterString) {
        long totalRowsCount = forumThreadDao.countByFiltersEq(filterString, ImmutableMap.of(ForumThreadModel_.forumJid, forum.getJid()));
        List<ForumThreadModel> forumThreadModels = forumThreadDao.findSortedByFiltersEq(orderBy, orderDir, filterString, ImmutableMap.of(ForumThreadModel_.forumJid, forum.getJid()), pageIndex * pageSize, pageSize);

        List<ForumThread> forumThreads = createForumThreadsFromModels(forum, forumThreadModels);

        return new Page<>(createForumThreadWithStatistics(forumThreads), totalRowsCount, pageIndex, pageSize);
    }

    @Override
    public Page<ForumThreadWithStatisticsAndStatus> getPageOfForumThreadsWithStatisticAndStatus(Forum forum, String userJid, long pageIndex, long pageSize, String orderBy, String orderDir, String filterString) {
        long totalRowsCount = forumThreadDao.countByFiltersEq(filterString, ImmutableMap.of(ForumThreadModel_.forumJid, forum.getJid()));
        List<ForumThreadModel> forumThreadModels = forumThreadDao.findSortedByFiltersEq(orderBy, orderDir, filterString, ImmutableMap.of(ForumThreadModel_.forumJid, forum.getJid()), pageIndex * pageSize, pageSize);

        List<ForumThread> forumThreads = createForumThreadsFromModels(forum, forumThreadModels);

        List<String> forumThreadsJid = forumThreads.stream().map(m -> m.getJid()).collect(Collectors.toList());
        List<UserItemModel> userItemModels = userItemDao.findSortedByFilters("id", "asc", "", ImmutableMap.of(UserItemModel_.userJid, userJid), ImmutableMap.of(UserItemModel_.itemJid, forumThreadsJid), 0, -1);
        ImmutableMap.Builder<String, UserItemModel> mapForumThreadJidToUserItemModelBuilder = ImmutableMap.builder();
        for (UserItemModel userItemModel : userItemModels) {
            mapForumThreadJidToUserItemModelBuilder.put(userItemModel.itemJid, userItemModel);
        }
        Map<String, UserItemModel> mapForumThreadJidToUserItemModel = mapForumThreadJidToUserItemModelBuilder.build();

        List<ForumThreadWithStatistics> forumThreadWithStatistics = createForumThreadWithStatistics(forumThreads);

        ImmutableList.Builder<ForumThreadWithStatisticsAndStatus> forumThreadsWithStatisticsAndStatusBuilder = ImmutableList.builder();
        for (ForumThreadWithStatistics forumThreadWithStatistic : forumThreadWithStatistics) {
            UserItemModel userItemModel = mapForumThreadJidToUserItemModel.get(forumThreadWithStatistic.getForumThread().getJid());
            boolean hasNewPost = (userItemModel == null) || (userItemModel.timeUpdate < forumThreadWithStatistic.getForumThread().getLastUpdate().getTime());

            forumThreadsWithStatisticsAndStatusBuilder.add(new ForumThreadWithStatisticsAndStatus(forumThreadWithStatistic, hasNewPost));
        }

        return new Page<>(forumThreadsWithStatisticsAndStatusBuilder.build(), totalRowsCount, pageIndex, pageSize);
    }

    @Override
    public ForumThread findForumThreadById(long forumThreadId) throws ForumThreadNotFoundException {
        ForumThreadModel forumThreadModel = forumThreadDao.findById(forumThreadId);

        if (forumThreadModel == null) {
            throw new ForumThreadNotFoundException("Forum Thread Not Found.");
        }

        ForumModel forumModel = forumDao.findByJid(forumThreadModel.forumJid);
        Forum forum = ForumServiceUtils.createForumWithParentFromModel(forumDao, forumLastPostDao, forumModuleDao, forumModuleFactory, forumModel);

        return ForumThreadServiceUtils.createForumThreadFromModelAndForum(forumThreadModel, forum);
    }

    @Override
    public ForumThread createForumThread(Forum forum, String name, String userJid, String userIpAddress) {
        ForumThreadModel forumThreadModel = new ForumThreadModel();
        forumThreadModel.forumJid = forum.getJid();
        forumThreadModel.name = name;

        forumThreadDao.persist(forumThreadModel, userJid, userIpAddress);

        ForumServiceUtils.updateForumAndParents(forumDao, forumLastPostDao, forum, userJid, userIpAddress);

        return ForumThreadServiceUtils.createForumThreadFromModelAndForum(forumThreadModel, forum);
    }

    private List<ForumThread> createForumThreadsFromModels(Forum forum, List<ForumThreadModel> forumThreadModels) {
        return forumThreadModels.stream().map(m -> ForumThreadServiceUtils.createForumThreadFromModelAndForum(m, forum)).collect(Collectors.toList());
    }

    private List<ForumThreadWithStatistics> createForumThreadWithStatistics(List<ForumThread> forumThreads) {
        ImmutableList.Builder<ForumThreadWithStatistics> forumThreadsWithStatisticsBuilder = ImmutableList.builder();
        for (ForumThread forumThread : forumThreads) {
            long viewCount = userItemDao.countByFiltersEq("", ImmutableMap.of(UserItemModel_.itemJid, forumThread.getJid(), UserItemModel_.status, UserItemStatus.VIEWED.name()));
            long replyCount = threadPostDao.countByFiltersEq("", ImmutableMap.of(ThreadPostModel_.threadJid, forumThread.getJid())) - 1;

            forumThreadsWithStatisticsBuilder.add(new ForumThreadWithStatistics(forumThread, viewCount, replyCount));
        }

        return forumThreadsWithStatisticsBuilder.build();
    }
}
