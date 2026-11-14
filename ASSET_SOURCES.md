# Application assets

- Home uses text, solid colors and CSS gradients, without promotional artwork or movie/series posters.
- Live channel logos are supplied by the user's provider. They load lazily, use no referrer and are not bundled. Native WebView image requests are limited to logo URLs registered from that account's channel metadata. Missing/unsupported images use a text fallback.
- `app/src/main/assets/cairo.ttf`: Cairo variable font from https://github.com/google/fonts/tree/main/ofl/cairo ; license preserved as `Cairo-OFL.txt`.
- Apache license notices are retained for AndroidX/Media3. Android launcher icon and TV banner remain application identity resources.
