package org.iatoki.judgels.raguel;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.iatoki.judgels.raguel.modules.ForumModule;
import org.iatoki.judgels.raguel.modules.ForumModules;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class Forum {

    private final long id;
    private final String jid;
    private final Forum parentForum;
    private final List<Forum> subforums;
    private final String name;
    private final String description;
    private final Map<ForumModules, ForumModule> modules;
    private final Date lastUpdate;
    private final String lastUpdateUserJid;

    public Forum(long id, String jid, Forum parentForum, List<Forum> subforums, String name, String description, Map<ForumModules, ForumModule> modules, Date lastUpdate, String lastUpdateUserJid) {
        this.id = id;
        this.jid = jid;
        this.parentForum = parentForum;
        this.subforums = subforums;
        this.name = name;
        this.description = description;
        this.modules = modules;
        this.lastUpdate = lastUpdate;
        this.lastUpdateUserJid = lastUpdateUserJid;
    }

    public long getId() {
        return id;
    }

    public String getJid() {
        return jid;
    }

    public Forum getParentForum() {
        return parentForum;
    }

    public List<Forum> getSubforums() {
        return subforums;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Set<ForumModules> getModulesSet() {
        return ImmutableSet.copyOf(modules.keySet());
    }

    public List<ForumModule> getModules() {
        return ImmutableList.copyOf(modules.values());
    }

    public boolean containsModule(ForumModules forumModules) {
        return modules.containsKey(forumModules);
    }

    public ForumModule getModule(ForumModules forumModules) {
        return modules.get(forumModules);
    }

    public Date getLastUpdate() {
        return lastUpdate;
    }

    public String getLastUpdateUserJid() {
        return lastUpdateUserJid;
    }

    public boolean containsJidInHierarchy(String forumJid) {
        Forum forum = this;
        while (forum != null) {
            if (forum.getJid().equals(forumJid)) {
                return true;
            }
            forum = forum.getParentForum();
        }
        return false;
    }

    public String prependSpacesBasedOnLevel(int totalSpaces) {
        int depth = 0;
        Forum parent = getParentForum();
        while (parent != null) {
            depth++;
            parent = parent.getParentForum();
        }

        StringBuilder sb = new StringBuilder();
        while (depth != 0) {
            for (int i = 0; i < totalSpaces; ++i) {
                sb.append("&nbsp;");
            }
            depth--;
        }
        sb.append(getName());

        return sb.toString();
    }
}
