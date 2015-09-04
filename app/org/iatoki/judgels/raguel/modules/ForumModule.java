package org.iatoki.judgels.raguel.modules;

import play.data.Form;
import play.mvc.Http;
import play.twirl.api.Html;

public interface ForumModule {

    ForumModules getType();

    String toJSONString();

    Html generateConfigFormInput(Form<?> form);

    Form<?> generateConfigForm();

    Form<?> updateModuleByFormFromRequest(Http.Request request);
}
