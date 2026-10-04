package app.aimal.extension.subtitles;

import android.content.Context;
import android.graphics.Typeface;
import android.util.Xml;
import org.xmlpull.v1.XmlPullParser;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class SubtitleFonts {
    public static final String DEFAULT_CSS = "https://raw.githubusercontent.com/neura-neura/noir-player/54eb8d176475186580259686050b8f4bec141aeb/public/vendor/gotham-pro-font/fonts.min.css";
    private static final Map<String, Typeface> cache = new HashMap<>();
    private static Context app;
    private SubtitleFonts() { }

    public static synchronized void init(Context context) { app = context.getApplicationContext(); }

    public static synchronized Typeface resolve(String name) {
        Typeface result = cache.get(name);
        if (result != null) return result;
        try {
            File saved = app == null ? null : new File(new File(app.getFilesDir(), "aimal-fonts"), fileName(name));
            if (saved != null && saved.isFile()) result = Typeface.createFromFile(saved);
            else if (name.startsWith("/")) result = Typeface.createFromFile(name);
            else result = Typeface.create(name, Typeface.NORMAL);
        } catch (Exception ignored) { result = Typeface.SANS_SERIF; }
        cache.put(name, result);
        return result;
    }

    public static List<String> installed() {
        TreeSet<String> names = new TreeSet<>();
        Collections.addAll(names, "sans-serif", "sans-serif-condensed", "serif", "monospace");
        try (InputStream input = new FileInputStream("/system/etc/fonts.xml")) {
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(input, "UTF-8");
            for (int event = parser.next(); event != XmlPullParser.END_DOCUMENT; event = parser.next()) {
                if (event == XmlPullParser.START_TAG && (parser.getName().equals("family") || parser.getName().equals("alias"))) {
                    String name = parser.getAttributeValue(null, "name");
                    if (name != null) names.add(name);
                }
            }
        } catch (Exception ignored) { }
        if (app != null) {
            String[] saved = new File(app.getFilesDir(), "aimal-fonts").list();
            if (saved != null) for (String file : saved) {
                if (file.endsWith(".name")) try (InputStream in = new FileInputStream(new File(new File(app.getFilesDir(), "aimal-fonts"), file))) {
                    names.add(new String(read(in, 4096), StandardCharsets.UTF_8));
                } catch (Exception ignored) { }
            }
        }
        return new ArrayList<>(names);
    }

    /** Downloads SFNT fonts, which Android supports natively, off the UI thread. */
    public static List<String> loadCss(Context context, String address) throws Exception {
        init(context);
        URL cssUrl = new URL(address);
        String css = new String(download(cssUrl, 1024 * 1024), StandardCharsets.UTF_8);
        Matcher faces = Pattern.compile("@font-face\\s*\\{([^}]+)\\}", Pattern.CASE_INSENSITIVE).matcher(css);
        List<String> faceBlocks = new ArrayList<>();
        while (faces.find()) faceBlocks.add(faces.group(1));
        // Prefer the regular upright face when a family provides many weights.
        faceBlocks.sort(java.util.Comparator.comparingInt(face ->
                face.matches("(?is).*font-weight\\s*:\\s*400.*") && !face.matches("(?is).*font-style\\s*:\\s*italic.*") ? 0 : 1));
        List<String> names = new ArrayList<>();
        File directory = new File(context.getFilesDir(), "aimal-fonts");
        if (!directory.isDirectory() && !directory.mkdirs()) throw new Exception("No se pudo crear el caché de fuentes");
        for (String face : faceBlocks) {
            if (names.size() >= 64) break;
            Matcher family = Pattern.compile("font-family\\s*:\\s*['\"]?([^;'\"}]+)", Pattern.CASE_INSENSITIVE).matcher(face);
            if (!family.find()) continue;
            String name = family.group(1).trim();
            if (names.contains(name)) continue;
            Matcher urls = Pattern.compile("url\\(\\s*['\"]?([^)'\"\\s]+)", Pattern.CASE_INSENSITIVE).matcher(face);
            while (urls.find()) {
                String path = urls.group(1);
                // WOFF/WOFF2/EOT cannot be passed to Android Typeface.
                if (!path.matches("(?i).*\\.(ttf|otf)([?#].*)?")) continue;
                byte[] bytes;
                try { bytes = download(new URL(cssUrl, path), 16 * 1024 * 1024); }
                catch (Exception unavailable) { continue; }
                File temporary = File.createTempFile("font-", ".tmp", directory);
                try {
                    try (FileOutputStream out = new FileOutputStream(temporary)) { out.write(bytes); }
                    Typeface loaded = Typeface.createFromFile(temporary);
                    File target = new File(directory, fileName(name));
                    try (FileOutputStream out = new FileOutputStream(target)) { out.write(bytes); }
                    try (FileOutputStream out = new FileOutputStream(new File(directory, fileName(name) + ".name"))) {
                        out.write(name.getBytes(StandardCharsets.UTF_8));
                    }
                    synchronized (SubtitleFonts.class) { cache.put(name, loaded); }
                    names.add(name);
                } finally { temporary.delete(); }
                break;
            }
        }
        if (names.isEmpty()) throw new Exception("El CSS debe incluir fuentes TTF u OTF; Android no admite WOFF/WOFF2");
        return names;
    }

    private static String fileName(String name) { return "font-" + Integer.toHexString(name.hashCode()); }
    private static byte[] download(URL url, int limit) throws Exception {
        if (!url.getProtocol().equals("https")) throw new Exception("La URL de fuentes debe usar HTTPS");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setConnectTimeout(10000); connection.setReadTimeout(10000);
        connection.setInstanceFollowRedirects(false);
        try {
            if (connection.getResponseCode() != 200) throw new Exception("Error HTTP " + connection.getResponseCode());
            try (InputStream in = connection.getInputStream()) { return read(in, limit); }
        } finally { connection.disconnect(); }
    }
    private static byte[] read(InputStream in, int limit) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int count;
        while ((count = in.read(buffer)) != -1) {
            if (out.size() + count > limit) throw new Exception("La fuente excede el tamaño permitido");
            out.write(buffer, 0, count);
        }
        return out.toByteArray();
    }
}
