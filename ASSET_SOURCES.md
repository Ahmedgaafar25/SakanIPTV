# Project image and font assets

- `app/src/main/assets/stadium.webp`: original generated bitmap, created with the built-in image generation tool and encoded to WebP for the APK. It is artwork for the dashboard, not a claim about a scheduled match.
- Final prompt: "Create a wide cinematic 3:1 image of two anonymous adult male football players, one in a plain midnight navy kit and one in a plain red kit, contesting a ball on a stadium pitch at night. Players occupy left 42% of image, full bodies on grass, dramatic white stadium lights and atmospheric mist. Right 58% must be mostly very dark navy empty stadium shadows, allowing white Arabic interface copy to be overlaid by code. Premium realistic sports broadcast photography, deep navy and cool blue palette. No words, no letters, no logos, no scoreboards, no brand marks, no UI, no framing television. Production artwork for an Android IPTV app."
- `app/src/main/assets/cairo.ttf`: Cairo variable font from https://github.com/google/fonts/tree/main/ofl/cairo ; license preserved as `Cairo-OFL.txt`.
- Channel logos and content posters are loaded at runtime from the user's provider metadata. They are not bundled or generated.
