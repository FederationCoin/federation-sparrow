# Federation Sparrow

Desktop wallet for FederationCoin. Not audited. Use at your own risk; no warranty of any kind. See the [Apache 2.0 license](LICENSE). Not affiliated with Sparrow Wallet.

## For users

**Testnet is the public net.** Point the wallet at a local `federationcoind -testnet` (P2P 35333, RPC 35332, addresses `tfcn1…`). Explorer: [mempool.federationcoin.org](https://mempool.federationcoin.org). Site: [federationcoin.org](https://federationcoin.org).

**Main is not live.** Dummy MAIN (placeholder genesis, magic `00000000`, P2P 4095, RPC 4094, HRP `fcn`) is not launched. Do not treat it as the product chain.

Home directory is `~/.federationcoin-sparrow` on Linux and macOS, or `%APPDATA%\Federationcoin-sparrow` on Windows, so it does not collide with the node (`~/.federationcoin`). The log file is `federationcoin-sparrow.log`. Packaged Linux installs land in `/opt/federationcoin-sparrow` and the binary is `federationcoin-sparrow`.

Blake2b applies from height 1. BIP32 print form uses FederationCoin version bytes. A Bitcoin `tpub` / `xpub` will not import; recreate the testnet wallet after upgrade.

Hot single-sig against a local testnet node is the current success bar. Hardware, PayNym, and a downloadable installer are later. Builds are unsigned; Gatekeeper and SmartScreen may warn.

Issues: [FederationCoin/federation-sparrow](https://github.com/FederationCoin/federation-sparrow/issues). Do not open them on privkeyio or sparrowwallet.

## For developers

Forked from [privkeyio/shrike](https://github.com/privkeyio/shrike) (itself a Sparrow fork). Origin is `git@github.com:FederationCoin/federation-sparrow.git`. Mainline is `federationcoin`. GitHub is detached from that fork; **never push** `upstream` (`privkeyio/shrike`) or sparrowwallet.

Chain identity lives in the `drongo` submodule, not in this UI tree. Java packages remain `com.sparrowwallet.*` (upstream layout). `fxsvgimage` is a fetch-only pin of [hervegirod/fxsvgimage](https://github.com/hervegirod/fxsvgimage) tag `1.1`. Never push that remote.

Java libraries that are not on Maven Central are **git submodules built from source**, not jars in `libs/`. `libs/` is gitignored local scratch. Do not fetch `https://code.sparrowwallet.com/api/packages/sparrowwallet/maven`. `maven.federationcoin.org` is not provisioned.

### Clone and build

```bash
git clone --recursive git@github.com:FederationCoin/federation-sparrow.git
git checkout federationcoin
git submodule update --init --recursive
```

Java 25 or higher. Release binaries use [Eclipse Temurin 25.0.2+10](https://github.com/adoptium/temurin25-binaries/releases/tag/jdk-25.0.2%2B10). With [SDKMAN](https://sdkman.io/), `sdk env install` matches `.sdkmanrc`.

Debian/Ubuntu extras for installers:

```bash
sudo apt install -y rpm fakeroot binutils
```

```bash
./gradlew jpackage
# Arch and similar, skip deb/rpm:
./gradlew jpackage -PskipInstallers=true
```

Windows installers need [WiX v3](https://github.com/wixtoolset/wix3/releases). After `git pull`, run `git submodule update --init --recursive` so gitlink SHAs match origin.

Run from source (the launcher script is still named `sparrow`):

```bash
./sparrow
./sparrow -n testnet
./sparrow -h
```

`--dir` / `-d` sets the home folder. `--network` / `-n` is `mainnet`, `testnet`, `regtest`, `signet`, or `testnet4` (testnet here is testnet3). Fallback: `export SPARROW_NETWORK=testnet` (that environment variable name is unchanged in code). A `network-testnet` marker file in the home folder also selects testnet.

On Linux and macOS, XDG directories are opt-in if they already exist (`$XDG_CONFIG_HOME/federationcoin-sparrow`, and the matching data/state/cache dirs). `-d` disables XDG.

### Branching

Work on a branch off `federationcoin`. Open a same-repo pull request; a human merges. Do not push straight to mainline. Current work branch for this tree: `get-to-mainnet`. Java libs (`drongo`, `lark`, `tern`, `hummingbird`, `toucan`, `bokmakierie`) stay on `federationcoin` until they get their own work branches.

### Release

Version is `version` in `build.gradle`. Tags (human, on origin mainline, before Package):

```text
vMAJOR.MINOR.PATCH-federationcoin.<fork>
vMAJOR.MINOR.PATCH-federationcoin.<fork>.<ext>
```

Example: `v2.6.0-federationcoin.0` or `.rc1`. Package may open a **draft** GitHub Release only. No `npm publish`, no Maven, no public Docker. Unsigned macOS and Windows. Submodule gitlink SHAs must already be on those GitHub repos. Process: [golive notes](https://github.com/ldelarua/workspace-FederationCoin/blob/master/docs/golive-notes.md).

### Quality

Code quality checks and metrics will be added over time.
