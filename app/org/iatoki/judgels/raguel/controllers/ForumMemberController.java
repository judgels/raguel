package org.iatoki.judgels.raguel.controllers;

import com.google.common.collect.ImmutableList;
import org.apache.commons.io.FileUtils;
import org.iatoki.judgels.api.JudgelsAPIClientException;
import org.iatoki.judgels.api.jophiel.JophielClientAPI;
import org.iatoki.judgels.api.jophiel.JophielPublicAPI;
import org.iatoki.judgels.api.jophiel.JophielUser;
import org.iatoki.judgels.jophiel.services.BaseAvatarCacheService;
import org.iatoki.judgels.jophiel.services.UserActivityMessageService;
import org.iatoki.judgels.play.HtmlTemplate;
import org.iatoki.judgels.play.Page;
import org.iatoki.judgels.play.services.BaseJidCacheService;
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
import play.twirl.api.Html;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.util.List;

public final class ForumMemberController extends AbstractForumController {

    private static final long PAGE_SIZE = 1000;

    private final JophielPublicAPI jophielPublicAPI;
    private final ForumService forumService;
    private final ForumMemberService forumMemberService;
    private final UserService userService;

    @Inject
    public ForumMemberController(BaseJidCacheService jidCacheService, BaseAvatarCacheService avatarCacheService, JophielClientAPI jophielClientAPI, JophielPublicAPI jophielPublicAPI, UserActivityMessageService userActivityMessageService, ForumMemberService forumMemberService, ForumService forumService, UserService userService) {
        super(jidCacheService, avatarCacheService, jophielClientAPI, jophielPublicAPI, userActivityMessageService, forumMemberService);
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
        if (!isCurrentUserModeratorOrAdmin()) {
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
        if (!isCurrentUserModeratorOrAdmin()) {
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

        userService.upsertUserFromJophielUser(jophielUser, getCurrentUserJid(), getCurrentUserIpAddress());
        forumMemberService.createForumMember(forum.getJid(), jophielUser.getJid(), getCurrentUserJid(), getCurrentUserIpAddress());

        return redirect(routes.ForumMemberController.viewMembers(forum.getId()));
    }

    @Transactional
    @RequireCSRFCheck
    public Result postUploadMember(long forumId) throws ForumNotFoundException {
        Forum forum = forumService.findForumById(forumId);
        if (!isCurrentUserModeratorOrAdmin()) {
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
                                userService.upsertUserFromJophielUser(jophielUser, getCurrentUserJid(), getCurrentUserIpAddress());
                                forumMemberService.createForumMember(forum.getJid(), jophielUser.getJid(), getCurrentUserJid(), getCurrentUserIpAddress());
                            } else {
                                failedUploadsBuilder.add(new UploadResult(username, Messages.get("forum.member.new.error.registered")));
                            }
                        } else {
                            failedUploadsBuilder.add(new UploadResult(username, Messages.get("forum.member.new.error.invalid")));
                        }
                    } catch (JudgelsAPIClientException e) {
                        failedUploadsBuilder.add(new UploadResult(username, Messages.get("forum.member.new.error.invalid")));
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
        if (!isCurrentUserModeratorOrAdmin()) {
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
        HtmlTemplate htmlTemplate = getBaseHtmlTemplate(forum);

        Html content = listAddMembersView.render(forum.getId(), forumMembers, pageIndex, orderBy, orderDir, filterString, forumMemberCreateForm, forumMemberUploadForm, jophielPublicAPI.getUserAutocompleteAPIEndpoint());
        htmlTemplate.setContent(content);

        htmlTemplate.markBreadcrumbLocation(Messages.get("forum.text.members"), routes.ForumMemberController.viewMembers(forum.getId()));

        return renderTemplate(htmlTemplate);
    }

    private Result showUploadMemberResult(List<UploadResult> failedUploads, Forum forum) {
        HtmlTemplate htmlTemplate = getBaseHtmlTemplate(forum);

        Html content;
        if (failedUploads.size() > 0) {
            content = uploadResultView.render(failedUploads);
        } else {
            content = messageView.render(Messages.get("forum.member.upload.text.success"));
        }

        htmlTemplate.setContent(content);
        htmlTemplate.setMainTitle(Messages.get("forum.member.upload.text.result"));

        htmlTemplate.markBreadcrumbLocation(Messages.get("forum.text.members"), routes.ForumMemberController.viewMembers(forum.getId()));

        return renderTemplate(htmlTemplate);
    }

    @Override
    protected HtmlTemplate getBaseHtmlTemplate(Forum forum) {
        HtmlTemplate htmlTemplate = super.getBaseHtmlTemplate(forum);

        htmlTemplate.markBreadcrumbLocation(Messages.get("forum.text.members"), routes.ForumController.jumpToMembers(forum.getId()));
        return htmlTemplate;
    }
}
