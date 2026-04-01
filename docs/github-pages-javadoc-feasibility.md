# Javadoc GitHub Pages Setup

## Recommended Workflow
This repository now uses a dedicated GitHub Actions workflow at `.github/workflows/javadoc.yml` to generate and publish API documentation.

### What the workflow does
1. Triggers on every push to `main`.
2. Uses `ubuntu-latest` with Temurin JDK 17.
3. Runs native Javadoc directly (no Maven/Gradle):
   `javadoc -d docs -sourcepath src $(find src -name "*.java")`
4. Publishes `docs/` to the `gh-pages` branch with `peaceiris/actions-gh-pages@v3`.

## GitHub Pages configuration
After the first successful workflow run:
1. Open **Repository Settings → Pages**.
2. Set **Source** to **Deploy from a branch**.
3. Select branch **`gh-pages`** and folder **`/(root)`**.
4. Save.

### If GitHub Pages shows README instead of Javadoc
That means Pages is still serving from `main` (root or `/docs`) instead of `gh-pages`.
Re-check step 2/3 above, then wait ~1–2 minutes for Pages to rebuild.
This repository also includes redirect `index.html` files in CI output to route `/` to `/javadoc/index.html`.

The published Javadoc will be available at:
`https://<username>.github.io/<repository>/`
