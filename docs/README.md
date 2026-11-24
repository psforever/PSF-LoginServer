# API documentation

The API documentation is generated from the Scala sources with
[sbt-unidoc](https://github.com/sbt/sbt-unidoc) and published to
[GitHub Pages](https://psforever.github.io/PSF-LoginServer/) by the
`Publish Docs` workflow on every push to `master`.

To build it locally:

```sh
sbt docs/unidoc
```

The generated site is written to `docs/target/scala-2.13/unidoc/`. That
directory is generated output and is not committed.
