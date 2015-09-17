package org.iatoki.judgels.raguel.services.impls;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.iatoki.judgels.play.IdentityUtils;
import org.iatoki.judgels.play.Page;
import org.iatoki.judgels.raguel.Forum;
import org.iatoki.judgels.raguel.ForumThread;
import org.iatoki.judgels.raguel.ForumThreadNotFoundException;
import org.iatoki.judgels.raguel.ForumThreadWithStatistics;
import org.iatoki.judgels.raguel.UserItemStatus;
import org.iatoki.judgels.raguel.models.daos.ForumDao;
import org.iatoki.judgels.raguel.models.daos.ForumModuleDao;
import org.iatoki.judgels.raguel.models.daos.ForumThreadDao;
import org.iatoki.judgels.raguel.models.daos.ThreadPostDao;
import org.iatoki.judgels.raguel.models.daos.UserItemDao;
import org.iatoki.judgels.raguel.models.entities.ForumModel;
import org.iatoki.judgels.raguel.models.entities.ForumThreadModel;
import org.iatoki.judgels.raguel.models.entities.ForumThreadModel_;
import org.iatoki.judgels.raguel.models.entities.ThreadPostModel_;
import org.iatoki.judgels.raguel.models.entities.UserItemModel_;
import org.iatoki.judgels.raguel.modules.forum.ForumModuleFactory;
import org.iatoki.judgels.raguel.services.ForumThreadService;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.List;
import java.util.stream.Collectors;

@Singleton
@Named("forumThreadService")
public final class ForumThreadServiceImpl implements ForumThreadService {

    private final ForumDao forumDao;
    private final ForumModuleDao forumModuleDao;
    private final ForumModuleFactory forumModuleFactory;
    private final ForumThreadDao forumThreadDao;
    private final ThreadPostDao threadPostDao;
    private final UserItemDao userItemDao;

    @Inject
    public ForumThreadServiceImpl(ForumDao forumDao, ForumModuleDao forumModuleDao, ForumModuleFactory forumModuleFactory, ForumThreadDao forumThreadDao, ThreadPostDao threadPostDao, UserItemDao userItemDao) {
        this.forumDao = forumDao;
        this.forumModuleDao = forumModuleDao;
        this.forumModuleFactory = forumModuleFactory;
        this.forumThreadDao = forumThreadDao;
        this.threadPostDao = threadPostDao;
        this.userItemDao = userItemDao;
    }

    @Override
    public Page<ForumThreadWithStatistics> getPageOfForumThreadsWithStatistic(Forum forum, long pageIndex, long pageSize, String orderBy, String orderDir, String filterString) {
        long totalRowsCount = forumThreadDao.countByFiltersEq(filterString, ImmutableMap.of(ForumThreadModel_.forumJid, forum.getJid()));
        List<ForumThreadModel> forumThreadModels = forumThreadDao.findSortedByFiltersEq(orderBy, orderDir, filterString, ImmutableMap.of(ForumThreadModel_.forumJid, forum.getJid()), pageIndex, pageSize);

        List<ForumThread> forumThreads = forumThreadModels.stream().map(m -> ForumThreadServiceUtils.createForumThreadFromModelAndForum(m, forum)).collect(Collectors.toList());

        ImmutableList.Builder<ForumThreadWithStatistics> forumThreadsWithStatisticsBuilder = ImmutableList.builder();
        for (ForumThread forumThread : forumThreads) {
            long viewCount = userItemDao.countByFiltersEq("", ImmutableMap.of(UserItemModel_.itemJid, forumThread.getJid(), UserItemModel_.status, UserItemStatus.VIEWED.name()));
            long replyCount = threadPostDao.countByFiltersEq("", ImmutableMap.of(ThreadPostModel_.threadJid, forumThread.getJid())) - 1;

            forumThreadsWithStatisticsBuilder.add(new ForumThreadWithStatistics(forumThread, viewCount, replyCount));
        }

        return new Page<>(forumThreadsWithStatisticsBuilder.build(), totalRowsCount, pageIndex, pageSize);
    }

    @Override
    public ForumThread findForumThreadById(long forumThreadId) throws ForumThreadNotFoundException {
        ForumThreadModel forumThreadModel = forumThreadDao.findById(forumThreadId);

        if (forumThreadModel == null) {
            throw new ForumThreadNotFoundException("Forum Thread Not Found.");
        }

        ForumModel forumModel = forumDao.findByJid(forumThreadModel.forumJid);
        Forum forum = ForumServiceUtils.createForumWithParentsFromModel(forumDao, forumModuleDao, forumModuleFactory, forumModel);

        return ForumThreadServiceUtils.createForumThreadFromModelAndForum(forumThreadModel, forum);
    }

    @Override
    public String createForumThread(String forumJid, String name) {
        ForumThreadModel forumThreadModel = new ForumThreadModel();
        forumThreadModel.forumJid = forumJid;
        forumThreadModel.name = name;

        forumThreadDao.persist(forumThreadModel, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());

        String parentForumJid = forumThreadModel.forumJid;
        while ((parentForumJid != null) && !parentForumJid.isEmpty()) {
            ForumModel forumModel = forumDao.findByJid(parentForumJid);
            forumDao.edit(forumModel, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());

            parentForumJid = forumModel.parentJid;
        }

        return forumThreadModel.jid;
    }
}
