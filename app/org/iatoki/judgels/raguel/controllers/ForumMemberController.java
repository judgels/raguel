package org.iatoki.judgels.raguel.controllers;

import com.google.common.collect.ImmutableList;
import org.apache.commons.io.FileUtils;
import org.iatoki.judgels.api.JudgelsAPIClientException;
import org.iatoki.judgels.api.jophiel.JophielPublicAPI;
import org.iatoki.judgels.api.jophiel.JophielUser;
import org.iatoki.judgels.play.IdentityUtils;
import org.iatoki.judgels.play.InternalLink;
import org.iatoki.judgels.play.LazyHtml;
import org.iatoki.judgels.play.Page;
import org.iatoki.judgels.play.controllers.AbstractJudgelsController;
import org.iatoki.judgels.play.views.html.layouts.heading3Layout;
import org.iatoki.judgels.play.views.html.layouts.messageView;
import org.iatoki.judgels.raguel.Forum;
import org.iatoki.judgels.raguel.ForumMember;
import org.iatoki.judgels.raguel.ForumMemberNotFoundException;
import org.iatoki.judgels.raguel.ForumNotFoundException;
import org.iatoki.judgels.raguel.UploadResult;
import org.iatoki.judgels.raguel.forms.ForumMemberAddForm;
import org.iatoki.judgels.raguel.forms.ForumMemberUploadForm;
import org.iatoki.judgels.raguel.services.ForumMemberService;
import org.iatoki.judgels.raguel.services.ForumService;
import org.iatoki.judgels.raguel.services.UserService;
import org.iatoki.judgels.raguel.views.html.forum.member.listAddMembersView;
import org.iatoki.judgels.raguel.views.html.uploadResultView;
import play.data.Form;
import play.db.jpa.Transactional;
import play.filters.csrf.AddCSRFToken;
import play.filters.csrf.RequireCSRFCheck;
import play.i18n.Messages;
import play.mvc.Http;
import play.mvc.Result;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public final class ForumMemberController extends AbstractJudgelsController {

    private static final long PAGE_SIZE = 1000;

    private final JophielPublicAPI jophielPublicAPI;
    private final ForumService forumService;
    private final ForumMemberService forumMemberService;
    private final UserService userService;

    @Inject
    public ForumMemberController(JophielPublicAPI jophielPublicAPI, ForumService forumService, ForumMemberService forumMemberService, UserService userService) {
        this.jophielPublicAPI = jophielPublicAPI;
        this.forumService = forumService;
        this.forumMemberService = forumMemberService;
        this.userService = userService;
    }

    @Transactional(readOnly = true)
    @AddCSRFToken
    public Result viewMembers(long forumId) throws ForumNotFoundException {
        return listAddMembers(forumId, 0, "id", "asc", "");
    }

    @Transactional(readOnly = true)
    @AddCSRFToken
    public Result listAddMembers(long forumId, long pageIndex, String orderBy, String orderDir, String filterString) throws ForumNotFoundException {
        Forum forum = forumService.findForumById(forumId);
        if (!RaguelControllerUtils.getInstance().isModeratorOrAbove()) {
            return redirect(routes.ForumController.viewForums(forumId));
        }

        Page<ForumMember> forumMembers = forumMemberService.getPageOfMembersInForum(forum.getJid(), pageIndex, PAGE_SIZE, orderBy, orderDir, filterString);

        Form<ForumMemberAddForm> forumMemberCreateForm = Form.form(ForumMemberAddForm.class);
        Form<ForumMemberUploadForm> forumMemberUploadForm = Form.form(ForumMemberUploadForm.class);

        return showlistAddMember(forumMembers, pageIndex, orderBy, orderDir, filterString, forumMemberCreateForm, forumMemberUploadForm, forum);
    }

    @Transactional
    @RequireCSRFCheck
    public Result postAddMember(long forumId, long pageIndex, String orderBy, String orderDir, String filterString) throws ForumNotFoundException {
        Forum forum = forumService.findForumById(forumId);
        if (!RaguelControllerUtils.getInstance().isModeratorOrAbove()) {
            return redirect(routes.ForumController.viewForums(forumId));
        }

        Form<ForumMemberAddForm> forumMemberCreateForm = Form.form(ForumMemberAddForm.class).bindFromRequest();

        if (formHasErrors(forumMemberCreateForm)) {
            return showlistAddMemberWithMemberAddForm(pageIndex, orderBy, orderDir, filterString, forumMemberCreateForm, forum);
        }

        ForumMemberAddForm forumMemberCreateData = forumMemberCreateForm.get();
        JophielUser jophielUser;
        try {
            jophielUser = jophielPublicAPI.findUserByUsername(forumMemberCreateData.username);
        } catch (JudgelsAPIClientException e) {
            forumMemberCreateForm.reject("error.member.create.userNotExist");

            return showlistAddMemberWithMemberAddForm(pageIndex, orderBy, orderDir, filterString, forumMemberCreateForm, forum);
        }

        if (jophielUser == null) {
            forumMemberCreateForm.reject("error.member.create.userNotExist");

            return showlistAddMemberWithMemberAddForm(pageIndex, orderBy, orderDir, filterString, forumMemberCreateForm, forum);
        }

        if (forumMemberService.isMemberInForum(forum.getJid(), jophielUser.getJid())) {
            forumMemberCreateForm.reject("error.member.create.userIsAlreadyMember");

            return showlistAddMemberWithMemberAddForm(pageIndex, orderBy, orderDir, filterString, forumMemberCreateForm, forum);
        }

        userService.upsertUserFromJophielUser(jophielUser, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
        forumMemberService.createForumMember(forum.getJid(), jophielUser.getJid(), IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());

        return redirect(routes.ForumMemberController.viewMembers(forum.getId()));
    }

    @Transactional
    @RequireCSRFCheck
    public Result postUploadMember(long forumId) throws ForumNotFoundException {
        Forum forum = forumService.findForumById(forumId);
        if (!RaguelControllerUtils.getInstance().isModeratorOrAbove()) {
            return redirect(routes.ForumController.viewForums(forumId));
        }

        ImmutableList.Builder<UploadResult> failedUploadsBuilder = ImmutableList.builder();
        Http.MultipartFormData body = request().body().asMultipartFormData();
        Http.MultipartFormData.FilePart file;

        file = body.getFile("usernames");
        if (file != null) {
            File userFile = file.getFile();
            try {
                String[] usernames = FileUtils.readFileToString(userFile).split("\n");
                for (String username : usernames) {
                    try {
                        JophielUser jophielUser = jophielPublicAPI.findUserByUsername(username);
                        if (jophielUser != null) {
                            if (!forumMemberService.isMemberInForum(forum.getJid(), jophielUser.getJid())) {
                                userService.upsertUserFromJophielUser(jophielUser, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
                                forumMemberService.createForumMember(forum.getJid(), jophielUser.getJid(), IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
                            } else {
                                failedUploadsBuilder.add(new UploadResult(username, Messages.get("error.member.isAlreadyMember")));
                            }
                        } else {
                            failedUploadsBuilder.add(new UploadResult(username, Messages.get("error.member.userNotExist")));
                        }
                    } catch (JudgelsAPIClientException e) {
                        failedUploadsBuilder.add(new UploadResult(username, Messages.get("error.member.userNotExist")));
                    }
                }

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        List<UploadResult> failedUploads = failedUploadsBuilder.build();

        return showUploadMemberResult(failedUploads, forum);
    }

    @Transactional
    public Result removeMember(long forumId, long forumMemberId) throws ForumNotFoundException, ForumMemberNotFoundException {
        Forum forum = forumService.findForumById(forumId);
        ForumMember forumMember = forumMemberService.findMemberInForumById(forumMemberId);
        if (!RaguelControllerUtils.getInstance().isModeratorOrAbove()) {
            return redirect(routes.ForumController.viewForums(forumId));
        }

        forumMemberService.deleteForumMember(forumMember.getId());

        return redirect(routes.ForumMemberController.viewMembers(forum.getId()));
    }

    private Result showlistAddMemberWithMemberAddForm(long pageIndex, String orderBy, String orderDir, String filterString, Form<ForumMemberAddForm> forumMemberAddForm, Forum forum) {
        Form<ForumMemberUploadForm> forumMemberUploadForm = Form.form(ForumMemberUploadForm.class);

        Page<ForumMember> pageOfMembers = forumMemberService.getPageOfMembersInForum(forum.getJid(), pageIndex, PAGE_SIZE, orderBy, orderDir, filterString);

        return showlistAddMember(pageOfMembers, pageIndex, orderBy, orderDir, filterString, forumMemberAddForm, forumMemberUploadForm, forum);
    }

    private Result showlistAddMember(Page<ForumMember> forumMembers, long pageIndex, String orderBy, String orderDir, String filterString, Form<ForumMemberAddForm> forumMemberCreateForm, Form<ForumMemberUploadForm> forumMemberUploadForm, Forum forum) {
        LazyHtml content = new LazyHtml(listAddMembersView.render(forum.getId(), forumMembers, pageIndex, orderBy, orderDir, filterString, forumMemberCreateForm, forumMemberUploadForm, jophielPublicAPI.getUserAutocompleteAPIEndpoint()));
        content.appendLayout(c -> heading3Layout.render(Messages.get("forum.members"), c));

        ForumControllerUtils.getInstance().appendTabsLayout(content, forum, IdentityUtils.getUserJid());
        RaguelControllerUtils.getInstance().appendSidebarLayout(content);
        appendBreadcrumbsLayout(content, forum,
                new InternalLink(Messages.get("forum.members"), routes.ForumMemberController.viewMembers(forum.getId()))
        );
        RaguelControllerUtils.getInstance().appendTemplateLayout(content, "Forum - Members");

        return RaguelControllerUtils.getInstance().lazyOk(content);
    }

    private Result showUploadMemberResult(List<UploadResult> failedUploads, Forum forum) {
        LazyHtml content;
        if (failedUploads.size() > 0) {
            content = new LazyHtml(uploadResultView.render(failedUploads));
        } else {
            content = new LazyHtml(messageView.render(Messages.get("forum.member.upload.success")));
        }
        content.appendLayout(c -> heading3Layout.render(Messages.get("forum.member.upload.result"), c));

        ForumControllerUtils.getInstance().appendTabsLayout(content, forum, IdentityUtils.getUserJid());
        RaguelControllerUtils.getInstance().appendSidebarLayout(content);
        appendBreadcrumbsLayout(content, forum,
                new InternalLink(Messages.get("forum.members"), routes.ForumMemberController.viewMembers(forum.getId()))
        );
        RaguelControllerUtils.getInstance().appendTemplateLayout(content, "Forum - Members - Upload Result");

        return RaguelControllerUtils.getInstance().lazyOk(content);
    }

    private void appendBreadcrumbsLayout(LazyHtml content, Forum forum, InternalLink... lastLinks) {
        Forum parentForum = forum.getParentForum();
        ImmutableList.Builder<InternalLink> internalLinkBuilder;
        if (parentForum != null) {
            internalLinkBuilder = ForumControllerUtils.getForumBreadcrumbsBuilder(parentForum);
        } else {
            internalLinkBuilder = ImmutableList.builder();
        }
        internalLinkBuilder.add(new InternalLink(Messages.get("forum.members"), routes.ForumController.jumpToMembers(forum.getId())));
        internalLinkBuilder.addAll(Arrays.asList(lastLinks));
        ForumControllerUtils.appendBreadcrumbsLayout(content, internalLinkBuilder.build());
    }
}
