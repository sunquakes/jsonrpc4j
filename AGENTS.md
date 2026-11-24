# AGENTS.md

Guidance for AI agents working in the `jsonrpc4j` repository.

## Versioning

Determine the next version from the remote tags and the project version in
`pom.xml` before preparing a release. Git tags use the `vx.y.z` form, while the
Maven project version and changelog headings use `x.y.z` without the `v` prefix.
The tag must match the `v[0-9]+.[0-9]+.[0-9]+` trigger of
`.github/workflows/publish-release.yml`.

1. **Inspect the remote versions.** Run `git fetch --tags origin` and then
   `git ls-remote --tags --sort=-v:refname origin`. To list only final versions,
   run:

   ```bash
   git ls-remote --tags origin | sed 's#.*refs/tags/##' | grep -E '^v[0-9]+\.[0-9]+\.[0-9]+$' | sed 's/^v//' | sort -V | tail -1
   ```

   Use `ls-remote` rather than `git describe` because sandbox checkouts are often
   shallow and because the release workflow is driven by the pushed remote tag.

2. **Establish the release base.** Read the top-level project `<version>` in
   `pom.xml`, not a dependency version. If it is `x.y.z-SNAPSHOT` and the final
   tag `vx.y.z` does not exist, `x.y.z` is the planned next version, even when a
   prerelease such as `vx.y.z-alpha` already exists. Prerelease tags are not final
   releases and must never be overwritten or re-tagged.

3. **Choose the bump when no release is already planned.** Increment the patch
   (`x.y.z+1`) for fixes, refactors and documentation; the minor (`x.y+1.0`) for
   backward-compatible features; and the major (`x+1.0.0`) for breaking changes
   to the public API. Base this on the latest final tag.

4. **Use the version consistently.** The POM project version, `CHANGELOG.md`
   heading and git tag must represent the same release: `x.y.z`, `[x.y.z]` and
   `vx.y.z`, respectively.

## Releasing

When the user defines a release version (`x.y.z`) or asks for the next version,
execute these steps in this exact order. Do not stop halfway and do not reorder
them:

1. **Prepare the release files.** Prepend `## [x.y.z] - YYYY-MM-DD` and the
   release notes to `CHANGELOG.md`, retaining a `---` separator before older
   entries. The release workflow extracts notes up to the first separator. Set
   the top-level project version in `pom.xml` to `x.y.z`, removing `-SNAPSHOT`;
   do not change dependency versions. Run the full test suite with JDK 17 before
   committing.

2. **Commit and push to `development`.** Commit both release files as
   `chore: release version x.y.z` and push with `git push origin development`.

3. **Create and merge the pull request to `main`.** Create the PR with:

   ```bash
   gh pr create --base main --head development --title "chore: release version x.y.z" --body "..."
   ```

   Wait for the required checks and SonarCloud Quality Gate to pass before
   merging. Do not tag the release before the merge is complete.

4. **Publish the tag from the merged commit.** Fetch `origin/main`, tag the PR
   merge commit as `vx.y.z`, and push that tag:

   ```bash
   git tag vx.y.z <merged-commit-sha>
   git push origin vx.y.z
   ```

   This triggers `.github/workflows/publish-release.yml`, which creates the
   GitHub Release from the latest changelog entry and runs `mvn deploy` to
   publish to Maven Central. The tag without its `v` prefix must exactly equal
   the POM project version. Never re-tag an already published version.

## Documentation

- When adding or modifying a user-facing feature or API, update `README.md` and
  `README_ZH.md` in the same change so the documented usage matches the
  implementation.
- In the `README.md` and `README_ZH.md` installation sections, document release
  usage without a `<repositories>` block; release artifacts resolve from Maven
  Central by default.
- Show the Sonatype Central snapshot repository
  (`https://central.sonatype.com/repository/maven-snapshots/`) only in the
  snapshot installation instructions.
- Keep the documented dependency version equal to the released POM version and
  link that version on Maven Central.

## Do not

- Do not bump the POM version or edit `CHANGELOG.md` unless a release was
  explicitly requested.
- Do not include `-SNAPSHOT` in a release tag or deploy a snapshot as a final
  release.
- Do not tag the `development` branch tip instead of the merged `main` commit.
- Do not run a local `mvn deploy` for the release; publishing belongs to the tag
  workflow.
