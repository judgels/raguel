package org.iatoki.judgels.raguel.modules;

import java.util.Comparator;

public final class ForumModuleComparator implements Comparator<ForumModule> {

    @Override
    public int compare(ForumModule o1, ForumModule o2) {
        return o1.getType().compareTo(o2.getType());
    }
}
