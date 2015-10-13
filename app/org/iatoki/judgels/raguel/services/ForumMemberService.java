package org.iatoki.judgels.raguel.services;

import org.iatoki.judgels.play.Page;
import org.iatoki.judgels.raguel.ForumMember;
import org.iatoki.judgels.raguel.ForumMemberNotFoundException;

import java.util.List;

public interface ForumMemberService {

    boolean isMemberInForum(String forumJid, String forumMemberJid);

    ForumMember findMemberInForumById(long forumMemberId) throws ForumMemberNotFoundException;

    ForumMember findMemberInForumAndJid(String forumJid, String forumMemberJid);

    List<ForumMember> getMembersInForum(String forumJid);

    Page<ForumMember> getPageOfMembersInForum(String forumJid, long pageIndex, long pageSize, String orderBy, String orderDir, String filterString);

    void createForumMember(String forumJid, String userJid, String createUserJid, String createUserIpAddress);

    void deleteForumMember(long forumMemberId);
}
