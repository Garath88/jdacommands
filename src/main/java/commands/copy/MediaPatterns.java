package commands.copy;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public final class MediaPatterns {
    private static final Pattern URL_TWITTER_PATTERN =
        Pattern.compile("http(s?)://pbs(-[0-9]+)?.twimg.com/media/[^./]+.(jpg|png)((:[a-z]+)?)$");
    private static final Pattern URL_TWITTER_STATUS_PATTERN =
        Pattern.compile("http(s?)://(www\\.)?twitter\\.com/([A-Za-z0-9-_.]+/status/|statuses/|i/web/status/)([0-9?s=]+)(/photo/1)?$");
    private static final Pattern URL_FC2_PATTERN =
        Pattern.compile("http(s?)://.*fc2.*");
    private static final Pattern URL_BOORU_PATTERN =
        Pattern.compile("http(s?)://.*booru.*(jpg|png|jpeg|gif)\"");
    private static final Pattern URL_CDN_DISCORD_PATTERN =
        Pattern.compile("^http(s?)://cdn.discordapp.com/.*(jpg|png|jpeg|gif)\"");
    private static final Pattern URL_MEDIA_DISCORD_PATTERN =
        Pattern.compile("^http(s?)://media.discordapp.net/.*(jpg|png|jpeg|gif)\"");
    private static final Pattern URL_WEBMSHARE_PATTERN =
        Pattern.compile("http(s?)://.*webmshare.com.*");
    private static final Pattern URL_IMGUR_SINGLE_PATTERN =
        Pattern.compile("http(s?)://(i\\.)?imgur\\.com/[A-Za-z0-9]+[.]+(jpg|png|jpeg|gif)");
    private static final Pattern URL_IMGUR_ALBUM_PATTERN =
        Pattern.compile("http(s?)://imgur\\.com/(a/|gallery/|r/[^/]+/)[A-Za-z0-9]+(#[A-Za-z0-9]+)?$");
    private static final Pattern URL_GFYCAT_PATTERN =
        Pattern.compile("http(s?)://gfycat\\.com/(gifs/detail/)?[A-Za-z]+$");
    private static final Pattern URL_NOZOMI_PATTERN =
        Pattern.compile("http(s?)://i.nozomi.la");
    private static final Pattern URL_WEBM_PATTERN =
        Pattern.compile("http(s?)://.+\\.webm$");
    private static final Pattern URL_MP4_PATTERN =
        Pattern.compile("http(s?)://.+\\.mp4$");
    public static final Pattern URL_PATTERN =
        Pattern.compile("https?://(www\\.)?[-a-zA-Z0-9@:%._+~#=]{2,256}\\.[a-z]{2,6}\\b[-a-zA-Z0-9@:%_+.~#?&/=]*");
    public static final Pattern IMAGE_PATTERN =
        Pattern.compile("[^\\s]+\\.(?i)(jpg|png|gif|bmp)$");

    private static final List<Pattern> PATTERNS = Arrays.asList(
        URL_TWITTER_PATTERN,
        URL_TWITTER_STATUS_PATTERN,
        URL_FC2_PATTERN,
        URL_BOORU_PATTERN,
        URL_CDN_DISCORD_PATTERN,
        URL_MEDIA_DISCORD_PATTERN,
        URL_WEBMSHARE_PATTERN,
        URL_IMGUR_SINGLE_PATTERN,
        URL_IMGUR_ALBUM_PATTERN,
        URL_GFYCAT_PATTERN,
        URL_NOZOMI_PATTERN,
        URL_WEBM_PATTERN,
        URL_MP4_PATTERN);

    private MediaPatterns() {
    }

    static List<Pattern> getMediaPatterns() {
        return PATTERNS;
    }
}