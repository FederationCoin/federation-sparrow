# Reproducing unified sighash on FederationCoin regtest

How to run Federation Sparrow against a solo FederationCoin regtest node and confirm that a spend it signs uses the unified signature hash (`UNIFIED_ALL`) and that `federationcoind` accepts it.

This chain is always Blake2b header-v2. There is no activation height and no Knots `-testactivationheight`. Build `federationcoind` and `federationcoin-cli` from `FederationCoin/` (origin `FederationCoin/FederationCoin`). Dummy MAIN is unlaunched; this check is for the wallet against a local node.

The digest is SHA256 `TaggedHash("UnifiedSighash")` over an empty `scriptCode`. Dilithium 44/87 and native SegWit v0 P2WPKH all use that message. It is not BIP143.

## Start the node

```
mkdir -p "$HOME/unified-sighash-regtest"

federationcoind -regtest -daemon \
  -datadir=$HOME/unified-sighash-regtest \
  -rpcuser=regtest -rpcpassword=regtest \
  -fallbackfee=0.00000001
```

Regtest RPC is `18443`. The chain minimum fee is 1 token per 12 vB (`SettlementFee`).

## Mine and confirm the header

```
alias cli='federationcoin-cli -regtest -datadir=$HOME/unified-sighash-regtest -rpcuser=regtest -rpcpassword=regtest -rpcwallet=test'

cli createwallet test
ADDR=$(cli getnewaddress)
cli generatetoaddress 101 $ADDR
cli getblockheader $(cli getblockhash 1) false | tr -d '\n' | wc -c   # 328 hex chars, 164-byte header v2
```

The node does not create a wallet on its own. Name the wallet in the alias: once Sparrow connects, Cormorant loads a second wallet, and unqualified wallet RPCs fail with error -19.

## Point Federation Sparrow at it

```
SPARROW_NETWORK=regtest /opt/federationcoin-sparrow/bin/federationcoin-sparrow
SPARROW_NETWORK=regtest ./gradlew run
```

Choose the **federationcoind** server type with URL `127.0.0.1:18443` and the RPC user and password above.

## Send and inspect the hash type

Fund a Sparrow receive address (Dilithium 87 default, or Dilithium 44 / P2WPKH) and mature it:

```
WALLET_ADDR=<receive address from Federation Sparrow>
cli generatetoaddress 1 $WALLET_ADDR
cli generatetoaddress 100 $ADDR
```

Send from Sparrow to `cli getnewaddress`. Inspect:

```
cli getrawtransaction <txid> true
```

The spend must verify as unified. A signature that does not match `TaggedHash("UnifiedSighash")` is rejected. The node accepting the transaction is the other half of the check.

The BIP32 child private key is the ML-DSA seed. There is no second `FCN-MLDSA*-CHILD` derivation. See `FederationCoin/doc/federation-spend.md`.

## Library checks (no GUI)

`UnifiedSigHashTest` in drongo checks the digest. Those run with `./gradlew test` in `drongo/`.

`SparrowSendHarness` builds the PSBT, lets `AppServices` decide the hash type, then `Wallet.sign()` / `Wallet.finalise()`:

```
DEPS=$(./gradlew -q :printTestClasspath | tail -1)

java -cp "$DEPS" com.sparrowwallet.sparrow.SparrowSendHarness scriptpubkey
java -cp "$DEPS" com.sparrowwallet.sparrow.SparrowSendHarness send \
    <prevTxid> <prevVout> <prevValue> <destScriptHex> <destValue> true
```

Fund the printed scriptPubKey on the regtest chain and sign its output with the second command. Hand the result to `cli testmempoolaccept`.
