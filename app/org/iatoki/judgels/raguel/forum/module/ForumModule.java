package org.iatoki.judgels.raguel.forum.module;

import com.google.gson.Gson;
import org.iatoki.judgels.raguel.forum.module.html.emptyFormView;
import play.data.Form;
import play.mvc.Http;
import play.twirl.api.Html;

public abstract class ForumModule {

    public abstract ForumModules getType();

    public String toJSONString() {
        return new Gson().toJson(this);
    }

    public Html generateConfigFormInput(Form<?> form) {
        return emptyFormView.render();
    }

    public Form<?> generateConfigForm() {
        return Form.form();
    }

    public Form<?> updateModuleByFormFromRequest(Http.Request request) {
        return Form.form().bindFromRequest(request);
    }
}
