package org.iatoki.judgels.raguel.forum;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.iatoki.judgels.raguel.forum.member.ForumMemberDao;
import org.iatoki.judgels.raguel.forum.module.ForumModuleDao;
import org.iatoki.judgels.raguel.forum.thread.ForumThreadDao;
import org.iatoki.judgels.raguel.user.item.UserItemDao;
import org.iatoki.judgels.raguel.forum.module.ForumModuleModel;
import org.iatoki.judgels.raguel.forum.thread.ForumThreadModel;
import org.iatoki.judgels.raguel.forum.thread.ForumThreadModel_;
import org.iatoki.judgels.raguel.user.item.UserItemModel;
import org.iatoki.judgels.raguel.user.item.UserItemModel_;
import org.iatoki.judgels.raguel.forum.module.ForumModule;
import org.iatoki.judgels.raguel.forum.module.ForumModuleFactory;
import org.iatoki.judgels.raguel.forum.module.ForumModules;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.stream.Collectors;

@Singleton
public final class ForumServiceImpl implements ForumService {

    private final ForumDao forumDao;
    private final ForumLastPostDao forumLastPostDao;
    private final ForumMemberDao forumMemberDao;
    private final ForumModuleDao forumModuleDao;
    private final ForumModuleFactory forumModuleFactory;
    private final ForumThreadDao forumThreadDao;
    private final UserItemDao userItemDao;

    @Inject
    public ForumServiceImpl(ForumDao forumDao, ForumLastPostDao forumLastPostDao, ForumMemberDao forumMemberDao, ForumModuleDao forumModuleDao, ForumModuleFactory forumModuleFactory, ForumThreadDao forumThreadDao, UserItemDao userItemDao) {
        this.forumDao = forumDao;
        this.forumLastPostDao = forumLastPostDao;
        this.forumMemberDao = forumMemberDao;
        this.forumModuleDao = forumModuleDao;
        this.forumModuleFactory = forumModuleFactory;
        this.forumThreadDao = forumThreadDao;
        this.userItemDao = userItemDao;
    }

    @Override
    public boolean forumExistsByJid(String forumJid) {
        return forumDao.existsByJid(forumJid);
    }

    @Override
    public List<Forum> getAllForumsForReferences() {
        Stack<Forum> forumStack = new Stack<>();
        forumStack.add(null);

        ImmutableList.Builder<Forum> forumBuilder = ImmutableList.builder();

        while (!forumStack.isEmpty()) {
            Forum currentForum = forumStack.pop();

            String currentJid;
            if (currentForum != null) {
                currentJid = currentForum.getJid();
                forumBuilder.add(currentForum);
            } else {
                currentJid = "";
            }

            List<ForumModel> forumModels = forumDao.findSortedByFiltersEq("id", "asc", "", ImmutableMap.of(ForumModel_.parentJid, currentJid), 0, -1);
            for (ForumModel forumModel : forumModels) {
                Forum forum = ForumServiceUtils.createPlainForumFromModel(forumModel, currentForum);
                if (currentForum != null) {
                    currentForum.getSubforums().add(forum);
                }
                forumStack.push(forum);
            }
        }

        return forumBuilder.build();
    }

    @Override
    public List<Forum> getChildForums(String parentJid, long maxDepth) {
        List<ForumModel> forumModels = forumDao.findSortedByFiltersEq("id", "asc", "", ImmutableMap.of(ForumModel_.parentJid, parentJid), 0, -1);

        List<Forum> forums = forumModels.stream().map(m -> ForumServiceUtils.createForumWithParentAndSubforumsUntilCertainDepthFromModel(forumDao, forumLastPostDao, forumModuleDao, forumModuleFactory, m, maxDepth)).collect(Collectors.toList());

        return forums;
    }

    @Override
    public List<Forum> getAllowedChildForums(String parentJid, long maxDepth) {
        List<Forum> forums = getChildForums(parentJid, maxDepth);

        ImmutableList.Builder<Forum> allowedForums = ImmutableList.builder();
        for (Forum forum : forums) {
            if (!forum.containModule(ForumModules.MEMBER)) {
                allowedForums.add(forum);
            }
        }

        return allowedForums.build();
    }

