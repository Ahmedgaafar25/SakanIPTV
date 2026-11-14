# Application assets

- Home uses text, inline outline symbols, solid colors and CSS gradients. No promotional artwork is bundled.
- Channel logos and movie/series posters come from the user's provider. They load lazily with local vector/text fallbacks. Native WebView requests are restricted to artwork URLs registered from account metadata, without added account cookies/bearer headers.
- `app/src/main/assets/cairo.ttf`: Cairo variable font from https://github.com/google/fonts/tree/main/ofl/cairo ; license preserved as `Cairo-OFL.txt`.
- Apache notices are retained for AndroidX/Media3. Android launcher icon and TV banner remain application identity resources.
