package utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jagrosh.jdautilities.commons.utils.FinderUtil;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Category;

public final class CategoryUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(CategoryUtil.class);
    private static final String CATEGORY = "current threads";

    private CategoryUtil() {
    }

    public static Category getThreadCategory(JDA jda) {
        // TODO remove hardcoded category
        return FinderUtil.findCategories(CATEGORY, jda)
            .stream()
            .findFirst()
            .orElseThrow(() -> {
                String errorMsg = String.format("Custom category \"%s\" was not found!", CATEGORY);
                LOGGER.error(errorMsg);
                return new IllegalStateException(errorMsg);
            });
    }
}