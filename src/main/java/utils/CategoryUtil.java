package utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jagrosh.jdautilities.commons.utils.FinderUtil;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.Guild;

public final class CategoryUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(CategoryUtil.class);
    private static final String THREAD_CATEGORY = "current threads";
    private static final String RP_CATEGORY = "the basement";
    private static final String ARCHIVE_CATEGORY = "public archive";
    private static final String CATEGORY_NOT_FOUND = "Category \"%s\" was not found!";

    private CategoryUtil() {
    }

    public static Category getPublicArchiveCategory(JDA jda) {
        return FinderUtil.findCategories(ARCHIVE_CATEGORY, jda)
            .stream()
            .findFirst()
            .orElseThrow(() -> {
                String errorMsg = String.format(CATEGORY_NOT_FOUND, ARCHIVE_CATEGORY);
                LOGGER.error(errorMsg);
                return new IllegalStateException(errorMsg);
            });
    }


    public static Category getThreadCategory(JDA jda) {
        return FinderUtil.findCategories(THREAD_CATEGORY, jda)
            .stream()
            .findFirst()
            .orElseThrow(() -> {
                String errorMsg = String.format(CATEGORY_NOT_FOUND, THREAD_CATEGORY);
                LOGGER.error(errorMsg);
                return new IllegalStateException(errorMsg);
            });
    }

    public static Category getRpCategory(JDA jda) {
        return FinderUtil.findCategories(RP_CATEGORY, jda)
            .stream()
            .findFirst()
            .orElseThrow(() -> {
                String errorMsg = String.format(CATEGORY_NOT_FOUND, RP_CATEGORY);
                LOGGER.error(errorMsg);
                return new IllegalStateException(errorMsg);
            });
    }


    public static List<Category> getSelfRoleCategories(Guild guild) {
        List<Category> categories = new ArrayList<>();

        Category theBasement = FinderUtil.findCategories(RP_CATEGORY, guild).stream()
            .findFirst().orElse(null);
        categories.add(theBasement);

        Category publicArchive = FinderUtil.findCategories(ARCHIVE_CATEGORY, guild).stream()
            .findFirst().orElse(null);
        categories.add(publicArchive);

        return categories.stream()
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }
}