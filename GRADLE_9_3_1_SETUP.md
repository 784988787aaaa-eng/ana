# Gradle 9.3.1 — Project Build Setup

This project is pinned to **Gradle 9.3.1**.

The GitHub Actions workflow installs exactly Gradle 9.3.1 through
`gradle/actions/setup-gradle@v4` and then runs `gradle assembleDebug`.

The `gradle/wrapper/gradle-wrapper.properties` file also records the exact
official Gradle 9.3.1 binary distribution URL as the project's canonical
Gradle version.

## Important

This repository does not include a copied Gradle distribution ZIP. GitHub
Actions downloads the official Gradle 9.3.1 distribution through the setup
action, which is preferable to committing a large binary archive to source
control.
