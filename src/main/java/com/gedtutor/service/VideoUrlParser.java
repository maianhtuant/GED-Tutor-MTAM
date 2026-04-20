package com.gedtutor.service;

import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses YouTube / Vimeo URLs and extracts the embed id + provider so the view
 * can render an &lt;iframe&gt; without any JavaScript.
 */
@Component
public class VideoUrlParser {

    private static final Pattern YT_WATCH = Pattern.compile("[?&]v=([A-Za-z0-9_-]{6,})");
    private static final Pattern YT_SHORT = Pattern.compile("youtu\\.be/([A-Za-z0-9_-]{6,})");
    private static final Pattern YT_EMBED = Pattern.compile("youtube\\.com/embed/([A-Za-z0-9_-]{6,})");
    private static final Pattern VIMEO    = Pattern.compile("vimeo\\.com/(?:video/)?(\\d+)");

    public record Parsed(String provider, String embedId) {}

    public Parsed parse(String url) {
        if (url == null) return new Parsed("unknown", null);
        String u = url.trim();

        Matcher m;
        if ((m = YT_WATCH.matcher(u)).find()) return new Parsed("youtube", m.group(1));
        if ((m = YT_SHORT.matcher(u)).find()) return new Parsed("youtube", m.group(1));
        if ((m = YT_EMBED.matcher(u)).find()) return new Parsed("youtube", m.group(1));
        if ((m = VIMEO.matcher(u)).find())    return new Parsed("vimeo",   m.group(1));

        return new Parsed("unknown", null);
    }

    public String embedUrl(String provider, String embedId) {
        if (embedId == null) return null;
        return switch (provider) {
            case "youtube" -> "https://www.youtube.com/embed/" + embedId;
            case "vimeo"   -> "https://player.vimeo.com/video/" + embedId;
            default -> null;
        };
    }
}
