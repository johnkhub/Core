package za.co.imqs.coreservice.dataaccess;

import java.util.List;
import java.util.UUID;

public interface TagRepository {
    List<String> getTagsFor(UUID uuid);
    boolean hasTag(UUID uuid, String tag);
    void addTags(UUID uuid, String ...tags);
    void deleteTags(UUID uuid, String ...tags);
}
