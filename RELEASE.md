# Release

## Version

Current version: `1.0.0`

Version configuration:

```text
app/build.gradle.kts
```

## Local Build

```bash
./gradlew testDebugUnitTest assembleDebug
```

APK output:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## GitHub Actions Release

Pushing a version tag creates a GitHub Release and uploads the APK automatically:

```bash
git tag v1.0.0
git push origin v1.0.0
```

Workflow file:

```text
.github/workflows/android.yml
```
