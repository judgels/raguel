package org.iatoki.judgels.raguel.forum.member;

import com.google.inject.ImplementedBy;
import org.iatoki.judgels.play.Page;

import java.util.List;

@ImplementedBy(ForumMemberServiceImpl.class)
public interface ForumMemberService {

    boolean isMemberInForum(String forumJid, String forumMemberJid);

    ForumMember findMemberInForumById(long forumMemberId) throws ForumMemberNotFoundException;

    ForumMember findMemberInForumAndJid(String forumJid, String forumMemberJid);

    List<ForumMember> getMembersInForum(String forumJid);

    Page<ForumMember> getPageOfMembersInForum(String forumJid, long pageIndex, long pageSize, String orderBy, String orderDir, String filterString);

    void createForumMember(String forumJid, String userJid, String createUserJid, String createUserIpAddress);

    void deleteForumMember(long forumMemberId);
}
