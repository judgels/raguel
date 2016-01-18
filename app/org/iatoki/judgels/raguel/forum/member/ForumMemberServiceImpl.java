package org.iatoki.judgels.raguel.forum.member;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import org.iatoki.judgels.play.Page;
import org.iatoki.judgels.raguel.forum.ForumDao;
import org.iatoki.judgels.raguel.forum.ForumModel;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

@Singleton
public final class ForumMemberServiceImpl implements ForumMemberService {

    private final ForumDao forumDao;
    private final ForumMemberDao forumMemberDao;

    @Inject
    public ForumMemberServiceImpl(ForumDao forumDao, ForumMemberDao forumMemberDao) {
        this.forumDao = forumDao;
        this.forumMemberDao = forumMemberDao;
    }

    @Override
    public boolean isMemberInForum(String forumJid, String forumMemberJid) {
        return forumMemberDao.existsInForumByMemberJid(forumJid, forumMemberJid);
    }

    @Override
    public ForumMember findMemberInForumById(long forumMemberId) throws ForumMemberNotFoundException {
        ForumMemberModel forumMemberModel = forumMemberDao.findById(forumMemberId);
        if (forumMemberModel == null) {
            throw new ForumMemberNotFoundException("Forum Member not found.");
        }

        return createForumMemberFromModel(forumMemberModel);
    }

    @Override
    public ForumMember findMemberInForumAndJid(String forumJid, String forumMemberJid) {
        ForumMemberModel forumMemberModel = forumMemberDao.findInForumByMemberJid(forumJid, forumMemberJid);
        return createForumMemberFromModel(forumMemberModel);
    }

    @Override
    public List<ForumMember> getMembersInForum(String forumJid) {
        List<ForumMemberModel> forumMemberModels = forumMemberDao.findSortedByFiltersEq("id", "asc", "", ImmutableMap.of(ForumMemberModel_.forumJid, forumJid), 0, -1);
        return Lists.transform(forumMemberModels, m -> createForumMemberFromModel(m));
    }

    @Override
    public Page<ForumMember> getPageOfMembersInForum(String forumJid, long pageIndex, long pageSize, String orderBy, String orderDir, String filterString) {
        long totalPages = forumMemberDao.countByFiltersEq(filterString, ImmutableMap.of(ForumMemberModel_.forumJid, forumJid));
        List<ForumMemberModel> forumMemberModels = forumMemberDao.findSortedByFiltersEq(orderBy, orderDir, filterString, ImmutableMap.of(ForumMemberModel_.forumJid, forumJid), pageIndex * pageSize, pageSize);

        List<ForumMember> forumMembers = Lists.transform(forumMemberModels, m -> createForumMemberFromModel(m));

        return new Page<>(forumMembers, totalPages, pageIndex, pageSize);
    }

    @Override
    public void createForumMember(String forumJid, String userJid, String createUserJid, String createUserIpAddress) {
        ForumModel forumModel = forumDao.findByJid(forumJid);

        ForumMemberModel forumMemberModel = new ForumMemberModel();
        forumMemberModel.forumJid = forumModel.jid;
        forumMemberModel.userJid = userJid;

        forumMemberDao.persist(forumMemberModel, createUserJid, createUserIpAddress);

        forumDao.edit(forumModel, createUserJid, createUserIpAddress);
    }

    @Override
    public void deleteForumMember(long forumMemberId) {
        ForumMemberModel forumMemberModel = forumMemberDao.findById(forumMemberId);
        forumMemberDao.remove(forumMemberModel);
    }

    private static ForumMember createForumMemberFromModel(ForumMemberModel forumMemberModel) {
        return new ForumMember(forumMemberModel.id, forumMemberModel.forumJid, forumMemberModel.userJid);
    }
}
