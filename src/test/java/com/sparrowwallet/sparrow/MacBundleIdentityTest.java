package com.sparrowwallet.sparrow;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The macOS bundle has to name this application, not the one it was forked from.
 *
 * jpackage copies this plist verbatim from the resource directory, so it overrides what the image name would
 * have produced. Left as upstream's, CFBundleExecutable read Sparrow while the bundle shipped Contents/MacOS
 * /federationcoin-sparrow, and Finder reports an app whose executable is missing as damaged or incomplete.
 *
 * CFBundleIdentifier matters separately: two bundles claiming com.sparrowwallet.sparrow leaves LaunchServices
 * to choose between this and an installed Sparrow. The display name is Federation Sparrow, which contains
 * the word Sparrow, so identity checks target the upstream bundle id and exact name Sparrow, not a substring.
 */
public class MacBundleIdentityTest {
    private static final Path PLIST = Path.of("src/main/deploy/package/macos/Info.plist");

    private String value(String key) throws Exception {
        Matcher matcher = Pattern.compile("<key>" + key + "</key>\\s*<string>([^<]*)</string>")
                .matcher(Files.readString(PLIST));
        Assertions.assertTrue(matcher.find(), key + " is not set in the bundle plist");
        return matcher.group(1);
    }

    @Test
    public void testTheBundleNamesAnExecutableItShips() throws Exception {
        String executable = value("CFBundleExecutable");
        Assertions.assertEquals("federationcoin-sparrow", executable,
                "Finder runs this; it must match the jpackage image name");
        Assertions.assertFalse(executable.equalsIgnoreCase("Sparrow"),
                "Finder runs this; it must not still be named Sparrow");
    }

    @Test
    public void testTheBundleNamesAnIconItShips() throws Exception {
        String icon = value("CFBundleIconFile");
        Assertions.assertTrue(icon.endsWith(".icns"), "jpackage names the copied icon after the application");
        Assertions.assertFalse(icon.equalsIgnoreCase("Sparrow.icns"));
    }

    @Test
    public void testTheBundleHasAnIdentityOfItsOwn() throws Exception {
        Assertions.assertEquals("org.federationcoin.federation-sparrow", value("CFBundleIdentifier"));
        Assertions.assertEquals("Federation Sparrow", value("CFBundleName"));
        Assertions.assertFalse(value("CFBundleIdentifier").equals("com.sparrowwallet.sparrow"),
                "CFBundleIdentifier still claims upstream's identity, which collides with an installed Sparrow");
        Assertions.assertFalse(value("CFBundleName").equals("Sparrow"),
                "CFBundleName still claims upstream's identity");
    }

    @Test
    public void testNothingInTheBundleStillNamesUpstream() throws Exception {
        String contents = Files.readString(PLIST);
        Assertions.assertFalse(contents.contains("com.sparrowwallet.sparrow"),
                "the bundle plist still refers to upstream's bundle id");
        Assertions.assertFalse(contents.contains(">Sparrow<"),
                "the bundle plist still uses Sparrow as an identity string");
        Assertions.assertFalse(contents.toLowerCase().contains("shrike"),
                "the bundle plist still names the previous product");
    }
}
