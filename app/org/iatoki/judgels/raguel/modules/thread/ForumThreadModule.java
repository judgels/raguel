package org.iatoki.judgels.raguel.modules.thread;

import com.google.gson.Gson;
import org.iatoki.judgels.raguel.modules.ForumModules;
import org.iatoki.judgels.raguel.modules.ForumModule;
import org.iatoki.judgels.raguel.modules.ForumModules;
import org.iatoki.judgels.raguel.views.html.forum.modules.emptyFormView;
import play.data.Form;
import play.mvc.Http;
import play.twirl.api.Html;

public final class ForumThreadModule implements ForumModule {

    @Override
    public ForumModules getType() {
        return ForumModules.THREAD;
    }

    @Override
    public String toJSONString() {
        return new Gson().toJson(this);
    }

    @Override
    public Html generateConfigFormInput(Form<?> form) {
        return emptyFormView.render();
    }

    @Override
    public Form<?> generateConfigForm() {
        return Form.form();
    }

    @Override
    public Form<?> updateModuleByFormFromRequest(Http.Request request) {
        return Form.form().bindFromRequest(request);
    }
}