    @Override
    public List<ForumWithStatus> getChildForumsWithStatus(String parentJid, String userJid, long maxDepth) {
        List<Forum> forums = getChildForums(parentJid, maxDepth);

        ImmutableList.Builder<ForumWithStatus> forumWithStatusesBuilder = ImmutableList.builder();
        for (Forum forum : forums) {
            Stack<Forum> forumStack = new Stack<>();
            forumStack.push(forum);

            boolean hasNewPost = false;
            while (!hasNewPost && !forumStack.isEmpty()) {
                Forum currentForum = forumStack.pop();

                if (currentForum.containModule(ForumModules.THREAD)) {
                    List<ForumThreadModel> forumThreadModels = forumThreadDao.findSortedByFiltersEq("id", "asc", "", ImmutableMap.of(ForumThreadModel_.forumJid, currentForum.getJid()), 0, -1);
                    List<String> forumThreadJids = forumThreadModels.stream().map(t -> t.jid).collect(Collectors.toList());
                    List<UserItemModel> userItemModels = userItemDao.findSortedByFilters("id", "asc", "", ImmutableMap.of(UserItemModel_.userJid, userJid), ImmutableMap.of(UserItemModel_.itemJid, forumThreadJids), 0, -1);
                    ImmutableMap.Builder<String, UserItemModel> mapItemJidToUserItemModelBuilder = ImmutableMap.builder();
                    for (UserItemModel userItemModel : userItemModels) {
                        mapItemJidToUserItemModelBuilder.put(userItemModel.itemJid, userItemModel);
                    }
                    Map<String, UserItemModel> mapItemJidToUserItemModel = mapItemJidToUserItemModelBuilder.build();

                    for (int i = 0; !hasNewPost && (i < forumThreadModels.size()); ++i) {
                        UserItemModel userItemModel = mapItemJidToUserItemModel.get(forumThreadModels.get(i).jid);
                        hasNewPost = (userItemModel == null) || (userItemModel.timeUpdate < forumThreadModels.get(i).timeUpdate);
                    }
                }

                if (!hasNewPost) {
                    for (Forum childForum : currentForum.getSubforums()) {
                        forumStack.push(childForum);
                    }
                }
            }

            forumWithStatusesBuilder.add(new ForumWithStatus(forum, hasNewPost));
        }

        return forumWithStatusesBuilder.build();
    }

    @Override
    public List<ForumWithStatus> getAllowedChildForumsWithStatus(String parentJid, String userJid, long maxDepth) {
        List<ForumWithStatus> forumsWithStatus = getChildForumsWithStatus(parentJid, userJid, maxDepth);
        List<String> forumJidsWhereIsMember = forumMemberDao.getForumJidsByJid(userJid);

        ImmutableList.Builder<ForumWithStatus> allowedForumsWithStatus = ImmutableList.builder();
        for (ForumWithStatus forumWithStatus : forumsWithStatus) {
            if (forumWithStatus.getForum().containOrInheritModule(ForumModules.MEMBER)) {
                Forum forumWithMember = forumWithStatus.getForum().getForumOrParentWithModule(ForumModules.MEMBER);
                if (forumJidsWhereIsMember.contains(forumWithMember.getJid())) {
                    allowedForumsWithStatus.add(forumWithStatus);
                }
            } else {
                allowedForumsWithStatus.add(forumWithStatus);
            }
        }

        return allowedForumsWithStatus.build();
    }

    @Override
    public Forum findForumById(long forumId) throws ForumNotFoundException {
        ForumModel intendedForumModel = forumDao.findById(forumId);

        if (intendedForumModel == null) {
            throw new ForumNotFoundException("Forum Not Found.");
        }

        return ForumServiceUtils.createForumWithParentAndSubforumsUntilCertainDepthFromModel(forumDao, forumLastPostDao, forumModuleDao, forumModuleFactory, intendedForumModel, 1);
    }

    @Override
    public Forum findForumByJid(String forumJid) {
        ForumModel intendedForumModel = forumDao.findByJid(forumJid);

        return ForumServiceUtils.createForumWithParentAndSubforumsUntilCertainDepthFromModel(forumDao, forumLastPostDao, forumModuleDao, forumModuleFactory, intendedForumModel, 1);
    }

    @Override
    public void createForum(Forum parentForum, String name, String description, String userJid, String userIpAddress) {
        ForumModel forumModel = new ForumModel();
        if (parentForum == null) {
            forumModel.parentJid = "";
        } else {
            forumModel.parentJid = parentForum.getJid();
        }
        forumModel.name = name;
        forumModel.description = description;

        forumDao.persist(forumModel, userJid, userIpAddress);

        ForumServiceUtils.updateForumAndParents(forumDao, parentForum, userJid, userIpAddress);
    }

    @Override
    public void updateForum(Forum forum, Forum parentForum, String name, String description, String userJid, String userIpAddress) {
        ForumModel forumModel = forumDao.findByJid(forum.getJid());
        if (parentForum == null) {
            forumModel.parentJid = "";
        } else {
            forumModel.parentJid = parentForum.getJid();
        }
        forumModel.name = name;
        forumModel.description = description;

        forumDao.edit(forumModel, userJid, userIpAddress);

        ForumServiceUtils.updateForumAndParents(forumDao, forum.getParentForum(), userJid, userIpAddress);
        ForumServiceUtils.updateForumAndParents(forumDao, parentForum, userJid, userIpAddress);
    }

    @Override
    public void updateForumModuleConfiguration(String forumJid, Collection<ForumModule> forumModules, String userJid, String userIpAddress) {
        for (ForumModule forumModule : forumModules) {
            ForumModuleModel forumModuleModel = forumModuleDao.findInForumByName(forumJid, forumModule.getType().name());
            forumModuleModel.config = forumModule.toJSONString();

            forumModuleDao.edit(forumModuleModel, userJid, userIpAddress);
        }

        ForumModel forumModel = forumDao.findByJid(forumJid);

        forumDao.edit(forumModel, userJid, userIpAddress);
    }
}
