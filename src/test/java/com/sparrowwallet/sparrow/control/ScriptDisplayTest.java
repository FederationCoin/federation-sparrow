package com.sparrowwallet.sparrow.control;

import com.sparrowwallet.drongo.protocol.ModernSig;
import com.sparrowwallet.drongo.protocol.Script;
import com.sparrowwallet.drongo.protocol.ScriptChunk;
import com.sparrowwallet.drongo.protocol.ScriptOpCodes;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

public class ScriptDisplayTest {
    @Test
    public void p2wpkhMatchesBeforeGeneric() {
        Script p2wpkh = new Script(java.util.List.of(
                new ScriptChunk(ScriptOpCodes.OP_0, null),
                new ScriptChunk(0, new byte[20])));
        ScriptDisplay matched = firstMatch(p2wpkh);
        Assertions.assertNotNull(matched);
        Assertions.assertInstanceOf(ScriptDisplay.OpcodeHashDisplay.class, matched);
        Assertions.assertEquals("<wpkh>", ((ScriptDisplay.OpcodeHashDisplay)matched).placeholder());
    }

    @Test
    public void modernSingleMatchesFingerprintAndOpcodes() {
        byte[] fingerprint = new byte[32];
        fingerprint[0] = 1;
        Script single = ModernSig.singleScript(fingerprint, ModernSig.PARAM_DILITHIUM_44);
        ScriptDisplay matched = firstMatch(single);
        Assertions.assertInstanceOf(ScriptDisplay.ModernSingleDisplay.class, matched);
    }

    @Test
    public void modernPolicyMatchesThresholdAndHashes() {
        byte[] first = new byte[32];
        first[0] = 1;
        byte[] second = new byte[32];
        second[0] = 2;
        Script policy = ModernSig.policyScript(1, List.of(first, second), ModernSig.PARAM_DILITHIUM_87);
        ScriptDisplay matched = firstMatch(policy);
        Assertions.assertInstanceOf(ScriptDisplay.ModernPolicyDisplay.class, matched);
    }

    private static ScriptDisplay firstMatch(Script script) {
        for(ScriptDisplay display : ScriptDisplay.kinds()) {
            if(display.matches(script)) {
                return display;
            }
        }
        return null;
    }
}
